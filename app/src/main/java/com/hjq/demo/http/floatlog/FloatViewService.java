package com.hjq.demo.http.floatlog;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


import com.hjq.demo.R;
import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.utils.Trace;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


/**
 * Created by r on 17/3/15.
 */

public class FloatViewService extends Service {

    private FloatView mFloatView;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Trace.d("通过 start 启动: here");
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        int notificationId = startId;
        String channelId = "";
        // 适配 Android 8.0 通知渠道新特性
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "logcat",
                    "日志", NotificationManager.IMPORTANCE_LOW);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});
            channel.setSound(null, null);
            notificationManager.createNotificationChannel(channel);
            channelId = channel.getId();
        }
// 创建一个 PendingIntent，用于监听通知的移除事件
        Intent deleteIntent = new Intent(this, MyNotificationDeleteReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                ActivityManager.getInstance().getTopActivity(), // 使用可变的上下文
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT // 或者其他标志
        );

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                // 设置通知时间
                .setWhen(System.currentTimeMillis())
                // 设置通知标题
                .setContentTitle(getString(R.string.app_name) + "日志查看器-移除以关闭")
                // 设置通知小图标
                .setSmallIcon(R.mipmap.ic_launcher)
                // 设置通知大图标
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                // 设置通知静音
                .setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
                .setDeleteIntent(pendingIntent)
                // 设置通知的优先级
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Notification build = notificationBuilder.build();
        // 设置为前台服务 需要 foreground service 权限
        startForeground(1, build);
        //当服务被杀死后 系统会重启该服务
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Trace.d("绑定了服务");
        return new FloatViewServiceBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Trace.d("onUnbind: 解绑了服务");
        return super.onUnbind(intent);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Trace.d("开启了服务");
        EventBus.getDefault().register(this);
        mFloatView = new FloatView(this);
        mFloatView.show();
    }

    @Subscribe
    public void onEvent(HttpLogEvent event) {
        mFloatView.setContent(event);
    }

    public class FloatViewServiceBinder extends Binder {
        public FloatViewService getService() {
            return FloatViewService.this;
        }
    }

    @Override
    public void onDestroy() {
        Trace.d("onDestroy: 服务关闭");
        super.onDestroy();
        stopForeground(true);// 停止前台服务--参数：表示是否移除之前的通知
        EventBus.getDefault().unregister(this);
        destroyFloat();
    }

    public void destroyFloat() {
        if (mFloatView != null) {
            mFloatView.destroy();
        }
        mFloatView = null;
    }
}
