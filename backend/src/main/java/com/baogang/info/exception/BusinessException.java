package com.baogang.info.exception;

/**
 * 业务异常，抛出后会被全局异常处理器捕获，返回友好提示给前端
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 默认错误码：通用业务异常
     */
    private static final int DEFAULT_ERROR_CODE = -1;

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = DEFAULT_ERROR_CODE;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = DEFAULT_ERROR_CODE;
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * 构造业务异常，可指定是否生成堆栈跟踪
     *
     * @param code 错误码
     * @param message 错误消息
     * @param cause 异常原因
     * @param enableSuppression 是否启用抑制
     * @param writableStackTrace 是否生成堆栈跟踪
     */
    protected BusinessException(int code, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
