package com.coooolfan.easyhome.exception;

/**
 * 业务异常
 * @author lima
 */
public class BaseException
        extends RuntimeException {
    public BaseException() {
    }
    public BaseException(String msg) {
        super(msg);
    }
}
