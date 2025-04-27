package com.bndg.smack.utils;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.text.TextUtils;


import androidx.annotation.RequiresPermission;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bndg.smack.SmartCommHelper;
import com.bndg.smack.enums.SmartContentType;

/**
 * @author r
 * @date 2024/9/29
 * @description Brief description of the file content.
 */
public class OtherUtil {
    public static String getFileExtension(String filename) {
        if (!TextUtils.isEmpty(filename)) {
            int dot = filename.lastIndexOf('.');
            if (dot > -1 && dot < filename.length() - 1) {
                return filename.substring(dot + 1);
            }
        }
        return "";
    }

    public static String IMAGE_REGEX = "(?i)\\.(jpg|jpeg|png|gif|bmp|webp)$";
    public static String VIDEO_REGEX = "(?i)\\.(mp4|mkv|avi|mov|flv|wmv|webm|3gp|mpeg|mpg|ts|rmvb)$";

    public static boolean matcherUrl(String urlString, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(urlString);
        return matcher.find();
    }

    public static String getMessageTypeByUrl(String url) {
        if (matcherUrl(url, IMAGE_REGEX)) {
            return SmartContentType.IMAGE;
        } else if (matcherUrl(url, VIDEO_REGEX)) {
            return SmartContentType.TEXT;
        }
        return SmartContentType.TEXT;
    }

    public static String getAndroidId(){
        return Settings.System.getString(
                SmartCommHelper.getInstance().getApplication().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
    }

    /**
     * 获取AndroidID
     * @return
     */
    @SuppressLint("HardwareIds")
    public static String getAndroidID() {
        String id = Settings.Secure.getString(
                SmartCommHelper.getInstance().getApplication().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        if ("9774d56d682e549c".equals(id)) return "";
        return id == null ? "" : id;
    }

    /**
     * 判断字符串是否匹配正则
     * @param regex
     * @param input
     * @return
     */
    public static boolean isMatch(final String regex, final CharSequence input) {
        return input != null && input.length() > 0 && Pattern.matches(regex, input);
    }
    /**
     * Return whether network is connected.
     * <p>Must hold {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />}</p>
     *
     * @return {@code true}: connected<br>{@code false}: disconnected
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public static boolean isConnected() {
        NetworkInfo info = getActiveNetworkInfo();
        return info != null && info.isConnected();
    }
    @RequiresPermission(ACCESS_NETWORK_STATE)
    private static NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager cm =
                (ConnectivityManager) SmartCommHelper.getInstance().getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return null;
        return cm.getActiveNetworkInfo();
    }
}
