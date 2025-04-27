package com.hjq.demo.chat.activity;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hjq.demo.R;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.UserDao;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.listener.ContactCallback;
import com.hjq.demo.chat.widget.LoadingDialog;

import butterknife.BindView;
import butterknife.OnClick;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.callback.IFriendListener;

/**
 * 申请添加对方为朋友
 *
 * @author zhou
 */
public class NewFriendsApplyConfirmActivity extends ChatBaseActivity implements View.OnClickListener {

    // 申请备注
    @BindView(R.id.et_apply_remark)
    EditText mApplyRemarkEt;
    @BindView(R.id.tv_right)
    TextView tv_right;

    // 联系人备注
    @BindView(R.id.et_contact_alias)
    EditText mContactAliasEt;

    // 所有权限
    @BindView(R.id.rl_chats_moments_werun_etc)
    RelativeLayout mChatsMomentsWerunEtcRl;

    @BindView(R.id.iv_chats_moments_werun_etc)
    ImageView mChatsMomentsWerunEtcIv;

    // 仅聊天
    @BindView(R.id.rl_chats_only)
    RelativeLayout mChatsOnlyRl;

    @BindView(R.id.iv_chats_only)
    ImageView mChatsOnlyIv;

    private String mContactId;
    private String mFrom;

    private LoadingDialog mDialog;

    private String mRelaPrivacy;
    private String mRelaHideMyPosts;
    private String mRelaHideHisPosts;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_new_friends_apply_confirm;
    }

    public void initView() {
        tv_right.setVisibility(View.VISIBLE);
        tv_right.setEnabled(true);
        tv_right.setText(R.string.send);
        mDialog = new LoadingDialog(this);
        mRelaPrivacy = Constant.PRIVACY_CHATS_MOMENTS_WERUN_ETC;
        mRelaHideMyPosts = Constant.SHOW_MY_POSTS;
        mRelaHideHisPosts = Constant.SHOW_HIS_POSTS;

        mFrom = getIntent().getStringExtra("from");
        mContactId = getIntent().getStringExtra("contactId");

        UserDao.getInstance().getUserById(NewFriendsApplyConfirmActivity.this, mContactId, new ContactCallback() {
            @Override
            public void getUser(@Nullable User mContact) {
                if (null != mContact) {
                    mApplyRemarkEt.setText(getString(R.string.i_am) + myUserInfo.getUserNickName());
                    if (TextUtils.isEmpty(mContact.getUserContactAlias())) {
                        mContactAliasEt.setText(mContact.getUserNickName());
                    } else {
                        mContactAliasEt.setText(mContact.getUserContactAlias());
                    }
                }
            }
        });

    }

    @Override
    protected void initData() {

    }

    @OnClick({R.id.rl_chats_moments_werun_etc, R.id.rl_chats_only,
            R.id.tv_right})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_chats_moments_werun_etc:
                mRelaPrivacy = Constant.PRIVACY_CHATS_MOMENTS_WERUN_ETC;

                mChatsMomentsWerunEtcIv.setVisibility(View.VISIBLE);
                mChatsOnlyIv.setVisibility(View.GONE);

                break;
            case R.id.rl_chats_only:
                mRelaPrivacy = Constant.PRIVACY_CHATS_ONLY;

                mChatsMomentsWerunEtcIv.setVisibility(View.GONE);
                mChatsOnlyIv.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_right:
                String applyRemark = mApplyRemarkEt.getText().toString();
                String relaContactAlias = mContactAliasEt.getText().toString();
                addXmppFriendApply(mContactId, applyRemark, relaContactAlias);
                break;
            default:
                break;
        }
    }

    /**
     * 发送好友申请
     */
    private void addXmppFriendApply(String mContactId, String applyRemark, String relaContactAlias) {
        mDialog.dismiss();
        SmartIMClient.getInstance().getFriendshipManager().addFriend(mContactId, applyRemark,
                relaContactAlias, new IFriendListener() {
            @Override
            public void onSendRequestSuccess() {
                // 应该是发送好友请求成功
                toast(getString(R.string.sented));
                // 在这里执行你的成功处理逻辑
                finish();
            }

            @Override
            public void onSendRequestFailed() {
                // 添加好友失败
                // 在这里执行你的失败处理逻辑
                toast(getString(R.string.sent_failed));
            }
        });
    }

    /**
     * 发送好友申请
     *
     * @param applyRemark      申请备注
     * @param fromUserId       请求人用户ID
     * @param toUserId         接收人用户ID
     * @param relaContactFrom  好友来源
     * @param relaContactAlias 联系人备注
     * @param relaPrivacy      朋友权限 "0":聊天、朋友圈、运动  "1":仅聊天
     * @param relaHideMyPosts  朋友圈和视频动态 "0":可以看我 "1":不让他看我
     * @param relaHideHisPosts 朋友圈和视频动态 "0":可以看他 "1":不看他
     */
    private void addFriendApply(String applyRemark, String fromUserId, String toUserId, String relaContactFrom, String relaContactAlias,
                                String relaPrivacy, String relaHideMyPosts, String relaHideHisPosts) {

        finish();
    }

    @Override
    public void initListener() {

    }
}
