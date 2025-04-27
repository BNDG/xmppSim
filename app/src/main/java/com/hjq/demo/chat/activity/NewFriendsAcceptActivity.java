package com.hjq.demo.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hjq.demo.R;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.DBManager;
import com.hjq.demo.chat.dao.UserDao;
import com.hjq.demo.chat.entity.FriendApply;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.listener.ContactCallback;
import com.hjq.demo.chat.utils.AvatarGenerator;
import com.hjq.demo.chat.widget.LoadingDialog;
import com.rxjava.rxlife.RxLife;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * 好友申请详情
 *
 * @author 可以前往验证页面
 */
public class NewFriendsAcceptActivity extends ChatBaseActivity {
    @BindView(R.id.ll_nick_name)
    LinearLayout mNickNameLl;

    @BindView(R.id.tv_nick_name)
    TextView mNickNameTv;
    @BindView(R.id.iv_more)
    View iv_more;

    @BindView(R.id.tv_name)
    TextView mNameTv;

    @BindView(R.id.sdv_avatar)
    ImageView mAvatarSdv;

    @BindView(R.id.iv_sex)
    ImageView mSexIv;

    @BindView(R.id.tv_from)
    TextView mFromTv;

    FriendApply mFriendApply;
    LoadingDialog mDialog;
    String mContactId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_new_friends_accept;
    }

    public void initView() {
        iv_more.setVisibility(View.VISIBLE);
        mDialog = new LoadingDialog(this);
        final String applyId = getIntent().getStringExtra("applyId");
        DBManager.Companion.getInstance(this)
                .getFriendApplyByApplyId(applyId)
                .to(RxLife.to(this))
                .subscribe(new Consumer<List<FriendApply>>() {
                    @Override
                    public void accept(List<FriendApply> friendApplies) throws Throwable {
                        if (!friendApplies.isEmpty()) {
                            mFriendApply = friendApplies.get(0);
                            mContactId = mFriendApply.getFriendUserId();
                            UserDao.getInstance().getUserById(NewFriendsAcceptActivity.this, mContactId, new ContactCallback() {
                                @Override
                                public void getUser(@Nullable User userById) {
                                    if (userById == null) {
                                        return;
                                    }
                                    loadData(userById);
                                }
                            });
                        }
                    }
                });
    }

    @Override
    protected void initData() {

    }

    @OnClick({R.id.iv_more, R.id.rl_go_confirm})
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.iv_more:
                UserSettingActivity.start(NewFriendsAcceptActivity.this, mContactId, mFriendApply.getFriendNickname(), Constant.IS_NOT_FRIEND);
                break;
            case R.id.rl_go_confirm:
                intent = new Intent(NewFriendsAcceptActivity.this, NewFriendsAcceptConfirmActivity.class);
                intent.putExtra("applyId", mFriendApply.getApplyId());
                startActivity(intent);
                break;
        }
    }


    private void loadData(User contact) {
        if (!TextUtils.isEmpty(contact.getUserContactAlias())) {
            mNameTv.setText(contact.getUserContactAlias());
            mNickNameLl.setVisibility(View.VISIBLE);
            mNickNameTv.setText(getString(R.string.nick_name_label) + contact.getUserNickName());
        } else {
            mNickNameLl.setVisibility(View.GONE);
            mNameTv.setText(mFriendApply.getFriendNickname());
        }

        AvatarGenerator.loadAvatar(this, contact.getUserId(), contact.getUserNickName(),
                mAvatarSdv, true);
        // 性别
        if (Constant.USER_SEX_MALE.equals(contact.getUserSex())) {
            mSexIv.setImageResource(R.drawable.icon_sex_male);
        } else if (Constant.USER_SEX_FEMALE.equals(contact.getUserSex())) {
            mSexIv.setImageResource(R.drawable.icon_sex_female);
        } else {
            mSexIv.setVisibility(View.GONE);
        }

        // 来源
        mFromTv.setText(getString(R.string.from_search_account));
        if (false) {
            // 渲染朋友圈图片
            List<String> circlePhotoList = null;
            try {
                if (null == circlePhotoList) {
                    circlePhotoList = new ArrayList<>();
                }
            } catch (Exception e) {
                circlePhotoList = new ArrayList<>();
            }
            switch (circlePhotoList.size()) {
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void initListener() {

    }
}
