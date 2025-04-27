package com.hjq.demo.chat.activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hjq.demo.R;
import com.hjq.demo.aop.Log;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.DBManager;
import com.hjq.demo.chat.dao.UserDao;
import com.hjq.demo.chat.entity.AvatarEntity;
import com.hjq.demo.chat.entity.CardInfoBean;
import com.hjq.demo.chat.entity.ChatEvent;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.listener.ContactCallback;
import com.hjq.demo.chat.listener.SimpleResultCallback;
import com.hjq.demo.chat.widget.ConfirmDialog;
import com.hjq.demo.chat.widget.LoadingDialog;
import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.utils.JsonParser;
import com.hjq.widget.layout.SettingBar;
import com.hjq.widget.view.SwitchButton;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.callback.IFriendListCallback;
import com.bndg.smack.callback.ISmartCallback;
import com.bndg.smack.model.SmartUserInfo;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * 资料设置
 * 拉黑 删除 星标 备注
 *
 * @author zhou
 */
public class UserSettingActivity extends ChatBaseActivity {

    private static final int REQUEST_CODE_ADD_TO_HOME_SCREEN = 1;

    @BindView(R.id.tv_title)
    TextView mTitleTv;

    // 把他推荐给朋友
    @BindView(R.id.rl_share_contact)
    RelativeLayout mShareContactRl;

    // 添加到桌面
    @BindView(R.id.rl_add_to_home_screen)
    RelativeLayout mAddToHomeScreenRl;
    @BindView(R.id.rl_edit_contact)
    SettingBar rl_edit_contact;

    // 加入黑名单
    @BindView(R.id.rl_block)
    View mBlockRl;

    @BindView(R.id.sb_add_star)
    SwitchButton sb_add_star;
    @BindView(R.id.rl_start)
    View rl_start;

    @BindView(R.id.sb_block)
    SwitchButton sb_block;
    // 投诉
    @BindView(R.id.rl_report)
    View mReportRl;

    // 删除
    @BindView(R.id.rl_delete)
    RelativeLayout mDeleteRl;

    String mContactId;
    String mIsFriend;
    User mContact;

    LoadingDialog mDialog;
    private String mContactNickname;

    @Log
    public static void start(Context context, String contactId, String contactName, String friendType) {
        Intent intent = new Intent(context, UserSettingActivity.class);
        intent.putExtra(Constant.CONTACT_ID, contactId);
        intent.putExtra(Constant.CONTACT_NICK_NAME, contactName);
        intent.putExtra(Constant.FRIEND_TYPE, friendType);
        context.startActivity(intent);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_user_setting;
    }

