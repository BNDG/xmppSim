package com.hjq.demo.chat.listener

import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bndg.smack.SmartCommHelper
import com.bndg.smack.callback.IFriendListener
import com.bndg.smack.model.SmartUserInfo
import com.blankj.utilcode.util.TimeUtils
import com.hjq.demo.chat.activity.NewFriendsActivity
import com.hjq.demo.chat.cons.Constant
import com.hjq.demo.chat.dao.DBManager
import com.hjq.demo.chat.dao.UserDao
import com.hjq.demo.chat.entity.FriendApply
import com.hjq.demo.chat.entity.User
import com.hjq.demo.chat.utils.AvatarGenerator
import com.hjq.demo.chat.utils.CommonUtil
import com.hjq.demo.chat.utils.PreferencesUtil
import com.hjq.demo.manager.ActivityManager

/**
 * @author r
 * @date 2024/8/26
 * @description Brief description of the file content.
 */
object FriendListener : IFriendListener {
    override fun onNewFriendRequest(userInfo: SmartUserInfo) {
        processAddFriendsApplyMessage(userInfo)
    }

    override fun onFriendAdded(userInfo: SmartUserInfo) {
        val intent = Intent()
        intent.setAction(Constant.FRIEND_ADDED)
        intent.putExtra(Constant.FRIEND_USER_INFO, userInfo)
        LocalBroadcastManager.getInstance(SmartCommHelper.getInstance().application)
            .sendBroadcast(intent)
    }

    override fun onFriendDeleted(userId: String?) {
        val intent = Intent()
        intent.setAction(Constant.FRIEND_DELETED)
        intent.putExtra(Constant.CONTACT_ID, userId)
        LocalBroadcastManager.getInstance(SmartCommHelper.getInstance().application)
            .sendBroadcast(intent)
    }

    /**
     * 好友在线状态
     */
    override fun receivedFriendStatus(userId: String?, isOnLine: Boolean) {
        val intent = Intent()
        intent.setAction(Constant.FRIEND_STATUS)
        intent.putExtra(Constant.CONTACT_ID, userId)
        intent.putExtra(Constant.ONLINE_STATUS, isOnLine)
        LocalBroadcastManager.getInstance(SmartCommHelper.getInstance().application)
            .sendBroadcast(intent)
    }


    /**
     * 处理好友申请消息
     *
     * @param context
     * @param extras
     */
    private fun processAddFriendsApplyMessage(userInfo: SmartUserInfo) {
        val targetFullUseId = userInfo.userId
        val nickname = userInfo.nickname
        var friendApply: FriendApply

        val subscribe = DBManager.getInstance(ActivityManager.getInstance().application)
            .getFriendApplyByFromUserId(targetFullUseId)
            ?.subscribe { friendApplyList ->
                friendApply = if (friendApplyList.isEmpty()) {
                    val newFriendApply = FriendApply()
                    newFriendApply.applyId = CommonUtil.generateId()
                    newFriendApply
                } else {
                    friendApplyList[0]
                }
                // 对方的id
                friendApply.status = Constant.FRIEND_APPLY_STATUS_NONE
                friendApply.friendUserId = targetFullUseId
                friendApply.friendNickname = nickname
                val myUserId = PreferencesUtil.getInstance().userId
                friendApply.belongAccount = myUserId
                friendApply.createTime = TimeUtils.getNowMills().toString()
                friendApply.applyRemark = userInfo.friendRequestIntro
                DBManager.getInstance(ActivityManager.getInstance().application)
                    .saveFriendApply(friendApply)
                    ?.subscribe();
                // 预保存一下对方信息 为了点击查看对方资料
                var userById = User()
                userById.belongAccount = PreferencesUtil.getInstance().userId
                userById.userId = targetFullUseId
                userById.userNickName = nickname
                UserDao.getInstance().saveOrUpdateContact(userById)
                userInfo.userAvatar?.let {
                    AvatarGenerator.saveAvatarFileByByte(
                        it,
                        userInfo,
                        false
                    )
                }
                if (ActivityManager.getInstance().resumedActivity is NewFriendsActivity) {
                    val msgIntent = Intent(
                        Constant.RECEIVED_FRIEND_APPLY
                    )
                    msgIntent.putExtra(Constant.FRIEND_APPLY_INTRO, userInfo.friendRequestIntro)
                    LocalBroadcastManager.getInstance(ActivityManager.getInstance().application)
                        .sendBroadcast(msgIntent)
                } else {
                    val msgIntent = Intent(
                        Constant.RECEIVED_FRIEND_APPLY
                    )
                    msgIntent.putExtra(Constant.FRIEND_APPLY_INTRO, userInfo.friendRequestIntro)
                    LocalBroadcastManager.getInstance(ActivityManager.getInstance().application)
                        .sendBroadcast(msgIntent)
                }
            }
    }
}