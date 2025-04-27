package com.hjq.demo.chat.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hjq.demo.R;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.DBManager;
import com.hjq.demo.chat.dao.MessageDao;
import com.hjq.demo.chat.dao.UserDao;
import com.hjq.demo.chat.entity.ChatEvent;
import com.hjq.demo.chat.entity.ChatMessage;
import com.hjq.demo.chat.entity.FriendApply;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.listener.SimpleResultCallback;
import com.hjq.demo.chat.manager.ChatMessageManager;
import com.hjq.demo.chat.utils.CommonUtil;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.chat.widget.LoadingDialog;
import com.hjq.demo.utils.Trace;
import com.rxjava.rxlife.RxLife;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.callback.ISmartCallback;
import com.bndg.smack.enums.SmartContentType;
import com.bndg.smack.enums.SmartConversationType;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * 通过朋友验证
 *
 * @author 验证页面
 */
public class NewFriendsAcceptConfirmActivity extends ChatBaseActivity {

    // 好友备注
    @BindView(R.id.et_remark)
    EditText mRemarkEt;

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

    @BindView(R.id.tv_right)
    TextView mAcceptTv;


    private String mApplyId;
    private LoadingDialog mDialog;

    private String mRelaPrivacy;
    private String mRelaHideMyPosts;
    private String mRelaHideHisPosts;
    private FriendApply mFriendApply;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_new_friends_accept_confirm;
    }

    public void initView() {
        mAcceptTv.setVisibility(View.VISIBLE);
        mAcceptTv.setEnabled(true);
        mAcceptTv.setText(R.string.complete);
        mDialog = new LoadingDialog(this);
        mRelaPrivacy = Constant.PRIVACY_CHATS_MOMENTS_WERUN_ETC;
        mRelaHideMyPosts = Constant.SHOW_MY_POSTS;
        mRelaHideHisPosts = Constant.SHOW_HIS_POSTS;
        mApplyId = getIntent().getStringExtra("applyId");
        DBManager.Companion.getInstance(this)
                .getFriendApplyByApplyId(mApplyId)
                .to(RxLife.to(this))
                .subscribe(new Consumer<List<FriendApply>>() {
                    @Override
                    public void accept(List<FriendApply> friendApplies) throws Throwable {
                        if (!friendApplies.isEmpty()) {
                            mFriendApply = friendApplies.get(0);
                            mRemarkEt.setText(mFriendApply.getFriendNickname());
                        }
                    }
                });
        mRemarkEt = findViewById(R.id.et_remark);

    }

    @Override
    protected void initData() {

    }

    @OnClick({R.id.rl_chats_moments_werun_etc, R.id.rl_chats_only, R.id.tv_right})
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
                acceptXmppFriendApply();
                break;
            default:
                break;
        }
    }

    /**
     * 接受好友申请
     */
    private void acceptXmppFriendApply() {
        // 同意请求
        String targetUserId = mFriendApply.getFriendUserId();
        SmartIMClient.getInstance()
                .getFriendshipManager()
                .acceptFriendReq(targetUserId, mFriendApply.getFriendNickname(), new ISmartCallback() {
                    @Override
                    public void onSuccess() {
                        mDialog.dismiss();
                        Trace.d(targetUserId + ">> acceptXmppFriendApply: ok");
                        mFriendApply.setStatus(Constant.FRIEND_APPLY_STATUS_ACCEPT);
                        // 刷新好友请求状态
                        DBManager.Companion.getInstance(NewFriendsAcceptConfirmActivity.this)
                                .saveFriendApply(mFriendApply)
                                .to(RxLife.to(NewFriendsAcceptConfirmActivity.this))
                                .subscribe();
                        User user = new User();
                        user.setBelongAccount(PreferencesUtil.getInstance().getUserId());
                        user.setUserId(targetUserId);
                        user.setUserNickName(mFriendApply.getFriendNickname());
                        // 应该是还没有头像
                        user.setUserHeader(CommonUtil.generateUserHeader(mFriendApply.getFriendNickname()));
                        user.setUserSex(mFriendApply.getFriendUserSex());
                        user.setUserType(Constant.USER_TYPE_REG);
                        user.setIsFriend(Constant.IS_FRIEND);
                        user.setIsBlocked(Constant.CONTACT_IS_NOT_BLOCKED);
                        UserDao.getInstance().saveOrUpdateContact(user, new SimpleResultCallback() {
                            @Override
                            public void onResult(boolean isSuccess) {
                                ChatMessage chatMessage = ChatMessage.createTextMsg(targetUserId,
                                        SmartConversationType.SINGLE.name(),
                                        SmartContentType.TEXT,
                                        getString(R.string.start_chat_message));
                                MessageDao.getInstance().saveAndSetLastTimeStamp(chatMessage, callback -> {
                                    ChatMessageManager.getInstance().sendSingleMessage(chatMessage.getConversationId(),
                                            user.getUserNickName(),
                                            chatMessage.getMessageContent(), chatMessage.getMessageType(),
                                            new ArrayList<>(), -1, chatMessage.getOriginId());
                                    toast(R.string.sented);
                                    // 通知已经处理了好友请求
                                    ChatEvent event = new ChatEvent(ChatEvent.REFRESH_CONTACT);
                                    Bundle bundle = new Bundle();
                                    bundle.putString(Constant.CONTACT_ID, user.getUserId());
                                    bundle.putBoolean(Constant.FRIEND_ADDED, true);
                                    event.bundle = bundle;
                                    EventBus.getDefault().post(event);
                                    UserInfoActivity.start(NewFriendsAcceptConfirmActivity.this, targetUserId);
                                    finish();
                                });
                            }
                        });
                    }

                    @Override
                    public void onFailed(int code, String desc) {
                        mDialog.dismiss();
                        Trace.d("acceptXmppFriendApply: not ok");
                        toast(desc);
                    }
                });

    }

    @Override
    public void initListener() {

    }
}
