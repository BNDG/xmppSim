package com.hjq.demo.chat.activity;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.KeyboardUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.demo.R;
import com.hjq.demo.chat.adapter.SmartFriendsAdapter;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.UserDao;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.listener.ContactCallback;
import com.hjq.demo.chat.utils.AvatarGenerator;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.chat.widget.LQRRecyclerView;
import com.hjq.demo.chat.widget.LoadingDialog;
import com.hjq.demo.utils.Trace;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

import com.bndg.smack.SmartCommHelper;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.callback.IUserInfoCallback2;
import com.bndg.smack.model.SmartUserInfo;


/**
 * 搜索好友
 *
 * @author zhou
 */
public class AddFriendsBySearchActivity extends ChatBaseActivity {

    @BindView(R.id.et_search)
    EditText mSearchEt;

    @BindView(R.id.rl_search)
    RelativeLayout mSearchRl;

    @BindView(R.id.tv_search)
    TextView mSearchTv;

    @BindView(R.id.iv_clear)
    ImageView mClearIv;

    LoadingDialog mDialog;
    private SmartFriendsAdapter mAdapter;
    private LQRRecyclerView mRvContacts;

    @Override
    public int getLayoutId() {
        return R.layout.activity_add_friends_by_search;
    }

    @Override
    public void initView() {
        mRvContacts = findViewById(R.id.rv_list);
    }

    @Override
    public void initListener() {
        mSearchEt.addTextChangedListener(new TextChange());
    }

    @Override
    public void initData() {

        mDialog = new LoadingDialog(AddFriendsBySearchActivity.this);
        mAdapter = new SmartFriendsAdapter(new ArrayList<>());
        mAdapter.setShowHeader(false);
        mRvContacts.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int position) {
                mDialog.show();
                User item = (User) baseQuickAdapter.getItem(position);
                if (item != null) {
                    searchXmppUserByJid(item.getUserId(), item.getUserNickName(),
                            item.getUserPhone(), item.getUserAccount());
                }
            }
        });
        // 初始化弹出软键盘
        KeyboardUtils.showSoftInput(mSearchEt);
    }

    @OnClick({R.id.rl_search, R.id.iv_clear})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_search:
                mDialog.setMessage(getString(R.string.searching_for_user));
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();
                KeyboardUtils.hideSoftInput(mSearchEt);
                String keyword = mSearchEt.getText().toString().trim();
                searchXmppUserByJid(keyword, "", "", "");
                break;
            case R.id.iv_clear:
                mSearchEt.setText("");
                break;
        }
    }
    /**
     * 通过xmpp jid搜索用户 要求对方有名片
     *
     * @param account
     * @param tNickname
     */
    private void searchXmppUserByJid(String account, String tNickname, String phoneData, String accountData) {
        String imUserId;
        if (!account.contains("@")) {
            imUserId = SmartCommHelper.getInstance().getUserIdByAccount(account);
        } else {
            imUserId = account;
        }
        SmartIMClient.getInstance().getSmartCommUserManager().getUserInfo(imUserId, new IUserInfoCallback2() {
            @Override
            public void onSuccess(SmartUserInfo userInfo) {
                mDialog.dismiss();
                String nickName = userInfo.getNickname();
                Trace.d(imUserId,
                        "onClick: 查找" + nickName);
                if (TextUtils.isEmpty(nickName) && TextUtils.isEmpty(tNickname)) {
                    toast(getString(R.string.user_not_found));
                    return;
                }
                if (imUserId.equals(myUserInfo.getUserId())) {
                    // 是自己
                    Intent intent = new Intent(AddFriendsBySearchActivity.this, UserInfoMyActivity.class);
                    startActivity(intent);
                } else {
                    UserDao.getInstance().getUserById(AddFriendsBySearchActivity.this, imUserId, new ContactCallback() {
                        @Override
                        public void getUser(User user) {
                            if (null != user && user.isFriend()) {
                                AvatarGenerator.saveAvatarFileByUserInfo(userInfo, false);
                                user.setUserPhone(phoneData);
                                user.setUserAccount(accountData);
                                UserDao.getInstance().saveOrUpdateContact(user);
                                // 好友，进入用户详情页
                                UserInfoActivity.start(AddFriendsBySearchActivity.this, user.getUserId());
                            } else {
                                // 陌生人，进入陌生人详情页
                                User userById = new User();
                                userById.setBelongAccount(PreferencesUtil.getInstance().getUserId());
                                userById.setUserNickName(nickName);
                                userById.setUserId(imUserId);
                                userById.setUserPhone(phoneData);
                                userById.setUserAccount(accountData);
                                UserDao.getInstance().saveOrUpdateContact(userById);
                                UserInfoActivity.start(AddFriendsBySearchActivity.this, imUserId, Constant.CONTACTS_FROM_WX_ID);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailed(int code, String desc) {
                mDialog.dismiss();
                toast(desc);
            }
        });
    }


    class TextChange implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            boolean searchHasText = mSearchEt.getText().toString().length() > 0;
            if (searchHasText) {
                mSearchRl.setVisibility(View.VISIBLE);
                mSearchTv.setText(mSearchEt.getText().toString().trim());

                mClearIv.setVisibility(View.VISIBLE);
            } else {
                mSearchRl.setVisibility(View.GONE);
                mSearchTv.setText("");

                mClearIv.setVisibility(View.GONE);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

}