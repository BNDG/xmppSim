package com.hjq.demo.chat.listener;

public interface OnFragmentListener {
    default void processStrangerInfo(String userid, String nickname, String phone, String aliasusername){}
}