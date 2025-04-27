package com.bndg.smack.contract;

import org.jivesoftware.smack.packet.ExtensionElement;

import java.util.List;

import com.bndg.smack.callback.IFileCallback;
import com.bndg.smack.callback.IMsgCallback;
import com.bndg.smack.extensions.base.IExtension;

public interface ISmartCommMsg {

    /**
     * @param msgType           消息类型
     * @param messageContent    消息内容
     * @param extensionElements 扩展元素
     * @param msgCallback       发送回调
     */
    void sendGroupMessage(String groupId, String msgType, String messageContent, List<IExtension> extensionElements, IMsgCallback msgCallback);

    /**
     * @param targetId          对方id
     * @param messageContent    消息内容
     * @param msgType           消息类型
     * @param extensionElements 扩展元素
     * @param iMsgCallback      发送回调
     */
    void sendSingleMessage(String targetId, String msgType, String messageContent, List<IExtension> extensionElements, IMsgCallback iMsgCallback);

    /**
     * 发送文件
     *
     * @param filePath
     */
    void sendFileByOriginalMeans(String filePath, IFileCallback iFileCallback);

    /**
     * 发送阅读回执
     *
     * @param messageId
     */
    void sendReceipt(String toJid, String messageId);

}
