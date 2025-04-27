package com.hjq.demo.chat.entity;

import android.text.TextUtils;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.utils.Trace;

/**
 * 用户
 * Android Sugar ORM
 *
 * @author zhou
 */
@Entity(tableName = "contact")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;
    // m1918@tigase.bndg.cn Jid的格式
    private String userId;
    // 身份标识
    private String unionid;
    // 用户类型 "REG": 普通注册用户 "WEIXIN": 团队 "FILEHELPER": 文件传输助手
    private String userType = Constant.USER_TYPE_REG;
    // 昵称
    private String userNickName;
    // 手机号
    private String userPhone;
    // 用户名
    private String userAccount;
    // 头像
    private String userAvatar;
    // 首字母
    private String userHeader;
    // 性别
    private String userSex;
    // 地区
    private String userRegion;
    // 个性签名
    private String userSign;
    // 邮箱
    private String userEmail;
    // 是否已绑定邮箱
    private String userIsEmailLinked;
    // 是否是好友
    private String isFriend;
    // 订阅状态from 对方有我 to我有对方 both互相好友 none非好友
    private String subscribeStatus;
    // 联系人状态
    @Ignore
    private boolean selected;

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    @Ignore boolean isOnline;
    //属于哪个账户
    private String belongAccount;
    // 联系人相关
    private String userContactMobiles;
    // 备注 联系人别名
    private String userContactAlias;
    // 联系人其他信息
    private String userContactDesc;

    /**
     * 联系人权限相关
     */
    private String userContactPrivacy;
    //
    private String userContactHideMyPosts;
    //
    private String userContactHideHisPosts;

    // 是否星标好友
    private String isStarred;

    // 是否在黑名单中
    private String isBlocked = Constant.CONTACT_IS_NOT_BLOCKED;

    @Ignore
    public User(String fromUserId, String fromUserName) {
        this.userId = fromUserId;
        this.userNickName = fromUserName;
    }

    public User() {
    }

    public static String getAccountById(String contactId) {
        int index = contactId.indexOf("@");
        if (index != -1) {
            return contactId.substring(0, index);
        } else {
            return contactId;
        }
    }

    public String getSubscribeStatus() {
        return subscribeStatus;
    }

    public void setSubscribeStatus(String subscribeStatus) {
        this.subscribeStatus = subscribeStatus;
    }

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public String getUnionid() {
        return unionid;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid;
    }

    public String getBelongAccount() {
        return belongAccount;
    }

    public void setBelongAccount(String belongAccount) {
        this.belongAccount = belongAccount;
    }

    /**
     * @return "m1920@tigase.bndg.cn"
     */
    public String getUserId() {
        return userId == null ? "" : userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserNickName() {
        return TextUtils.isEmpty(userNickName) ? userId : userNickName;
    }

    public void setUserNickName(String userNickName) {
        this.userNickName = userNickName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getUserHeader() {
        return userHeader;
    }

    public void setUserHeader(String userHeader) {
        this.userHeader = userHeader;
    }

    public String getUserSex() {
        return userSex;
    }

    public void setUserSex(String userSex) {
        this.userSex = userSex;
    }

    public String getUserRegion() {
        return userRegion;
    }

    public void setUserRegion(String userRegion) {
        this.userRegion = userRegion;
    }

    public String getUserSign() {
        return userSign;
    }

    public void setUserSign(String userSign) {
        this.userSign = userSign;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserIsEmailLinked() {
        return userIsEmailLinked;
    }

    public void setUserIsEmailLinked(String userIsEmailLinked) {
        this.userIsEmailLinked = userIsEmailLinked;
    }

    public String getIsFriend() {
        return isFriend;
    }

    public boolean isFriend() {
        return Constant.IS_FRIEND.equals(isFriend);
    }

    public void setIsFriend(String isFriend) {
        this.isFriend = isFriend;
    }

    public String getUserContactMobiles() {
        return userContactMobiles;
    }

    public void setUserContactMobiles(String userContactMobiles) {
        this.userContactMobiles = userContactMobiles;
    }

    public String getUserContactAlias() {
        return userContactAlias;
    }

    public void setUserContactAlias(String userContactAlias) {
        this.userContactAlias = userContactAlias;
    }

    public String getUserContactDesc() {
        return userContactDesc;
    }

    public void setUserContactDesc(String userContactDesc) {
        this.userContactDesc = userContactDesc;
    }

    public String getUserContactPrivacy() {
        return userContactPrivacy;
    }

    public void setUserContactPrivacy(String userContactPrivacy) {
        this.userContactPrivacy = userContactPrivacy;
    }

    public String getUserContactHideMyPosts() {
        return userContactHideMyPosts;
    }

    public void setUserContactHideMyPosts(String userContactHideMyPosts) {
        this.userContactHideMyPosts = userContactHideMyPosts;
    }

    public String getUserContactHideHisPosts() {
        return userContactHideHisPosts;
    }

    public void setUserContactHideHisPosts(String userContactHideHisPosts) {
        this.userContactHideHisPosts = userContactHideHisPosts;
    }

    public String getIsStarred() {
        return isStarred;
    }

    public void setIsStarred(String isStarred) {
        this.isStarred = isStarred;
    }

    public String getIsBlocked() {
        return isBlocked;
    }

    public void setIsBlocked(String isBlocked) {
        this.isBlocked = isBlocked;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        User other = (User) obj;
        if (!userId.equals(other.getUserId()) || !belongAccount.equals(other.getBelongAccount())) {
            Trace.d("equals: " + userId);
            return false;
        }
        Trace.d("equals: " + userId);
        return true;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public String getConversationTitle() {
        return getUserContactAlias() == null ? getUserNickName() : getUserContactAlias();
    }
}