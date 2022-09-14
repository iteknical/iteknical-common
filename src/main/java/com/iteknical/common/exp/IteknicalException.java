package com.iteknical.common.exp;

/**
 * 异常基类
 * 
 * @author tony
 */
public class IteknicalException extends RuntimeException {
    private final int code;

    public IteknicalException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
