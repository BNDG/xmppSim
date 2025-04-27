package com.hjq.demo.http.api;

import com.hjq.http.config.IRequestApi;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject
 *    time   : 2019/12/07
 *    desc   : 获取验证码
 */
public final class GetCodeApi implements IRequestApi {

    @Override
    public String getApi() {
        return "chat/rcnt/sendSms";
    }

    /** 手机号 */
    private String phone;

    public GetCodeApi setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public final static class Bean extends BaseRequestBean {
        public String verificationCode;
    }
}