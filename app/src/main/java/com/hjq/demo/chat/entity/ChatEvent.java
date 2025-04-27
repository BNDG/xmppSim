package com.hjq.demo.chat.entity;

import android.os.Bundle;

/**
 * author : r
 * time   : 2024/6/1 9:06 PM
 * desc   :
 */
public class ChatEvent {
    // 刷新会话列表
    public static final String REFRESH_CONVERSATION_LIST = "REFRESH_CONVERSATION_LIST";
    public static final String REFRESH_CHAT_UI = "REFRESH_CHAT_UI";
    // 呼叫相关
    public static final String CLOSE_CALL = "CLOSE_CALL";
    public static final String SEND_CALL_MSG = "SEND_CALL_MSG";
    public static final String END_CALL_MSG = "END_CALL_MSG";
    public static final String CANCEL_CALL_MSG = "CANCEL_CALL_MSG";
    public static final String REFUSE_CALL_MSG = "REFUSE_CALL_MSG";
    public static final String SEND_ACCEPT_CALL = "SEND_ACCEPT_CALL";
    public static final String ACCEPT_CALL = "ACCEPT_CALL";
    public static final String CLOSE_GROUP_CALL = "CLOSE_GROUP_CALL";
    public static final String LEAVE_CALL_MSG = "LEAVE_CALL_MSG";
    public static final String SEND_GROUP_CALL_MSG = "SEND_GROUP_CALL_MSG";
    public static final String END_GROUP_CALL_MSG = "END_GROUP_CALL_MSG";
    public static final String CANCEL_GROUP_CALL_MSG = "CANCEL_GROUP_CALL_MSG";
    public static final String RECEIVED_ACCEPT_GROUP_CALL = "RECEIVED_ACCEPT_GROUP_CALL";
    // 删除会话
    public static final String CONVERSATION_REMOVED = "CONVERSATION_REMOVED";
    // 刷新会话新消息内容 默认只更新消息详情和未读数 消息时间
    public static final String CONVERSATION_ITEM_CONTENT_UPDATE = "CONVERSATION_ITEM_CONTENT_UPDATE";
    // 刷新好友头像
    public static final String REFRESH_USER_AVATAR = "REFRESH_USER_AVATAR";
    // 刷新群聊成员头像
    public static final String REFRESH_GROUP_MEMBER_AVATAR = "REFRESH_GROUP_MEMBER_AVATAR";
    // 刷新用户信息
    public static final String REFRESH_USER_INFO = "REFRESH_USER_INFO";
    // 群聊中被踢出
    public static final String KICKED_ME = "KICKED_ME";
    // 群聊被解散
    public static final String GROUP_DESTROYED = "GROUP_DESTROYED";
    // 好友删除
    public static final String CONTACT_REMOVED = "CONTACT_REMOVED";
    // 刷新好友
    public static final String REFRESH_CONTACT = "REFRESH_CONTACT";
    // 文件下载完成
    public static final String FILE_DOWNLOAD_COMPLETE = "FILE_DOWNLOAD_COMPLETE";
    // 文件下载进度
    public static final String FILE_DOWNLOAD_PROGRESS = "FILE_DOWNLOAD_PROGRESS";
    // 文件上传完成
    public static final String UPLOAD_FILE_SUCCESS = "UPLOAD_FILE_SUCCESS";


    public String getWhat() {
        return what;
    }

    public ChatEvent setWhat(String what) {
        this.what = what;
        return this;
    }

    private String what;
    public Object obj;
    public Bundle bundle;

    public ChatEvent(String what) {
        this.what = what;
    }

}
