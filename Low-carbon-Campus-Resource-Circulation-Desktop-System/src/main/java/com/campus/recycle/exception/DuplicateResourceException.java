package com.campus.recycle.exception;

/**
 * 资源重复异常（如用户名、分类名重复）。
 */
public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
