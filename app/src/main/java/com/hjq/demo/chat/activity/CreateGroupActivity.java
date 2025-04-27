package com.hjq.demo.chat.activity;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hjq.demo.R;
import com.hjq.demo.chat.adapter.PickContactAdapter;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.ConversationDao;
import com.hjq.demo.chat.dao.DBManager;
import com.hjq.demo.chat.dao.GroupMemberDao;
import com.hjq.demo.chat.dao.MessageDao;
import com.hjq.demo.chat.dao.UserDao;
import com.hjq.demo.chat.entity.ChatMessage;
import com.hjq.demo.chat.entity.ChatRoomEntity;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.listener.ContactCallback;
import com.hjq.demo.chat.utils.AvatarGenerator;
import com.hjq.demo.chat.utils.CommonUtil;
import com.hjq.demo.chat.utils.PinyinComparator;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.chat.widget.LoadingDialog;
import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.utils.Trace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bndg.smack.SmartIMClient;
import com.bndg.smack.callback.IChatRoomCallback;
import com.bndg.smack.entity.SmartMessage;
import com.bndg.smack.enums.SmartContentType;
import com.bndg.smack.enums.SmartConversationType;
import com.bndg.smack.model.SmartUserInfo;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * 创建群组
 * 进入此页面的三种场景
 * 场景1: 首页发起群聊，此时所有friend都是unchecked状态
 * 场景2: 单聊设置发起群聊, 此时有一个用户(单聊对象)处于checked状态，其他friend都是unchecked状态
 * 场景3: 群聊设置拉人，此时此群里所有人都处于checked状态，其他friend都是unchecked状态
 *
 * @author zhou
 */
public class CreateGroupActivity extends ChatBaseActivity {

    private PickContactAdapter contactAdapter;
    private ListView listView;

    // 创建群聊类型
    // 1.首页发起群聊
    // 2.单聊拉人
    // 3.群聊拉人
    String createType;

    // 单聊
    String firstUserId;
    String firstUserNickName;

    // 群聊拉人
    String groupId;
    List<String> firstUserIdList;
    List<String> firstUserNickNameList;

    // 可滑动的显示选中用户的View
    private LinearLayout mAvatarListLl;
    private ImageView mSearchIv;
    private TextView mSaveTv;


    private List<String> checkedUserIdList = new ArrayList<>();
    private List<User> checkedUserList = new ArrayList<>();

    private List<String> initUserIdList = new ArrayList<>();
    private int totalCount = 0;

    LoadingDialog loadingDialog;

