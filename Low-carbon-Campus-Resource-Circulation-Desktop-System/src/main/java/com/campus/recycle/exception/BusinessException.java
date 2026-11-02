package com.campus.recycle.exception;

/**
 * 业务异常基类。
 * 统一业务层的受检错误，便于上层（GUI / 调用方）统一捕获与提示。
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
