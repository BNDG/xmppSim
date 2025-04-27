package com.hjq.demo.app;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.SPUtils;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonToken;
import com.hjq.bar.TitleBar;
import com.hjq.demo.BuildConfig;
import com.hjq.demo.R;
import com.hjq.demo.aop.Log;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.extensions.AitExtension;
import com.hjq.demo.chat.extensions.CallExtension;
import com.hjq.demo.chat.extensions.CardInfoExtension;
import com.hjq.demo.chat.extensions.FileInfoExtension;
import com.hjq.demo.chat.extensions.ImageSizeExtension;
import com.hjq.demo.chat.extensions.VideoInfoExtension;
import com.hjq.demo.chat.extensions.VoiceInfoExtension;
import com.hjq.demo.chat.listener.ChatRoomListener;
import com.hjq.demo.chat.listener.ConnectionListener;
import com.hjq.demo.chat.listener.FriendListener;
import com.hjq.demo.chat.manager.ChatMessageManager;
import com.hjq.demo.chat.manager.PushNotificationHelper;
import com.hjq.demo.chat.receiver.SmartMessageReceiver;
import com.hjq.demo.http.glide.GlideApp;
import com.hjq.demo.http.model.RequestHandler;
import com.hjq.demo.http.model.RequestServer;
import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.other.AppConfig;
import com.hjq.demo.other.CrashHandler;
import com.hjq.demo.other.DebugLoggerTree;
import com.hjq.demo.other.TitleBarStyle;
import com.hjq.demo.other.ToastLogInterceptor;
import com.hjq.demo.other.ToastStyle;
import com.hjq.demo.utils.Trace;
import com.hjq.gson.factory.GsonFactory;
import com.hjq.gson.factory.ParseExceptionCallback;
import com.hjq.http.EasyConfig;
import com.hjq.http.config.IRequestInterceptor;
import com.hjq.http.model.HttpHeaders;
import com.hjq.http.model.HttpParams;
import com.hjq.http.request.HttpRequest;
import com.hjq.language.MultiLanguages;
import com.hjq.language.OnLanguageListener;
import com.hjq.toast.ToastUtils;
import com.hjq.umeng.UmengClient;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.mmkv.MMKV;
import com.zxy.tiny.Tiny;

import java.util.Locale;

import com.bndg.smack.SmartCommHelper;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.constant.SmartConstants;
import okhttp3.OkHttpClient;
import timber.log.Timber;

/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2018/10/18
 * desc   : 应用入口
 */
public final class AppApplication extends Application implements  Application.ActivityLifecycleCallbacks {
    private BroadcastReceiver smartMessageReceiver;
    private int foregroundActivityCount = 0;
    public static long startTime = 0;

    @Log("启动耗时")
    @Override
    public void onCreate() {
        super.onCreate();
        startTime = System.currentTimeMillis();
        android.util.Log.w(">>>>", "AppApplication onCreate: >>>>>>>>> start");
        initSdk(this);
        // chat start
        // 注册广播接收器
        IntentFilter msgFilter = new IntentFilter();
        msgFilter.addAction(SmartConstants.ACCOUNT_STATUS);
        msgFilter.addAction(Constant.ACTION_DOWNLOAD_COMPLETE);
        msgFilter.addAction(Constant.ACTION_DOWNLOAD_PROGRESS);
        smartMessageReceiver = new SmartMessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(smartMessageReceiver, msgFilter);
        // 初始化多语种框架
        MultiLanguages.init(this);
        // 设置语种变化监听器
        MultiLanguages.setOnLanguageListener(new OnLanguageListener() {

            @Override
            public void onAppLocaleChange(Locale oldLocale, Locale newLocale) {
                android.util.Log.w("MultiLanguages", "监听到应用切换了语种，旧语种：" + oldLocale + "，新语种：" + newLocale);
            }

            @Override
            public void onSystemLocaleChange(Locale oldLocale, Locale newLocale) {
                android.util.Log.w("MultiLanguages", "监听到系统切换了语种，旧语种：" + oldLocale + "，新语种：" + newLocale +
                        "，是否跟随系统：" + MultiLanguages.isSystemLanguage(AppApplication.this));
            }
        });
        android.util.Log.w(">>>>", "AppApplication onCreate: >>>>>>>>> end 用时 " + (System.currentTimeMillis() - startTime));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        // 绑定语种
        super.attachBaseContext(MultiLanguages.attach(newBase));
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // 清理所有图片内存缓存
        GlideApp.get(this).onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        // 根据手机内存剩余情况清理图片内存缓存
        GlideApp.get(this).onTrimMemory(level);
    }

