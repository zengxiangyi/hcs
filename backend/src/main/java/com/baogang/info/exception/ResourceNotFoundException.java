package com.baogang.info.exception;

/**
 * 资源未找到异常，对应 HTTP 404。
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
