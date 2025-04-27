package com.hjq.demo.chat.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.bndg.smack.SmartCommHelper;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.callback.IConnectionListener;
import com.bndg.smack.callback.IFriendListCallback;
import com.bndg.smack.callback.ISmartCallback;
import com.bndg.smack.callback.IUserInfoCallback2;
import com.bndg.smack.constant.SmartConstants;
import com.bndg.smack.entity.SmartMessage;
import com.bndg.smack.enums.SmartConversationType;
import com.bndg.smack.model.SmartUserInfo;
import com.hjq.base.BaseDialog;
import com.hjq.base.FragmentPagerAdapter;
import com.hjq.demo.R;
import com.hjq.demo.app.AppApplication;
import com.hjq.demo.app.AppFragment;
import com.hjq.demo.chat.adapter.SmartConversationAdapter;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.ConversationDao;
import com.hjq.demo.chat.dao.DBManager;
import com.hjq.demo.chat.dao.MessageDao;
import com.hjq.demo.chat.dao.UserDao;
import com.hjq.demo.chat.entity.AvatarEntity;
import com.hjq.demo.chat.entity.ChatEvent;
import com.hjq.demo.chat.entity.ChatMessage;
import com.hjq.demo.chat.entity.ConversationInfo;
import com.hjq.demo.chat.entity.QrCodeContent;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.entity.enums.MessageStatus;
import com.hjq.demo.chat.fragment.ChatsFragment;
import com.hjq.demo.chat.fragment.ContactsFragment;
import com.hjq.demo.chat.fragment.DiscoverFragment;
import com.hjq.demo.chat.fragment.MeFragment;
import com.hjq.demo.chat.listener.ConnectionListener;
import com.hjq.demo.chat.listener.ContactCallback;
import com.hjq.demo.chat.listener.OnFragmentListener;
import com.hjq.demo.chat.manager.CallManager;
import com.hjq.demo.chat.manager.ChatMessageManager;
import com.hjq.demo.chat.manager.ContactManager;
import com.hjq.demo.chat.manager.MessageNotifycation;
import com.hjq.demo.chat.manager.NotificationCompatUtil;
import com.hjq.demo.chat.utils.AvatarGenerator;
import com.hjq.demo.chat.utils.BatteryHelper;
import com.hjq.demo.chat.utils.CommonUtil;
import com.hjq.demo.chat.utils.DeviceInfoUtil;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.other.DoubleClickHelper;
import com.hjq.demo.ui.dialog.MessageDialog;
import com.hjq.demo.utils.CheckUtil;
import com.hjq.demo.utils.JsonParser;
import com.hjq.demo.utils.Trace;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.rxjava.rxlife.RxLife;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * 主activity
 *
 * @author zhou
 */
