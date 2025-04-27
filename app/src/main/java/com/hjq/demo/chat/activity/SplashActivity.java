package com.hjq.demo.chat.activity;

import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.Button;

import com.airbnb.lottie.LottieAnimationView;
import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.SPUtils;
import com.hjq.base.BaseDialog;
import com.hjq.demo.R;
import com.hjq.demo.app.AppActivity;
import com.hjq.demo.app.AppApplication;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.utils.CryptoUtil;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.other.AppConfig;
import com.hjq.demo.ui.dialog.MessageDialog;
import com.hjq.demo.utils.Trace;
import com.hjq.umeng.UmengClient;
import com.tencent.bugly.crashreport.CrashReport;

import com.bndg.smack.SmartCommHelper;
import me.leolin.shortcutbadger.ShortcutBadger;


public class SplashActivity extends AppActivity implements View.OnClickListener {

    private static final int sleepTime = 150;

    Button mLoginBtn;
    Button mRegisterBtn;

    private static final int SHOW_OPERATE_BTN = 0x3000;

    private LottieAnimationView mLottieView;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_splash;
    }

    protected void initView() {
        Trace.w(">>>>", "SplashActivity initView: 用时 " + (System.currentTimeMillis() - AppApplication.startTime));
        mLottieView = findViewById(R.id.lav_splash_lottie);
        /*AlphaAnimation animation = new AlphaAnimation(0.3f, 1.0f);
        animation.setDuration(1000);
        findViewById(R.id.ll_splash).setAnimation(animation);*/
        mLoginBtn = findViewById(R.id.btn_login);
        mRegisterBtn = findViewById(R.id.btn_register);
        mLoginBtn.setOnClickListener(this);
        mRegisterBtn.setOnClickListener(this);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                mLottieView.playAnimation();
            }
        }, 50);
    }

    @Override
    protected void onResume() {
        super.onResume();
        long time = System.currentTimeMillis() - AppApplication.startTime;
        Trace.w(">>>>", "SplashActivity onResume: 用时 " + time);

    }

    @Override
    protected void initData() {
        // 判断是否已经同意隐私协议 如果没同意过 弹出对话框
        if (!SPUtils.getInstance().getBoolean(Constant.PRIVACY_AGREEMENT)) {
            post(() -> {
                showPrivacyAgreementDialog();
            });
        } else {
            initOtherData();
        }
        ShortcutBadger.removeCount(SplashActivity.this);
    }

    private void showPrivacyAgreementDialog() {
        // 创建提示语句
        String message = String.format(getString(R.string.privacy_agreement_dialog), getString(R.string.privacy_agreement_name));

        // 将隐私政策链接设置为蓝色可点击
        SpannableString spannableString = new SpannableString(message);
        int start = message.indexOf(getString(R.string.privacy_agreement_name));
        int end = start + getString(R.string.privacy_agreement_name).length();
        String privacyPolicyUrl = "https://www.bing.com/";
        spannableString.setSpan(new URLSpan(privacyPolicyUrl), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // 消息对话框
        new MessageDialog.Builder(getActivity())
                // 标题可以不用填写
                .setTitle(getString(R.string.tips))
                // 内容必须要填写
                .setSpannableMessage(spannableString)
                // 确定按钮文本
                .setConfirm(getString(R.string.common_confirm))
                // 设置 null 表示不显示取消按钮
                .setCancel(getString(R.string.common_cancel))
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                // 设置点击按钮后不关闭对话框
                //.setAutoDismiss(false)
                .setListener(new MessageDialog.OnListener() {

                    @Override
                    public void onConfirm(BaseDialog dialog) {
                        SPUtils.getInstance().put(Constant.PRIVACY_AGREEMENT, true);
                        UmengClient.init(ActivityManager.getInstance().getApplication(), "test");
                        //...在这里设置strategy的属性，在bugly初始化时传入
                        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(SplashActivity.this.getApplication());
                        // 通过UserStrategy设置
                        strategy.setDeviceID(DeviceUtils.getAndroidID());
                        // 通过UserStrategy设置
                        strategy.setDeviceModel(DeviceUtils.getModel());
                        // 获取APP ID并调用初始化方法，Bugly会为自动检测环境并完成配置：
                        CrashReport.initCrashReport(SplashActivity.this.getApplication(), AppConfig.getBuglyId(), AppConfig.isDebug());
                        initOtherData();
                    }

                    @Override
                    public void onCancel(BaseDialog dialog) {
                        finish();

                    }
                })
                .show();
    }

    private void initOtherData() {
        if (PreferencesUtil.getInstance().isLogin()
                && !TextUtils.isEmpty(PreferencesUtil.getInstance().getUserId())) {
            String[] myAccount = CryptoUtil.decryptCredentials2();
            if (null != myAccount) {
                // 有用户信息 自动登录
                Trace.w(">>>>", "startActivity MainActivity: -----------------");
                startActivity(MainActivity.class);
                finish();
            } else {
                mLoginBtn.setVisibility(View.VISIBLE);
                mRegisterBtn.setVisibility(View.VISIBLE);
            }
        } else {
            mLoginBtn.setVisibility(View.VISIBLE);
            mRegisterBtn.setVisibility(View.VISIBLE);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Trace.d("initView: Splash onStop--------------");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                break;
            case R.id.btn_register:
                startActivity(new Intent(SplashActivity.this, RegisterActivity.class));
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
