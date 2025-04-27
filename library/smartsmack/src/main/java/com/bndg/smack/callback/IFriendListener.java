package com.bndg.smack.callback;

import java.util.List;

import com.bndg.smack.model.SmartUserInfo;


/**
 * @author r
 * @date 2024/8/26
 * @description 好友监听 新申请 新增 删除 上下线通知
 */
public interface IFriendListener {
    /**
     * 好友申请新增通知，两种情况会收到这个回调：
     * 自己申请加别人好友
     * 别人申请加自己好友
     */
    default void onNewFriendRequest(SmartUserInfo smartFriendInfo) {
    }

    default void onSendRequestSuccess(){}

    default void onSendRequestFailed(){}

    /**
     * 好友申请删除通知，四种情况会收到这个回调
     * <p>
     * 调用 deleteFriendApplication 主动删除好友申请
     * 调用 refuseFriendApplication 拒绝好友申请
     * 调用 acceptFriendApplication 同意好友申请且同意类型为 V2TIM_FRIEND_ACCEPT_AGREE 时
     * 申请加别人好友被拒绝
     *
     * @param userIDList
     */
    default void onFriendApplyListDeleted(List<String> userIDList) {
    }

    /**
     * 好友申请已读通知，如果调用 setFriendApplicationRead 设置好友申请列表已读，会收到这个回调（主要用于多端同步）
     */
    default void onFriendApplyListRead() {
    }

    /**
     * 好友新增通知
     *
     * @param users
     */
    default void onFriendAdded(SmartUserInfo users) {
    }

    /**
     * 好友删除通知，，两种情况会收到这个回调：
     * <p>
     * 自己删除好友（单向和双向删除都会收到回调）
     * 好友把自己删除（双向删除会收到）
     */
    default void onFriendDeleted(String userId) {
    }

    /**
     * 黑名单新增通知
     *
     * @param infoList
     */
    default void onBlackListAdd(List<SmartUserInfo> infoList) {
    }

    /**
     * 黑名单删除通知
     *
     * @param userList
     */
    default void onBlackListDeleted(List<String> userList) {
    }

    /**
     * 好友资料更新通知
     *
     * @param infoList
     */
    default void onFriendInfoChanged(List<SmartUserInfo> infoList) {
    }


    default void receivedFriendStatus(String userId, boolean isOnLine){}
}