    private EditText mSearchEt;
    private User firstUser;
    private String roomName;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_create_group;
    }

    @Override
    protected void initView() {
        TextView tv_left = findViewById(R.id.tv_left);
        tv_left.setText(getString(R.string.create_group));
        tv_left.setVisibility(View.VISIBLE);
        loadingDialog = new LoadingDialog(CreateGroupActivity.this);
        createType = getIntent().getStringExtra("createType");
        if (Constant.CREATE_GROUP_TYPE_FROM_SINGLE.equals(createType)) {
            firstUserId = getIntent().getStringExtra("userId");
            firstUserNickName = getIntent().getStringExtra("userNickName");
            initUserIdList.add(firstUserId);
            UserDao.getInstance().getUserById(CreateGroupActivity.this, firstUserId, new ContactCallback() {
                @Override
                public void getUser(@Nullable User userById) {
                    firstUser = userById;
                }
            });
        } else if (Constant.CREATE_GROUP_TYPE_FROM_GROUP.equals(createType)) {
            groupId = getIntent().getStringExtra("groupId");
            firstUserIdList = getIntent().getStringArrayListExtra("userIdList");
            firstUserNickNameList = getIntent().getStringArrayListExtra("userIdNickNameList");

            initUserIdList.addAll(firstUserIdList);
        }
        UserDao.getInstance().getAllFriendList(CreateGroupActivity.this, new ContactCallback() {
            @Override
            public void getContactList(@androidx.annotation.NonNull List<User> users) {
                // 对list进行排序
                Collections.sort(users, new PinyinComparator() {
                });
                contactAdapter = new PickContactAdapter(CreateGroupActivity.this,
                        R.layout.item_pick_contact_list, users, checkedUserIdList, initUserIdList);
                listView.setAdapter(contactAdapter);
            }
        });

        mAvatarListLl = findViewById(R.id.ll_avatar_list);
        mSearchIv = findViewById(R.id.iv_search);
        mSaveTv = findViewById(R.id.tv_right);
        mSearchEt = findViewById(R.id.et_search);

        listView = findViewById(R.id.lv_friends);
        mSaveTv.setText(getString(R.string.confirm));
        mSaveTv.setVisibility(View.VISIBLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final User friend = contactAdapter.getItem(position);
                CheckBox mPickFriendCb = view.findViewById(R.id.cb_pick_friend);
                boolean isEnabled = mPickFriendCb.isEnabled();
                boolean isChecked = mPickFriendCb.isChecked();
                if (isEnabled) {
                    if (isChecked) {
                        mPickFriendCb.setChecked(false);
                        removeCheckedImage(friend.getUserId(), friend);
                    } else {
                        mPickFriendCb.setChecked(true);
                        addCheckedImage(friend);
                    }
                }
            }
        });

        mSaveTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Constant.CREATE_GROUP_TYPE_FROM_SINGLE.equals(createType)) {
                    loadingDialog.setMessage(getString(R.string.creating_group));
                    loadingDialog.show();
                    createXmppGroup();
                } else if (Constant.CREATE_GROUP_TYPE_FROM_GROUP.equals(createType)) {
                    loadingDialog.setMessage(getString(R.string.adding_member));
                    loadingDialog.show();
                    addGroupMembers(groupId, "");
                }
            }
        });
    }

    /**
     * 创建聊天室
     * 至少3个人
     */
    private void createXmppGroup() {
        // 获取选中的群成员userId
        List<String> pickedUserIdList = new ArrayList<>();
        // 添加单聊对象的
        pickedUserIdList.add(firstUserId);
        pickedUserIdList.addAll(checkedUserIdList);
        // 群成员昵称列表
        List<String> pickedUserNicknameList = new ArrayList<>();
        pickedUserNicknameList.add(firstUser.getUserNickName());
        for (User checkedUser : checkedUserList) {
            pickedUserNicknameList.add(checkedUser.getUserNickName());
        }
        // 1-100的随机数
        roomName = PreferencesUtil.getInstance().getUser().getUserNickName() +
                Constant.SEPARATOR_NICKNAME +
                pickedUserNicknameList.get(0) +
                Constant.SEPARATOR_NICKNAME +
                pickedUserNicknameList.get(1) +
                "...";
        SmartIMClient.getInstance().getSmartCommChatRoomManager().createRoom(roomName, new IChatRoomCallback() {
            @Override
            public void createSuccess(String roomId) {
                groupId = roomId;
                // 邀请的群成员昵称
                String inviteUsernames = TextUtils.join(Constant.SEPARATOR_NICKNAME, pickedUserNicknameList);
                Trace.d("create room>>>>" + groupId, inviteUsernames);
                String createInviteMsg = String.format(getString(R.string.group_chat_invite_msg), inviteUsernames);
                SmartMessage smartMessage = SmartMessage.createConversationMsg(CommonUtil.generateId(),
                        PreferencesUtil.getInstance().getUserId(),
                        myUserInfo.getUserNickName(),
                        SmartConversationType.GROUP.name(),
                        SmartContentType.SYSTEM,
                        groupId,
                        createInviteMsg);
                ChatMessage textMsg = ChatMessage.createTextMsg(groupId,
                        SmartConversationType.GROUP.name(),
                        SmartContentType.SYSTEM,
                        createInviteMsg);
                MessageDao.getInstance().saveAndSetLastTimeStamp(textMsg, isSuccess -> {
                    // 创建群聊实体
                    ChatRoomEntity chatRoomEntity = new ChatRoomEntity();
                    chatRoomEntity.setChatRoomJid(groupId);
                    chatRoomEntity.setChatRoomName(roomName);
                    ArrayList<String> memberUseridList = new ArrayList<>();
                    memberUseridList.add(myUserInfo.getUserId());
                    memberUseridList.addAll(pickedUserIdList);
                    ArrayList<String> memberNicknameList = new ArrayList<>();
                    memberNicknameList.add(myUserInfo.getUserNickName());
                    memberNicknameList.addAll(pickedUserNicknameList);
                    chatRoomEntity.setRoomOwnerJid(myUserInfo.getUserId());
                    DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                            .saveChatRoom(chatRoomEntity)
                            .subscribe(new CompletableObserver() {
                                @Override
                                public void onSubscribe(@NonNull Disposable d) {

                                }

                                @Override
                                public void onComplete() {
                                    ConversationDao.getInstance().createGroupConversation(groupId, smartMessage);
                                    inviteUsersToGroup(pickedUserIdList);
                                }

                                @Override
                                public void onError(@NonNull Throwable e) {

                                }
                            });
                });
            }

            @Override
            public void createFailed(int code, String desc) {
                loadingDialog.dismiss();
                toast(desc);
            }
        });
    }

    /**
     * 邀请群成员
     *
     * @param pickedUserIdList
     */
    private void inviteUsersToGroup(List<String> pickedUserIdList) {
        Trace.d("inviteUsersToGroup: ");
        List<SmartUserInfo> userInfos = new ArrayList<>();
        checkedUserList.add(firstUser);
        for (User user : checkedUserList) {
            SmartUserInfo userInfo1 = new SmartUserInfo();
            userInfo1.setUserId(user.getUserId());
            userInfo1.setMemberAccount(User.getAccountById(user.getUserId()));
            userInfo1.setNickname(user.getUserNickName());
            userInfos.add(userInfo1);
        }
        // 默认原因是群成员名字
        SmartIMClient.getInstance().getSmartCommChatRoomManager().inviteUserToGroup2(groupId, userInfos, "", new IChatRoomCallback() {
            @Override
            public void joinRoomSuccess(String groupId) {
                // 加上自己
                checkedUserList.add(myUserInfo);
                // 邀请群成员 保存群成员
                GroupMemberDao.getInstance().createGroupAddMembers(checkedUserList, groupId);
                // 进入群聊页面
                ChatActivity.start(getActivity(), SmartConversationType.GROUP.name(), groupId, roomName);
                ActivityManager.getInstance().finishAllActivities(MainActivity.class, ChatActivity.class);
            }

            @Override
            public void joinRoomFailed(int code, String groupId, String desc) {
                loadingDialog.dismiss();
                toast(desc);
            }
        });

    }

    @Override
    protected void initData() {

    }

    private void addCheckedImage(final User friend) {
        // 是否已包含
        if (checkedUserIdList.contains(friend.getUserId())) {
            return;
        }
        totalCount++;
        checkedUserIdList.add(friend.getUserId());
        checkedUserList.add(friend);
        // 包含TextView的LinearLayout
        // 参数设置
        LinearLayout.LayoutParams menuLinerLayoutParames = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        View view = LayoutInflater.from(this).inflate(
                R.layout.item_create_group_header, null);
        ImageView mAvatarSdv = view.findViewById(R.id.sdv_avatar);
        AvatarGenerator.loadAvatar(this, friend.getUserId(), friend.getUserNickName(), mAvatarSdv, false);
        menuLinerLayoutParames.setMargins(6, 0, 6, 0);
        // 设置id，方便后面删除
        view.setTag(friend.getUserId());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeCheckedImage(friend.getUserId(), friend);
                contactAdapter.notifyDataSetChanged();
            }
        });
        mAvatarListLl.addView(view, menuLinerLayoutParames);
        if (totalCount > 0) {
            if (mSearchIv.getVisibility() == View.VISIBLE) {
                mSearchIv.setVisibility(View.GONE);
            }
            mSaveTv.setText(String.format(getString(R.string.done_contacts), totalCount));
            mSaveTv.setEnabled(true);
        }
    }

    private void removeCheckedImage(String userId, User friend) {
        View view = mAvatarListLl.findViewWithTag(userId);
        mAvatarListLl.removeView(view);
        totalCount--;
        checkedUserIdList.remove(userId);
        checkedUserList.remove(friend);
        mSaveTv.setText(String.format(getString(R.string.done_contacts), totalCount));
        if (totalCount <= 0) {
            if (mSearchIv.getVisibility() == View.GONE) {
                mSearchIv.setVisibility(View.VISIBLE);
            }
            mSaveTv.setText(getString(R.string.done));
            mSaveTv.setEnabled(false);
        }
    }

    /**
     * 邀请
     * @param groupId
     * @param reason
     */
    private void addGroupMembers(final String groupId, String reason) {
        List<SmartUserInfo> userInfos = new ArrayList<>();
        for (User user : checkedUserList) {
            SmartUserInfo userInfo1 = new SmartUserInfo();
            userInfo1.setUserId(user.getUserId());
            userInfo1.setMemberAccount(User.getAccountById(user.getUserId()));
            userInfo1.setNickname(user.getUserNickName());
            userInfos.add(userInfo1);
        }
        if (!checkedUserIdList.isEmpty()) {
//            mSmartCommService.getSmartCommChatRoomManager().inviteUsers(checkedUserIdList, groupId, reason);
            SmartIMClient.getInstance().getSmartCommChatRoomManager().inviteUsers2(userInfos, groupId, reason);
            toast(R.string.sented);
        }
        loadingDialog.dismiss();
        ActivityManager.getInstance().finishAllActivities(MainActivity.class);
    }

    @Override
    public void initListener() {

    }
}
