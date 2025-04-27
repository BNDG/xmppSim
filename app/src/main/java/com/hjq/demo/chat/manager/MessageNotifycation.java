package com.hjq.demo.chat.manager;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.blankj.utilcode.util.StringUtils;
import com.hjq.demo.R;
import com.hjq.demo.chat.activity.ChatActivity;
import com.hjq.demo.chat.activity.MainActivity;
import com.hjq.demo.chat.activity.SplashActivity;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.DBManager;
import com.hjq.demo.chat.entity.ChatMessage;
import com.hjq.demo.chat.entity.ConversationInfo;
import com.hjq.demo.chat.receiver.AnswerCallReceiver;
import com.hjq.demo.chat.receiver.DeclineCallReceiver;
import com.hjq.demo.chat.utils.JimUtil;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.utils.Trace;
import com.tencent.mmkv.MMKV;

import java.util.ArrayList;

import com.bndg.smack.entity.SmartMessage;
import com.bndg.smack.enums.SmartConversationType;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * @author r
 * @date 2024/6/7
 * @description 消息通知管理
 */
public class MessageNotifycation {
    private static volatile MessageNotifycation instance;

    // 前台通知的id
    public static final int NOTIFICATION_ID = 100;

    private MessageNotifycation() {
    }

    public static MessageNotifycation getInstance() {
        if (instance == null) {
            synchronized (MessageNotifycation.class) {
                if (instance == null) {
                    instance = new MessageNotifycation();
                }
            }
        }
        return instance;
    }

