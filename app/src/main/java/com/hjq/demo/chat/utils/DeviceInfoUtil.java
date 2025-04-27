package com.hjq.demo.chat.utils;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;

import com.hjq.demo.chat.entity.DeviceInfo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DeviceInfoUtil {

    private static final String TAG = "DeviceInfoUtil";
    private static DeviceInfoUtil instance;

    public DeviceInfoUtil() {

    }

    public static DeviceInfoUtil getInstance() {
        if (instance == null) {
            instance = new DeviceInfoUtil();
        }
        return instance;
    }

    public static boolean areNotificationsEnabled(Context context) {
        NotificationManagerCompat.from(context).areNotificationsEnabled();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return true;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            return isEnableV19(context);
        }else {
            return isEnableV26(context);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static boolean isEnableV19(Context context) {
        final String CHECK_OP_NO_THROW = "checkOpNoThrow";
        final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";
        AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        ApplicationInfo appInfo = context.getApplicationInfo();
        String pkg = context.getApplicationContext().getPackageName();
        int uid = appInfo.uid;
        Class appOpsClass = null; /* Context.APP_OPS_MANAGER */
        try {
            appOpsClass = Class.forName(AppOpsManager.class.getName());
            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE, String.class);
            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
            int value = (int) opPostNotificationValue.get(Integer.class);
            return ((int) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (NoSuchFieldException e) {
        } catch (InvocationTargetException e) {
        } catch (IllegalAccessException e) {
        } catch (Exception e) {
        }
        return false;
    }


    private static boolean isEnableV26(Context context) {
        ApplicationInfo appInfo = context.getApplicationInfo();
        String pkg = context.getApplicationContext().getPackageName();
        int uid = appInfo.uid;
        try {
            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            Method sServiceField = notificationManager.getClass().getDeclaredMethod("getService");
            sServiceField.setAccessible(true);
            Object sService = sServiceField.invoke(notificationManager);

            Method method = sService.getClass().getDeclaredMethod("areNotificationsEnabledForPackage"
                    , String.class, Integer.TYPE);
            method.setAccessible(true);
            return (boolean) method.invoke(sService, pkg, uid);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 获取手机品牌
     *
     * @return 手机品牌
     */
    public String getPhoneBrand() {
        return Build.BRAND;
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    public String getPhoneModel() {
        return Build.MODEL;
    }

    /**
     * 获取操作系统版本
     *
     * @return 操作系统版本
     */
    public String getOS() {
        return "Android" + Build.VERSION.RELEASE;
    }


    /**
     * 获取手机分辨率
     *
     * @param context context
     * @return 手机分辨率
     */
    public String getResolution(Context context) {
        // 方法1 Android获得屏幕的宽和高
        WindowManager windowManager = ((Activity) context).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();
        Log.w(TAG, "分辨率：" + screenWidth + "*" + screenHeight);
        return screenWidth + "*" + screenHeight;

    }

    /**
     * 获取运营商信息
     *
     * @param context context
     * @return 运营商信息
     */
    public String getOperator(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String simOperator = manager.getSimOperator();
        String operator;
        if (!TextUtils.isEmpty(simOperator)) {
            if (simOperator.equals("46000") || simOperator.equals("46002") || simOperator.equals("46007")) {
                // 中国移动
                operator = "中国移动";
            } else if (simOperator.equals("46003")) {
                // 中国电信
                operator = "中国电信";
            } else if (simOperator.equals("46001") || simOperator.equals("46006")) {
                // 中国联通
                operator = "中国联通";
            } else {
                // 未知
                operator = "未知";
            }
        } else {
            // 未知
            operator = "未知";
        }
        return operator;
    }


    /**
     * 获取设备信息
     *
     * @param context context
     * @return 设备信息
     */
    public DeviceInfo getDeviceInfo(Context context) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setPhoneBrand(getPhoneBrand());
        deviceInfo.setPhoneModel(getPhoneModel());
        deviceInfo.setOs(getOS());
        deviceInfo.setResolution(getResolution(context));
        deviceInfo.setOperator(getOperator(context));
        return deviceInfo;
    }


}
