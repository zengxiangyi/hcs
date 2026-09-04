package com.baogang.info.common;

import org.springframework.security.core.AuthenticationException;

/**
 * JWT 认证失败异常，携带业务码以便前端区分处理：
 *   - {@link #CODE_TOKEN_EXPIRED}（40101）：token 已过期，前端应引导重新登录
 *   - {@link #CODE_TOKEN_INVALID}（40102）：token 缺失或无效（签名错误/格式非法等）
 *
 * <p>HTTP 状态码固定 401，业务码写入统一响应体的 {@code code} 字段
 * （见 {@code SecurityConfig#unauthorizedEntryPoint}）。
 */
public class JwtAuthenticationException extends AuthenticationException {

    /** token 已过期 */
    public static final int CODE_TOKEN_EXPIRED = 40101;
    /** token 缺失或无效 */
    public static final int CODE_TOKEN_INVALID = 40102;

    /** 业务码，写入响应体 code 字段 */
    private final int businessCode;

    public JwtAuthenticationException(int businessCode, String message) {
        super(message);
        this.businessCode = businessCode;
    }

    public int getBusinessCode() {
        return businessCode;
    }
}