    /**
     * 构建通知
     *
     * @param channelId
     * @param chatIntent
     * @param msgEntity
     * @param notificationManager
     * @param callId
     */
    public void buildNotifycation(
            String channelId,
            Intent chatIntent,
            SmartMessage msgEntity,
            NotificationManager notificationManager,
            String callId,
            String notifyTitle) {
        chatIntent.putExtra(Constant.CONVERSATION_TITLE, notifyTitle);
        String groupId = msgEntity.getGroupId();
        int id = msgEntity.getFromUserId().hashCode();
        if (!msgEntity.isSingle()) {
            id = groupId.hashCode();
            Trace.d("buildNotifycation: groupId " + groupId,
                    "title " + notifyTitle,
                    "id > " + id,
                    "intent " + chatIntent.getStringExtra(Constant.GROUP_ID));
        }
        if (!TextUtils.isEmpty(callId)) {
            id = callId.hashCode();
            Trace.d("buildNotifycation: groupId " + groupId,
                    "title " + notifyTitle,
                    "id call " + id,
                    "intent " + chatIntent.getStringExtra(Constant.GROUP_ID));
        }
        String fromNickname = msgEntity.getFromNickname();
        if (msgEntity.isSingle()) {
        } else {
            fromNickname = msgEntity.getGroupSenderNickname();
        }
        PendingIntent pendingIntent = null;
        Context context = ActivityManager.getInstance().getApplication();
        if (ActivityManager.getInstance().containsActivity(MainActivity.class)) {
            pendingIntent = PendingIntent.getActivity(
                    context,
                    id,
                    chatIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
        } else {
            // 设置返回栈，确保MainActivity -> ChatActivity的顺序
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            // 添加MainActivity作为底部的Activity
//        stackBuilder.addNextIntentWithParentStack(chatIntent);
            // 主Activity的Intent，用于构建返回栈
            Intent splashIntent = new Intent(context, SplashActivity.class);
            Intent mainIntent = new Intent(context, MainActivity.class);
            stackBuilder.addNextIntentWithParentStack(mainIntent);
            stackBuilder.addNextIntent(chatIntent);

            int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pendingIntentFlags |= PendingIntent.FLAG_IMMUTABLE;
            }
            pendingIntent = stackBuilder.getPendingIntent(0, pendingIntentFlags);
        }
        CharSequence content = JimUtil.getMessageContent(msgEntity.getMessageType(), msgEntity.getMessageContent());

        if (msgEntity.isSingle()) {
        } else {
            content = fromNickname + ": " + JimUtil.getMessageContent(msgEntity.getMessageType(), msgEntity.getMessageContent());
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context,
                channelId)
                // 设置通知时间
                .setWhen(System.currentTimeMillis())
                // 设置通知标题
                .setContentTitle(notifyTitle)
                .setContentText(content)
                // 设置通知小图标
                .setSmallIcon(R.mipmap.ic_launcher)
                // 设置通知大图标
                .setLargeIcon(
                        BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent)
                // 设置通知的优先级
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        if (ChatMessage.isStartCallType(msgEntity.getMessageType())) {
            Trace.d("buildNotifycation: 设置通知类别为电话通知");
            notificationBuilder.setCategory(NotificationCompat.CATEGORY_CALL) // 设置通知类别为电话通知
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)  // 设置通知可见性
                    .setAutoCancel(true);  // 点击通知后自动清除
            // 设置震动
            long[] vibrationPattern = {0, 1000, 500, 1000};  // 设置震动模式（等待0ms，震动1s，静止0.5s，震动1s）
            notificationBuilder.setVibrate(vibrationPattern);
        }
        Notification build = notificationBuilder.build();
        notificationManager.notify(id, build);
    }

    /**
     * 聊天消息通知
     *
     * @param msgEntity
     */
    public void createMsgNotify(SmartMessage msgEntity) {
        if (TextUtils.isEmpty(msgEntity.getMessageContent())) {
            return;
        }
        String conversationId = msgEntity.isSingle() ? msgEntity.getFromUserId() : msgEntity.getGroupId();
        boolean isMuted = MMKV.defaultMMKV().getBoolean(conversationId + Constant.MUTE_KEY, false);
        if (isMuted) {
            Trace.d("createMsgNotify: 开启了免打扰");
            return;
        }

        boolean isSingle = SmartConversationType.SINGLE.name().equals(msgEntity.getConversationType());
        NotificationManager notificationManager = ActivityManager.getInstance().getApplication()
                .getSystemService(
                        NotificationManager.class);
        String channelId;
        // 适配 Android 8.0 通知渠道新特性
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("chat_channel", StringUtils.getString(R.string.new_message_notification),
                    NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(false);
            // 开启振动
            channel.enableVibration(true);
            // 设置振动频率
            channel.setVibrationPattern(new long[]{0, 100, 50, 100, 50, 100});
            notificationManager.createNotificationChannel(channel);
            channelId = channel.getId();
        } else {
            channelId = "";
        }
        Context packageContext = ActivityManager.getInstance().getApplication();
        Intent chatIntent = new Intent(packageContext, ChatActivity.class); //创建Intent对象
        chatIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        chatIntent.putExtra(Constant.RECEIVE_NEW_MSG, msgEntity);
        if (isSingle) {
            chatIntent.putExtra(Constant.CONVERSATION_TYPE, SmartConversationType.SINGLE.name());
            chatIntent.putExtra(Constant.CONVERSATION_ID, msgEntity.getFromUserId());
        } else {
            chatIntent.putExtra(Constant.CONVERSATION_TYPE, SmartConversationType.GROUP.name());
            chatIntent.putExtra(Constant.CONVERSATION_ID, msgEntity.getGroupId());
        }
        if (!msgEntity.isSingle()) {
            // 查询群聊标题
            Disposable subscribe = DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                    .getConversationByConversationId(PreferencesUtil.getInstance().getUserId(),
                            msgEntity.getGroupId())
                    .subscribe(conversationInfoList -> {
                        ConversationInfo conversationInfo;
                        if (conversationInfoList.isEmpty()) {
                            conversationInfo = null;
                        } else {
                            conversationInfo = conversationInfoList.get(0);
                        }
                        String conversationTitle = "";
                        if (conversationInfo != null) {
                            conversationTitle = conversationInfo.getConversationTitle();
                        }
                        conversationTitle = TextUtils.isEmpty(conversationTitle) ? ActivityManager.getInstance().getApplication()
                                .getString(R.string.group_chats) : conversationTitle;
                        buildNotifycation(channelId, chatIntent, msgEntity,
                                notificationManager, "", conversationTitle);
                    });

        } else {
            buildNotifycation(channelId, chatIntent, msgEntity,
                    notificationManager, "", msgEntity.getFromNickname());
        }
    }

    /**
     * 开启前台服务
     *
     * @param context
     * @param content
     */
    public void createForeground(Context context, String content) {
        /*NotificationManager notificationManager = context.getSystemService(
                NotificationManager.class);
        String channelId = "";
        // 适配 Android 8.0 通知渠道新特性
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("xmpp_service", "连接状态",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(false);
            // 开启振动
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});
            channel.setSound(null, null);
            notificationManager.createNotificationChannel(channel);
            channelId = channel.getId();
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context,
                channelId)
                // 设置通知时间
                .setWhen(System.currentTimeMillis())
                // 设置通知标题
                .setContentTitle(context.getString(R.string.app_name) + "正在节能运行").setContentText(content)
                // 设置通知小图标
                .setSmallIcon(R.mipmap.ic_launcher)
                // 设置通知大图标
                .setLargeIcon(
                        BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                // 设置通知静音
                .setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
                // 设置通知的优先级
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Notification build = notificationBuilder.build();
        context.startForeground(NOTIFICATION_ID, build);*/
    }

    /**
     * 开启前台服务
     *
     * @param context
     * @param content
     */
    public void updateForeground(Context context, String content) {
        NotificationManager notificationManager = context.getSystemService(
                NotificationManager.class);
        String channelId = "";
        // 适配 Android 8.0 通知渠道新特性
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("xmpp_service", StringUtils.getString(R.string.connection_status),
                    NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(false);
            // 开启振动
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});
            channel.setSound(null, null);
            notificationManager.createNotificationChannel(channel);
            channelId = channel.getId();
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context,
                channelId)
                // 设置通知时间
                .setWhen(System.currentTimeMillis())
                // 设置通知标题
                .setContentTitle(context.getString(R.string.app_name) + "正在节能运行").setContentText(content)
                // 设置通知小图标
                .setSmallIcon(R.mipmap.ic_launcher)
                // 设置通知大图标
                .setLargeIcon(
                        BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                // 设置通知静音
                .setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
                // 设置通知的优先级
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Notification build = notificationBuilder.build();
        notificationManager.notify(NOTIFICATION_ID, build);
    }

    public void createCallNotify(SmartMessage msgEntity, String callId, String callType, String callCreatorInfo) {
    }

    public void cancelCallNotify(int callNotifyId) {
        NotificationManager notificationManager = ActivityManager.getInstance().getApplication().getSystemService(
                NotificationManager.class);
        notificationManager.cancel(callNotifyId);
    }

    public void cancelAll() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ActivityManager.getInstance().getApplication());
        notificationManager.cancelAll();
    }


    public void buildCallNotifycation(
            String channelId,
            Intent chatIntent,
            SmartMessage msgEntity,
            NotificationManager notificationManager,
            String callId,
            String notifyTitle) {
        chatIntent.putExtra(Constant.CONVERSATION_TITLE, notifyTitle);
        String fromNickname = msgEntity.getFromNickname();
        if (msgEntity.isSingle()) {
        } else {
            fromNickname = msgEntity.getGroupSenderNickname();
        }
        PendingIntent pendingIntent = null;
        Context context = ActivityManager.getInstance().getApplication();
        if (ActivityManager.getInstance().containsActivity(MainActivity.class)) {
            pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    chatIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
        } else {
            // 设置返回栈，确保MainActivity -> ChatActivity的顺序
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            // 添加MainActivity作为底部的Activity
//        stackBuilder.addNextIntentWithParentStack(chatIntent);
            // 主Activity的Intent，用于构建返回栈
            Intent splashIntent = new Intent(context, SplashActivity.class);
            Intent mainIntent = new Intent(context, MainActivity.class);
            stackBuilder.addNextIntentWithParentStack(mainIntent);
            stackBuilder.addNextIntent(chatIntent);

            int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pendingIntentFlags |= PendingIntent.FLAG_IMMUTABLE;
            }
            pendingIntent = stackBuilder.getPendingIntent(0, pendingIntentFlags);
        }
        CharSequence content = JimUtil.getMessageContent(msgEntity.getMessageType(), msgEntity.getMessageContent());
        String groupId = msgEntity.getGroupId();
        if (msgEntity.isSingle()) {
        } else {
            content = fromNickname + ": " + JimUtil.getMessageContent(msgEntity.getMessageType(), msgEntity.getMessageContent());
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context,
                channelId)
                // 设置通知时间
                .setWhen(System.currentTimeMillis())
                // 设置通知标题
                .setContentTitle(notifyTitle)
                .setContentText(content)
                // 设置通知小图标
                .setSmallIcon(R.mipmap.ic_launcher)
                // 设置通知大图标
                .setLargeIcon(
                        BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent)
                // 设置通知的优先级
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        if (ChatMessage.isStartCallType(msgEntity.getMessageType())) {
            Trace.d("buildNotifycation: 设置通知类别为电话通知");
            notificationBuilder.setCategory(NotificationCompat.CATEGORY_CALL) // 设置通知类别为电话通知
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)  // 设置通知可见性
                    .setAutoCancel(true);  // 点击通知后自动清除
            // 设置震动
            long[] vibrationPattern = {0, 1000, 500, 1000};  // 设置震动模式（等待0ms，震动1s，静止0.5s，震动1s）
            notificationBuilder.setVibrate(vibrationPattern);
        }

        // Load the custom layout.
        String packageName = ActivityManager.getInstance().getApplication().getPackageName();
        RemoteViews views = new RemoteViews(packageName, R.layout.incoming_call_notify);
        views.setTextViewText(R.id.caller_name, notifyTitle);
        views.setTextViewText(R.id.call_type, "来电");
// Set the custom content view for the notification.
        notificationBuilder.setContent(views);
// Define the actions (buttons) for the notification.
        Bundle extras = chatIntent.getExtras();
        if (null != extras) {
            Intent answerIntent = new Intent(context, AnswerCallReceiver.class);
            answerIntent.putExtras(chatIntent.getExtras());
            PendingIntent answerPendingIntent = PendingIntent.getBroadcast(context, 0, answerIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            Intent declineIntent = new Intent(context, DeclineCallReceiver.class);
            declineIntent.putExtras(chatIntent.getExtras());
            PendingIntent declinePendingIntent = PendingIntent.getBroadcast(context, 1, declineIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.answer_button, answerPendingIntent);
            views.setOnClickPendingIntent(R.id.decline_button, declinePendingIntent);
            Notification build = notificationBuilder.build();
            int id = msgEntity.getFromUserId().hashCode();
            if (!TextUtils.isEmpty(callId)) {
                id = callId.hashCode();
            }
            notificationManager.notify(id, build);
        }
    }
}
