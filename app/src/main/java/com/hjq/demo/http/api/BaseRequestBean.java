package com.hjq.demo.http.api;

/**
 * @author r
 * @date 2024/7/8
 * @description Brief description of the file content.
 */
public class BaseRequestBean {
    public int code;
    public String msg;

    /**
     * 是否请求成功
     */
    public boolean isRequestSucceed() {
        return 200 == code;
    }

    public boolean isTokenFailure() {
        return false;
    }
}
