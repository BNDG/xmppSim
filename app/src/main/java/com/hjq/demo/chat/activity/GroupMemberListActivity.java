package com.hjq.demo.chat.activity;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.hjq.base.BaseActivity;
import com.hjq.demo.R;
import com.hjq.demo.aop.Log;
import com.hjq.demo.chat.adapter.SmartFriendsAdapter;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.DBManager;
import com.hjq.demo.chat.entity.GroupMember;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.chat.widget.LQRRecyclerView;
import com.hjq.demo.chat.widget.QuickIndexBar;
import com.rxjava.rxlife.RxLife;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.callback.IChatRoomCallback;
import com.bndg.smack.model.SmartUserInfo;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * @author r
 * @date 2024/7/8
 * @description 群成员列表-选择群成员（移除或发起群通话）
 */
public class GroupMemberListActivity extends ChatBaseActivity {
    private static final String MEMBER_ACCOUNTS = "MEMBER_ACCOUNTS";
    private static final String MEMBER_REAL_JIDS = "MEMBER_REAL_JIDS";
    private SmartFriendsAdapter mAdapter;

    @BindView(R.id.rvContacts)
    LQRRecyclerView mRvContacts;
    @BindView(R.id.qib)
    QuickIndexBar mQib;
    @BindView(R.id.tvLetter)
    TextView mTvLetter;
    private TextView tv_title;
    private TextView tv_right;
    private List<User> pickedUsers;
    private ArrayList<String> checkedUserIdList = new ArrayList<>();
    private boolean isCallType;
    private List<String> excludeJidList;

