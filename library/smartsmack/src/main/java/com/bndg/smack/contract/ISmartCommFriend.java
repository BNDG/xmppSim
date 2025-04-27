package com.bndg.smack.contract;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterEntry;

import com.bndg.smack.callback.IFriendListener;
import com.bndg.smack.callback.IFriendListCallback;
import com.bndg.smack.callback.ISmartCallback;
import com.bndg.smack.callback.IUserInfoCallBack;
import com.bndg.smack.model.SmartUserInfo;

public interface ISmartCommFriend {
    /**
     *
     * @param userId  对方id
     * @param userNickname 对方昵称
     * @param friendRequestIntro 添加好友申请说明
     * @param iAddFriendCallback
     */
    void addFriend(String userId,String friendRequestIntro, String userNickname, IFriendListener iAddFriendCallback);

    /**
     * 添加好友 有分组
     *
     * @param userName  userName
     * @param name      name
     * @param groupName groupName
     * @return boolean
     */
    boolean addFriend(String userName, String name, String groupName);

    /**
     * 删除好友
     *
     * @param userJid userJid
     * @return boolean
     */
    void deleteFriend(String userJid, ISmartCallback smartCallback);

    /**
     * 查询用户
     *
     * @param userName userName
     * @return List<HashMap < String, String>>
     */
    void searchFriends(String userName, IUserInfoCallBack callBack);

    /**
     *
     * @param targetId  对方id
     * @param targetNickname 对方昵称
     * @param smartCallback
     */
    void acceptFriendReq(String targetId, String targetNickname, ISmartCallback smartCallback);

    void rejectPresence(String to, ISmartCallback smartCallback);

    /**
     * 获取好友列表
     */
    void getFriendList(IFriendListCallback friendListCallback);

    void addFriendListener(IFriendListener instance);
    void removeFriendListener(IFriendListener instance);

    void receivedFriendRequest(SmartUserInfo info);

    void receivedFriendAdded(SmartUserInfo info);

    void receivedFriendDeleted(String userId);

    void release();

    void blockContact(String jid, ISmartCallback callback);

    void unblockContacts(String jid, ISmartCallback iSmartCallback);

    boolean checkIsFriend(String toContactJid);

    void receivedFriendPresent(Presence presence, RosterEntry entry);

    boolean isOnline(String userId);
}

