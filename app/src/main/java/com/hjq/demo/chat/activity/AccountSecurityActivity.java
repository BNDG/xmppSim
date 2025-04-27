package com.hjq.demo.chat.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ClipboardUtils;
import com.hjq.demo.R;
import com.hjq.toast.ToastUtils;
import com.hjq.widget.layout.SettingBar;

import butterknife.BindView;
import butterknife.OnClick;
import com.bndg.smack.OmemoHelper;

/**
 * 账号与安全
 *
 * @author zhou
 */
public class AccountSecurityActivity extends ChatBaseActivity {

    @BindView(R.id.tv_title)
    TextView mTitleTv;

    @BindView(R.id.sb_sim_id)
    SettingBar mWeChatIdTv;
    @BindView(R.id.rl_finger_print)
    SettingBar mFingerPrintTv;

    @Override
    public int getLayoutId() {
        return R.layout.activity_account_security;
    }

    @Override
    public void initView() {
        setTitleStrokeWidth(mTitleTv);
    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {
        String userPhone = myUserInfo.getUserPhone();
        if (!TextUtils.isEmpty(userPhone)) {
            mWeChatIdTv.setRightText(userPhone);
        } else {
            mWeChatIdTv.setRightText(myUserInfo.getUserAccount());
        }
        mTitleTv.setText(getString(R.string.account_security));
        mFingerPrintTv.setRightText(OmemoHelper.getInstance().getFingerprint());
    }

    @OnClick({R.id.rl_password, R.id.rl_finger_print})
    public void onClick(View view) {
        if (view.getId() == R.id.rl_password) {// 设置密码
            startActivity(new Intent(AccountSecurityActivity.this, ModifyPasswordActivity.class));
        } else if (view.getId() == R.id.rl_finger_print) {
            ClipboardUtils.copyText(OmemoHelper.getInstance().getFingerprint());
            ToastUtils.show(getContext().getString(R.string.already_copy));
        }
    }

}