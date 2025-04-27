package com.hjq.demo.chat.activity;

import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.hjq.demo.R;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.listener.ContactCallback;

import butterknife.BindView;
import butterknife.OnClick;
import com.bndg.smack.enums.SmartConversationType;

/**
 * 文件传输助手
 *
 * @author zhou
 */
public class UserInfoFileHelperActivity extends ChatBaseActivity {

    @BindView(R.id.rl_send_message)
    RelativeLayout mSendMessageRl;

    private String mContactId;
    private String userNickName = "";


    @Override
    protected int getLayoutId() {
        return R.layout.activity_user_info_file_helper;
    }

    public void initView() {
        mContactId = getIntent().getStringExtra("userId");
        userDao.getUserById(UserInfoFileHelperActivity.this, mContactId, new ContactCallback() {
            @Override
            public void getUser(@Nullable User userById) {
                if (userById != null) {
                    userNickName = userById.getUserNickName();
                    setTitle(userNickName);
                }
            }
        });
    }

    @Override
    protected void initData() {

    }


    @OnClick({R.id.rl_send_message})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_send_message:
                ChatActivity.start(this, SmartConversationType.SINGLE.name(),
                        mContactId,
                        userNickName);
                break;
        }
    }

    @Override
    public void initListener() {

    }
}
