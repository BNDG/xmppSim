package com.hjq.demo.chat.activity;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.hjq.base.BaseDialog;
import com.hjq.demo.R;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.utils.BatteryHelper;
import com.hjq.demo.ui.dialog.MenuDialog;
import com.hjq.demo.utils.AutoStartPermissionManager;
import com.hjq.widget.layout.SettingBar;

import java.util.ArrayList;
import java.util.List;

/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2018/10/18
 * desc   : 通用设置界面-自启动-电池优化
 */
public final class GeneralSettingActivity extends ChatBaseActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.general_setting_activity;
    }

    @Override
    protected void initView() {
        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText(getString(R.string.settings));
        setOnClickListener(R.id.rl_multi_languages, R.id.rl_auto_start, R.id.rl_power, R.id.rl_battery, R.id.rl_channel);
    }

    @Override
    protected void initData() {
        if (BatteryHelper.isOptimizingBattery()) {
            findViewById(R.id.rl_battery).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.rl_auto_start) {
            AutoStartPermissionManager.startToAutoStartSetting(this);
        } else if (view.getId() == R.id.rl_power) {
            AutoStartPermissionManager.startBatteryStrategyActivity(this);
        } else if (view.getId() == R.id.rl_battery) {
            BatteryHelper.ignoreBatteryOptimization();
        } else if (view.getId() == R.id.rl_channel) {
            startActivity(new Intent(this, ChannelDiscoverActivity.class));
        }else if (view.getId() == R.id.rl_multi_languages) {
            startActivity(new Intent(this, MultiLanguagesActivity.class));
        }
    }

    @Override
    public void initListener() {

    }
}