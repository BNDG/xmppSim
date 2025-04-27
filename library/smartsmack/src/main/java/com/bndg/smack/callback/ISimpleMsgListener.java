package com.bndg.smack.callback;

import com.bndg.smack.SmartIMClient;
import com.bndg.smack.entity.SmartMessage;

/**
 * @author r
 * @date 2024/6/24
 * @description 消息接收监听器
 */
public interface ISimpleMsgListener {
    void receivedSmartMessage(SmartMessage message);

    default void receivedTestGroupMembers(String inviteJid, String roomId, String memberIds, String memberNicknames) {
    }

    void receivedErrorMessage(String smartMessageId, int code, String desc);

}
