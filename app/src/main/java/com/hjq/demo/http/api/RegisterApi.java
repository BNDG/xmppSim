package com.hjq.demo.http.api;

import com.hjq.http.config.IRequestApi;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject
 *    time   : 2019/12/07
 *    desc   : 用户注册
 */
public final class RegisterApi implements IRequestApi {

    @Override
    public String getApi() {
        return "chat/rcnt/register";
    }

    // 昵称
    private String nickname;
    // 手机号
    private String phone;
    // 用户名
    private String aliasusername;
    // 验证码
    private String verificationCode;
    // 密码
    private String aliaspassword;


    public RegisterApi setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
        return this;
    }
    public RegisterApi setAliaspassword(String aliaspassword) {
        this.aliaspassword = aliaspassword;
        return this;
    }

    public RegisterApi setAliasusername(String aliasusername) {
        this.aliasusername = aliasusername;
        return this;
    }

    public RegisterApi setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }
    public RegisterApi setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public final static class Bean extends BaseRequestBean {

    }
}