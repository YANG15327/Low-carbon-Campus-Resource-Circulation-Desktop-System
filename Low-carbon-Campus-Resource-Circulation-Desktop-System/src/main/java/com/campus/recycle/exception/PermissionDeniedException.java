package com.campus.recycle.exception;

/**
 * 权限不足异常（如非物品发布者尝试修改/下架他人物品）。
 */
public class PermissionDeniedException extends BusinessException {

    public PermissionDeniedException(String message) {
        super(message);
    }
}
