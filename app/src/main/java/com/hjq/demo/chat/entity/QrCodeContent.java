package com.hjq.demo.chat.entity;

import com.hjq.demo.chat.utils.CryptoUtil;

import java.util.Map;

public class QrCodeContent {
    public final static String QR_CODE_TYPE_USER = "user";
    public static final String START = "user,";

    private String type;

    public String getPhone() {
        return CryptoUtil.simpleDecrypt(phone);
    }

    public void setPhone(String phone) {
        this.phone = CryptoUtil.simpleEncrypt(phone);
    }

    public String getAliasusername() {
        return CryptoUtil.simpleDecrypt(aliasusername);
    }

    public void setAliasusername(String aliasusername) {
        this.aliasusername = CryptoUtil.simpleEncrypt(aliasusername);
    }

    public String getUserid() {
        return CryptoUtil.simpleDecrypt(userid);
    }

    public void setUserid(String userid) {
        this.userid = CryptoUtil.simpleEncrypt(userid);
    }

    private String phone;
    private String aliasusername;
    private String userid;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    private String nickname;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
