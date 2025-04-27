package com.bndg.smack.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import com.bndg.smack.SmartCommHelper;
import com.bndg.smack.SmartIMClient;

public class NetworkStateListener {

    private static final String TAG = "NetworkStateListener";
    private ConnectivityManager connectivityManager;
    private NetworkCallback networkCallback;

    public NetworkStateListener(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkCallback = new NetworkCallback();
    }

    public void registerNetworkCallback() {
        // 创建网络请求
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        // 判断版本，根据版本选择合适的注册方式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // API 24 及以上，直接使用 registerDefaultNetworkCallback
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        } else {
            // API 24 以下，使用 registerNetworkCallback
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
        }
    }

    public void unregisterNetworkCallback() {
        if (connectivityManager != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    // 自定义 NetworkCallback 类
    private class NetworkCallback extends ConnectivityManager.NetworkCallback {

        @Override
        public void onAvailable(Network network) {
            super.onAvailable(network);
            checkServiceStatus();
        }

        @Override
        public void onLost(Network network) {
            super.onLost(network);
        }

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities capabilities) {
            super.onCapabilitiesChanged(network, capabilities);
            if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                Log.d(TAG, "Network has internet capability");
            } else {
                Log.d(TAG, "Network does not have internet capability");
            }
        }
    }

    private void checkServiceStatus() {
        // 在这里检查服务状态并进行相应处理
        SmartCommHelper.getInstance().executeWithDelay(300, () -> {
            SmartTrace.file("监听到网络状态改变 查看连接情况");
            SmartIMClient.getInstance().checkConnection();
        });
    }
}
