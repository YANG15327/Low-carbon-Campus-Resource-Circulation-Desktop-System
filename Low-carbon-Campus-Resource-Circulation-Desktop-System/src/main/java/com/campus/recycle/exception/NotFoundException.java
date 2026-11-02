package com.campus.recycle.exception;

/**
 * 目标资源不存在异常。
 */
public class NotFoundException extends BusinessException {

    public NotFoundException(String message) {
        super(message);
    }
}