public class MainActivity extends ChatBaseActivity implements
        OnFragmentListener, IConnectionListener {

    public static final int REQUEST_CODE_SCAN = 0;
    public static final int REQUEST_CODE_CAMERA = 1;
    public static final int REQUEST_CODE_LOCATION = 2;
    private static final String INTENT_KEY_IN_FRAGMENT_INDEX = "fragmentIndex";
    private static final String INTENT_KEY_IN_FRAGMENT_CLASS = "fragmentClass";
    private ViewPager mViewPager;
    private FragmentPagerAdapter<AppFragment<?>> mPagerAdapter;
    private ChatsFragment mChatsFragment;
    private ContactsFragment mContactsFragment;
    private DiscoverFragment mDiscoverFragment;
    private MeFragment mMeFragment;

    private ImageView[] mMainButtonIvs;
    private TextView[] mMainButtonTvs;
    private int mIndex;
    // 当前fragment的index
    private int mCurrentTabIndex;

    private TextView mUnreadNewMsgsNumTv;
    private TextView mUnreadNewFriendsNumTv;

    private ConcurrentHashMap<String, Runnable> updateRunnableMap = new ConcurrentHashMap<>();
    private String from_where;
    private String myUserId;
    private TextView tv_chats;
    private TextView tv_contacts;
    private TextView tv_me;

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }


    public static void start(Context context, String from_where) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(Constant.FROM_WHERE, from_where);
        context.startActivity(intent);
    }

    @Override
    public void initView() {
        Trace.d("initView: Mainactivity  >>>>>>> ");
        mViewPager = findViewById(R.id.vp_home_pager);
        mChatsFragment = ChatsFragment.newInstance();
        mContactsFragment = ContactsFragment.newInstance();
        mDiscoverFragment = DiscoverFragment.newInstance();
        mMeFragment = MeFragment.newInstance();
        mMainButtonIvs = new ImageView[4];
        mMainButtonIvs[0] = findViewById(R.id.iv_chats);
        mMainButtonIvs[1] = findViewById(R.id.iv_contacts);
        mMainButtonIvs[2] = findViewById(R.id.iv_discover);
        mMainButtonIvs[3] = findViewById(R.id.iv_me);

        mMainButtonIvs[0].setSelected(true);
        mMainButtonTvs = new TextView[4];
        tv_chats = findViewById(R.id.tv_chats);
        mMainButtonTvs[0] = tv_chats;
        tv_contacts = findViewById(R.id.tv_contacts);
        mMainButtonTvs[1] = tv_contacts;
        mMainButtonTvs[2] = findViewById(R.id.tv_discover);
        tv_me = findViewById(R.id.tv_me);
        mMainButtonTvs[3] = tv_me;
        mMainButtonTvs[0].setTextColor(getColor(R.color.navigation_text_selected));

        mUnreadNewMsgsNumTv = findViewById(R.id.unread_msg_number);
        mUnreadNewFriendsNumTv = findViewById(R.id.unread_address_number);
    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {
        Trace.w("initData: start >>>>>>> ");
        EventBus.getDefault().register(this);
        mPagerAdapter = new FragmentPagerAdapter<>(this);
        mPagerAdapter.addFragment(mChatsFragment);
        mPagerAdapter.addFragment(mContactsFragment);
        mPagerAdapter.addFragment(mDiscoverFragment);
        mPagerAdapter.addFragment(mMeFragment);
        mViewPager.setAdapter(mPagerAdapter);
        onNewIntent(getIntent());
        from_where = getIntent().getStringExtra(Constant.FROM_WHERE);
        myUserId = PreferencesUtil.getInstance().getUserId();
        getMyUserInfo();
        registerMessageReceiver();
        post(() -> {
            // 第二次启动弹出
            if (!PreferencesUtil.getInstance().isFirst()) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BatteryHelper.ignoreBatteryOptimization();
                    }
                }, 1000);
            } else {
                PreferencesUtil.getInstance().setFirst(false);
                // 首次启动发送系统消息 tigase需要创建一个系统用户 如微信团队
                User user = new User();
                user.setBelongAccount(myUserId);
                user.setUserType(Constant.USER_TYPE_WEIXIN);
                String appName = getString(R.string.app_name);
                user.setUserNickName(String.format(getString(R.string.team), appName));
                user.setUserId("kptx");
                UserDao.getInstance().saveOrUpdateContact(user);
                SmartMessage kptx = SmartMessage.createSendSingleMessage(CommonUtil.generateId(), "kptx", myUserInfo.getUserId(),
                        Constant.MSG_TYPE_SPANNABLE, String.format(getString(R.string.welcome_back_to), appName) +
                                getString(R.string.ensure_setting_hit) +
                                getString(R.string.enable_auto_start) +
                                getString(R.string.add_app_to_whitelist) +
                                getString(R.string.enable_power_manager));
                kptx.setFromNickname(appName);
                ChatMessage chatMessage = ChatMessageManager.getInstance().wrapperToChatMessage(kptx);
                MessageDao.getInstance().save(chatMessage);
                ConversationDao.getInstance().saveSingleConversation(kptx, false);
                ChatEvent event = new ChatEvent(ChatEvent.CONVERSATION_ITEM_CONTENT_UPDATE);
                event.obj = "kptx";
                EventBus.getDefault().post(event);

            }
        });
        // 检查通知权限
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!DeviceInfoUtil.areNotificationsEnabled(MainActivity.this)) {
                    new MessageDialog.Builder(MainActivity.this)
                            .setTitle(getString(R.string.tips))
                            .setMessage(R.string.notification_detected_prompt)
                            // 确定按钮文本
                            .setConfirm(getString(R.string.common_confirm))
                            // 设置 null 表示不显示取消按钮
                            .setCancel(getString(R.string.common_cancel))
                            // 设置点击按钮后不关闭对话框
                            //.setAutoDismiss(false)
                            .setListener(new MessageDialog.OnListener() {

                                @Override
                                public void onConfirm(BaseDialog dialog) {
                                    try {
                                        // 根据isOpened结果，判断是否需要提醒用户跳转AppInfo页面，去打开App通知权限
                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                                        //这种方案适用于 API 26, 即8.0（含8.0）以上可以用
                                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                                        intent.putExtra(Settings.EXTRA_CHANNEL_ID, getApplicationInfo().uid);

                                        //这种方案适用于 API21——25，即 5.0——7.1 之间的版本可以使用
                                        intent.putExtra("app_package", getPackageName());
                                        intent.putExtra("app_uid", getApplicationInfo().uid);
                                        startActivity(intent);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        // 出现异常则跳转到应用设置界面：锤子坚果3——OC105 API25
                                        Intent intent = new Intent();
                                        //下面这种方案是直接跳转到当前应用的设置界面。
                                        //https://blog.csdn.net/ysy950803/article/details/71910806
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    }
                                }

                                @Override
                                public void onCancel(BaseDialog dialog) {
                                }
                            })
                            .show();
                }
            }
        }, 8000);

        ConnectionListener.INSTANCE.addConnectionListener(this);
        mUnreadNewFriendsNumTv.post(new Runnable() {
            @Override
            public void run() {
                if (Constant.FROM_LOGIN.equals(from_where)) {
                    // 登录成功后主动更新好友列表
                    Trace.d("onServiceReady: 登录成功后主动更新好友列表");
                    updateContacts();
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onAuthenticated();
                        }
                    }, 500);
                } else {
                    // 刷新好友列表
                    SmartIMClient.getInstance().fetchMessages();
                    mContactsFragment.refreshFriendsList();
                }
                // 第一次全部刷新
                // 刷新会话列表
                mChatsFragment.refreshConversationList();
                // 刷新未读消息数量
                refreshUnreadMsgNum();
                // 刷新新好友申请未读数量
                mContactsFragment.refreshNewFriendsUnreadNum();
                // 刷新新好友申请未读数量
                refreshFriendRequestUnreadCount();
                // 把数据库中发送中的消息改为发送失败
                MessageDao.getInstance().updateSendingToFailed();
            }
        });
        Trace.w("initData: end >>>>>>> ");
        if (SmartCommHelper.getInstance().connectionIsLoading()) {
            showDialog();
        }
    }

    /**
     * 获取我的用户信息
     */
    private void getMyUserInfo() {
        SmartIMClient.getInstance().getSmartCommUserManager().getUserInfo(myUserId, new IUserInfoCallback2() {
            @Override
            public void onSuccess(SmartUserInfo userInfo) {
                // 获取用户的昵称
                String nickname = userInfo.getNickname();
                UserDao.getInstance().getUserById(MainActivity.this, myUserId, new ContactCallback() {
                    @Override
                    public void getUser(@Nullable User userById) {
                        if (userById != null) {
                            userById.setUserNickName(CheckUtil.getNotNullString(nickname));
                            DBManager.Companion.getInstance(MainActivity.this)
                                    .saveOrUpdateContact(userById)
                                    .to(RxLife.to(MainActivity.this))
                                    .subscribe(new CompletableObserver() {
                                        @Override
                                        public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                                        }

                                        @Override
                                        public void onComplete() {
                                            PreferencesUtil.getInstance().setUser(userById);
                                            AvatarGenerator.saveAvatarFileByUserInfo(userInfo, false);
                                            mMeFragment.refreshMyName();
                                            myUserInfo = PreferencesUtil.getInstance().getUser();
                                        }

                                        @Override
                                        public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

                                        }
                                    });
                        }
                    }
                });
            }

            @Override
            public void onFailed(int code, String desc) {
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ChatEvent event) {
        if (mChatsFragment == null) {
            return;
        }
        if (SmartConstants.ACCOUNT_STATUS.equals(event.getWhat())) {
            mMeFragment.refreshMyStatus(String.valueOf(event.obj));
        } else if (ChatEvent.REFUSE_CALL_MSG.equals(event.getWhat())
                || ChatEvent.LEAVE_CALL_MSG.equals(event.getWhat())) {
            // 发送拒绝接听 任何界面都需要处理
            Bundle bundle = event.bundle;
            if (null != bundle) {
                String eventConversationId = bundle.getString(Constant.CONVERSATION_ID);
                String callId = bundle.getString(Constant.CALL_ID);
                String creatorInfo = bundle.getString(Constant.GROUP_CALL_CREATOR_INFO);
                boolean isGroup = bundle.getBoolean(SmartConversationType.GROUP.name());
                CallManager.getInstance().handleAnswer(eventConversationId, !isGroup,
                        callId, Constant.MSG_TYPE_CALL_REFUSE, myUserInfo.getUserId(), CheckUtil.getNotNullString(creatorInfo));
            }
        } else if (ChatEvent.SEND_ACCEPT_CALL.equals(event.getWhat())) {
            // 发送同意接听 任何界面都需要处理
            Bundle bundle = event.bundle;
            if (null != bundle) {
                String eventConversationId = bundle.getString(Constant.CONVERSATION_ID);
                String callId = bundle.getString(Constant.CALL_ID);
                String creatorInfo = bundle.getString(Constant.GROUP_CALL_CREATOR_INFO);
                boolean isGroup = bundle.getBoolean(SmartConversationType.GROUP.name());
                CallManager.getInstance().handleAnswer(eventConversationId, !isGroup,
                        callId, Constant.MSG_TYPE_ACCEPT_CALL, myUserInfo.getUserId(), CheckUtil.getNotNullString(creatorInfo));
            }
        } else if (ChatEvent.CONVERSATION_REMOVED.equals(event.getWhat())) {
            refreshUnreadMsgNum();
            mChatsFragment.conversationRemove(String.valueOf(event.obj));
        } else if (ChatEvent.CONVERSATION_ITEM_CONTENT_UPDATE.equals(event.getWhat())) {
            ArrayList<String> stringArrayList;
            if (event.bundle != null) {
                stringArrayList = event.bundle.getStringArrayList(SmartConversationAdapter.PAYLOADS);
            } else {
                stringArrayList = new ArrayList<>();
                stringArrayList.add(SmartConversationAdapter.REFRESH_CONTENT);
            }
            String conversationId = String.valueOf(event.obj);
            if (stringArrayList.contains(SmartConversationAdapter.REFRESH_AVATAR)
                    || stringArrayList.contains(SmartConversationAdapter.REFRESH_TITLE)) {
                // 头像&ait 及时刷新
                Trace.w("onEvent: title 头像&ait 及时刷新" + stringArrayList);
                mChatsFragment.conversationUpdate(conversationId, stringArrayList);
                return;
            }
            Runnable runnable = updateRunnableMap.get(conversationId);
            if (runnable != null) {
                removeCallbacks(runnable);
            }
            Runnable refreshRunnable = new Runnable() {
                @Override
                public void run() {
                    refreshUnreadMsgNum();
                    Trace.w("refreshUnreadMsgNum Runnable 刷新底部未读数和会话item未读 " + conversationId);
                    mChatsFragment.conversationUpdate(conversationId, stringArrayList);
                    updateRunnableMap.remove(conversationId);
                }
            };
            updateRunnableMap.put(conversationId, refreshRunnable);
            postDelayed(refreshRunnable, 200);
        } else if (ChatEvent.REFRESH_USER_AVATAR.equals(event.getWhat())) {
            Trace.d("onEvent: REFRESH_USER_AVATAR 刷新单聊会话头像 刷新联系人头像",
                    String.valueOf(event.obj));
            if (PreferencesUtil.getInstance().getUserId().equals(event.obj)) {
                mMeFragment.updateMyAvatar();
            }
            ArrayList<String> refreshList = new ArrayList<>();
            refreshList.add(SmartConversationAdapter.REFRESH_AVATAR);
            mChatsFragment.conversationUpdate(String.valueOf(event.obj), refreshList);
            mContactsFragment.updateUserAvatar(String.valueOf(event.obj));
        } else if (ChatEvent.CONTACT_REMOVED.equals(event.getWhat())) {
            String contactId = String.valueOf(event.obj);
            mContactsFragment.notifyContactRemoved(contactId);
        } else if (ChatEvent.REFRESH_CONTACT.equals(event.getWhat())) {
            // 更改备注后刷新好友
            Bundle bundle = event.bundle;
            if (null != bundle) {
                String userId = bundle.getString(Constant.CONTACT_ID);
                boolean isFriendRequest = bundle.getBoolean(Constant.FRIEND_ADDED, false);
                if (isFriendRequest) {
                    refreshFriendRequestUnreadCount();
                    mContactsFragment.refreshNewFriendsUnreadNum();
                }
                mContactsFragment.refreshFriend(userId);
            }
        }
    }

    /**
     * 更新好友
     */
    private void updateContacts() {
        showDialog();
        SmartIMClient.getInstance().getFriendshipManager().getFriendList(new IFriendListCallback() {
            @Override
            public void onSuccess(Set<SmartUserInfo> rosterEntries) {
                for (SmartUserInfo entry : rosterEntries) {
                    // 处理每个好友条目，例如打印或显示在UI上
                    Trace.d(
                            "type is .." + entry.getSubscribeStatus(),
                            "好友" + entry.getNickname());
                    updateContact(entry, false);
                }
                mContactsFragment.refreshFriendsList();
                hideDialog();
            }

            @Override
            public void onFailed(int code, String desc) {
                hideDialog();
                toast(desc);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        switchFragment(mPagerAdapter.getFragmentIndex(getSerializable(INTENT_KEY_IN_FRAGMENT_CLASS)));
        if(intent.getStringExtra(Constant.LANGUAGE) != null) {
            postDelayed(() ->{
                tv_chats.setText(R.string.tab_chats);
                tv_contacts.setText(R.string.tab_contacts);
                tv_me.setText(R.string.tab_me);
                mChatsFragment.changeUI();
                mContactsFragment.changeUI();
                mMeFragment.changeUI();
            }, 500);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // 保存当前 Fragment 索引位置
        outState.putInt(INTENT_KEY_IN_FRAGMENT_INDEX, mViewPager.getCurrentItem());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // 恢复当前 Fragment 索引位置
        switchFragment(savedInstanceState.getInt(INTENT_KEY_IN_FRAGMENT_INDEX));
    }

    private void switchFragment(int fragmentIndex) {
        if (fragmentIndex == -1) {
            return;
        }

        switch (fragmentIndex) {
            case 0:
            case 1:
            case 2:
            case 3:
                mViewPager.setCurrentItem(fragmentIndex);
                break;
            default:
                break;
        }
    }

    public void onTabClicked(View view) {
        switch (view.getId()) {
            case R.id.rl_chats:
                if(DoubleClickHelper.isOnDoubleClick(500)) {
                    mChatsFragment.scrollToTop();
                }
                // 刷新会话显示时间
//                mChatsFragment.refreshConversationList();
                mIndex = 0;
                break;
            case R.id.rl_contacts:
                mIndex = 1;
                break;
            case R.id.rl_discover:
                mIndex = 2;
                break;
            case R.id.rl_me:
                mIndex = 3;
                break;
        }
        mViewPager.setCurrentItem(mIndex);
        mMainButtonIvs[mCurrentTabIndex].setSelected(false);
        // 把当前tab设为选中状态
        mMainButtonIvs[mIndex].setSelected(true);
        mMainButtonTvs[mCurrentTabIndex].setTextColor(getColor(R.color.black_deep));
        mMainButtonTvs[mIndex].setTextColor(getColor(R.color.navigation_text_selected));
        mCurrentTabIndex = mIndex;
    }

    @Override
    protected void onResume() {
        super.onResume();
        long time = System.currentTimeMillis() - AppApplication.startTime;
        Trace.d("onResume: Mainactivity- " + time);
        long aLong = SPUtils.getInstance().getLong(Constant.REFRESH_CONVERSATION_LIST_TIME, System.currentTimeMillis());
        // 判断是否是今天 每天刷新一次
        if (!TimeUtils.isToday(aLong)) {
            mChatsFragment.refreshConversationList();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private MessageReceiver mMessageReceiver;

    public void registerMessageReceiver() {
        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(Constant.RECEIVED_FRIEND_APPLY);
        filter.addAction(Constant.UPLOAD_FILE_SUCCESS);
        filter.addAction(Constant.UPLOAD_FILE_FAILED);
        filter.addAction(Constant.FRIEND_ADDED);
        filter.addAction(Constant.FRIEND_DELETED);
        filter.addAction(Constant.FRIEND_STATUS);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);
    }

    @Override
    public void onAuthenticated() {
        Trace.w("重新连接后 onConnected: from service");
        hideDialog();
        if (mChatsFragment != null) {
            mChatsFragment.updateHeaderErrorView(false);
        }
        MessageNotifycation.getInstance().updateForeground(MainActivity.this, getString(R.string.connection_normally));
    }

    /**
     * 服务器连接中 是显示对话框吗？
     */
    @Override
    public void onServerConnecting() {
        showDialog();
    }

    @Override
    public void onServerConnectFailed(String desc) {
        if (mChatsFragment != null) {
            mChatsFragment.updateHeaderErrorView(true);
        }
        toast(desc);
        hideDialog();
        MessageNotifycation.getInstance().updateForeground(MainActivity.this, getString(R.string.connection_closed));
    }

    @Override
    public void onServerConnected() {
        Trace.w("连接服务器成功 onConnected: >>");
        if (mChatsFragment != null) {
            mChatsFragment.updateHeaderErrorView(false);
        }
        MessageNotifycation.getInstance().updateForeground(MainActivity.this, getString(R.string.connection_normally));
    }

    @Override
    public void onLoginFailed(int code, String desc) {
        toast(desc);
        hideDialog();
        if (code == SmartConstants.Error.ACCOUNT_DISABLED) {
            SettingActivity.logout(MainActivity.this);
        }
    }

    @Override
    public void onChatDataLoading() {
        if (mChatsFragment != null) {
            mChatsFragment.showChatDataLoading(true);
        }
    }

    @Override
    public void onChatDataLoaded() {
        if (mChatsFragment != null) {
            mChatsFragment.showChatDataLoading(false);
        }
    }

    @Override
    public void onKickedOffline() {
        ActivityManager.getInstance().finishAllActivities(MainActivity.class);
        NotificationCompatUtil.Companion.cancelAll(this);
        SmartIMClient.getInstance().logout(new ISmartCallback() {
            @Override
            public void onSuccess() {
                Trace.d("accept: ------------------------logout");

            }

            @Override
            public void onFailed(int code, String desc) {
                // 跳转至登录页
            }
        });
        PreferencesUtil.getInstance().logOut();
        Trace.d("onKickedOffline: ----------------------");
        post(new Runnable() {
            @Override
            public void run() {
                new MessageDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.tips))
                        .setMessage(getString(R.string.account_logged_in_elsewhere))
                        // 设置 null 表示不显示取消按钮
                        // 确定按钮文本
                        .setConfirm(getString(R.string.common_confirm))
                        .setCancel(null)
                        .setCancelable(false)
                        .setListener(new MessageDialog.OnListener() {
                            @Override
                            public void onConfirm(BaseDialog dialog) {
                                startActivity(SplashActivity.class);
                                ActivityManager.getInstance().finishAllActivities(SplashActivity.class);
                            }
                        })
                        .show();
            }
        });
    }

    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constant.RECEIVED_FRIEND_APPLY.equals(intent.getAction())) {
                // 新的好友申请 刷新新的好友申请未读消息
                Trace.d("onReceive: 新的好友申请 刷新新的好友申请未读消息");
                refreshFriendRequestUnreadCount();
                mContactsFragment.refreshNewFriendsUnreadNum();
            } else if (Constant.FRIEND_ADDED.equals(intent.getAction())) {
                // 把好友添加到联系人界面
                SmartUserInfo userInfo = intent.getParcelableExtra(Constant.FRIEND_USER_INFO);
                if (userInfo != null) {
                    updateContact(userInfo, true);
                    Trace.d("onReceive:updateContacts 对方同意了我的好友请求" + userInfo.getUserId(),
                            userInfo.getNickname());
                }
            } else if (Constant.FRIEND_DELETED.equals(intent.getAction())) {
                String contactId = intent.getStringExtra(Constant.CONTACT_ID);
                Trace.d("FRIEND_DELETED: " + contactId);
                ContactManager.getInstance().unbindFriendship(MainActivity.this, contactId);
            } else if (Constant.FRIEND_STATUS.equals(intent.getAction())) {
                String contactId = intent.getStringExtra(Constant.CONTACT_ID);
                boolean isOnLine = intent.getBooleanExtra(Constant.ONLINE_STATUS, false);
                mContactsFragment.refreshFriendStatus(contactId, isOnLine);
            } else if (Constant.UPLOAD_FILE_SUCCESS.equals(intent.getAction())) {
                // 交给chatMessageManager处理
                ChatMessageManager.getInstance().uploadFileSuccess(intent);
                EventBus.getDefault().post(new ChatEvent(ChatEvent.UPLOAD_FILE_SUCCESS));
            } else if (Constant.UPLOAD_FILE_FAILED.equals(intent.getAction())) {
                toast(getString(R.string.send_file_failed));
                String originID = intent.getStringExtra(Constant.MESSAGE_ORIGIN_ID);
                MessageDao.getInstance().getMessageByOriginId(MainActivity.this, originID, new MessageDao.MessageDaoCallback() {
                    @Override
                    public void getMessageByOriginId(ChatMessage chatMessage) {
                        chatMessage.setStatus(MessageStatus.SEND_FAIL.value());
                        MessageDao.getInstance().save(chatMessage);
                    }
                });
            }
        }
    }

    /**
     * 刷新好友
     *
     * @param contact
     * @param isNewAdded
     */
    private void updateContact(SmartUserInfo contact, boolean isNewAdded) {
        ContactManager.getInstance().addContact(MainActivity.this, contact, isNewAdded);
        DBManager.Companion.getInstance(this)
                .getAvatarByUserId(contact.getUserId())
                .to(RxLife.to(this))
                .subscribe(new Consumer<List<AvatarEntity>>() {
                    @Override
                    public void accept(List<AvatarEntity> avatarEntities) throws Throwable {
                        if (avatarEntities.isEmpty() || TextUtils.isEmpty(avatarEntities.get(0).getAvatarLocalPath())) {
                            // 获取好友的头像
                            Trace.d("accept: 获取好友的头像");
                            SmartIMClient.getInstance().getSmartCommUserManager().requestAvatarByUserId(contact.getUserId());
                        }
                    }
                });
    }

    public void refreshUnreadMsgNum() {
        // 发送的消息不用刷新
        DBManager.Companion.getInstance(this)
                .getConversationListByUserId(myUserInfo.getUserId())
                .to(RxLife.to(this))
                .subscribe(list -> {
                    int unreadNum = 0;
                    for (ConversationInfo conversationInfo : list) {
                        unreadNum += conversationInfo.getUnReadNum();
                    }
                    Trace.w(myUserInfo.getUserId(), "会话总数: " + list.size(), " 未读总数设置 " + unreadNum);
                    if (unreadNum > 0) {
                        mUnreadNewMsgsNumTv.setVisibility(View.VISIBLE);
                        mUnreadNewMsgsNumTv.setText(String.valueOf(unreadNum));
                    } else {
                        mUnreadNewMsgsNumTv.setVisibility(View.GONE);
                    }
                });
    }

    private void refreshFriendRequestUnreadCount() {
        DBManager.Companion.getInstance(getContext())
                .getUnReadFriendApplyCount()
                .to(RxLife.to(this))
                .subscribe(list -> {
                    if (!list.isEmpty()) {
                        mUnreadNewFriendsNumTv.setVisibility(View.VISIBLE);
                        mUnreadNewFriendsNumTv.setText(String.valueOf(list.size()));
                    } else {
                        mUnreadNewFriendsNumTv.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Trace.d("onDestroy: " + mMessageReceiver);
        if (mMessageReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        }
        ChatMessageManager.getInstance().cleanup();
        ConnectionListener.INSTANCE.removeConnectionListener(this);
        EventBus.getDefault().unregister(this);
    }


    @Override
    public int checkSelfPermission(String permission) {
        return super.checkSelfPermission(permission);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null) {
            return;
        }
        if (requestCode == Constant.REQUEST_CODE_SCAN_ONE) {
            HmsScan obj = data.getParcelableExtra(ScanUtil.RESULT);
            if (obj != null) {
                Trace.d("onActivityResult: ===" + obj.getOriginalValue());
                String scanResult = obj.getOriginalValue();
                if (TextUtils.isEmpty(scanResult)) {
                    return;
                }
                if (scanResult.startsWith(QrCodeContent.START)) {
                    String result = scanResult.substring(scanResult.indexOf(",") + 1);
                    QrCodeContent qrCodeContent = JsonParser.deserializeByJson(result, QrCodeContent.class);
                    if (qrCodeContent != null && QrCodeContent.QR_CODE_TYPE_USER.equals(qrCodeContent.getType())) {
                        String imUserId = qrCodeContent.getUserid();
                        if (myUserInfo.getUserId().equals(imUserId)) {
                            // 是自己
                            Intent intent = new Intent(MainActivity.this, UserInfoMyActivity.class);
                            startActivity(intent);
                        } else {
                            UserDao.getInstance().getUserById(MainActivity.this,
                                    imUserId, new ContactCallback() {
                                        @Override
                                        public void getUser(@Nullable User userById) {
                                            if (userById != null && userById.isFriend()) {
                                                // 好友，进入用户详情页
                                                UserInfoActivity.start(MainActivity.this, userById.getUserId());
                                            } else {
                                                // 陌生人，进入陌生人详情页
                                                processStrangerInfo(imUserId, qrCodeContent.getNickname(), qrCodeContent.getPhone(), qrCodeContent.getAliasusername());
                                            }
                                        }
                                    });
                        }
                    }
                } else {
                    ScanResultActivity.start(getActivity(), scanResult);
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // 判断是否需要执行返回桌面的操作
            Trace.d("onKeyDown: 返回桌面-=--=-=");
            moveTaskToBack(true); // 将任务移到后台，应用看起来就像回到了桌面
            return true; // 消耗掉这个事件，防止默认行为（即退出当前Activity）
        }
        return super.onKeyDown(keyCode, event); // 对其他按键事件保持默认行为
    }

    /**
     * 扫码后-处理陌生人信息
     *
     * @param jid
     * @param tNickname
     * @param phoneData
     * @param accountData
     */
    public void processStrangerInfo(String jid, String tNickname, String phoneData, String accountData) {
        showDialog();
        SmartIMClient.getInstance().getSmartCommUserManager().getUserInfo(jid, new IUserInfoCallback2() {
            @Override
            public void onSuccess(SmartUserInfo userInfo) {
                hideDialog();
                String nickName = userInfo.getNickname();
                Trace.d("onClick: 查找到===" + nickName);
                if (TextUtils.isEmpty(nickName) && TextUtils.isEmpty(tNickname)) {
                    toast(getString(R.string.user_not_found));
                    return;
                }
                AvatarGenerator.saveAvatarFileByUserInfo(userInfo, false);
                // 陌生人，进入陌生人详情页
                User userById = new User();
                userById.setBelongAccount(PreferencesUtil.getInstance().getUserId());
                userById.setUserNickName(nickName);
                userById.setUserId(jid);
                userById.setUserPhone(phoneData);
                userById.setUserAccount(accountData);
                DBManager.Companion.getInstance(MainActivity.this)
                        .saveContact(userById)
                        .to(RxLife.to(MainActivity.this))
                        .subscribe(new CompletableObserver() {
                            @Override
                            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                            }

                            @Override
                            public void onComplete() {
                                UserInfoActivity.start(getContext(), jid, Constant.CONTACTS_FROM_WX_ID);
                            }

                            @Override
                            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

                            }
                        });
            }

            @Override
            public void onFailed(int code, String desc) {
                hideDialog();
                toast(desc);
            }
        });
    }
}