package com.hjq.demo.http.floatlog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.blankj.utilcode.util.ServiceUtils;
import com.hjq.demo.utils.Trace;


/**
 * author : r
 * time   : 2023/8/3 9:22 AM
 * desc   :
 */
public class MyNotificationDeleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent fvsIntent = new Intent(context, FloatViewService.class);
        boolean serviceRunning = ServiceUtils.isServiceRunning(FloatViewService.class);
        Trace.d("onReceive: get receive==" + serviceRunning);
        if (serviceRunning) {
            context.stopService(fvsIntent);
        }
    }
}
