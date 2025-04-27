package com.bndg.smack.callback;

/**
 * @author r
 * @date 2024/5/29
 * @description 文件发送回调
 */
public interface IFileCallback {
    void onSuccess(String fileUrl);
    void onFailed(int code, String desc);
}
