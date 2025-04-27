package com.hjq.demo.chat.entity;

import android.text.TextUtils;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.hjq.demo.chat.adapter.SmartConversationAdapter;
import com.hjq.demo.utils.CheckUtil;

import java.util.Objects;

import com.bndg.smack.enums.SmartConversationType;

/**
 * @author r
 * @date 2024/5/20
 * @description 持久化会话记录
 */
@Entity(tableName = "conversation_info")
public class ConversationInfo implements MultiItemEntity, Cloneable {
    @PrimaryKey(autoGenerate = true)
    public int id;
    // 会话类型 单聊 群聊
    private String conversationType;
    // 会话标题 单聊是对方昵称 群聊自定义
    private String conversationTitle;
    // 会话id
    private String conversationId;
    // 会话所属账号
    private String belongAccount;
    // 最后一条消息时间
    private long lastMsgDate;
    // 群信息 json字符串
    private String groupMemberNicknames;
    // 会话未读数
    private int unReadNum;
    // 会话是否可用 退群、被移除出群、被删好友导致会话不可用
    private boolean available = true;
    // 是否置顶
    private boolean pinned = false;
    public boolean reloadAvatar;
    // 会话最后一条消息摘要
    public String digest;
    // 最后一条消息id
    public String fromJid;

    public String getDigest() {
        return CheckUtil.getNotNullString(digest);
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
    public String getGroupMemberNicknames() {
        return groupMemberNicknames;
    }

    public void setGroupMemberNicknames(String groupMemberNicknames) {
        this.groupMemberNicknames = groupMemberNicknames;
    }

    public String getFromJid() {
        return fromJid;
    }

    public void setFromJid(String fromJid) {
        this.fromJid = fromJid;
    }
    public String getBelongAccount() {
        return belongAccount;
    }

    public void setBelongAccount(String belongAccount) {
        this.belongAccount = belongAccount;
    }

    public int getUnReadNum() {
        return unReadNum;
    }

    public void setUnReadNum(int unReadNum) {
        this.unReadNum = unReadNum;
    }


    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }
    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public ConversationInfo() {
    }

    public String getConversationType() {
        return conversationType;
    }

    public void setConversationType(String conversationType) {
        this.conversationType = conversationType;
    }

    public String getConversationTitle() {
        return TextUtils.isEmpty(conversationTitle) ? "" : conversationTitle;
    }

    public void setConversationTitle(String conversationTitle) {
        this.conversationTitle = conversationTitle;
    }

    public long getLastMsgDate() {
        return lastMsgDate;
    }

    public void setLastMsgDate(long lastMsgDate) {
        this.lastMsgDate = lastMsgDate;
    }

    /**
     * 获取未读数
     *
     * @return
     */
    public int getUnReadMsgCnt() {
        return unReadNum;
    }

    @Override
    public int getItemType() {
        return SmartConversationType.SINGLE.name().equals(conversationType) ?
                SmartConversationAdapter.SINGLE :
                SmartConversationAdapter.GROUP;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConversationInfo that = (ConversationInfo) o;
        return Objects.equals(conversationId, that.conversationId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(conversationId);
    }

    @Override
    public ConversationInfo clone() {
        try {
            ConversationInfo clone = (ConversationInfo) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
