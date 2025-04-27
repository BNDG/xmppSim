package com.hjq.demo.chat.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.hjq.base.BaseDialog;
import com.hjq.demo.R;
import com.hjq.demo.aop.Log;
import com.hjq.demo.chat.adapter.GroupMemberAdapter;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.DBManager;
import com.hjq.demo.chat.dao.MessageDao;
import com.hjq.demo.chat.dao.UserDao;
import com.hjq.demo.chat.entity.ChatEvent;
import com.hjq.demo.chat.entity.ConversationInfo;
import com.hjq.demo.chat.entity.GroupMember;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.listener.ContactCallback;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.chat.widget.ConfirmDialog;
import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.ui.dialog.InputDialog;
import com.hjq.demo.utils.Trace;
import com.hjq.widget.layout.SettingBar;
import com.hjq.widget.view.SwitchButton;
import com.rxjava.rxlife.RxLife;
import com.tencent.mmkv.MMKV;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import com.bndg.smack.SmartCommHelper;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.callback.IChatRoomCallback;
import com.bndg.smack.callback.IGroupMemberCallback;
import com.bndg.smack.enums.SmartConversationType;
import com.bndg.smack.model.SmartUserInfo;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * 群聊设置
 *
 * @author zhou
 * @description 群成员20人列表 群名称 群信息
 */
public class ChatGroupSettingActivity extends ChatBaseActivity implements View.OnClickListener {
    private String groupId;

    private TextView mMemberNumTv;
    private RecyclerView rv_grid;
    private boolean isBlock = false;
    // 清空聊天记录
    private View tv_all_members;
    private GroupMemberAdapter mAdapter;
    private List<GroupMember> groupMemberInfoList;
    private String groupName;
    private boolean available;
    private int maxShowCount = 19;
    private boolean isOwner;
    private SwitchButton sb_mute;
    private SettingBar sbGroupName;
    private SettingBar sbMyGroupNick;

    @Log
    public static void start(Context context, String groupId, String groupName) {
        Intent intent = new Intent(context, ChatGroupSettingActivity.class);
        intent.putExtra(Constant.GROUP_ID, groupId);
        intent.putExtra(Constant.GROUP_NAME, groupName);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_group_chat_setting;
    }

