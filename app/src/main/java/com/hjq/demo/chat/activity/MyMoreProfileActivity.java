package com.hjq.demo.chat.activity;

import android.content.Intent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hjq.demo.R;
import com.hjq.demo.chat.cons.Constant;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * 更多信息
 *
 * @author zhou
 */
public class MyMoreProfileActivity extends ChatBaseActivity {

    @BindView(R.id.tv_title)
    TextView mTitleTv;

    @BindView(R.id.rl_sex)
    RelativeLayout mSexRl;

    @BindView(R.id.rl_region)
    RelativeLayout mRegionRl;


    @BindView(R.id.tv_sex)
    TextView mSexTv;

    @BindView(R.id.tv_region)
    TextView mRegionTv;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_my_more_profile;
    }

    public void initView() {
        mTitleTv.setText(R.string.more_info);
        setTitleStrokeWidth(mTitleTv);

        String userSex = myUserInfo.getUserSex();

        if (Constant.USER_SEX_MALE.equals(userSex)) {
            mSexTv.setText(getString(R.string.sex_male));
        } else if (Constant.USER_SEX_FEMALE.equals(userSex)) {
            mSexTv.setText(getString(R.string.sex_female));
        }
        mRegionTv.setText(myUserInfo.getUserRegion());
    }

    @Override
    protected void initData() {

    }

    @OnClick({R.id.rl_sex, R.id.rl_region})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_sex:
                break;
            case R.id.rl_region:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Constant.USER_SEX_MALE.equals(myUserInfo.getUserSex())) {
            mSexTv.setText(getString(R.string.sex_male));
        } else if (Constant.USER_SEX_FEMALE.equals(myUserInfo.getUserSex())) {
            mSexTv.setText(getString(R.string.sex_female));
        } else {
            mSexTv.setText("");
        }
        mRegionTv.setText(myUserInfo.getUserRegion());
    }

    @Override
    public void initListener() {

    }
}