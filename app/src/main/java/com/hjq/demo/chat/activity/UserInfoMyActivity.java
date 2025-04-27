package com.hjq.demo.chat.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hjq.demo.R;
import com.hjq.demo.aop.SingleClick;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.utils.AvatarGenerator;

import butterknife.BindView;
import com.bndg.smack.SmartCommHelper;
import com.bndg.smack.enums.SmartConversationType;

/**
 * 自己用户详情页
 *
 * @author zhou
 */
public class UserInfoMyActivity extends ChatBaseActivity {

    @BindView(R.id.sdv_avatar)
    ImageView mAvatarSdv;

    @BindView(R.id.tv_name)
    TextView mNameTv;

    @BindView(R.id.iv_sex)
    ImageView mSexIv;

    @BindView(R.id.tv_wx_id)
    TextView mWxIdTv;

    @BindView(R.id.tv_operate)
    TextView mOperateTv;


    @Override
    public int getLayoutId() {
        return R.layout.activity_user_info_my;
    }

    @Override
    public void initView() {

        setTitleStrokeWidth(mOperateTv);
    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {
        AvatarGenerator.loadAvatar(this, myUserInfo.getUserId(), myUserInfo.getUserNickName(), mAvatarSdv, true);
        mNameTv.setText(myUserInfo.getUserNickName());
        if (Constant.USER_SEX_MALE.equals(myUserInfo.getUserSex())) {
            mSexIv.setImageResource(R.drawable.icon_sex_male);
        } else if (Constant.USER_SEX_FEMALE.equals(myUserInfo.getUserSex())) {
            mSexIv.setImageResource(R.drawable.icon_sex_female);
        } else {
            mSexIv.setVisibility(View.GONE);
        }
        if (SmartCommHelper.getInstance().isDeveloperMode()) {
            mWxIdTv.setText(String.format(getString(R.string.wx_id), myUserInfo.getUserId()));
        } else {
            if (!TextUtils.isEmpty(myUserInfo.getUserPhone())) {
                mWxIdTv.setText(String.format(getString(R.string.wx_id), myUserInfo.getUserPhone()));
            } else {
                mWxIdTv.setText(String.format(getString(R.string.wx_id), myUserInfo.getUserAccount()));
            }
        }
        setOnClickListener(R.id.rl_operate);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @SingleClick
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            // 发消息
            case R.id.rl_operate:
                ChatActivity.start(this, SmartConversationType.SINGLE.name(),
                        myUserInfo.getUserId(), myUserInfo.getUserNickName());
                break;
        }
    }
}