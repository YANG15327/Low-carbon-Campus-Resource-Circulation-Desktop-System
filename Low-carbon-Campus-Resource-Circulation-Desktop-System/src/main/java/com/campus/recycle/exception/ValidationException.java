package com.campus.recycle.exception;

/**
 * 输入合法性校验失败异常（防御性编程）。
 */
public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(message);
    }
}
