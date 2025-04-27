package com.hjq.demo.chat.activity;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.hjq.demo.R;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * "设置" - "隐私"
 *
 * @author zhou
 */
public class PrivacyActivity extends ChatBaseActivity {

    @BindView(R.id.tv_title)
    TextView mTitleTv;

    @Override
    public int getLayoutId() {
        return R.layout.activity_privacy;
    }

    @Override
    public void initView() {
        mTitleTv.setText(R.string.privacy);
        setTitleStrokeWidth(mTitleTv);
    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.rl_mobile_blocked_list})
    public void onClick(View view) {
        switch (view.getId()) {
            // 通讯录黑名单
            case R.id.rl_mobile_blocked_list:
                startActivity(new Intent(PrivacyActivity.this, BlockedContactActivity.class));
                break;
        }
    }

}