    public void initView() {
        mMemberNumTv = findViewById(R.id.tv_left);
        mMemberNumTv.setVisibility(View.VISIBLE);
        mMemberNumTv.setOnLongClickListener(v -> {
            findViewById(R.id.bt_update).setVisibility(View.VISIBLE);
            findViewById(R.id.tv_info).setVisibility(View.VISIBLE);
            return true;
        });
        rv_grid = findViewById(R.id.rv_grid);
        tv_all_members = findViewById(R.id.tv_all_members);
        sbGroupName = findViewById(R.id.sb_group_name);
        sbMyGroupNick = findViewById(R.id.my_group_nick);
        sb_mute = findViewById(R.id.sb_mute);
        groupId = getIntent().getStringExtra(Constant.GROUP_ID);
        groupName = getIntent().getStringExtra(Constant.GROUP_NAME);
        sb_mute.setChecked(MMKV.defaultMMKV().getBoolean(groupId + Constant.MUTE_KEY, false));
        sb_mute.setOnCheckedChangeListener((button, isChecked) -> {
            MMKV.defaultMMKV().putBoolean(groupId + Constant.MUTE_KEY, isChecked);
        });
        DBManager.Companion.getInstance(this)
                .getConversationByConversationId(myUserInfo.getUserId(), groupId)
                .to(RxLife.to(this))
                .subscribe(conversationInfoList -> {
                    ConversationInfo conversationInfo;
                    if (conversationInfoList.isEmpty()) {
                        conversationInfo = null;
                    } else {
                        conversationInfo = conversationInfoList.get(0);
                    }
                    if (conversationInfo != null) {
                        sbGroupName.setRightText(conversationInfo.getConversationTitle());
                        available = conversationInfo.isAvailable();
                    }
                });
        mAdapter = new GroupMemberAdapter(new ArrayList<>());
        mAdapter.setOnItemClickListener((baseQuickAdapter, view, i) -> {
            GroupMember item = (GroupMember) baseQuickAdapter.getItem(i);
            if (item != null) {
                String memberRealUserId = item.getMemberRealUserId();
                // 如果有realUserId 进入个人页面
                Trace.d("realJid -> " + memberRealUserId + " \n account -> " + item.getMemberName());
                if (!TextUtils.isEmpty(memberRealUserId)) {
                    if (memberRealUserId.equals(myUserInfo.getUserId())) {
                        // 是自己
                        Intent intent = new Intent(ChatGroupSettingActivity.this, UserInfoMyActivity.class);
                        startActivity(intent);
                    } else {
                        UserDao.getInstance().getUserById(ChatGroupSettingActivity.this, memberRealUserId, new ContactCallback() {
                            @Override
                            public void getUser(@Nullable User userById) {
                                if (userById != null && userById.isFriend()) {
                                    // 好友，进入用户详情页
                                    UserInfoActivity.start(ChatGroupSettingActivity.this, memberRealUserId);
                                } else {
                                    // 陌生人，进入陌生人详情页
                                    userById = new User();
                                    userById.setBelongAccount(PreferencesUtil.getInstance().getUserId());
                                    userById.setUserNickName(item.getMemberName());
                                    userById.setUserId(memberRealUserId);
                                    UserDao.getInstance().saveOrUpdateContact(userById);
                                    UserInfoActivity.start(ChatGroupSettingActivity.this, memberRealUserId, Constant.CONTACTS_FROM_WX_ID);
                                }
                            }
                        });

                    }

                }
            }
        });
        mAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                if (view.getId() == R.id.iv_add_member) {
                    // 群组拉人 匿名无法判断是否好友已在群中
                    ArrayList<String> checkedUserIdList = new ArrayList<>();
                    ArrayList<String> checkedUserNickNameList = new ArrayList<>();
                    for (GroupMember user : groupMemberInfoList) {
                        checkedUserIdList.add(user.getMemberOriginId());
                        checkedUserNickNameList.add(user.getMemberName());
                    }
                    Intent intent = new Intent(ChatGroupSettingActivity.this, CreateGroupActivity.class);
                    intent.putExtra("createType", Constant.CREATE_GROUP_TYPE_FROM_GROUP);
                    intent.putExtra("groupId", groupId);
                    intent.putStringArrayListExtra("userIdList", checkedUserIdList);
                    intent.putStringArrayListExtra("userNickNameList", checkedUserNickNameList);
                    startActivity(intent);
                } else if (view.getId() == R.id.iv_del_member) {
                    // 传递id list过去还有昵称
                    GroupMemberListActivity.start(ChatGroupSettingActivity.this, groupId, new GroupMemberListActivity.OnRemoveListener() {
                        @Override
                        public void removeSuccess(ArrayList<String> checkedAccounts) {
                            if (checkedAccounts != null && !checkedAccounts.isEmpty()) {
                                List<GroupMember> data = mAdapter.getData();
                                ArrayList<Integer> indexs = new ArrayList<>();
                                for (int k = 0; k < data.size(); k++) {
                                    GroupMember member = data.get(k);
                                    if (member.getItemType() != GroupMemberAdapter.DEFAULT_MEMBER) {
                                        continue;
                                    }
                                    for (String account : checkedAccounts) {
                                        if (member.getMemberAccount().equals(account)) {
                                            // 需要移除
                                            indexs.add(k);
                                            break;
                                        }
                                    }
                                }
                                Trace.d("onItemChildClick: " + indexs.size());
                                // 从后向前删除，以避免索引问题
                                for (int j = indexs.size() - 1; j >= 0; j--) {
                                    int index = indexs.get(j);
                                    data.remove(index);
                                    mAdapter.notifyItemRemoved(index);
                                }
                            }
                            DBManager.Companion.getInstance(ChatGroupSettingActivity.this)
                                    .getGroupMemberByGroupId(groupId)
                                    .to(RxLife.to(ChatGroupSettingActivity.this))
                                    .subscribe(new Consumer<List<GroupMember>>() {
                                        @Override
                                        public void accept(List<GroupMember> groupMembers) throws Throwable {
                                            mMemberNumTv.setText(String.format("%s(%d)", getString(R.string.chat_info), groupMembers.size()));
                                        }
                                    });
                        }
                    });
                }
            }
        });
        rv_grid.setLayoutManager(new GridLayoutManager(this, 5));
        rv_grid.setAdapter(mAdapter);
        setOnClickListener(R.id.rl_exit_group, R.id.sb_group_name, R.id.tv_all_members, R.id.my_group_nick,
                R.id.rl_clear, R.id.rl_info, R.id.rl_search_chat_msg, R.id.rl_change_group_manager);
        EventBus.getDefault().register(this);
        if (SmartCommHelper.getInstance().isDeveloperMode()) {
            sbMyGroupNick.setVisibility(View.VISIBLE);
        }
    }

    protected void loadInfo() {
        TextView tv_group_info = findViewById(R.id.tv_info);
        SmartIMClient.getInstance().getSmartCommChatRoomManager().getGroupMemberList(groupId, new IGroupMemberCallback() {
            @Override
            public void onSuccess(List<SmartUserInfo> smartUserInfoList) {
                StringBuilder sb = new StringBuilder();
                sb.append("现在人数>>>>").append(smartUserInfoList.size()).append("\r\n")
                        .append("我是否在muc中 " + SmartCommHelper.getInstance().checkMucJoined(groupId)).append("\r\n");
                for (SmartUserInfo userInfo1 : smartUserInfoList) {
                    sb.append("--------realJid = >>>>")
                            .append(userInfo1.getUserId())
                            .append("\r\n")
                            .append("nickname = >>>>")
                            .append(userInfo1.getNickname())
                            .append("\r\n")
                            .append("role = >>>>")
                            .append(userInfo1.getRoleName())
                            .append("\r\n")
                            .append("affiliation = >>>>")
                            .append(userInfo1.getAffiliationName())
                            .append("\r\n");
                }
                tv_group_info.setText(sb.toString());
            }
        });
        Trace.d("run: start--");
        DBManager.Companion.getInstance(this)
                .findMemberByGroupIdAndAccount(SmartCommHelper.getInstance().getAccountIdInGroup(groupId), groupId)
                .to(RxLife.to(this))
                .subscribe(new Consumer<List<GroupMember>>() {
                    @Override
                    public void accept(List<GroupMember> groupMembers) throws Throwable {
                        if (!groupMembers.isEmpty()) {
                            GroupMember myMemberInfo = groupMembers.get(0);
                            isOwner = myMemberInfo.isOwner();
                            sbMyGroupNick.setRightText(myMemberInfo.getMemberAccount());
                            boolean hasManagerPermission = available && (isOwner || myMemberInfo.isAdmin());
                            if (hasManagerPermission) {
                                maxShowCount = 18;
                            }
                            DBManager.Companion.getInstance(ChatGroupSettingActivity.this)
                                    .getGroupMemberByGroupId(groupId)
                                    .to(RxLife.to(ChatGroupSettingActivity.this))
                                    .subscribe(new Consumer<List<GroupMember>>() {
                                        @Override
                                        public void accept(List<GroupMember> groupMembers) throws Throwable {
                                            groupMemberInfoList = groupMembers;
                                            Trace.d("run: ecd--");
                                            if (groupMemberInfoList.size() > maxShowCount) {
                                                List<GroupMember> data = groupMemberInfoList.subList(0, maxShowCount);
                                                mAdapter.setList(data);
                                                tv_all_members.setVisibility(View.VISIBLE);
                                            } else {
                                                mAdapter.setList(groupMemberInfoList);
                                            }
                                            mAdapter.addData(new GroupMember().setItemType(GroupMemberAdapter.ADD_MEMBER));
                                            if (hasManagerPermission) {
                                                mAdapter.addData(new GroupMember().setItemType(GroupMemberAdapter.DEL_MEMBER));
                                            }
                                            mMemberNumTv.setText(String.format("%s(%d)", getString(R.string.chat_info), groupMemberInfoList.size()));
                                        }
                                    });
                        }
                    }
                });
    }

    @Override
    protected void initData() {
        loadInfo();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sb_group_name:
                if (isOwner) {
                    UpdateGroupNameActivity.start(this, groupId, groupName, new UpdateGroupNameActivity.UpdateGroupNameListener() {
                        @Override
                        public void updateGroupName(String groupName) {
                            sbGroupName.setRightText(groupName);
                        }
                    });
                } else {
                    toast(getString(R.string.only_owner_permission));
                }
                break;
            case R.id.rl_change_group_manager:
                if (isOwner) {
                    ChatGroupManagerActivity.start(this, groupId);
                } else {
                    toast(R.string.no_permission);
                }
                break;
            case R.id.tv_all_members:
                GroupMemberAllActivity.start(this, groupId);
                break;
            case R.id.rl_clear:
                final ConfirmDialog clearConfirmDialog = new ConfirmDialog(this, "",
                        getString(R.string.delete_group_chat_history), getString(R.string.delete), "");
                clearConfirmDialog.setOnDialogClickListener(new ConfirmDialog.OnDialogClickListener() {
                    @Override
                    public void onOkClick() {
                        // 清除本地message
                        MessageDao.getInstance().deleteMessageByConversationId(groupId);
                        clearConfirmDialog.dismiss();
                        EventBus.getDefault().post(new ChatEvent(ChatEvent.REFRESH_CHAT_UI));
                    }

                    @Override
                    public void onCancelClick() {
                        clearConfirmDialog.dismiss();
                    }
                });
                // 点击空白处消失
                clearConfirmDialog.setCancelable(true);
                clearConfirmDialog.show();
                break;

            case R.id.rl_exit_group:
                final ConfirmDialog exitConfirmDialog = new ConfirmDialog(this, "",
                        getString(R.string.leave_group_chat), getString(R.string.delete), "");
                exitConfirmDialog.setOnDialogClickListener(new ConfirmDialog.OnDialogClickListener() {
                    @Override
                    public void onOkClick() {
                        exitConfirmDialog.dismiss();
                        exitGroup(groupId);
                    }

                    @Override
                    public void onCancelClick() {
                        exitConfirmDialog.dismiss();
                    }
                });
                // 点击空白处消失
                exitConfirmDialog.setCancelable(true);
                exitConfirmDialog.show();
                break;
            case R.id.rl_info:
                GroupInfoActivity.start(ChatGroupSettingActivity.this, groupId);
                break;
            case R.id.rl_search_chat_msg:
                SearchRecordActivity.start(this, SmartConversationType.GROUP.name(), groupId, groupName);
                break;
            case R.id.my_group_nick:
                new InputDialog.Builder(this)
                        // 内容可以不用填写
                        .setContent(sbMyGroupNick.getRightText())
                        // 确定按钮文本
                        .setConfirm(getString(R.string.common_confirm))
                        // 设置 null 表示不显示取消按钮
                        .setCancel(getString(R.string.common_cancel))
                        // 设置点击按钮后不关闭对话框
                        //.setAutoDismiss(false)
                        .setListener(new InputDialog.OnListener() {

                            @Override
                            public void onConfirm(BaseDialog dialog, String content) {
                                if (!TextUtils.isEmpty(content)) {
                                    if (!content.contentEquals(sbMyGroupNick.getRightText())) {
                                        showDialog();
                                        SmartIMClient.getInstance().getSmartCommChatRoomManager().changeMyNickname(content, groupId, new IChatRoomCallback() {

                                            @Override
                                            public void changeNicknameInGroupSuccess() {
                                                hideDialog();
                                                toast(getString(R.string.success));
                                                sbMyGroupNick.setRightText(content);
                                            }

                                            @Override
                                            public void changeNicknameInGroupFailed(int code, String desc) {
                                                hideDialog();
                                                toast(desc);
                                            }

                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancel(BaseDialog dialog) {
                            }
                        })
                        .show();
                break;
        }
    }

    /**
     * 删除并退出
     *
     * @param groupId
     */
    private void exitGroup(String groupId) {
        SmartIMClient.getInstance().getSmartCommChatRoomManager().leaveRoom(
                groupId, new IChatRoomCallback() {
                    @Override
                    public void leaveSuccess() {
                        // 删除会话 回到聊天列表页面

                    }

                    @Override
                    public void leaveFailed(int code, String desc) {
                    }
                });
        DBManager.Companion.getInstance(ChatGroupSettingActivity.this)
                .deleteConversation(myUserInfo.getUserId(), groupId)
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        ChatEvent event = new ChatEvent(ChatEvent.CONVERSATION_REMOVED);
                        event.obj = groupId;
                        EventBus.getDefault().post(event);
                        DBManager.Companion.getInstance(ChatGroupSettingActivity.this)
                                .deleteMemberByGroupId(groupId);
                        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                        ActivityManager.getInstance().finishAllActivities(MainActivity.class);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }
                });

    }

    @Override
    public void initListener() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ChatEvent event) {
        if (ChatEvent.REFRESH_GROUP_MEMBER_AVATAR.equals(event.getWhat())) {
            String memberUserId = String.valueOf(event.obj);
            if (groupMemberInfoList == null) {
                return;
            }
            for (int i = 0; i < groupMemberInfoList.size(); i++) {
                GroupMember groupMember = groupMemberInfoList.get(i);
                if (groupMember.getMemberOriginId().equals(memberUserId)) {
                    mAdapter.notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
