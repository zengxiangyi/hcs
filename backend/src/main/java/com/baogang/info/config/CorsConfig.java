package com.baogang.info.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * 跨域（CORS）配置。
 *
 * 允许的来源由配置项 {@code cors.allowed-origins} 控制，多个来源用逗号分隔：
 *   - 开发/测试阶段取值 {@code *}
 *   - 生产环境确定部署 IP 后改为具体白名单，如 {@code http://10.21.46.191:8080}
 *
 * 注意：origin 使用通配符时凭据（allow-credentials）必须关闭，否则浏览器会拒绝，
 * 故此处统一关闭凭据。
 */
@Configuration
public class CorsConfig {

    /** 允许的请求方法 */
    private static final List<String> ALLOWED_METHODS =
            List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");
    /** 允许的请求头（* 表示全部） */
    private static final List<String> ALLOWED_HEADERS = List.of("*");
    /** 预检请求缓存时间（秒） */
    private static final long MAX_AGE = 3600;

    /**
     * 允许的来源，配置项 {@code cors.allowed-origins}，逗号分隔，默认 *。
     */
    @Value("${cors.allowed-origins:*}")
    private List<String> allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 通配来源必须为 false，否则浏览器拒绝
        config.setAllowCredentials(false);
        config.setAllowedOriginPatterns(allowedOrigins);
        config.setAllowedMethods(ALLOWED_METHODS);
        config.setAllowedHeaders(ALLOWED_HEADERS);
        config.setExposedHeaders(Arrays.asList(
                HttpHeaders.CONTENT_DISPOSITION, HttpHeaders.CONTENT_TYPE, HttpHeaders.LOCATION));
        config.setMaxAge(MAX_AGE);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