    @Log
    public static void start(BaseActivity activity, String param, OnRemoveListener listener) {
        Intent intent = new Intent(activity, GroupMemberListActivity.class);
        intent.putExtra(Constant.GROUP_ID, param);
        activity.startActivityForResult(intent, ((resultCode, data) -> {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    listener.removeSuccess(data.getStringArrayListExtra(MEMBER_ACCOUNTS));
                }
            }
        }));
    }

    @Log
    public static void start(BaseActivity activity, String param, String accounts, OnRemoveListener listener) {
        Intent intent = new Intent(activity, GroupMemberListActivity.class);
        intent.putExtra(Constant.GROUP_ID, param);
        intent.putExtra(Constant.EXCLUDE_JIDS, accounts);
        activity.startActivityForResult(intent, ((resultCode, data) -> {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    listener.checkedAccounts(data.getStringArrayListExtra(MEMBER_REAL_JIDS));
                }
            }
        }));
    }

    @Log
    public static void start(BaseActivity activity, String param, CreateChatActivity.CreateChatsListener listener) {
        Intent intent = new Intent(activity, GroupMemberListActivity.class);
        intent.putExtra(Constant.GROUP_ID, param);
        intent.putExtra(Constant.CALL_TYPE, true);
        activity.startActivityForResult(intent, ((resultCode, data) -> {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    listener.getCheckedContacts(data.getStringArrayListExtra(CreateChatActivity.USER_IDS),
                            data.getStringExtra(CreateChatActivity.USER_NICKNAMES));
                }
            }
        }));
    }

    @Override
    public void initListener() {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.group_member_list_activity;
    }

    @Override
    protected void initView() {
        tv_title = findViewById(R.id.tv_title);
        tv_right = findViewById(R.id.tv_right);
        tv_title.setText(getString(R.string.chat_members));
        tv_right.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initData() {
        isCallType = getIntent().getBooleanExtra(Constant.CALL_TYPE, false);
        String groupId = getIntent().getStringExtra(Constant.GROUP_ID);
        String excludeJids = getIntent().getStringExtra(Constant.EXCLUDE_JIDS);
        mAdapter = new SmartFriendsAdapter(new ArrayList<>());
        if (excludeJids != null) {
            String[] split = excludeJids.split(",");
            excludeJidList = Arrays.asList(split);
        }
        if (isCallType || excludeJids != null) {
            tv_right.setText(getString(R.string.confirm));
        } else {
            tv_right.setText(getString(R.string.delete));
        }
        mAdapter.setShowHeader(false);
        mAdapter.setFromWhere(SmartFriendsAdapter.FROM_GROUP_SETTING);
        mRvContacts.setAdapter(mAdapter);
        mAdapter.setPickedListener(new SmartFriendsAdapter.PickedListener() {
            @Override
            public void getPickedUsers(List<User> pickedUser) {
                pickedUsers = pickedUser;
                String rightText = getString(R.string.delete);
                if (isCallType) {
                    rightText = getString(R.string.confirm);
                }
                if (!pickedUsers.isEmpty()) {
                    tv_right.setEnabled(true);
                    tv_right.setText(rightText + "(" + pickedUsers.size() + ")");
                } else {
                    tv_right.setEnabled(false);
                    tv_right.setText(rightText);
                }
            }
        });
        tv_right.setOnClickListener(v -> {
            ArrayList<SmartUserInfo> smartUserInfos = new ArrayList<>();
            ArrayList<String> userAccounts = new ArrayList<>();
            ArrayList<String> userJids = new ArrayList<>();
            for (User user : pickedUsers) {
                // userAccount 用来移除群成员
                SmartUserInfo userInfo = new SmartUserInfo();
                userInfo.setMemberAccount(user.getUserAccount());
                userInfo.setUserId(user.getUserId());
                smartUserInfos.add(userInfo);
                userAccounts.add(user.getUserAccount());
                userJids.add(user.getUserId());
            }
            showDialog();
            if (isCallType) {
                hideDialog();
                checkedUserIdList.clear();
                for (User user : pickedUsers) {
                    checkedUserIdList.add(user.getUserId());
                }
                setResult(RESULT_OK, new Intent()
                        .putStringArrayListExtra(CreateChatActivity.USER_IDS, checkedUserIdList)
                        .putStringArrayListExtra(CreateChatActivity.USER_NICKNAMES, userAccounts));
                finish();
            } else {
                if (excludeJids != null) {
                    Intent data = new Intent();
                    data.putStringArrayListExtra(GroupMemberListActivity.MEMBER_REAL_JIDS, userJids);
                    setResult(RESULT_OK, data);
                    finish();
                } else {
                    SmartIMClient.getInstance().getSmartCommChatRoomManager().kickGroupMember(smartUserInfos, groupId, new IChatRoomCallback() {
                        @Override
                        public void kickedSuccess() {
                            for (SmartUserInfo userInfo : smartUserInfos) {
                                GroupMember groupMember = new GroupMember();
                                groupMember.setGroupId(groupId);
                                groupMember.setMemberAccount(userInfo.getMemberAccount());
                                groupMember.setBelongAccount(PreferencesUtil.getInstance().getUserId());
                                DBManager.Companion.getInstance(getApplicationContext())
                                        .kickedMember(groupMember)
                                        .subscribe();
                            }
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    hideDialog();
                                    Intent data = new Intent();
                                    data.putStringArrayListExtra(GroupMemberListActivity.MEMBER_ACCOUNTS, userAccounts);
                                    setResult(RESULT_OK, data);
                                    finish();
                                }
                            });
                        }

                        @Override
                        public void kickedFailed() {
                            hideDialog();
                        }
                    });
                }
            }

        });
        mQib.setOnLetterUpdateListener(new QuickIndexBar.OnLetterUpdateListener() {
            @Override
            public void onLetterUpdate(String letter) {
                //显示对话框
                showLetter(letter);
                //滑动到第一个对应字母开头的联系人
                if ("↑".equalsIgnoreCase(letter)) {
                    mRvContacts.moveToPosition(0);
                } else if ("☆".equalsIgnoreCase(letter)) {
                    mRvContacts.moveToPosition(0);
                } else {
                    List<User> data = mAdapter.getData();
                    for (int i = 0; i < data.size(); i++) {
                        User friend = data.get(i);
                        String c = friend.getUserHeader().charAt(0) + "";
                        if (c.equalsIgnoreCase(letter)) {
                            mRvContacts.moveToPosition(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onLetterCancel() {
                //隐藏对话框
                hideLetter();
            }
        });
        DBManager.Companion.getInstance(this)
                .getGroupMemberByGroupId(groupId)
                .to(RxLife.to(this))
                .subscribe(new Consumer<List<GroupMember>>() {
                    @Override
                    public void accept(List<GroupMember> groupMembers) throws Throwable {
                        ArrayList<User> data = new ArrayList<>();
                        // 是因为用的user adapter所以才把member转成user
                        for (GroupMember member : groupMembers) {
                            User user = new User();
                            String memberUserid = member.getMemberOriginId();
                            if (PreferencesUtil.getInstance().getUserId().equals(memberUserid)) {
                                // 是自己 跳过
                                continue;
                            }
                            if (excludeJidList != null && excludeJidList.contains(member.getMemberOriginId())) {
                                continue;
                            }
                            user.setUserId(memberUserid);
                            user.setUserAccount(member.getMemberAccount());
                            user.setUserContactAlias(member.getMemberName());
                            user.setSelected(false);
                            data.add(user);
                        }
                        mAdapter.setList(data);
                    }
                });


    }


    private void showLetter(String letter) {
        mTvLetter.setVisibility(View.VISIBLE);
        mTvLetter.setText(letter);
    }


    private void hideLetter() {
        mTvLetter.setVisibility(View.GONE);
    }

    /**
     * 注册监听
     */
    public interface OnRemoveListener {
        default void removeSuccess(ArrayList<String> stringArrayExtra) {
        }

        default void checkedAccounts(ArrayList<String> stringArrayListExtra) {
        }
    }
}
