package com.hjq.demo.chat.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.hjq.demo.chat.adapter.GroupMemberAdapter;

import java.util.Objects;

import com.bndg.smack.muc.MUCManager;

/**
 * @author r
 * @date 2024/6/3
 * @description Brief description of the file content.
 */
@Entity(tableName = "group_member")
public class GroupMember implements MultiItemEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    // 群成员所属群id
    private String groupId;
    // muc群成员账号 群内唯一 是表示用户身份的字段 用来管理群成员 比如移除禁言
    private String memberAccount;

    // muc群成员个人信息的真实昵称
    private String memberName;
    // 群成员真实的jid 用来判断联系人是否已被邀请 在xmpp匿名是entityFullJid
    private String memberRealUserId;
    // 成员角色，例如管理员、普通成员等
    private String role;

    // 归属于哪个账户
    private String belongAccount;

    // 岗位 所有者（owner）、管理者（admin）、成员（member）、排斥者（outcast）
    private String affiliation;


    public boolean isOwner() {
        return MUCManager.getInstance().isOwner(affiliation);
    }

    public boolean isAdmin() {
        return MUCManager.getInstance().isAdmin(affiliation);
    }

    public GroupMember setItemType(int itemType) {
        this.itemType = itemType;
        return this;
    }

    @Ignore
    private int itemType = GroupMemberAdapter.DEFAULT_MEMBER;

    public String getBelongAccount() {
        return belongAccount;
    }

    public void setBelongAccount(String belongAccount) {
        this.belongAccount = belongAccount;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getMemberAccount() {
        return memberAccount;
    }

    public void setMemberAccount(String memberAccount) {
        this.memberAccount = memberAccount;
    }

    public String getMemberRealUserId() {
        return memberRealUserId;
    }

    public void setMemberRealUserId(String memberRealUserId) {
        this.memberRealUserId = memberRealUserId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public int getItemType() {
        return itemType;
    }


    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupMember that = (GroupMember) o;
        return Objects.equals(groupId, that.groupId) && Objects.equals(memberAccount, that.memberAccount)
                && Objects.equals(belongAccount, that.belongAccount);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(groupId);
        result = 31 * result + Objects.hashCode(memberAccount);
        result = 31 * result + Objects.hashCode(belongAccount);  // 加入 belongAccount
        return result;
    }

    public boolean isVisitor() {
        return MUCManager.getInstance().isVisitor(role);
    }

    public boolean isParticipant() {
        return MUCManager.getInstance().isParticipant(role);
    }

    public String getMemberOriginId() {
        // 如果能获取到真实id 就用真实id 否则用原始id
        return memberRealUserId != null ? memberRealUserId : getGroupId() + "/" + getMemberAccount();
    }
}
