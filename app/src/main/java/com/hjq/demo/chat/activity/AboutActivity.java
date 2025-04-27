package com.hjq.demo.chat.activity;

import android.view.View;
import android.widget.TextView;

import com.hjq.demo.R;
import com.hjq.demo.other.AppConfig;

import butterknife.OnClick;

/**
 * 关于微信
 *
 * @author zhou
 */
public class AboutActivity extends ChatBaseActivity {

    @Override
    public int getLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    public void initView() {
        TextView tv_version_name = findViewById(R.id.tv_version_name);
        tv_version_name.setText("Version " + AppConfig.getVersionName());
    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {

    }

}