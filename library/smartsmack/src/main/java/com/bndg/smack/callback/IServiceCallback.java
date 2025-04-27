package com.bndg.smack.callback;

/**
 * @author r
 * @date 2024/6/12
 * @description sdk使用 发现服务回调
 */
public interface IServiceCallback {
    void onSuccess(String service);

    void onFailed(int code, String desc);
}
