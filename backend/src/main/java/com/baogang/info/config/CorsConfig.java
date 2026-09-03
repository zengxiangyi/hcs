package com.baogang.info.config;

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
 * 配置项在代码中固定，不依赖外部配置或环境变量，避免参数缺失导致启动失败：
 *   - 默认允许所有来源（origin 通配）、关闭凭据，兼容开发与生产通用场景。
 *   - 如需收紧，直接修改下方常量即可，无需额外配置文件。
 *
 * 注意：origin 使用通配符时凭据（allow-credentials）必须关闭，否则浏览器会拒绝，
 * 故此处统一关闭凭据。
 */
@Configuration
public class CorsConfig {

    /** 允许的来源（* 表示全部） */
    private static final List<String> ALLOWED_ORIGINS = List.of("*");
    /** 允许的请求方法 */
    private static final List<String> ALLOWED_METHODS =
            List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");
    /** 允许的请求头（* 表示全部） */
    private static final List<String> ALLOWED_HEADERS = List.of("*");
    /** 预检请求缓存时间（秒） */
    private static final long MAX_AGE = 3600;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 通配来源必须为 false，否则浏览器拒绝
        config.setAllowCredentials(false);
        config.setAllowedOriginPatterns(ALLOWED_ORIGINS);
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
