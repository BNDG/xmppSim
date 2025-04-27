package com.hjq.demo.chat.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.utils.Trace;

public class BatteryHelper {
    private static final String LOG_TAG = BatteryHelper.class.getSimpleName();

    /**
     * 判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
     * @return true：加入电池优化的白名单；false：没有加入电池优化的白名单
     */
    public static boolean isOptimizingBattery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Context context = ActivityManager.getInstance().getApplication().getApplicationContext();
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        } else {
            return true;
        }
    }

    @SuppressLint("BatteryLife")
    public static void sendIgnoreButteryOptimizationIntent(Activity activity) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            Uri uri = Uri.parse("package:" + ActivityManager.getInstance().getApplication().getPackageName());
            intent.setData(uri);

            try {
                activity.startActivityForResult(intent, 42);
            } catch (ActivityNotFoundException e) {
            }
        }
    }

    /**
     * 忽略电池优化
     */
    public static void ignoreBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Context context = ActivityManager.getInstance().getApplication().getApplicationContext();
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

            boolean hasIgnored = powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
            //  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
            if (!hasIgnored) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    ActivityManager.getInstance().getTopActivity().startActivity(intent);
                }
            } else {
            }
        }
    }
}
