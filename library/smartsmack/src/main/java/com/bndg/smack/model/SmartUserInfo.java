//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.bndg.smack.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.jivesoftware.smack.roster.packet.RosterPacket;
import org.jivesoftware.smackx.muc.MUCAffiliation;
import org.jivesoftware.smackx.muc.MUCRole;

import java.util.Objects;

/**
 * @author r
 * @date 2024/5/20
 * @description 用户信息类
 */

public class SmartUserInfo implements Parcelable, Comparable<SmartUserInfo> {
    public static String MODERATOR = MUCRole.moderator.name();
    public static String VISITOR = MUCRole.visitor.name();
    // 用户JID
    protected String userId;
    // 昵称
    protected String nickname = "";

    public String getSubscribeStatus() {
        return subscribeStatus;
    }

    public void setSubscribeStatus(String subscribeStatus) {
        this.subscribeStatus = subscribeStatus;
    }

    // both 互为好友。none 不是好友 没有订阅关系。
    // from 对方订阅了我的状态，但我没有订阅对方的状态。 我在对方的好友列表中。
    // to 我订阅了对方的状态，但对方没有 订阅我的状态。对方在我的好友列表中。
    protected String subscribeStatus = "";
    protected int blacklist = -1;

    // 群成员账号 唯一的
    protected String memberAccount;

    protected String groupId;
    // 角色
    protected String role;
    protected String affiliation;
    // 好友申请信息
    private String friendRequestIntro;
    // 头像
    private byte[] userAvatar;
    // 头像hash
    private String userAvatarHash;

    public SmartUserInfo() {
    }

    public String getRoleName() {
        if (MUCRole.participant.name().equals(role)) {
            return "参与者";
        } else if (MUCRole.visitor.name().equals(role)) {
            return "访客";
        } else if (MUCRole.moderator.name().equals(role)) {
            return "主持人";
        } else if (MUCRole.none.name().equals(role)) {
            return "未分配";
        }
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAffiliationName() {
        if (MUCAffiliation.owner.name().equals(affiliation)) {
            return "房间所有者";
        } else if (MUCAffiliation.admin.name().equals(affiliation)) {
            return "管理员";
        } else if (MUCAffiliation.member.name().equals(affiliation)) {
            return "成员";
        } else if (MUCAffiliation.outcast.name().equals(affiliation)) {
            return "禁止访问房间";
        } else if (MUCAffiliation.none.name().equals(affiliation)) {
            return "无特殊权限";
        }
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userID) {
        this.userId = userID;
    }

    public String getMemberAccount() {
        return memberAccount;
    }

    public void setMemberAccount(String memberAccount) {
        this.memberAccount = memberAccount;
    }

    public String getNickname() {
        return this.nickname;
    }

    public byte[] getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(byte[] userAvatar) {
        this.userAvatar = userAvatar;
    }
    public String getUserAvatarHash() {
        return userAvatarHash;
    }

    public void setUserAvatarHash(String userAvatarHash) {
        this.userAvatarHash = userAvatarHash;
    }

    public String getFriendRequestIntro() {
        return friendRequestIntro;
    }

    public void setFriendRequestIntro(String friendRequestIntro) {
        this.friendRequestIntro = friendRequestIntro;
    }
    public void setNickname(String var1) {
        this.nickname = var1;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userId);
        dest.writeString(this.nickname);
        dest.writeInt(this.blacklist);
        dest.writeString(this.memberAccount);
        dest.writeString(this.role);
        dest.writeString(this.affiliation);
        dest.writeString(this.subscribeStatus);
        dest.writeString(this.groupId);
    }

    protected SmartUserInfo(Parcel in) {
        this.userId = in.readString();
        this.nickname = in.readString();
        this.blacklist = in.readInt();
        this.memberAccount = in.readString();
        this.role = in.readString();
        this.affiliation = in.readString();
        this.subscribeStatus = in.readString();
        this.groupId = in.readString();
    }

    public static final Parcelable.Creator<SmartUserInfo> CREATOR = new Parcelable.Creator<SmartUserInfo>() {
        @Override
        public SmartUserInfo createFromParcel(Parcel source) {
            return new SmartUserInfo(source);
        }

        @Override
        public SmartUserInfo[] newArray(int size) {
            return new SmartUserInfo[size];
        }
    };

    public String getAffiliation() {
        return affiliation;
    }

    public String getRole() {
        return role;
    }

    public boolean isNotFriend() {
        return RosterPacket.ItemType.none.toString().equals(subscribeStatus);
    }

    public boolean isFriend() {
        return RosterPacket.ItemType.both.toString().equals(subscribeStatus) || RosterPacket.ItemType.to.toString().equals(subscribeStatus);
    }

    /**
     * 对方在我的好友列表中
     * @return
     */
    public boolean inMyFriendList() {
        return RosterPacket.ItemType.to.toString().equals(subscribeStatus);
    }

    /**
     * 我在对方的好友列表中
     * @return
     */
    public boolean inHisFriendList() {
        return RosterPacket.ItemType.from.toString().equals(subscribeStatus);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SmartUserInfo userInfo = (SmartUserInfo) o;
        return Objects.equals(userId, userInfo.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId);
    }

    @Override
    public int compareTo(SmartUserInfo other) {
        // 按照用户名进行比较，可以根据需求自定义排序规则
        return this.userId.compareTo(other.userId);
    }
}
