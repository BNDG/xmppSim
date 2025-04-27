package com.hjq.demo.http.model;

import androidx.annotation.Nullable;

import java.util.Map;

import okhttp3.Headers;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject
 *    time   : 2019/12/07
 *    desc   : 统一接口数据结构
 */
public class HttpData<T> {
    /** 请求头 */
    private Headers headers;

    /** 返回码 */
    private int code;
    /** 提示语 */
    private String msg;
    /** 数据 */
    private T data;

    public void setHeaders(Headers headers) {
        this.headers = headers;
    }

    public Headers getHeaders() {
        return headers;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

    /**
     * 是否请求成功
     */
    public boolean isRequestSucceed() {
        return 200 == code;
    }

    /**
     * 是否 Token 失效
     */
    public boolean isTokenFailure() {
        return "401".equals(code);
    }


}