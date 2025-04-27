package com.hjq.demo.chat.activity;

import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.hjq.demo.R;
import com.hjq.demo.chat.adapter.BlockedContactAdapter;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.UserDao;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.listener.ContactCallback;

import java.util.List;

import butterknife.BindView;

/**
 * 通讯录黑名单
 *
 * @author zhou
 */
public class BlockedContactActivity extends ChatBaseActivity {

    @BindView(R.id.tv_title)
    TextView mTitleTv;

    @BindView(R.id.lv_blocked_contact)
    ListView mBlockedContactLv;

    BlockedContactAdapter mBlockedContactAdapter;

    @Override
    public int getLayoutId() {
        return R.layout.activity_blocked_contact;
    }

    @Override
    public void initView() {
        setTitleStrokeWidth(mTitleTv);
        mTitleTv.setText(R.string.mobile_blocked_list);
    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {
        UserDao.getInstance().getAllBlockedUserList(BlockedContactActivity.this, new ContactCallback() {
            @Override
            public void getAllBlockedUserList(@NonNull List<User> users) {
                mBlockedContactAdapter = new BlockedContactAdapter(BlockedContactActivity.this, users);
                mBlockedContactLv.setAdapter(mBlockedContactAdapter);
            }
        });


        mBlockedContactLv.setOnItemClickListener((parent, view, position, id) -> {
            User blockedContact = mBlockedContactAdapter.getItem(position);
            if (blockedContact.isFriend()) {
                UserInfoActivity.start(BlockedContactActivity.this, blockedContact.getUserId());
            } else {
                UserInfoActivity.start(BlockedContactActivity.this,
                        blockedContact.getUserId(),
                        Constant.CONTACTS_FROM_WX_ID);
            }
        });

    }

}