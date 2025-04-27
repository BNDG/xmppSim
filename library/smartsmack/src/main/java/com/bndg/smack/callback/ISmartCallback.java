package com.bndg.smack.callback;

/**
 * @author r
 * @date 2024/5/29
 * @description 通用的回调.
 */
public interface ISmartCallback {
    void onSuccess();
    void onFailed(int code, String desc);

}
