package com.hjq.demo.chat.entity;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "friendApply")
public class FriendApply {
    @PrimaryKey
    @NonNull
    private String applyId = "";
    // 好友ID
    private String friendUserId;
    // 申请备注
    private String applyRemark;
    // 申请时间
    private String createTime;
    // 发起人昵称
    private String friendNickname;
    // 发起人头像
    private String friendUserAvatar;
    // 性别
    private String friendUserSex;
    // 个签
    private String friendUserSign;
    // 所属账号
    private String belongAccount;
    // 状态 同意 已查看 拒绝
    private String status;

    public String getBelongAccount() {
        return belongAccount;
    }

    public void setBelongAccount(String belongAccount) {
        this.belongAccount = belongAccount;
    }

    public String getApplyId() {
        return applyId;
    }

    public void setApplyId(String applyId) {
        this.applyId = applyId;
    }

    public String getFriendUserId() {
        return friendUserId;
    }

    public void setFriendUserId(String friendUserId) {
        this.friendUserId = friendUserId;
    }

    public String getApplyRemark() {
        return applyRemark;
    }

    public void setApplyRemark(String applyRemark) {
        this.applyRemark = applyRemark;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getFriendNickname() {
        return TextUtils.isEmpty(friendNickname) ? friendUserId : friendNickname;
    }

    public void setFriendNickname(String friendNickname) {
        this.friendNickname = friendNickname;
    }

    public String getFriendUserAvatar() {
        return friendUserAvatar;
    }

    public void setFriendUserAvatar(String friendUserAvatar) {
        this.friendUserAvatar = friendUserAvatar;
    }

    public String getFriendUserSex() {
        return friendUserSex;
    }

    public void setFriendUserSex(String friendUserSex) {
        this.friendUserSex = friendUserSex;
    }

    public String getFriendUserSign() {
        return friendUserSign;
    }

    public void setFriendUserSign(String friendUserSign) {
        this.friendUserSign = friendUserSign;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
