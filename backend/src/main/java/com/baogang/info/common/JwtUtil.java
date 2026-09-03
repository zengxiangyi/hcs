package com.baogang.info.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT token 生成与验证工具。
 *
 * 使用 jjwt 0.13.0 的 fluent API：密钥由配置项 {@code jwt.secret} 经 HS256 派生，
 * 过期时间由 {@code jwt.expiration-ms} 控制。
 */
@Component
public class JwtUtil {

    /** JWT 签名密钥（Base64 长度需满足 HS256 至少 256 bit） */
    @Value("${jwt.secret}")
    private String secret;

    /** token 有效期（毫秒） */
    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    /** 签发者 */
    @Value("${jwt.issuer:baogang-info}")
    private String issuer;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 为指定 subject（通常为用户名/用户标识）生成 token。
     *
     * @param subject 主体标识
     * @return 已签名的 JWT 字符串
     */
    public String generateToken(String subject) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(subject)
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析并验证 token，返回其中的 Claims。
     *
     * @param token JWT 字符串
     * @return 解析成功的 Claims（含 subject、过期时间等）
     * @throws io.jsonwebtoken.JwtException 当签名无效或已过期时抛出
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证 token 合法性（签名 + 过期 + 签发者）。
     *
     * @param token JWT 字符串
     * @return 合法且未过期返回 true，否则 false
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 token 中取出 subject。
     *
     * @param token JWT 字符串
     * @return subject，解析失败返回 null
     */
    public String getSubject(String token) {
        try {
            return parseToken(token).getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}
