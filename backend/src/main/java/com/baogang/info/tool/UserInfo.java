package com.baogang.info.tool;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 登录用户信息工具类
 */
public class UserInfo {

    private UserInfo() {
    }

    /**
     * 获取当前登录用户名（principal 为 JWT 中的 subject，即用户名 String）。
     *
     * @return 用户名；未认证、匿名用户或异常 principal 时返回空字符串
     */
    public static String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "";
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof String username && !username.isBlank()) {
            return username;
        }
        return "";
    }
}
