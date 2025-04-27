package com.hjq.demo.chat.manager

import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.bndg.smack.entity.SmartMessage
import com.bndg.smack.enums.SmartConversationType
import com.blankj.utilcode.util.StringUtils
import com.hjq.demo.R
import com.hjq.demo.chat.activity.ChatActivity
import com.hjq.demo.chat.activity.MainActivity
import com.hjq.demo.chat.cons.Constant
import com.hjq.demo.chat.receiver.AnswerCallReceiver
import com.hjq.demo.chat.receiver.DeclineCallReceiver
import com.hjq.demo.manager.ActivityManager
import com.hjq.demo.utils.Trace
/**
 * @author r
 * @date 2024/11/28
 * @description 通知发送帮助类
 */

object PushNotificationHelper {

    private val newRTCChannelID: String = "new_rtc_channel_id"
    private val newMsgChannelID: String = "new_msg_channel_id"

    /**
     * 通知渠道-聊天消息(重要性级别-高：发出声音)
     * VISIBILITY_PUBLIC 表示通知的内容（例如标题、正文）可以在锁屏上显示。这意味着即使设备处于锁定状态，通知的内容也会在锁屏界面上公开显示。
     */
    private val MESSAGE = NotificationCompatUtil.Channel(
        channelId = newMsgChannelID,
        name = ActivityManager.getInstance().getApplication()
            .getString(R.string.new_message_notification),
        importance = NotificationManager.IMPORTANCE_HIGH,
        lockScreenVisibility = NotificationCompat.VISIBILITY_PUBLIC,
        vibrate = longArrayOf(0, (3 * 70).toLong(), 70.toLong(), 70.toLong()),
        sound = Uri.parse(
            "android.resource://" + ActivityManager.getInstance()
                .getApplication().packageName + "/" + R.raw.newmsg
        )
    )

    /** 通知渠道-@提醒消息(重要性级别-紧急：发出提示音，并以浮动通知的形式显示 & 锁屏显示 & 振动0.25s )*/
    private val MENTION = NotificationCompatUtil.Channel(
        channelId = newMsgChannelID,
        name = ActivityManager.getInstance().getApplication()
            .getString(R.string.new_message_notification),
        importance = NotificationManager.IMPORTANCE_HIGH,
        lockScreenVisibility = NotificationCompat.VISIBILITY_PUBLIC,
        vibrate = longArrayOf(0, (3 * 70).toLong(), 70.toLong(), 70.toLong()),
        sound = Uri.parse(
            "android.resource://" + ActivityManager.getInstance()
                .getApplication().packageName + "/" + R.raw.newmsg
        )
    )

    /** 通知渠道-系统通知(重要性级别-中：无提示音) */
    private val NOTICE = NotificationCompatUtil.Channel(
        channelId = newMsgChannelID,
        name = ActivityManager.getInstance().getApplication()
            .getString(R.string.new_message_notification),
        importance = NotificationManager.IMPORTANCE_LOW
    )

    /** 通知渠道-音视频通话(重要性级别-紧急：发出提示音，并以浮动通知的形式显示 & 锁屏显示 & 振动4s停2s再振动4s ) */
    private val CALL = NotificationCompatUtil.Channel(
        channelId = newRTCChannelID,
        name = ActivityManager.getInstance().getApplication().getString(R.string.call_invitation),
        importance = NotificationManager.IMPORTANCE_HIGH,
        lockScreenVisibility = NotificationCompat.VISIBILITY_PUBLIC,
        vibrate = longArrayOf(0, 4000, 2000, 4000),
        sound = Uri.parse(
            "android.resource://" + ActivityManager.getInstance()
                .getApplication().packageName + "/" + R.raw.newrtc
        )
    )
    /** 通知渠道-音视频通话(重要性级别-紧急：发出提示音，并以浮动通知的形式显示 & 锁屏显示 & 振动4s停2s再振动4s ) */
    private val FOREGROUND = NotificationCompatUtil.Channel(
            channelId = "chat_foreground",
            name = ActivityManager.getInstance().getApplication().getString(R.string.connection_status),
            importance = NotificationManager.IMPORTANCE_DEFAULT,
        )

