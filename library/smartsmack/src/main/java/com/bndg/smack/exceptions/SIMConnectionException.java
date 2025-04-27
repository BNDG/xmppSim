package com.bndg.smack.exceptions;

/**
 * @author r
 * @date 2024/11/22
 * @description SIM连接异常
 */

public class SIMConnectionException extends Exception {
    private int code; // 错误码字段

    public SIMConnectionException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
