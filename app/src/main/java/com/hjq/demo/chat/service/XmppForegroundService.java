package com.hjq.demo.chat.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.hjq.demo.utils.Trace;

public class XmppForegroundService extends Service {


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 在这里执行你的XMPP连接和管理逻辑
        Trace.d("onStartCommand: 在这里执行你的XMPP连接和管理逻辑" );
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // 如果需要，这里可以处理服务销毁时的逻辑，比如断开XMPP连接
        Trace.w("onDestroy: 服务销毁");
    }
}