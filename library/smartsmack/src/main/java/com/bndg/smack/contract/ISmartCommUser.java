package com.bndg.smack.contract;

import com.bndg.smack.callback.IFriendListCallback;
import com.bndg.smack.callback.ILoginCallback;
import com.bndg.smack.callback.ISmartCallback;
import com.bndg.smack.callback.IUserInfoCallBack;
import com.bndg.smack.callback.IUserInfoCallback2;
import com.bndg.smack.model.SmartUserInfo;

import org.jivesoftware.smack.packet.Presence;
import org.jxmpp.jid.BareJid;

import java.io.File;

public interface ISmartCommUser {
    void initData();

    /**
     * 登录
     *
     * @param userName 用户名
     * @param passWord 密码
     * @return
     */
    void login(String userName, String passWord, ILoginCallback iLoginCallback);

    /**
     * 创建用户
     *
     * @param userName 用户名
     * @param passWord 密码
     */
    void createAccount(String userName, String passWord, String nickName, ISmartCallback smartCallback);

    /**
     * 修改用户头像
     *
     * @param file file
     */
    void changeImage(File file, IUserInfoCallback2 userVCardCallback);

    /**
     * 删除当前用户
     *
     * @return true成功
     */
    boolean deleteAccount();

    /**
     * 修改密码
     *
     * @return true成功
     */
    boolean changePassword(String pwd);

    /**
     * 判断OpenFire用户的状态 strUrl :
     * url格式 - http://my.openfire.com:9090/plugins/presence
     * /status?jid=user1@SERVER_NAME&type=xml
     * 返回值 : 0 - 用户不存在; 1 - 用户在线; 2 - 用户离线
     * 说明 ：必须要求 OpenFire加载 presence 插件，同时设置任何人都可以访问
     */
    int IsUserOnLine(String user);

    /**
     * 设置在线、离线等状态
     *
     * @param type
     */
    void setOnLine(Presence.Type type);

    /**
     * 更改用户状态
     */
    void setPresence(int code);


    /**
     * 获取用户VCard信息  非常耗时?
     *
     * @param user user
     * @return VCard
     */
    <T> T getUserInfo(String user);

    void getUserInfo(String entityBareJid, IUserInfoCallback2 userVCardCallback);


    /**
     * 获取电子名片的昵称 如无返回空串
     *
     * @param jid
     * @return
     */
    void getUserNickname(String jid, IUserInfoCallBack iUserInfoCallBack);

    void getUserAvatar(BareJid jid, IUserInfoCallback2 userVCardCallback);

    void getUserIsOnLine(String userJid, IUserInfoCallBack userInfoCallBack);

    /**
     * @param callback
     */
    void setMyUserInfo(SmartUserInfo smartUserInfo, IUserInfoCallback2 callback);

    /**
     * 请求群头像
     *
     * @param jidString
     */
    void requestAvatarByUserId(String jidString);

    void release();

    String getUserNicknameSync(String jid, boolean needQueryCard);

    void getBlockList(IFriendListCallback listener);

    void checkTrustDevice(String conversationId, ISmartCallback callback);
}
