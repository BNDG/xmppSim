package com.hjq.demo.http.api;

import com.hjq.http.config.IRequestApi;

/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2019/12/07
 * desc   : 用户登录
 */
public final class LoginApi implements IRequestApi {

    @Override
    public String getApi() {
        return "chat/rcnt/login";
    }

    /**
     * 手机号
     */
    private String phone;


    private String aliasusername;

    public LoginApi setAliaspassword(String aliaspassword) {
        this.aliaspassword = aliaspassword;
        return this;
    }

    /**
     * 登录密码
     */
    private String aliaspassword;

    public LoginApi setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public LoginApi setAliasusername(String aliasusername) {
        this.aliasusername = aliasusername;
        return this;
    }

    public final static class Bean extends BaseRequestBean {

        public String tusername;
        public String tpassword;
        public String unionid;
        public String openid;
    }
}