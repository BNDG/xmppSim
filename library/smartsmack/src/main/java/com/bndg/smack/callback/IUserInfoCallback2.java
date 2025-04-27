package com.bndg.smack.callback;

import android.graphics.Bitmap;

import com.bndg.smack.model.SmartUserInfo;

/**
 * @author r
 * @date 2024/5/29
 * @description 获取用户信息回调
 */
public interface IUserInfoCallback2 {
    default void onSuccess(SmartUserInfo userInfo){};
    default void onFailed(int code, String desc){};

    default void onAvatarBitmapReceived(Bitmap bitmap){};

}
