package com.baogang.info.config;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.JwtAuthenticationException;
import com.baogang.info.common.JwtAuthenticationFilter;
import com.baogang.info.common.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * Spring Security 配置：以 JWT 过滤器接管认证，全程无状态（不创建 HttpSession）。
 *
 * 说明：本过滤器仅做 token 校验，不做角色鉴权；如需按角色限制接口，
 * 在下方 requestMatchers(...) 中补充 .hasRole(...) 即可。
 */
@Configuration
public class SecurityConfig {

    /** ObjectMapper 线程安全，复用单例，避免每次 401 响应都新建 */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 免认证白名单（ant 风格，相对 context-path，Servlet 路径）。
     * 同时用于 {@code SecurityConfig#securityFilterChain} 的 permitAll 与
     * {@link JwtAuthenticationFilter} 的 {@code shouldNotFilter}，避免放行路径双写。
     *
     * <p>前端已不再随本 war 分发：vite 产物单独打成 {@code ROOT.war}（context-path /），
     * 与本 war（context-path /api）同 Tomcat 部署，因此本应用不再持有任何静态资源，
     * 白名单只需放行登录/鉴权接口。
     */
    public static final String[] PUBLIC_PATHS = {
            "/auth/**"
    };

    /**
     * 未认证（无/非法 token）统一返回 401 + 统一 JSON，而非 Spring Security 默认的 403。
     * 响应体 code 携带业务码，供前端区分处理：
     *   - 40101 = token 已过期（引导重新登录）
     *   - 40102 = token 缺失或无效
     *
     * <p>同时注入 {@link JwtAuthenticationFilter}：该过滤器位于 ExceptionTranslationFilter
     * 之前，抛出 AuthenticationException 不会被引擎翻译成 401，必须由它自己调用本入口点。
     */
    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (HttpServletRequest request,
                HttpServletResponse response,
                AuthenticationException authException) -> {
            int code = JwtAuthenticationException.CODE_TOKEN_INVALID;
            String message = "Missing or invalid Authorization header";
            if (authException instanceof JwtAuthenticationException jwtEx) {
                code = jwtEx.getBusinessCode();
                message = jwtEx.getMessage();
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(OBJECT_MAPPER
                    .writeValueAsString(ApiResponse.error(code, message)));
        };
    }

    /**
     * JWT 过滤器实例。不使用组件扫描注册（见类注释），保证只存在一处注册来源。
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil,
                                                           AuthenticationEntryPoint unauthorizedEntryPoint) {
        return new JwtAuthenticationFilter(jwtUtil, PUBLIC_PATHS, unauthorizedEntryPoint);
    }

    /**
     * 关闭 Spring Boot 对 Filter bean 的自动（Servlet 容器链）注册。
     *
     * 若不禁用：该过滤器会同时出现在容器链（order = LOWEST_PRECEDENCE）和 Security 链
     * （{@code addFilterBefore}）中。OncePerRequestFilter 的 alreadyFiltered 标记在内层
     * 执行完毕的 finally 中被清除，外层再次进入时看不到标记，导致每个请求验签两遍。
     * 声明此 FilterRegistrationBean 后，Boot 的 ServletContextInitializerBeans 会把该
     * Filter 标记为已处理，不再追加默认注册。
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(jwtAuthenticationFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter,
                                                   AuthenticationEntryPoint unauthorizedEntryPoint) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 登录/鉴权接口与静态站点资源放行，其余均需通过 JWT 过滤器
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedEntryPoint))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
