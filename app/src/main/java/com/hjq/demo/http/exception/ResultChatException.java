package com.hjq.demo.http.exception;

import androidx.annotation.NonNull;

import com.hjq.demo.http.api.BaseRequestBean;
import com.hjq.demo.http.model.HttpData;
import com.hjq.http.exception.HttpException;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/EasyHttp
 *    time   : 2019/06/25
 *    desc   : 返回结果异常
 */
public final class ResultChatException extends HttpException {

    private final BaseRequestBean mData;

    public ResultChatException(String message, BaseRequestBean data) {
        super(message);
        mData = data;
    }

    public ResultChatException(String message, Throwable cause, BaseRequestBean data) {
        super(message, cause);
        mData = data;
    }

    @NonNull
    public BaseRequestBean getHttpData() {
        return mData;
    }
}