    @Override
    public void initView() {
        mTitleTv.setText(R.string.user_settings);
        setTitleStrokeWidth(mTitleTv);
    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {
        mIsFriend = getIntent().getStringExtra(Constant.FRIEND_TYPE);
        mContactId = getIntent().getStringExtra(Constant.CONTACT_ID);
        mContactNickname = getIntent().getStringExtra(Constant.CONTACT_NICK_NAME);
        mDialog = new LoadingDialog(UserSettingActivity.this);
        if (Constant.IS_NOT_FRIEND.equals(mIsFriend)) {
            // 非好友
            mShareContactRl.setVisibility(View.GONE);
            mAddToHomeScreenRl.setVisibility(View.GONE);
            rl_start.setVisibility(View.GONE);
            mReportRl.setVisibility(View.GONE);
            mDeleteRl.setVisibility(View.GONE);
        } else {
            // 好友
        }
        SmartIMClient.getInstance().getSmartCommUserManager().getBlockList(new IFriendListCallback() {
            @Override
            public void onSuccess(Set<SmartUserInfo> entries) {
                for (SmartUserInfo entry : entries) {
                    if (entry.getUserId().equals(mContactId)) {
                        sb_block.setChecked(true);
                    }
                }
            }

            @Override
            public void onFailed(int code, String desc) {

            }
        });
    }

    private void loadData(User user) {
        // 备注
        rl_edit_contact.setRightText(user.getUserContactAlias());
        // 是否星标好友
        if (Constant.CONTACT_IS_STARRED.equals(user.getIsStarred())) {
            // 是
            sb_add_star.setChecked(true);
        } else {
            // 否
            sb_add_star.setChecked(false);
        }
        // 是否加入黑名单
        if (Constant.CONTACT_IS_BLOCKED.equals(user.getIsBlocked())) {
            // 是
            sb_block.setChecked(true);
        } else {
            // 否
            sb_block.setChecked(false);
        }
        sb_add_star.setOnClickListener(v -> {
            if (sb_add_star.isChecked()) {
                setContactStarred(Constant.CONTACT_IS_STARRED);
            } else {
                setContactStarred(Constant.CONTACT_IS_NOT_STARRED);
            }
        });
        sb_block.setOnClickListener(v -> {
            if (sb_block.isChecked()) {
                setContactBlocked(Constant.CONTACT_IS_BLOCKED);
            } else {
                setContactBlocked(Constant.CONTACT_IS_NOT_BLOCKED);
            }
        });
    }

    @OnClick({R.id.rl_edit_contact, R.id.rl_add_to_home_screen,
            R.id.sb_block, R.id.sb_add_star, R.id.rl_delete, R.id.sb_share_contact})
    public void onClick(View view) {
        Intent intent;
        ConfirmDialog confirmDialog;
        switch (view.getId()) {
            case R.id.rl_edit_contact:
                // 设置备注和标签
                intent = new Intent(UserSettingActivity.this, EditContactActivity.class);
                intent.putExtra("contactId", mContactId);
                intent.putExtra("isFriend", mIsFriend);
                startActivity(intent);
                break;
            case R.id.rl_add_to_home_screen:
                // todo
                // 添加到桌面
                /*confirmDialog = new ConfirmDialog(UserSettingActivity.this, "已尝试添加到桌面",
                        "若添加失败，请前往系统设置，为kptk打开\"创建桌面快捷方式\"的权限。",
                        "了解详情", getString(R.string.cancel), getColor(R.color.navy_blue));
                // 点击空白处消失
                confirmDialog.show();
                intent = new Intent(UserSettingActivity.this, ChatActivity.class);
                intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                intent.putExtra("targetType", SmartConversationType.SINGLE.name());
                intent.putExtra("contactId", mContact.getUserId());
                intent.putExtra("contactNickName", mContact.getUserNickName());
                intent.putExtra("contactAvatar", mContact.getUserAvatar());
                new Thread(() -> {
                    Bitmap bitmap = getBitmapFromAvatarUrl(mContact.getUserAvatar());
                    addShortcut(UserSettingActivity.this, mContact.getUserNickName(), bitmap, intent);
                }).start();*/
                break;
            case R.id.rl_delete:
                // 删除联系人
                confirmDialog = new ConfirmDialog(UserSettingActivity.this, getString(R.string.delete_contacts),
                        String.format(getString(R.string.delete_contact_tips), mContact.getUserNickName()),
                        getString(R.string.delete), getString(R.string.cancel));
                confirmDialog.setOnDialogClickListener(new ConfirmDialog.OnDialogClickListener() {
                    @Override
                    public void onOkClick() {
                        confirmDialog.dismiss();
                        mDialog.setMessage(getString(R.string.please_wait));
                        mDialog.show();
                        deleteXmppContact(mContactId);
                    }

                    @Override
                    public void onCancelClick() {
                        confirmDialog.dismiss();
                    }
                });
                // 点击空白处消失
                confirmDialog.setCancelable(true);
                confirmDialog.show();
                break;
            case R.id.sb_share_contact:
                // 分享联系人
                CardInfoBean infoBean = new CardInfoBean();
                infoBean.setUserId(mContactId);
                infoBean.setNickName(mContactNickname);
                SelectConversationActivity.startExtensionType(UserSettingActivity.this, Constant.MSG_TYPE_CARD_INFO,
                        String.format(getString(R.string.recommend_contact), mContactNickname, mContactId), "", JsonParser.serializeToJson(infoBean));
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserDao.getInstance().getUserById(UserSettingActivity.this, mContactId, new ContactCallback() {
            @Override
            public void getUser(@Nullable User userById) {
                if (userById == null) {
                    return;
                }
                mContact = userById;
                loadData(mContact);
            }
        });
    }

    /**
     * 添加桌面图标快捷方式
     *
     * @param activity     Activity对象
     * @param name         快捷方式名称
     * @param icon         快捷方式图标
     * @param actionIntent 快捷方式图标点击动作
     */
    public void addShortcut(Activity activity, String name, Bitmap icon, Intent actionIntent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // 创建快捷方式的intent广播
            Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            // 添加快捷名称
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
            // 快捷图标是允许重复(不一定有效)
            shortcut.putExtra("duplicate", false);
            // 快捷图标
            // 使用Bitmap对象模式
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);
            // 添加携带的下次启动要用的Intent信息
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, actionIntent);
            // 发送广播
            activity.sendBroadcast(shortcut);
        } else {
            ShortcutManager shortcutManager = (ShortcutManager) activity.getSystemService(Context.SHORTCUT_SERVICE);
            if (null == shortcutManager) {
                // 创建快捷方式失败
                return;
            }
            ShortcutInfo shortcutInfo = new ShortcutInfo.Builder(activity, name)
                    .setShortLabel(name)
                    .setIcon(Icon.createWithBitmap(icon))
                    .setIntent(actionIntent)
                    .setLongLabel(name)
                    .build();
            shortcutManager.requestPinShortcut(shortcutInfo, PendingIntent.getActivity(activity,
                    REQUEST_CODE_ADD_TO_HOME_SCREEN, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT).getIntentSender());
        }
    }

    /**
     * 设置或取消星标朋友
     *
     * @param isStarred 是否星标好友
     */
    private void setContactStarred(final String isStarred) {
        if (Constant.CONTACT_IS_STARRED.equals(isStarred)) {
            sb_add_star.setChecked(true);
            mContact.setIsStarred(Constant.CONTACT_IS_STARRED);
            toast(R.string.already_followed);
        } else {
            sb_add_star.setChecked(false);
            mContact.setIsStarred(Constant.CONTACT_IS_NOT_STARRED);
            toast(R.string.unfollowed);
        }
        UserDao.getInstance().saveOrUpdateContact(mContact);

    }

    /**
     * 设置或取消加入黑名单
     *
     * @param action 是否加入黑名单
     */
    private void setContactBlocked(final String action) {
        if (Constant.CONTACT_IS_BLOCKED.equals(action)) {
            // 拉黑提示
            final ConfirmDialog confirmDialog = new ConfirmDialog(UserSettingActivity.this, getString(R.string.Block),
                    getString(R.string.block_content),
                    getString(R.string.confirm), getString(R.string.cancel), getColor(R.color.navy_blue));
            confirmDialog.setOnDialogClickListener(new ConfirmDialog.OnDialogClickListener() {
                @Override
                public void onOkClick() {
                    SmartIMClient.getInstance().getFriendshipManager().blockContact(mContactId, new ISmartCallback() {
                        @Override
                        public void onSuccess() {
                            confirmDialog.dismiss();
                            mContact.setIsBlocked(Constant.CONTACT_IS_BLOCKED);
                            UserDao.getInstance().saveOrUpdateContact(mContact);
                        }

                        @Override
                        public void onFailed(int code, String desc) {
                            sb_block.setChecked(false);
                            confirmDialog.dismiss();
                            toast(desc);
                        }
                    });
                }

                @Override
                public void onCancelClick() {
                    sb_block.setChecked(false);
                }
            });
            // 点击空白处消失
            confirmDialog.show();
        } else {
            SmartIMClient.getInstance().getFriendshipManager().unblockContacts(mContactId, new ISmartCallback() {
                @Override
                public void onSuccess() {
                    mContact.setIsBlocked(Constant.CONTACT_IS_NOT_BLOCKED);
                    UserDao.getInstance().saveOrUpdateContact(mContact, new SimpleResultCallback() {
                        @Override
                        public void onResult(boolean isSuccess) {
                            ChatEvent event = new ChatEvent(ChatEvent.REFRESH_CONTACT);
                            Bundle bundle = new Bundle();
                            bundle.putString(Constant.CONTACT_ID, mContact.getUserId());
                            bundle.putBoolean(Constant.FRIEND_ADDED, false);
                            event.bundle = bundle;
                            EventBus.getDefault().post(event);
                        }
                    });
                }

                @Override
                public void onFailed(int code, String desc) {
                    sb_block.setChecked(true);
                }
            });
        }

    }

    /**
     * 删除联系人
     *
     * @param userId    用户ID
     * @param contactId 联系人ID
     */
    private void deleteContact(final String userId, final String contactId) {
        mDialog.dismiss();
        // 清除本地记录
        // 通讯录删除
        UserDao.getInstance().getUserById(UserSettingActivity.this, contactId, new ContactCallback() {
            @Override
            public void getUser(@Nullable User user) {
                if (null != user) {
                    UserDao.getInstance().deleteUser(user);
                }
                // 朋友圈清除记录
                // 删除会话
                finish();
                // 跳转到首页第二个tab并refresh
            }
        });
    }

    /**
     * 删除好友
     *
     * @param contactId
     */
    private void deleteXmppContact(String contactId) {
        SmartIMClient.getInstance().getFriendshipManager()
                .deleteFriend(contactId, new ISmartCallback() {
                    @Override
                    public void onSuccess() {
                        mDialog.dismiss();
                        // 朋友圈清除记录
                        // 删除会话
                        DBManager.Companion.getInstance(UserSettingActivity.this)
                                .deleteConversation(myUserInfo.getUserId(), contactId)
                                .subscribe(new CompletableObserver() {
                                    @Override
                                    public void onSubscribe(@NonNull Disposable d) {

                                    }

                                    @Override
                                    public void onComplete() {
                                        ChatEvent event = new ChatEvent(ChatEvent.CONVERSATION_REMOVED);
                                        event.obj = contactId;
                                        EventBus.getDefault().post(event);
                                    }

                                    @Override
                                    public void onError(@NonNull Throwable e) {

                                    }
                                });
                        // 通知联系人界面更新
                        ChatEvent event1 = new ChatEvent(ChatEvent.CONTACT_REMOVED);
                        event1.obj = contactId;
                        EventBus.getDefault().post(event1);
                        // 清除本地记录
                        // 通讯录删除
                        UserDao.getInstance().getUserById(UserSettingActivity.this, contactId, new ContactCallback() {
                            @Override
                            public void getUser(@Nullable User user) {
                                if (null != user) {
                                    user.setIsFriend(Constant.IS_NOT_FRIEND);
                                    UserDao.getInstance().saveOrUpdateContact(user);
                                }
                                ActivityManager.getInstance().finishActivity(UserInfoActivity.class);
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onFailed(int code, String desc) {
                        toast(desc);
                    }
                });
    }

}
