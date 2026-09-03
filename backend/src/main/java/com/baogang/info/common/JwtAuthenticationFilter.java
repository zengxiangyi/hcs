package com.baogang.info.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.server.PathContainer;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT 认证过滤器，拦截所有请求校验 Authorization 头中的 Bearer token。
 *
 * 放行规则：
 *   - OPTIONS 预检请求（CORS）
 *   - 白名单路径（ant 风格），由外部传入，见 {@code SecurityConfig#PUBLIC_PATHS}
 *
 * 校验通过后将用户信息写入 Spring Security 上下文，供后续 Controller / 方法使用。
 *
 * 注意：本类故意不加 {@code @Component} / 不由组件扫描注册，只通过
 * {@code SecurityConfig#jwtAuthenticationFilter} 定义为 bean。原因见
 * {@code SecurityConfig#jwtAuthenticationFilterRegistration}——作为 Filter bean
 * 会被 Boot 自动注册到 Servlet 容器链，与 Security 链内的 {@code addFilterBefore}
 * 构成双重注册，导致每个请求重复验签。
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    /** 未指定白名单时的默认放行路径：登录/鉴权接口 */
    private static final String[] DEFAULT_PUBLIC_PATHS = {"/auth/**"};

    private static final PathPatternParser PATTERN_PARSER = new PathPatternParser();

    private final JwtUtil jwtUtil;

    /** 免认证路径（ant 风格，相对 context-path） */
    private final List<PathPattern> publicPatterns;

    /**
     * 认证失败处理器。为 null 时退化为 {@code sendError(401)}。
     *
     * <p>本过滤器挂在 Security 链的 {@code UsernamePasswordAuthenticationFilter} 之前，
     * 位置早于 {@code ExceptionTranslationFilter}，抛出的 AuthenticationException 不会被
     * 后者翻译成 401，而是冒泡到容器变成 500。所以这里必须主动调用该入口点返回 401 JSON。
     */
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this(jwtUtil, DEFAULT_PUBLIC_PATHS);
    }

    public JwtAuthenticationFilter(JwtUtil jwtUtil, String[] publicPaths) {
        this(jwtUtil, publicPaths, null);
    }

    public JwtAuthenticationFilter(JwtUtil jwtUtil, String[] publicPaths,
                                   AuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtUtil = jwtUtil;
        String[] paths = (publicPaths == null || publicPaths.length == 0) ? DEFAULT_PUBLIC_PATHS : publicPaths;
        this.publicPatterns = Arrays.stream(paths)
                .map(PATTERN_PARSER::parse)
                .toList();
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // OPTIONS 预检直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getServletPath();
        if (path == null || path.isEmpty()) {
            path = "/";
        }
        PathContainer container = PathContainer.parsePath(path);
        for (PathPattern pattern : publicPatterns) {
            if (pattern.matches(container)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(AUTH_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            // 不能抛异常：本过滤器早于 ExceptionTranslationFilter，抛出只会变成 500
            commence(request, response, "Missing or invalid Authorization header");
            return;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        if (!jwtUtil.validateToken(token)) {
            commence(request, response, "Invalid or expired token");
            return;
        }

        String subject = jwtUtil.getSubject(token);
        if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    subject, null, AuthorityUtils.NO_AUTHORITIES);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 终止请求并返回 401（JSON），与 {@code SecurityConfig} 的未认证响应保持一致。
     */
    private void commence(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException, ServletException {
        if (authenticationEntryPoint == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
            return;
        }
        authenticationEntryPoint.commence(request, response,
                new AuthenticationCredentialsNotFoundException(message));
    }
}
