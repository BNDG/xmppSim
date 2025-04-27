package com.hjq.demo.chat.activity;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.hjq.demo.R;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.widget.view.SwitchButton;

import com.bndg.smack.SmartCommHelper;

/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2018/10/18
 * desc   : 聊天设置界面 播放语音 聊天背景
 */
public final class ChatSettingActivity extends ChatBaseActivity implements SwitchButton.OnCheckedChangeListener {
    private SwitchButton sbAudioSwitch;

    @Override
    protected int getLayoutId() {
        return R.layout.chat_setting_activity;
    }

    @Override
    protected void initView() {
        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText(getString(R.string.chat_setting));
        setOnClickListener(R.id.sb_blacklist, R.id.sb_backup_db);
        sbAudioSwitch = findViewById(R.id.sb_audio_switch);
        sbAudioSwitch.setChecked(SPUtils.getInstance().getBoolean(Constant.USE_SPEAKERPHONE));
        sbAudioSwitch.setOnCheckedChangeListener(this);
        if (SmartCommHelper.getInstance().isDeveloperMode()) {
            findViewById(R.id.sb_backup_db).setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void initData() {
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.sb_blacklist) {
            startActivity(BlackListActivity.class);
        } else if (view.getId() == R.id.sb_backup_db) {
            startActivity(new Intent(this, BackupDbActivity.class));
        }

    }

    @Override
    public void initListener() {

    }

    @Override
    public void onCheckedChanged(SwitchButton button, boolean checked) {
        SPUtils.getInstance().put(Constant.USE_SPEAKERPHONE, checked);
    }
}