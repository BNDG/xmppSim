package com.bndg.smack.entity;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArraySet;


import java.util.Date;
import java.util.UUID;

import com.bndg.smack.SmartCommHelper;
import com.bndg.smack.enums.SmartContentType;
import com.bndg.smack.enums.SmartConversationType;

/**
 * @author r
 * @date 2024/11/13
 * @description 消息实体
 */

public class SmartMessage implements Parcelable {

    public SmartMessage() {
    }

    // 消息发送者的jid
    private String fromUserId;
    // 单聊消息发送者昵称 客户自己设置
    private String fromNickname;
    // 发送时 单聊对方jid 群聊room jid
    // 接收时 to是我的jid
    private String toUserId;
    // 消息id
    private String messageId;
    // 消息类型
    private String messageType;
    // 消息体
    private String messageContent;
    // 创建时间
    private long createTime;
    // 会话类型
    private String conversationType;
    // 群id
    private String groupId;
    // 群聊消息主题
    private String groupSubject;
    // 群聊消息发送者的昵称
    private String groupSenderNickname;
    // 消息存档id 拉取历史消息用的id
    private String archivedId;

    // 是否是历史消息
    private boolean isHistoryMsg;
    // 是否是离线消息
    private boolean isOfflineMsg;
    // 消息是否不计入会话未读数 发送方设置。默认为需要计入会话未读数
    private boolean isExcludedFromUnreadCount = true;
    private boolean isChangeType;
    private ArraySet<CharSequence> extensionsXml = new ArraySet<>();

    public ArraySet<CharSequence> getExtensionsXml() {
        return extensionsXml;
    }

    public void setExtensionsXml(ArraySet<CharSequence> extensionsXml) {
        this.extensionsXml = extensionsXml;
    }


    public boolean isOfflineMsg() {
        return isOfflineMsg;
    }

    public void setOfflineMsg(boolean offlineMsg) {
        isOfflineMsg = offlineMsg;
    }

    public boolean isHistoryMsg() {
        return isHistoryMsg;
    }

    public void setHistoryMsg(boolean historyMsg) {
        isHistoryMsg = historyMsg;
    }


    /**
     * 创建房间邀请消息
     *
     * @param fromJid
     * @param nickName
     * @param toJid
     * @param content
     * @return
     */
    public static SmartMessage newRoomInviteMsg(String fromJid, String nickName, String toJid, String content) {
        SmartMessage msgEntity = new SmartMessage();
        msgEntity.setMessageId(UUID.randomUUID().toString());
        msgEntity.setFromUserId(fromJid);
        msgEntity.setFromNickname(nickName);
        msgEntity.setToUserId(toJid);
        msgEntity.setMessageContent(content);
        msgEntity.setMessageType(SmartContentType.CUSTOM);
        msgEntity.setConversationType(SmartConversationType.GROUP.name());
        msgEntity.setCreateTime(new Date().getTime());
        return msgEntity;
    }

    /**
     * 创建会话消息
     *
     * @param fromJid
     * @param nickName
     * @param conversationType
     * @param toJid
     * @param content
     * @return
     */
    public static SmartMessage createConversationMsg(String messageId,
                                                     String fromJid,
                                                     String nickName,
                                                     String conversationType,
                                                     String messageType,
                                                     String toJid, String content) {
        SmartMessage smartMessage = new SmartMessage();
        smartMessage.setMessageId(messageId);
        smartMessage.setCreateTime(new Date().getTime());
        smartMessage.setFromUserId(fromJid);
        smartMessage.setFromNickname(nickName);
        smartMessage.setConversationType(conversationType);
        smartMessage.setMessageType(messageType);
        smartMessage.setToUserId(toJid);
        smartMessage.setMessageContent(content);
        return smartMessage;
    }

