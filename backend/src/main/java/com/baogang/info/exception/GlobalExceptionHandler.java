package com.baogang.info.exception;

import com.baogang.info.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理：将各类异常转换为统一 {@link ApiResponse} 结构。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Bean Validation 失败（@Valid 标注的 @RequestBody）。 */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), msg);
    }

    /** Bean Validation 失败（Controller 上 @Validated 标注的查询参数）。 */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraintViolation(ConstraintViolationException ex) {
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    /** 业务未找到等场景抛出的异常。 */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ApiResponse<Void> handleNotFound(ResourceNotFoundException ex) {
        return ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    /** 业务参数非法（如用户名重复）。 */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException ex) {
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    /** 兜底：未预期异常。必须落日志，否则线上无从排障。 */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("未处理异常 uri={} method={}", request.getRequestURI(), request.getMethod(), ex);
        return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误");
    }

    /** 路径参数类型不匹配（如 /xxx/{id} 传非数字）→ 400。 */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResponse<Void> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "参数类型错误: " + ex.getName());
    }

    /** 请求体 JSON 格式错误或缺失 → 400。 */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Void> handleNotReadable(HttpMessageNotReadableException ex) {
        log.warn("请求体不可读 uri={}", currentUri(), ex);
        return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "请求体格式错误或缺失");
    }

    /** 请求的 URL 不存在（此前落兜底变 500）→ 404。 */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoResourceFoundException.class)
    public ApiResponse<Void> handleNoResource(NoResourceFoundException ex) {
        return ApiResponse.error(HttpStatus.NOT_FOUND.value(), "接口不存在: " + ex.getResourcePath());
    }

    /** 数据库完整性冲突（唯一键重复、外键约束等）→ 409，落 warn 日志便于定位重复业务键来源。 */
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiResponse<Void> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("数据完整性冲突 uri={}", currentUri(), ex);
        return ApiResponse.error(HttpStatus.CONFLICT.value(), "数据冲突：业务键可能已存在");
    }

    private static String currentUri() {
        var attrs = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (attrs instanceof org.springframework.web.context.request.ServletRequestAttributes sra) {
            return sra.getRequest().getRequestURI();
        }
        return "-";
    }
}
