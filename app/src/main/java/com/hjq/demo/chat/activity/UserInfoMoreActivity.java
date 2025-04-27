package com.hjq.demo.chat.activity;

import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hjq.demo.R;
import com.hjq.demo.chat.dao.UserDao;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.listener.ContactCallback;

import butterknife.BindView;

/**
 * "用户信息"-"更多信息"
 * 共同群聊
 * 来源
 * @author zhou
 */
public class UserInfoMoreActivity extends ChatBaseActivity {

    @BindView(R.id.tv_title)
    TextView mTitleTv;

    @BindView(R.id.tv_from)
    TextView mFromTv;

    String mContactId;

    @Override
    public int getLayoutId() {
        return R.layout.activity_user_info_more;
    }

    @Override
    public void initView() {
        mTitleTv.setText(R.string.more_info);
        setTitleStrokeWidth(mTitleTv);
    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {
        mContactId = getIntent().getStringExtra("contactId");
    }

}