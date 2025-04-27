package com.hjq.demo.chat.activity;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.hjq.base.BaseDialog;
import com.hjq.demo.R;
import com.hjq.demo.chat.manager.NotificationCompatUtil;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.http.glide.GlideApp;
import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.manager.CacheDataManager;
import com.hjq.demo.manager.ThreadPoolManager;
import com.hjq.demo.ui.dialog.MessageDialog;
import com.hjq.widget.layout.SettingBar;

import butterknife.BindView;
import butterknife.OnClick;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.callback.ISmartCallback;

/**
 * 设置
 *
 * @author zhou
 */
public class SettingActivity extends ChatBaseActivity {

    /**
     * 标题
     */
    @BindView(R.id.tv_title)
    TextView mTitleTv;

    /**
     * 账号与安全
     */
    @BindView(R.id.rl_account_security)
    View mAccountSecurityRl;

    /**
     * 关于
     */
    @BindView(R.id.rl_about)
    View mAboutRl;

    /**
     * 退出
     */
    @BindView(R.id.rl_log_out)
    View mLogOutRl;
    private SettingBar mCleanCacheView;

    @Override
    public int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    public void initView() {
        mTitleTv.setText(R.string.settings);
        setTitleStrokeWidth(mTitleTv);
        mCleanCacheView = findViewById(R.id.sb_setting_cache);
    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {
        // 获取应用缓存大小
        mCleanCacheView.setRightText(CacheDataManager.getTotalCacheSize(this));
    }


    @OnClick({R.id.rl_account_security, R.id.rl_privacy, R.id.rl_about, R.id.rl_log_out, R.id.rl_chats,
            R.id.rl_general, R.id.sb_new_notification, R.id.sb_setting_cache})
    public void onClick(View view) {
        switch (view.getId()) {
            // 账号与安全
            case R.id.rl_account_security:
                startActivity(new Intent(SettingActivity.this, AccountSecurityActivity.class));
                break;
            //隐私
            case R.id.rl_privacy:
                startActivity(new Intent(SettingActivity.this, PrivacyActivity.class));
                break;
            // 关于kptk
            case R.id.rl_about:
                startActivity(new Intent(SettingActivity.this, AboutActivity.class));
                break;
            case R.id.rl_log_out:
                // 清除sharedpreferences中存储信息
                new MessageDialog.Builder(getActivity())
                        // 标题可以不用填写
                        .setTitle(getString(R.string.tips))
                        // 内容必须要填写
                        .setMessage(R.string.sure_to_quit)
                        // 确定按钮文本
                        .setConfirm(getString(R.string.common_confirm))
                        // 设置 null 表示不显示取消按钮
                        .setCancel(getString(R.string.common_cancel))
                        // 设置点击按钮后不关闭对话框
                        //.setAutoDismiss(false)
                        .setListener(new MessageDialog.OnListener() {

                            @Override
                            public void onConfirm(BaseDialog dialog) {
                                logout(SettingActivity.this);
                            }

                            @Override
                            public void onCancel(BaseDialog dialog) {
                            }
                        })
                        .show();

                break;
            case R.id.rl_chats:
                // 聊天
                startActivity(new Intent(SettingActivity.this, ChatSettingActivity.class));
                break;
            case R.id.rl_general:
                startActivity(new Intent(SettingActivity.this, GeneralSettingActivity.class));
                break;
            case R.id.sb_setting_cache:
                // 清除内存缓存（必须在主线程）
                GlideApp.get(getActivity()).clearMemory();
                ThreadPoolManager.getInstance().execute(() -> {
                    CacheDataManager.clearAllCache(this);
                    // 清除本地缓存（必须在子线程）
                    GlideApp.get(getActivity()).clearDiskCache();
                    post(() -> {
                        // 重新获取应用缓存大小
                        mCleanCacheView.setRightText(CacheDataManager.getTotalCacheSize(getActivity()));
                    });
                });
                break;
        }
    }
    public static void logout(Context context) {
        NotificationCompatUtil.Companion.cancelAll(context);
        // xmpp的退出
        SmartIMClient.getInstance().logout(new ISmartCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailed(int code, String desc) {
            }
        });
        PreferencesUtil.getInstance().logOut();
        context.startActivity(new Intent(context, SplashActivity.class));
        ActivityManager.getInstance().finishAllActivities(SplashActivity.class);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}