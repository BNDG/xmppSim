package com.bndg.smack.contract;

import org.jivesoftware.smackx.muc.MultiUserChat;

import java.io.File;
import java.util.List;

import com.bndg.smack.callback.IBookmarkedConferenceCallback;
import com.bndg.smack.callback.IChatRoomCallback;
import com.bndg.smack.callback.IGroupMemberCallback;
import com.bndg.smack.callback.ISmartCallback;
import com.bndg.smack.callback.IUserInfoCallBack;
import com.bndg.smack.callback.IUserInfoCallback2;
import com.bndg.smack.entity.FetchEntity;
import com.bndg.smack.model.SmartUserInfo;
import com.bndg.smack.muc.RoomChat;

public interface ISmartCommChatRoom {
    /**
     * 创建房间
     *
     * @param roomName     群名称
     * @param roomCallback 群事件回调
     */
    void createRoom(String roomName, IChatRoomCallback roomCallback);

    /**
     * 加入会议室
     *
     * @param groupId 会议室名
     */
    void realJoinRoom(String groupId, IChatRoomCallback roomCallback);

    /**
     * 加入会议室
     *
     * @param groupId 会议室名
     */
    void realJoinRoomWithPWD(String groupId,String pwd, IChatRoomCallback roomCallback);

    /**
     * 获取聊天室对象
     *
     * @param groupId
     * @return
     */
    MultiUserChat getMucIns(String groupId);

    boolean isJoined(String groupId);

    RoomChat getRoomChat(String groupId);

    /**
     * 获取我的岗位
     */
    void getMyAffiliation(MultiUserChat muc, IUserInfoCallBack callBack);

    /**
     * 解散聊天室
     *
     * @param roomId
     * @param roomCallback
     */
    void destroyRoom(String roomId, IChatRoomCallback roomCallback);

    /**
     * 邀请用户
     *
     * @param pickedUserIdList 被邀请的成员Userid
     * @param groupId          群id
     * @param reason           邀请原因
     */
    void inviteUsers(List<String> pickedUserIdList, String groupId, String reason);

    void inviteUsers2(List<SmartUserInfo> pickedUserIdList, String groupId, String reason);

    /**
     * 退出群聊
     *
     * @param groupId
     * @param iSmartCallback
     */
    void leaveRoom(String groupId, IChatRoomCallback iSmartCallback);

    /**
     * 获取书签保存的聊天室
     */
    void getRoomsByBookmark(IBookmarkedConferenceCallback bookmarkedConferenceCallback);

    /**
     * 获取聊天室成员
     *
     * @param groupId
     * @param iGroupMemberCallback
     */
    void getGroupMemberList(String groupId, IGroupMemberCallback iGroupMemberCallback);

    void fetchHistory(FetchEntity recordMsg);

    void getRoomInfo(String groupId, IChatRoomCallback callback);

    void changeRoomName(String roomJid, String roomName, IChatRoomCallback callback);

    void kickGroupMember(List<SmartUserInfo> userInfos, String groupId, IChatRoomCallback callback);

    /**
     * 邀请用户
     *
     * @param groupId           群实例对象
     * @param pickedUserIdList  被邀请的成员列表
     * @param iChatRoomCallback 群事件回调
     */
    void inviteUserToGroup(String groupId, List<String> pickedUserIdList, IChatRoomCallback iChatRoomCallback);

    void inviteUserToGroup2(String groupId, List<SmartUserInfo> pickedUserIdList, String reason, IChatRoomCallback iChatRoomCallback);

    /**
     * @param roomId
     * @param memberAccount
     * @param reason
     */
    void processMemberKicked(String roomId, String memberAccount, String reason);


    void release();

    /**
     * 重新加入
     *
     * @param string
     * @param iChatRoomCallback
     */
    void rejoinRoom(String string, IChatRoomCallback iChatRoomCallback);


    /**
     * 群名称更新
     *
     * @param groupId
     */
    void roomNameUpdated(String groupId);

    void putRecordMsg(String groupId, FetchEntity recordMsg);

    FetchEntity getFetchEntity(String groupId);

    void checkMucAndForceLeave();

    void changeMyNickname(String content, String groupId, IChatRoomCallback callback);

    void getGroupAdministrators(String groupId, IChatRoomCallback callback);

    void grantAdmin(List<String> realJids, String groupId, IChatRoomCallback callback);

    void revokeAdmin( String memberRealUserId,String groupId,  IChatRoomCallback iChatRoomCallback);

    void checkGroupStatus(String conversationId);

    void changeGroupAvatar(String groupId, File file, IChatRoomCallback iChatRoomCallback);

    void muteMember(boolean muted, String conversationId, String fromUserId, IChatRoomCallback iChatRoomCallback);
}