    /**
     * 显示聊天消息
     * @param context 上下文
     * @param id      通知的唯一ID
     * @param title   标题
     * @param text    正文文本
     */
    fun notifyMessage(
        id: Int,
        isSingle: Boolean,
        title: String?,
        text: String?,
        msgEntity: SmartMessage
    ) {
        Trace.d("notifyMessage: start")
        val chatIntent = Intent(ActivityManager.getInstance().application, ChatActivity::class.java)
        chatIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        chatIntent.putExtra(Constant.RECEIVE_NEW_MSG, msgEntity)
        if (isSingle) {
            chatIntent.putExtra(Constant.CONVERSATION_TYPE, SmartConversationType.SINGLE.name)
            chatIntent.putExtra(Constant.CONVERSATION_ID, msgEntity.getFromUserId())
        } else {
            chatIntent.putExtra(Constant.CONVERSATION_TYPE, SmartConversationType.GROUP.name)
            chatIntent.putExtra(Constant.CONVERSATION_ID, msgEntity.getGroupId())
        }
        chatIntent.putExtra(Constant.CONVERSATION_TITLE, title)
        val builder = NotificationCompatUtil.createNotificationBuilder(
            ActivityManager.getInstance().application,
            id,
            MESSAGE,
            title,
            text,
            chatIntent
        )

        // 默认情况下，通知的文字内容会被截断以放在一行。如果您想要更长的通知，可以使用 setStyle() 添加样式模板来启用可展开的通知。
        /*builder.setStyle(
            NotificationCompat.BigTextStyle()
                .bigText(text)
        )*/
        NotificationCompatUtil.notify(
            ActivityManager.getInstance().application,
            id,
            buildDefaultConfig(builder)
        );

        Trace.d("notifyMessage: end")
    }

    /**
     * 显示@提醒消息
     * @param context 上下文
     * @param id      通知的唯一ID
     * @param title   标题
     * @param text    正文文本
     */
    fun notifyMention(
        context: Context,
        id: Int,
        title: String?,
        text: String?
    ) {
        val intent = Intent(context, ChatActivity::class.java)

        val builder = NotificationCompatUtil.createNotificationBuilder(
            context,
            id,
            MENTION,
            title,
            text,
            intent
        )

        // 默认情况下，通知的文字内容会被截断以放在一行。如果您想要更长的通知，可以使用 setStyle() 添加样式模板来启用可展开的通知。
        builder.setStyle(
            NotificationCompat.BigTextStyle()
                .bigText(text)
        )

        NotificationCompatUtil.notify(context, id, buildDefaultConfig(builder));
    }

    /**
     * 显示系统通知
     * @param context 上下文
     * @param id      通知的唯一ID
     * @param title   标题
     * @param text    正文文本
     */
    fun notifyNotice(
        context: Context,
        id: Int,
        title: String?,
        text: String?
    ) {
        val intent = Intent(context, MainActivity::class.java)
        val builder = NotificationCompatUtil.createNotificationBuilder(
            context,
            id,
            NOTICE,
            title,
            text,
            intent
        )

        NotificationCompatUtil.notify(context, id, buildDefaultConfig(builder));
    }

    /**
     * 显示音视频通话
     * @param context 上下文
     * @param id      通知的唯一ID
     * @param title   标题
     * @param text    正文文本
     */
    fun notifyCall(
        id: Int,
        title: String?,
        text: String?,
        isSingle: Boolean,
        conversationId: String,
        callType: String,
        messageType: String,
        callId: String,
        callCreatorInfo: String?
    ) {
    }

    /**
     * 构建应用通知的默认配置
     * @param builder 构建器
     */
    private fun buildDefaultConfig(builder: NotificationCompat.Builder): Notification {
        builder.setSmallIcon(R.mipmap.ic_launcher)
        return builder.build()
    }

    fun createNotificationChannel(application: Application) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompatUtil.createChannel(application, CALL)
            NotificationCompatUtil.createChannel(application, MESSAGE)
            NotificationCompatUtil.createChannel(application, FOREGROUND)
        }
    }
}