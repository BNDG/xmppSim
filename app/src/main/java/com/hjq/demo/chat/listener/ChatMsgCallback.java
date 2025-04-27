package com.hjq.demo.chat.listener;

import com.hjq.demo.chat.entity.ChatMessage;

import com.bndg.smack.entity.SmartMessage;

/**
 * @author r
 * @date 2024/6/17
 * @description Brief description of the file content.
 */
public interface ChatMsgCallback {
    default void sendSingleMsgSuccess(String originId, SmartMessage message, ChatMessage chatMessage, int messageIndex){}
    default void sendSingleMsgFailed(String originId, int code, String desc, int messageIndex){}
    default void receivedMsg(SmartMessage message, ChatMessage chatMessage){}
    default void sendGroupMsgSuccess(String originId, SmartMessage message, ChatMessage chatMessage, int messageIndex){}
    default void sendGroupMsgFailed(String originId, int code, String desc, int messageIndex){}
    default void updateMsgSuccess(ChatMessage chatMessage){}
    default void updateMsgFailed(){}
}
