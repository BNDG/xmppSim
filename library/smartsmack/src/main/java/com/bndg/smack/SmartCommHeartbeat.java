package com.bndg.smack;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.bndg.smack.utils.SmartTrace;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Predicate;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SmartCommHeartbeat {
    private Disposable disposable;
    private boolean isReconnecting;
    private Disposable tryReconnect;


    public void startHeartDump() {
        // 设置默认的 Ping 间隔为 10 秒
        PingManager.setDefaultPingInterval(5 * 60);
        final PingManager pingManager = PingManager.getInstanceFor(SmartIMClient.getInstance().getConnection());
        SmartTrace.file("注册心跳监听器ping ");
        pingManager.registerPingFailedListener(new PingFailedListener() {
            @Override
            public void pingFailed() {
                if (!isReconnecting) {
                    // 在 Ping 失败时进行重连操作
                    SmartTrace.file("在 Ping 失败时进行重连操作 ");
                    isReconnecting = true;
                    tryReconnect();
                }
            }
        });
        pingManager.pingServerIfNecessary(); //发送ping
    }

    public void tryReconnect() {
        tryReconnect = Observable.interval(20, TimeUnit.SECONDS) // 每隔10秒尝试一次重连
                .observeOn(Schedulers.io())
                .takeUntil((Predicate<? super Long>) tick -> SmartIMClient.getInstance().getConnection().isAuthenticated() ) // 直到连接成功
                .subscribe(tick -> {
                    SmartTrace.w("Heartbeat 尝试重连中...");
                    SmartTrace.file("Heartbeat 尝试重连中...");
                    SmartIMClient.getInstance().checkConnection();
                    isReconnecting = false;
                });
    }

    // 停止心跳
    public void stopHeartbeat() {
        SmartTrace.d("停止心跳监听器");
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        if (tryReconnect != null && !tryReconnect.isDisposed()) {
            tryReconnect.dispose();
        }
    }

    private Disposable pingDisposable;

    public void startHeartDump2() {
        PingManager.setDefaultPingInterval(120);
        final PingManager pingManager = PingManager.getInstanceFor(SmartIMClient.getInstance().getConnection());
        int maxRetryAttempts = 100; // 最大重试次数
        int retryDelayBaseMs = 3000; // 初始重试延迟，例如3秒
        pingDisposable = Observable.interval(0, 30, TimeUnit.SECONDS)
                .flatMap((Function<Long, ObservableSource<Boolean>>) aLong ->
                        Observable.fromCallable(() -> {
                                    if (!pingDisposable.isDisposed()) {
                                        boolean isPingSuccess = pingManager.pingMyServer();
                                        if (!isPingSuccess) {
                                            SmartTrace.w("Ping IMServer 失败...");
                                            throw new RuntimeException("Ping IMServer 失败");
                                        } else {
                                            SmartTrace.w("Ping IMServer ok...");
                                        }
                                    }
                                    return true;
                                })
                                .subscribeOn(Schedulers.io()))
                .onErrorResumeNext(throwable -> {
                    // 在这里处理发生的异常
                    SmartTrace.e("Ping失败: ", throwable);
                    return Observable.just(false); // 返回一个默认值或者其他处理结果
                })
                .retryWhen(throwableObservable ->
                        throwableObservable.zipWith(Observable.range(1, maxRetryAttempts + 1), (throwable, attempt) -> {
                            // 检查网络连接
                            if (!isNetworkAvailableAndConnected()) {
                                SmartTrace.w("网络不可用，放弃重试...");
                                return Observable.error(throwable);
                            }
                            // 指数退避策略
                            long delay = (long) Math.pow(2, attempt - 1) * retryDelayBaseMs;
                            SmartTrace.w("尝试重连中... 第" + attempt + "次尝试，延迟" + delay + "毫秒");
                            return Observable.timer(delay, TimeUnit.MILLISECONDS)
                                    .flatMap((Function<Long, ObservableSource<?>>) ignored ->
                                            Observable.fromCallable(() -> {
                                                            // 尝试重新连接
                                                            SmartIMClient.getInstance().checkConnection();
                                                            return true;
                                                    })
                                                    .subscribeOn(Schedulers.io()));
                        }))
                .observeOn(Schedulers.io()) // 所有操作在IO线程执行
                .subscribe(isConnected -> {
                    if (isConnected) {
                        SmartTrace.w("Ping IMServer 成功...");
                    }
                }, throwable -> {
                    // 处理最终的异常
                    SmartTrace.e("心跳检测最终失败: ", throwable);
                });
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) SmartCommHelper.getInstance().getApplication()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    // 在适当的时候取消订阅
    public void stopHeartDump2() {
        if (pingDisposable != null && !pingDisposable.isDisposed()) {
            pingDisposable.dispose();
        }
    }

}
