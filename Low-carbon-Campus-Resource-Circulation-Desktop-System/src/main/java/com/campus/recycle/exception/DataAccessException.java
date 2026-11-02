package com.campus.recycle.exception;

/**
 * 数据访问异常，用于包装 JDBC / SQLite 底层异常。
 */
public class DataAccessException extends BusinessException {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