    /**
     * 初始化一些第三方框架
     */
    public static void initSdk(Application application) {
        // 设置标题栏初始化器
        TitleBar.setDefaultStyle(new TitleBarStyle());

        // 设置全局的 Header 构建器
        // 设置全局的 Header 构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator((cx, layout) ->
                        new ClassicsHeader(cx)
//                new MaterialHeader(application).setColorSchemeColors(ContextCompat.getColor(application, R.color.common_accent_color))
        );
        // 设置全局的 Footer 构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator((cx, layout) ->
//                new SmartBallPulseFooter(application)
                        new ClassicsFooter(cx).setDrawableSize(20)
        );
        // 设置全局初始化器
     /*   SmartRefreshLayout.setDefaultRefreshInitializer((cx, layout) -> {
            // 刷新头部是否跟随内容偏移
            layout.setEnableHeaderTranslationContent(true)
                    // 刷新尾部是否跟随内容偏移
                    .setEnableFooterTranslationContent(true)
                    // 加载更多是否跟随内容偏移
                    .setEnableFooterFollowWhenNoMoreData(true)
                    // 内容不满一页时是否可以上拉加载更多
                    .setEnableLoadMoreWhenContentNotFull(false)
                    // 仿苹果越界效果开关
                    .setEnableOverScrollDrag(false);
        });*/

        // 初始化吐司
        ToastUtils.init(application, new ToastStyle());
        // 设置调试模式
        ToastUtils.setDebugMode(AppConfig.isDebug());
        // 设置 Toast 拦截器
        ToastUtils.setInterceptor(new ToastLogInterceptor());

        // 本地异常捕捉
        CrashHandler.register(application);

        // Activity 栈管理初始化
        ActivityManager.getInstance().init(application);

        // MMKV 初始化
        MMKV.initialize(application);

        // 网络请求框架初始化
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .build();

        EasyConfig.with(okHttpClient)
                // 是否打印日志
                .setLogEnabled(AppConfig.isLogEnable())
                // 设置服务器配置
                .setServer(new RequestServer())
                // 设置请求处理策略
                .setHandler(new RequestHandler(application))
                // 设置请求重试次数
                .setRetryCount(1)
                .setInterceptor(new IRequestInterceptor() {
                    @Override
                    public void interceptArguments(@NonNull HttpRequest<?> httpRequest,
                                                   @NonNull HttpParams params,
                                                   @NonNull HttpHeaders headers) {
                    }
                })
                .into();

        // 设置 Json 解析容错监听
        GsonFactory.setParseExceptionCallback(new ParseExceptionCallback() {

            @Override
            public void onParseObjectException(TypeToken<?> typeToken, String fieldName, JsonToken jsonToken) {
                handlerGsonParseException("解析对象析异常：" + typeToken + "#" + fieldName + "，后台返回的类型为：" + jsonToken);
            }

            @Override
            public void onParseListItemException(TypeToken<?> typeToken, String fieldName, JsonToken listItemJsonToken) {
                handlerGsonParseException("解析 List 异常：" + typeToken + "#" + fieldName + "，后台返回的条目类型为：" + listItemJsonToken);
            }

            @Override
            public void onParseMapItemException(TypeToken<?> typeToken, String fieldName, String mapItemKey, JsonToken mapItemJsonToken) {
                handlerGsonParseException("解析 Map 异常：" + typeToken + "#" + fieldName + "，mapItemKey = " + mapItemKey + "，后台返回的条目类型为：" + mapItemJsonToken);
            }

            private void handlerGsonParseException(String message) {
               /* if (BuildConfig.DEBUG) {
                    throw new IllegalArgumentException(message);
                }  else {
                    CrashReport.postCatchedException(new IllegalArgumentException(message));
                }*/
            }
        });
        // 初始化日志打印
        if (AppConfig.isLogEnable()) {
            Timber.plant(new DebugLoggerTree());
        }

        // 注册网络状态变化监听
        ConnectivityManager connectivityManager = ContextCompat.getSystemService(application, ConnectivityManager.class);
        if (connectivityManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                @Override
                public void onLost(@NonNull Network network) {
                    Activity topActivity = ActivityManager.getInstance().getTopActivity();
                    if (!(topActivity instanceof LifecycleOwner)) {
                        return;
                    }

                    LifecycleOwner lifecycleOwner = ((LifecycleOwner) topActivity);
                    if (lifecycleOwner.getLifecycle().getCurrentState() != Lifecycle.State.RESUMED) {
                        return;
                    }

                    ToastUtils.show(R.string.common_network_error);
                }
            });
        }
        Trace.getConfig()
                .setLogSwitch(BuildConfig.DEBUG)// 设置log总开关，默认开
                .setGlobalTag(">>>>")// 设置log全局标签，默认为空
                // 当全局标签不为空时，我们输出的log全部为该tag，
                // 为空时，如果传入的tag为空那就显示类名，否则显示tag
                .setLog2FileSwitch(false)// 打印log时是否存到文件的开关，默认关
                .setBorderSwitch(true);// 输出日志是否带边框开关，默认开
        // 图片压缩
        Tiny.getInstance().init(application);
        initChatSdk(application);
        /*KeepLive.startWork(application, KeepLive.RunMode.ENERGY,
                new ForegroundNotification("kptk", "kptk正在运行", R.mipmap.ic_launcher),
                new KeepLiveService() {
                    @Override
                    public void onWorking() {

                    }

                    @Override
                    public void onStop() {

                    }
                });*/
        if (SPUtils.getInstance().getBoolean(Constant.PRIVACY_AGREEMENT)) {
            // 友盟统计、登录、分享 SDK 渠道 第三方sdk
            UmengClient.preInit(application, AppConfig.isLogEnable(), "test");
            //...在这里设置strategy的属性，在bugly初始化时传入
            CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(application);
            // 通过UserStrategy设置
            strategy.setDeviceID(DeviceUtils.getAndroidID());
            // 通过UserStrategy设置
            strategy.setDeviceModel(DeviceUtils.getModel());
            // 获取APP ID并调用初始化方法，Bugly会为自动检测环境并完成配置：
            CrashReport.initCrashReport(application, AppConfig.getBuglyId(), AppConfig.isDebug());
        }
    }

    private static void initChatSdk(Application application) {
        android.util.Log.w(">>>>", "SmartIMClient initSdk start >>>> : ");
        PushNotificationHelper.INSTANCE.createNotificationChannel(application);
        // 初始化sdk
        SmartIMClient.getInstance().initSDK(application);
        // 设置连接状态监听器
        SmartIMClient.getInstance().setConnectionListener(ConnectionListener.INSTANCE);
        // 设置消息监听器
        SmartIMClient.getInstance().setSimpleMsgListener(ChatMessageManager.getInstance());
        // 设置群组事件监听器
        SmartIMClient.getInstance().setChatRoomListener(ChatRoomListener.getInstance());
        // 设置好友事件监听器
        SmartIMClient.getInstance().getFriendshipManager().addFriendListener(FriendListener.INSTANCE);
        // 添加自定义扩展
        SmartCommHelper.getInstance().addExtensionProvider(AitExtension.ELEMENT_NAME, AitExtension.NAMESPACE, new AitExtension.Provider());
        SmartCommHelper.getInstance().addExtensionProvider(CallExtension.ELEMENT_NAME, CallExtension.NAMESPACE, new CallExtension.Provider());
        SmartCommHelper.getInstance().addExtensionProvider(ImageSizeExtension.ELEMENT_NAME, ImageSizeExtension.NAMESPACE, new ImageSizeExtension.Provider());
        SmartCommHelper.getInstance().addExtensionProvider(FileInfoExtension.ELEMENT_NAME, FileInfoExtension.NAMESPACE, new FileInfoExtension.Provider());
        SmartCommHelper.getInstance().addExtensionProvider(VoiceInfoExtension.ELEMENT_NAME, VoiceInfoExtension.NAMESPACE, new VoiceInfoExtension.Provider());
        SmartCommHelper.getInstance().addExtensionProvider(VideoInfoExtension.ELEMENT_NAME, VideoInfoExtension.NAMESPACE, new VideoInfoExtension.Provider());
        SmartCommHelper.getInstance().addExtensionProvider(CardInfoExtension.ELEMENT_NAME, CardInfoExtension.NAMESPACE, new CardInfoExtension.Provider());
        android.util.Log.w(">>>>", "SmartIMClient initSdk end >>>>:");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // 在应用终止时注销广播接收器
        unregisterReceiver(smartMessageReceiver);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        // 可选：记录Activity创建
    }

    @Override
    public void onActivityStarted(Activity activity) {
        foregroundActivityCount++;
        // 应用进入前台时，foregroundActivityCount会增加
    }

    @Override
    public void onActivityResumed(Activity activity) {
        // 可选：记录Activity恢复
    }

    @Override
    public void onActivityPaused(Activity activity) {
        // 可选：记录Activity暂停
    }

    @Override
    public void onActivityStopped(Activity activity) {
        foregroundActivityCount--;
        // 应用进入后台时，foregroundActivityCount会减到0
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        // 可选：保存Activity的状态
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        // 可选：记录Activity销毁
    }

    // 提供一个方法给Service或其他地方调用，判断应用是否在前台
    public boolean isApplicationInForeground() {
        return foregroundActivityCount > 0;
    }
}