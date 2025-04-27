package com.hjq.demo.chat.manager

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Vibrator
import android.provider.Settings
import android.text.TextUtils
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.blankj.utilcode.util.SPUtils
import com.hjq.demo.chat.activity.MainActivity
import com.hjq.demo.chat.activity.SplashActivity
import com.hjq.demo.chat.cons.Constant
import com.hjq.demo.manager.ActivityManager

/**
 * 通知兼容工具类
 *
 * 本类中的代码使用Android支持库中的NotificationCompatAPI。
 * 这些API允许您添加仅在较新版本Android上可用的功能，同时仍向后兼容Android4.0（API级别14）。
 * 但是，诸如内嵌回复操作等部分新功能在较旧版本上会导致发生空操作。
 */
class NotificationCompatUtil {

    companion object {

        /**
         * 创建通知
         * @param context           上下文
         * @param channel           通知渠道
         * @param title             标题
         * @param text              正文文本
         * @param intent            对点按操作做出响应意图
         * @return
         */
        fun createNotificationBuilder(
            context: Context,
            id:Int,
            channel: Channel,
            title: CharSequence? = null,
            text: CharSequence? = null,
            chatIntent: Intent? = null
        ): NotificationCompat.Builder {
            // 必须先创建通知渠道，然后才能在Android 8.0及更高版本上发布任何通知
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createChannel(context, channel)
            }
            val builder =
                NotificationCompat.Builder(context, channel.channelId)
                    .setPriority(getLowVersionPriority(channel)) // 通知优先级，优先级确定通知在Android7.1和更低版本上的干扰程度。
                    .setVisibility(channel.lockScreenVisibility) // 锁定屏幕公开范围
                    .setVibrate(channel.vibrate) // 震动模式
                    .setSound(channel.sound ?: Settings.System.DEFAULT_NOTIFICATION_URI)    // 声音
                    .setCategory(Notification.CATEGORY_MESSAGE)
                    .setOnlyAlertOnce(false) // 设置通知只会在通知首次出现时打断用户（通过声音、振动或视觉提示），而之后更新则不会再打断用户。

            // 标题，此为可选内容
            if (!TextUtils.isEmpty(title)) builder.setContentTitle(title)

            // 正文文本，此为可选内容
            if (!TextUtils.isEmpty(text)) builder.setContentText(text)

            // 设置通知的点按操作，每个通知都应该对点按操作做出响应，通常是在应用中打开对应于该通知的Activity。
            if (chatIntent != null) {
                val pendingIntent:PendingIntent
                if (ActivityManager.getInstance().containsActivity(MainActivity::class.java)) {
                    pendingIntent = PendingIntent.getActivity(
                        ActivityManager.getInstance().application,
                        id,
                        chatIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                } else {
                    // 设置返回栈，确保MainActivity -> ChatActivity的顺序
                    val stackBuilder = TaskStackBuilder.create(ActivityManager.getInstance().application)
                    // 添加MainActivity作为底部的Activity
//        stackBuilder.addNextIntentWithParentStack(chatIntent);
                    // 主Activity的Intent，用于构建返回栈
                    val splashIntent = Intent(ActivityManager.getInstance().application, SplashActivity::class.java)
                    val mainIntent = Intent(ActivityManager.getInstance().application, MainActivity::class.java)
                    stackBuilder.addNextIntentWithParentStack(mainIntent)
                    stackBuilder.addNextIntent(chatIntent)

                    var pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        pendingIntentFlags = pendingIntentFlags or PendingIntent.FLAG_IMMUTABLE
                    }
                    pendingIntent = stackBuilder.getPendingIntent(0, pendingIntentFlags)
                }
                builder.setContentIntent(pendingIntent)
                    .setAutoCancel(true) // 在用户点按通知后自动移除通知
                if(NotificationManager.IMPORTANCE_HIGH == channel.importance) builder.setFullScreenIntent(pendingIntent, false)
            }

            return builder
        }

        fun createCallNotificationBuilder(
            context: Context,
            channel: Channel,
            title: CharSequence? = null,
            text: CharSequence? = null,
            chatIntent: Intent? = null,
            requestCode:Int
        ): NotificationCompat.Builder {
            // 必须先创建通知渠道，然后才能在Android 8.0及更高版本上发布任何通知
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createChannel(context, channel)
            }
            val builder =
                NotificationCompat.Builder(context, channel.channelId)
                    .setPriority(getLowVersionPriority(channel)) // 通知优先级，优先级确定通知在Android7.1和更低版本上的干扰程度。
                    .setVisibility(channel.lockScreenVisibility) // 锁定屏幕公开范围
                    .setVibrate(channel.vibrate) // 震动模式
                    .setSound(channel.sound ?: Settings.System.DEFAULT_NOTIFICATION_URI)    // 声音
                    .setOnlyAlertOnce(true) // 设置通知只会在通知首次出现时打断用户（通过声音、振动或视觉提示），而之后更新则不会再打断用户。

            // 标题，此为可选内容
            if (!TextUtils.isEmpty(title)) builder.setContentTitle(title)