    /**
     * 创建单聊消息
     * @param stanzaId
     * @param fromUserId
     * @param toUserId
     * @param messageType
     * @param content
     * @return
     */
    public static SmartMessage createSendSingleMessage(String stanzaId, String fromUserId, String toUserId, String messageType, String content) {
        SmartMessage smartMessage = new SmartMessage();
        smartMessage.setMessageId(stanzaId);
        smartMessage.setCreateTime(new Date().getTime());
        smartMessage.setFromUserId(fromUserId);
        smartMessage.setFromNickname(SmartCommHelper.getInstance().getNickname());
        smartMessage.setToUserId(toUserId);
        smartMessage.setConversationType(SmartConversationType.SINGLE.name());
        smartMessage.setMessageType(messageType);
        smartMessage.setMessageContent(content);
        return smartMessage;
    }

    /**
     * 创建群聊消息
     *
     * @param stanzaId 消息id
     * @param groupId 群id
     * @param senderBareJid 发送者id
     * @param messageType 消息类型
     * @param content 消息内容
     * @return
     */
    public static SmartMessage createSendGroupMessage(String stanzaId, String groupId, String senderBareJid,
                                                       String senderNick, String messageType, String content) {
        SmartMessage smartMessage = new SmartMessage();
        smartMessage.setMessageId(stanzaId);
        smartMessage.setCreateTime(new Date().getTime());
        smartMessage.setFromUserId(senderBareJid);
        smartMessage.setToUserId(groupId);
        smartMessage.setGroupId(groupId);
        smartMessage.setGroupSenderNickname(senderNick);
        smartMessage.setConversationType(SmartConversationType.GROUP.name());
        smartMessage.setMessageType(messageType);
        smartMessage.setMessageContent(content);
        return smartMessage;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getArchivedId() {
        return archivedId;
    }

    public void setArchivedId(String archivedId) {
        this.archivedId = archivedId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getFromNickname() {
        return fromNickname;
    }

    public void setFromNickname(String fromNickname) {
        this.fromNickname = fromNickname;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        if(isChangeType) {
            messageContent = messageContent.trim();
        }
        this.messageContent = messageContent;
    }

    public String getConversationType() {
        return conversationType;
    }

    public void setConversationType(String conversationType) {
        this.conversationType = conversationType;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }


    public String getGroupSenderNickname() {
        return groupSenderNickname;
    }

    public void setGroupSenderNickname(String groupSenderNickname) {
        this.groupSenderNickname = groupSenderNickname;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    protected SmartMessage(Parcel in) {
        fromUserId = in.readString();
        fromNickname = in.readString();
        toUserId = in.readString();
        messageId = in.readString();
        messageType = in.readString();
        messageContent = in.readString();
        createTime = in.readLong();
        conversationType = in.readString();
        groupId = in.readString();
        groupSenderNickname = in.readString();
        archivedId = in.readString();
        groupSubject = in.readString();
        isHistoryMsg = in.readInt() == 1;
        isOfflineMsg = in.readInt() == 1;
        isExcludedFromUnreadCount = in.readInt() == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fromUserId);
        dest.writeString(fromNickname);
        dest.writeString(toUserId);
        dest.writeString(messageId);
        dest.writeString(messageType);
        dest.writeString(messageContent);
        dest.writeLong(createTime);
        dest.writeString(conversationType);
        dest.writeString(groupId);
        dest.writeString(groupSenderNickname);
        dest.writeString(archivedId);
        dest.writeString(groupSubject);
        dest.writeInt(isHistoryMsg ? 1 : 0);
        dest.writeInt(isOfflineMsg ? 1 : 0);
        dest.writeInt(isExcludedFromUnreadCount ? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SmartMessage> CREATOR = new Creator<SmartMessage>() {
        @Override
        public SmartMessage createFromParcel(Parcel in) {
            return new SmartMessage(in);
        }

        @Override
        public SmartMessage[] newArray(int size) {
            return new SmartMessage[size];
        }
    };

    public long getCreateTime() {
        return createTime;
    }

    public String getMessageId() {
        return messageId;
    }

    public boolean isSingle() {
        return SmartConversationType.SINGLE.name().equals(conversationType);
    }

    public void addExtensions(CharSequence extensionXml) {
        this.extensionsXml.add(extensionXml);
    }

    public void setGroupSubject(String subject) {
        this.groupSubject = subject;
    }

    public String getGroupSubject() {
        return groupSubject;
    }

    public void isReChangeType(boolean isChangeType) {
        this.isChangeType = isChangeType;
    }
}
