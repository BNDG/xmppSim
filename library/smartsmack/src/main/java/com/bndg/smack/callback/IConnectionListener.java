package com.bndg.smack.callback;

/**
 * author : r
 * time   : 2024/6/8 7:26 PM
 * desc   : 连接状态监听器
 */
public interface IConnectionListener {
    // 连接中
    void onServerConnecting();

    // 已连接
    void onServerConnected();

    // 连接失败
    void onServerConnectFailed(String desc);

    // 已鉴权
    void onAuthenticated();

    // 登录失败
    void onLoginFailed(int code, String desc);

    default void onChatDataLoading() {
    }

    // im加载结束
    default void onChatDataLoaded() {
    }

    // 当前用户被踢下线
    void onKickedOffline();

}