            // 正文文本，此为可选内容
            if (!TextUtils.isEmpty(text)) builder.setContentText(text)

            // 设置通知的点按操作，每个通知都应该对点按操作做出响应，通常是在应用中打开对应于该通知的Activity。
            if (chatIntent != null) {
                var pendingIntent: PendingIntent? = null
                val context: Context = ActivityManager.getInstance().application
                if (ActivityManager.getInstance().containsActivity(MainActivity::class.java)) {
                    pendingIntent = PendingIntent.getActivity(
                        context,
                        requestCode,
                        chatIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                } else {
                    // 设置返回栈，确保MainActivity -> ChatActivity的顺序
                    val stackBuilder = TaskStackBuilder.create(context)
                    // 添加MainActivity作为底部的Activity
//        stackBuilder.addNextIntentWithParentStack(chatIntent);
                    // 主Activity的Intent，用于构建返回栈
                    val splashIntent = Intent(context, SplashActivity::class.java)
                    val mainIntent = Intent(context, MainActivity::class.java)
                    stackBuilder.addNextIntentWithParentStack(mainIntent)
                    stackBuilder.addNextIntent(chatIntent)
                    var pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        pendingIntentFlags = pendingIntentFlags or PendingIntent.FLAG_IMMUTABLE
                    }
                    pendingIntent = stackBuilder.getPendingIntent(requestCode, pendingIntentFlags)
                }

                builder.setContentIntent(pendingIntent).setCategory(NotificationCompat.CATEGORY_CALL) // 设置通知类别为电话通知
                    .setAutoCancel(true) // 在用户点按通知后自动移除通知
                if(NotificationManager.IMPORTANCE_HIGH == channel.importance) builder.setFullScreenIntent(pendingIntent, false)
            }

            return builder
        }

        /**
         * 获取低版本的优先级
         * 要支持搭载 Android 7.1（API 级别 25）或更低版本的设备，
         * 您还必须使用 NotificationCompat 类中的优先级常量针对每条通知调用 setPriority()。
         * @param channel
         * @return
         */
        private fun getLowVersionPriority(channel: Channel): Int {
            return when (channel.importance) {
                NotificationManager.IMPORTANCE_HIGH -> NotificationCompat.PRIORITY_HIGH
                NotificationManager.IMPORTANCE_LOW -> NotificationCompat.PRIORITY_LOW
                NotificationManager.IMPORTANCE_MIN -> NotificationCompat.PRIORITY_MIN
                else -> NotificationCompat.PRIORITY_DEFAULT
            }
        }

        /**
         * 创建通知渠道
         * <p>
         * 反复调用这段代码也是安全的，因为创建现有通知渠道不会执行任何操作。
         * 注意：创建通知渠道后，您便无法更改通知行为，此时用户拥有完全控制权。不过，您仍然可以更改渠道的名称和说明。
         * @param context 上下文
         * @param channel 通知渠道
         */
        @RequiresApi(api = Build.VERSION_CODES.O)
        public fun createChannel(
            context: Context,
            channel: Channel
        ) {
            val notificationChannel =
                NotificationChannel(channel.channelId, channel.name, channel.importance)
            notificationChannel.description = channel.description   // 描述
            notificationChannel.vibrationPattern = channel.vibrate  // 震动模式
            notificationChannel.setSound(channel.sound ?: Settings.System.DEFAULT_NOTIFICATION_URI, notificationChannel.audioAttributes)    // 声音
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }

        /**
         * 显示通知
         *
         *
         * 请记得保存您传递到 NotificationManagerCompat.notify() 的通知 ID，因为如果之后您想要更新或移除通知，将需要使用这个 ID。
         * @param context      上下文
         * @param id           通知的唯一ID
         * @param notification 通知
         */
        fun notify(
            context: Context,
            id: Int,
            notification: Notification?
        ) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(id, notification)
        }

        /**
         * 取消通知
         * @param context 上下文
         * @param id      通知的唯一ID
         */
        fun cancelCallNotify(context: Context, id: Int) {
            SPUtils.getInstance().remove(Constant.CURRENT_CALL_ID)
            val vibrator = context
                .getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.cancel()
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(id)
        }

        /**
         * 取消所有通知
         * @param context 上下文
         */
        fun cancelAll(context: Context) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
        }

        fun deleteChannel(context: Context, channelId: String) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.deleteNotificationChannel(channelId)
            }
        }

    }

    /**
     * 通知渠道
     */
    data class Channel(
        val channelId: String,                                                       // 唯一渠道ID
        val name: CharSequence,                                                      // 用户可见名称
        val importance: Int,                                                         // 重要性级别
        val description: String? = null,                                             // 描述
        @NotificationCompat.NotificationVisibility
        val lockScreenVisibility: Int = NotificationCompat.VISIBILITY_SECRET,        // 锁定屏幕公开范围
        val vibrate: LongArray? = null,                                              // 震动模式
        val sound: Uri? = null                                                      // 声音
    )
}