package com.bndg.smack.callback;


import java.util.List;

import com.bndg.smack.model.SmartGroupInfo;
import com.bndg.smack.model.SmartUserInfo;

/**
 * @author r
 * @date 2024/6/19
 * @description 群组事件回调 创建 销毁 加入 退出 邀请 解散 成员变更
 */
public interface IChatRoomCallback {
    default void joinRoomSuccess(String groupId) {
    }

    default void joinRoomFailed(int code, String groupId, String desc) {
    }

    default void deleteRoomSuccess() {
    }

    default void deleteRoomFailed(int code, String desc) {
    }

    default void createSuccess(String groupId) {
    }

    default void createFailed(int code, String desc) {
    }

    default void memberJoined(String room, SmartUserInfo userInfo) {
    }

    default void memberOffline(String room, String memberAccount) {
    }

    default void memberKicked(String room, String nickname, String reason, boolean isSelf) {
    }

    default void leaveSuccess() {
    }

    default void leaveFailed(int code, String desc) {
    }

    default void invitationReceived(SmartGroupInfo roomInfo, String string, String reason,
                                    String password, List<SmartUserInfo> lists) {
    }

    default void getGroupInfo(SmartGroupInfo roomInfo) {
    }

    default void changeNameSuccess() {
    }

    default void changeNameFailed() {
    }

    default void kickedSuccess() {
    }

    default void kickedFailed() {
    }

    default void notifyJoinRoom(SmartGroupInfo roomInfo) {
    }

    default void getGroupInfoFailed() {
    }

    default void updateGroupAvatar(SmartUserInfo userInfo, String groupId) {
    }

    default void receivedMemberInfo(SmartUserInfo userInfo) {
    }

    /**
     * 收到群成员头像的hash
     *
     * @param userId
     * @param photoHash
     */
    default void receivedMemberAvatarHash(String userId, String photoHash) {
    }

    default void updateMemberInfo(String groupId, SmartUserInfo smartUserInfo) {
    }

    default void groupNameUpdated(String groupId, SmartGroupInfo roomInfo) {
    }

    default void groupDestroyed(String groupId, String reason) {
    }

    default void memberLeave(String groupId, String jidAccount) {
    }

    default void changeNicknameInGroupFailed(int code, String desc) {
    }

    default void changeNicknameInGroupSuccess() {
    }

    @Deprecated
    default void memberAccountChanged(String groupId, String oldAccount, String newAccount, String userName) {
    }

    default void getAdmins(List<SmartUserInfo> smartUserInfos) {
    }

    default void grantAdminSuccess() {
    }

    default void revokeAdminSuccess() {
    }

    default void startJoinGroup(String groupId){}

    default void memberBanned(String groupId, String memberAccount, String reason){}

    default void voiceGranted(String groupId, String memberAccount){}
    default void voiceRevoked(String groupId, String memberAccount){}

    default void changeGroupAvatarSuccess(){}

    default void changeGroupAvatarFailed(){}

    default void muteMemberSuccess(){}
}
