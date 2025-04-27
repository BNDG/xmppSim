package com.hjq.demo.chat.listener;

import android.os.Bundle;
import android.text.TextUtils;

import com.hjq.demo.R;
import com.hjq.demo.chat.activity.ChatActivity;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.ConversationDao;
import com.hjq.demo.chat.dao.DBManager;
import com.hjq.demo.chat.dao.MessageDao;
import com.hjq.demo.chat.entity.AvatarEntity;
import com.hjq.demo.chat.entity.ChatEvent;
import com.hjq.demo.chat.entity.ChatMessage;
import com.hjq.demo.chat.entity.ChatRoomEntity;
import com.hjq.demo.chat.entity.ConversationInfo;
import com.hjq.demo.chat.entity.GroupMember;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.utils.AvatarGenerator;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.utils.Trace;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.bndg.smack.SmartCommHelper;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.callback.IChatRoomCallback;
import com.bndg.smack.callback.IGroupMemberCallback;
import com.bndg.smack.enums.SmartContentType;
import com.bndg.smack.enums.SmartConversationType;
import com.bndg.smack.model.SmartGroupInfo;
import com.bndg.smack.model.SmartUserInfo;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * @author r
 * @date 2024/6/21
 * @description Brief description of the file content.
 */
public class ChatRoomListener implements IChatRoomCallback {
    private static volatile ChatRoomListener instance;
    private List<IChatRoomCallback> listeners = new ArrayList<>();

    public void addListener(IChatRoomCallback listener) {
        listeners.add(listener);
    }

    public void removeListener(IChatRoomCallback listener) {
        listeners.remove(listener);
    }

    private ChatRoomListener() {
    }

    public static ChatRoomListener getInstance() {
        if (instance == null) {
            synchronized (ChatRoomListener.class) {
                if (instance == null) {
                    instance = new ChatRoomListener();
                }
            }
        }
        return instance;
    }

    /**
     * 加入房间并更新房间名称
     * 有可能只是在后台运行
     *
     * @param roomInfo
     */
    @Override
    public void notifyJoinRoom(SmartGroupInfo roomInfo) {
        /*String groupId = multiUserChat.getRoom().toString();
        Disposable subscribe = DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .getConversationByConversationId(PreferencesUtil.getInstance().getUserId(), groupId)
                .subscribe(conversationInfoList -> {
                    ConversationInfo conversationInfo;
                    if (conversationInfoList.isEmpty()) {
                        conversationInfo = null;
                    } else {
                        conversationInfo = conversationInfoList.get(0);
                    }
                    if (conversationInfo != null) {
                        if (roomInfo != null && !TextUtils.isEmpty(roomInfo.getName())) {
                            conversationInfo.setConversationTitle(roomInfo.getName());
                            conversationInfo.setAvailable(true);
                            Trace.d("notifyJoinRoom: 已存在群聊");
                        } else {
                            // roominfo为空
                            conversationInfo.setAvailable(true);
                            Trace.d("notifyJoinRoom: 已存在群聊");
                        }
                        DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                                .saveConversation(conversationInfo).subscribe(new CompletableObserver() {
                                    @Override
                                    public void onSubscribe(@NonNull Disposable d) {

                                    }

                                    @Override
                                    public void onComplete() {
                                        if (ActivityManager.getInstance().containsActivity(MainActivity.class)) {
                                            ChatEvent event = new ChatEvent(ChatEvent.CONVERSATION_ITEM_CONTENT_UPDATE);
                                            event.obj = groupId;
                                            EventBus.getDefault().post(event);
                                        }
                                    }

                                    @Override
                                    public void onError(@NonNull Throwable e) {

                                    }
                                });

                    } else {
                        // 如果群聊不存在
                        Trace.d("notifyJoinRoom: 新建群聊");
                        ConversationDao.getInstance().saveGroupConversation(groupId, null, SmartMessage.createReJoinRoomMessage());
                        ChatEvent event = new ChatEvent(ChatEvent.CONVERSATION_ITEM_ADDED);
                        event.obj = groupId;
                        EventBus.getDefault().post(event);
                    }
                });*/
    }

    /**
     * 更新群成员
     *
     * @param smartUserInfoList
     * @param groupId
     */
    public void updateGroupMemberList(List<SmartUserInfo> smartUserInfoList, String groupId) {
        List<GroupMember> groupMemberList = new ArrayList<>();
        for (SmartUserInfo user : smartUserInfoList) {
            // 这里的nickname  是以用户的account加入的
            GroupMember groupMember = new GroupMember();
            groupMember.setGroupId(groupId);
            groupMember.setBelongAccount(PreferencesUtil.getInstance().getUserId());
            groupMember.setAffiliation(user.getAffiliation());
            groupMember.setRole(user.getRole());
            // 匿名时userid可能为null
            groupMember.setMemberRealUserId(user.getUserId());
            groupMember.setMemberAccount(user.getMemberAccount());
            groupMember.setMemberName(user.getNickname());
            groupMemberList.add(groupMember);
        }
        // 获取本地的群成员
        Disposable subscribe1 = DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .getGroupMemberByGroupId(groupId)
                .subscribe(new Consumer<List<GroupMember>>() {
                    @Override
                    public void accept(List<GroupMember> dbMemberList) throws Throwable {
                        if (dbMemberList.isEmpty()) {
                            Trace.d("accept: 如果没有群成员，则直接插入");
                            DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                                    .saveGroupMembers(groupMemberList)
                                    .subscribe();
                        } else {
                            // 如果有群成员，获取数据库中存在而拉取列表不存在的,代表需要删除
                            Trace.d("accept: 如果有群成员，获取数据库中存在而拉取列表不存在的,代表需要删除");
                            List<GroupMember> needDeleteList = dbMemberList.stream().filter(groupMember -> !groupMemberList.contains(groupMember))
                                    .collect(Collectors.toList());
                            if (!needDeleteList.isEmpty()) {
                                DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                                        .deleteGroupMembers(needDeleteList)
                                        .subscribe();
                            }
                            // 获取数据库中不存在而拉取列表存在的，代表需要新增
                            List<GroupMember> needAddList = groupMemberList.stream().filter(groupMember -> !dbMemberList.contains(groupMember))
                                    .collect(Collectors.toList());
                            if (!needAddList.isEmpty()) {
                                DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                                        .saveGroupMembers(needAddList)
                                        .subscribe();
                            }
                            // 获取数据库中存在的，代表需要更新
                            List<GroupMember> needUpdateList = dbMemberList.stream().filter(groupMember -> groupMemberList.contains(groupMember))
                                    .collect(Collectors.toList());
                            for (GroupMember groupMember : needUpdateList) {
                                GroupMember dbMember = dbMemberList.get(dbMemberList.indexOf(groupMember));
                                GroupMember groupMember1 = groupMemberList.get(groupMemberList.indexOf(groupMember));
                                if (!dbMember.getMemberName().equals(groupMember1.getMemberName())) {
                                    dbMember.setMemberName(groupMember1.getMemberName());
                                    dbMember.setAffiliation(groupMember1.getAffiliation());
                                    dbMember.setRole(groupMember1.getRole());
                                    Trace.d("获取成员列表后更新群成员:  setMemberName>>>> ");
                                    DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                                            .saveGroupMember(dbMember)
                                            .subscribe();
                                }
                            }
                        }
                    }
                });
    }

    /**
     * 更新群头像
     *
     * @param userInfo
     * @param groupId
     */
    @Override
    public void updateGroupAvatar(SmartUserInfo userInfo, String groupId) {
        userInfo.setUserId(groupId);
        AvatarGenerator.saveAvatarFileByUserInfo(userInfo, true);
    }

    @Override
    public void startJoinGroup(String groupId) {
        for (IChatRoomCallback listener : listeners) {
            listener.startJoinGroup(groupId);
        }
    }

    @Override
    public void joinRoomSuccess(String groupId) {
        Trace.w("加入成功后获取房间信息: >> getRoomInfo " + groupId);
        for (IChatRoomCallback listener : listeners) {
            listener.joinRoomSuccess(groupId);
        }
        // 查询入的房间最早一条消息
        Date date = null;
        /*ConversationInfo conversationById = ConversationDao.getInstance().getConversationById(groupId);
        if (null != conversationById) {
            String latestMessageId = conversationById.getLatestMessageId();
            ChatMessage chatMessage = MessageDao.getInstance().getMessageBySmartMessageId(latestMessageId);
            if (null != chatMessage) {
                date = new Date(chatMessage.getTimestamp());
            }
        }*/
/*        String msgId = "";
        ChatMessage earliestMessageByGroupId = MessageDao.getInstance().getEarliestMessageByGroupId(groupId);
        if (null != earliestMessageByGroupId) {
            Trace.d("joinRoom: >>>>>" + earliestMessageByGroupId.getMessageContent());
            msgId = earliestMessageByGroupId.getSmartMessageId();
        }
        mXmppService.getXmppChatRoomManager().fetchHistory(groupId, msgId);*/
        SmartIMClient.getInstance().getSmartCommChatRoomManager().getRoomInfo(groupId, new IChatRoomCallback() {
            @Override
            public void getGroupInfo(SmartGroupInfo roomInfo) {
                createOrUpdateChatRoom(groupId, roomInfo);
            }

            @Override
            public void getGroupInfoFailed() {
                createOrUpdateChatRoom(groupId, null);
            }
        });
        // 获取群的头像
        SmartIMClient.getInstance().getSmartCommUserManager().requestAvatarByUserId(groupId);
    }

    /**
     * 创建或更新聊天室
     *
     * @param groupId
     * @param roomInfo
     */
    private void createOrUpdateChatRoom(String groupId, SmartGroupInfo roomInfo) {
        Trace.w(groupId,
                "获取getRoomInfo后 createOrUpdateChatRoom");
        Disposable subscribe = DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .getChatRoomByRoomId(groupId)
                .subscribe(new Consumer<List<ChatRoomEntity>>() {
                    @Override
                    public void accept(List<ChatRoomEntity> chatRoomEntities) throws Throwable {
                        // 检查本地聊天室是否存在
                        if (chatRoomEntities.isEmpty()) {
                            Trace.w(groupId,
                                    "聊天室不存在 或者聊天室成员为空");
                            getRoomMembers(groupId, roomInfo);
                        } else {
                            // chatroom存在直接保存会话
                            Trace.w("accept: chatroom存在直接保存会话");
                            if (SmartCommHelper.getInstance().isDeveloperMode()) {
                                getRoomMembers(groupId, roomInfo);
                            } else {
                                ChatRoomEntity chatRoomEntity = chatRoomEntities.get(0);
                                chatRoomEntity.setModerated(roomInfo.isModerated());
                                chatRoomEntity.setChatRoomName(null != roomInfo ? roomInfo.getGroupName() : chatRoomEntity.getChatRoomName());
                                saveChatRoom(chatRoomEntity, roomInfo);
                            }
                        }
                    }
                });
    }

    /**
     * 获取群成员
     * 聊天室不存在 或者聊天室成员为空的时候拉取
     *
     * @param groupId
     * @param roomInfo
     */
    private void getRoomMembers(String groupId, SmartGroupInfo roomInfo) {
        // todo 更新群成员列表 暂时用xmpp处理群成员 后期要改成后台管理getRoomMembers
        SmartIMClient.getInstance().getSmartCommChatRoomManager().getGroupMemberList(groupId, new IGroupMemberCallback() {
            @Override
            public void onSuccess(List<SmartUserInfo> smartUserInfoList) {
                // 这里是不是频繁拉取？
                ChatRoomListener.getInstance().updateGroupMemberList(smartUserInfoList, groupId);
                // 目前xmpp只能获取在线的群成员 含有自己
                ChatRoomEntity chatRoomEntity = new ChatRoomEntity();
                chatRoomEntity.setChatRoomJid(groupId);
                chatRoomEntity.setModerated(roomInfo.isModerated());
                chatRoomEntity.setChatRoomName(null != roomInfo ? roomInfo.getGroupName() : "");
                List<String> memberJidList = new ArrayList<>();
                List<String> memberNicknameList = new ArrayList<>();
                for (SmartUserInfo smartUserInfo : smartUserInfoList) {
                    String userJid = smartUserInfo.getUserId();
                    if (TextUtils.isEmpty(userJid)) {
                        userJid = smartUserInfo.getMemberAccount();
                    }
                    memberJidList.add(userJid);
                    // 获取昵称用来显示头像，可能获取不到昵称-因为对方不是好友-或者还没有存到好友列表
                    // 弃用 已经用groupmember显示头像
                    memberNicknameList.add(smartUserInfo.getNickname());
                }
                saveChatRoom(chatRoomEntity, roomInfo);
            }

            @Override
            public void onFailed(int code, String desc) {
                Trace.w(desc);
            }
        });
    }

    private void queryMemberAvatars(String memberJidList) {
        if (TextUtils.isEmpty(memberJidList)) {
            return;
        }
        String[] split = memberJidList.split(Constant.SEPARATOR_CUSTOM);
        // 这里有可能群成员还没有存到数据库
        for (String memberRealUserId : split) {
            // 判断hash
            Disposable subscribe = DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                    .getAvatarByUserId(memberRealUserId)
                    .subscribe(new Consumer<List<AvatarEntity>>() {
                        @Override
                        public void accept(List<AvatarEntity> avatarEntities) throws Throwable {
                            if (avatarEntities.isEmpty() || TextUtils.isEmpty(avatarEntities.get(0).getAvatarLocalPath())) {
                                Trace.d("memberRealUserId:  " + memberRealUserId,
                                        " needQuery 不请求成员头像");
//                                SmartIMClient.getInstance().getSmartCommUserManager().queryAvatarByUserId(memberRealUserId);
                            }
                        }
                    });
        }
    }

    /**
     * 保存聊天室
     *
     * @param chatRoomEntity
     */
    private void saveChatRoom(ChatRoomEntity chatRoomEntity, SmartGroupInfo roomInfo) {
        DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .saveOrUpdateChatRoom(chatRoomEntity)
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        String groupId = chatRoomEntity.getChatRoomJid();
                        // 群聊信息更新后 刷新一下头像和会话标题
                        ConversationDao.getInstance().saveGroupConversation(groupId, roomInfo, null, false);
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

                    }
                });
    }

    @Override
    public void joinRoomFailed(int code, String groupId, String desc) {
        Trace.d("joinRoomFailed: groupId = " + groupId,
                desc);
        for (IChatRoomCallback listener : listeners) {
            listener.joinRoomFailed(code, groupId, desc);
        }

    }

    /**
     * 有成员加入的时候
     *
     * @param groupId
     * @param user
     */
    @Override
    public void memberJoined(String groupId, SmartUserInfo user) {
        GroupMember groupMember = new GroupMember();
        groupMember.setMemberAccount(user.getMemberAccount());
        groupMember.setGroupId(groupId);
        groupMember.setBelongAccount(PreferencesUtil.getInstance().getUserId());
        groupMember.setAffiliation(user.getAffiliation());
        groupMember.setRole(user.getRole());
        // 匿名时userid 为null
        groupMember.setMemberRealUserId(user.getUserId());
        groupMember.setMemberName(user.getNickname());
        Trace.d("memberJoined: 有成员加入的时候 >>> " + user.getMemberAccount(),
                user.getNickname(),
                user.getUserId());
        DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .saveGroupMember(groupMember)
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        // 成员加入 如果没有群头像 并且成员数量<9 则更新群头像
                        Disposable subscribe = DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                                .getAvatarByUserId(groupId)
                                .subscribe(new Consumer<List<AvatarEntity>>() {
                                    @Override
                                    public void accept(List<AvatarEntity> avatarEntities) throws Throwable {
                                        if (!avatarEntities.isEmpty()) {
                                        } else {
                                            Trace.d("accept: 没有群头像");
                                            Disposable subscribe1 = DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                                                    .getGroupMemberByGroupId(groupId)
                                                    .subscribe(new Consumer<List<GroupMember>>() {
                                                        @Override
                                                        public void accept(List<GroupMember> groupMembers) throws Throwable {
                                                            // 应该做一个防抖
                                                            if (groupMembers.size() < 9) {
                                                                Trace.d("accept: 成员数量<9 更新群头像");
                                                                ChatEvent event = new ChatEvent(ChatEvent.REFRESH_USER_AVATAR);
                                                                event.obj = groupId;
                                                                EventBus.getDefault().post(event);
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                });
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }
                });
    }

    /**
     * 成员下线 离开群都会触发
     *
     * @param room
     * @param memberAccount
     */
    @Override
    public void memberOffline(String room, String memberAccount) {
        if (SmartCommHelper.getInstance().isDeveloperMode()) {
            GroupMember groupMember = new GroupMember();
            groupMember.setGroupId(room);
            groupMember.setMemberAccount(memberAccount);
            groupMember.setBelongAccount(PreferencesUtil.getInstance().getUserId());
            DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                    .kickedMember(groupMember)
                    .subscribe();
        }
    }

    /**
     * 成员退群
     *
     * @param groupId
     * @param memberAccount
     */
    @Override
    public void memberLeave(String groupId, String memberAccount) {
        GroupMember groupMember = new GroupMember();
        groupMember.setGroupId(groupId);
        groupMember.setMemberAccount(memberAccount);
        groupMember.setBelongAccount(PreferencesUtil.getInstance().getUserId());
        DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .kickedMember(groupMember)
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        // 删除群成员后更新群头像
                        Trace.d("memberLeave: 更新群头像");
                        ChatEvent event = new ChatEvent(ChatEvent.REFRESH_USER_AVATAR);
                        event.obj = groupId;
                        EventBus.getDefault().post(event);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }
                });
    }

    @Override
    public void memberBanned(String groupId, String memberAccount, String reason) {

    }

    @Override
    public void voiceGranted(String groupId, String memberAccount) {
        for (IChatRoomCallback listener : listeners) {
            listener.voiceGranted(groupId, memberAccount);
        }
    }

    @Override
    public void voiceRevoked(String groupId, String memberAccount) {
        for (IChatRoomCallback listener : listeners) {
            listener.voiceRevoked(groupId, memberAccount);
        }
    }

    @Override
    public void memberKicked(String groupId, String memberAccount, String reason, boolean isSelf) {
        // 判断如果是自己 需要删除
        if (isSelf) {
            Disposable subscribe = DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                    .getConversationByConversationId(PreferencesUtil.getInstance().getUserId(), groupId)
                    .subscribe(conversationInfoList -> {
                        ConversationInfo conversationInfo;
                        if (conversationInfoList.isEmpty()) {
                            conversationInfo = null;
                        } else {
                            conversationInfo = conversationInfoList.get(0);
                        }
                        Trace.d("memberKicked:被移除出群 " + groupId,
                                "conversationById" + conversationInfo);
                        if (null != conversationInfo) {
                            conversationInfo.setAvailable(false);
                            DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                                    .saveConversation(conversationInfo).subscribe(new CompletableObserver() {
                                        @Override
                                        public void onSubscribe(@NonNull Disposable d) {

                                        }

                                        @Override
                                        public void onComplete() {
                                            User user = PreferencesUtil.getInstance().getUser();
                                            ChatMessage textMsg = ChatMessage.createTextMsg(groupId,
                                                    SmartConversationType.GROUP.name(),
                                                    SmartContentType.SYSTEM,
                                                    ActivityManager.getInstance().getApplication().getString(R.string.kicked_from_group));
                                            MessageDao.getInstance().saveAndSetLastTimeStamp(textMsg, isSuccess -> {
                                                if (ActivityManager.getInstance().containsActivity(ChatActivity.class)) {
                                                    ChatEvent event = new ChatEvent(ChatEvent.KICKED_ME);
                                                    Bundle bundle = new Bundle();
                                                    bundle.putString(Constant.GROUP_ID, groupId);
                                                    bundle.putString(Constant.MESSAGE_ORIGIN_ID, textMsg.getOriginId());
                                                    event.bundle = bundle;
                                                    EventBus.getDefault().post(event);
                                                }
                                            });

                                        }

                                        @Override
                                        public void onError(@NonNull Throwable e) {

                                        }
                                    });

                        }
                    });
        }
        GroupMember groupMember = new GroupMember();
        groupMember.setGroupId(groupId);
        groupMember.setMemberAccount(memberAccount);
        groupMember.setBelongAccount(PreferencesUtil.getInstance().getUserId());
        DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .kickedMember(groupMember)
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        // 删除群成员后更新群头像
                        Trace.d("onComplete: 踢出群成员后更新群头像");
                        ChatEvent event = new ChatEvent(ChatEvent.REFRESH_USER_AVATAR);
                        event.obj = groupId;
                        EventBus.getDefault().post(event);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }
                });
    }

    /**
     * 收到邀请加入群聊
     *
     * @param inviterJid
     * @param reason
     * @param password
     */
    @Override
    public void invitationReceived(SmartGroupInfo roomInfo,
                                   String inviterJid,
                                   String reason, String password,
                                   List<SmartUserInfo> smartUserInfoList) {
        // 这里假设是好友才会被邀请 todo 暂时交给chatmanger处理
       /* User userById = UserDao.getInstance().getUserById(inviterJid);
        if (userById != null) {
            String content = String.format(ActivityManager.getInstance().getApplication().getString(
                            R.string.received_room_invite),
                    userById.getUserNickName());
            SmartMessage smartMessage = SmartMessage.newRoomInviteMsg(
                    "", userById.getUserNickName(), PreferencesUtil.getInstance().getUserId(),
                    content);
            String groupId = room.getRoom().toString();
            ChatMessage.createTextMsg(groupId,
                    SmartConversationType.GROUP.name(),
                    SmartContentType.SYSTEM,
                    PreferencesUtil.getInstance().getUserId(),
                    PreferencesUtil.getInstance().getUser().getUserNickName(),
                    groupId,
                    groupId,
                    content);
            // 先保存聊天室 再创建会话
            ChatRoomEntity chatRoomEntity = new ChatRoomEntity();
            chatRoomEntity.setChatRoomJid(groupId);
            List<String> memberJidList = new ArrayList<>();
            List<String> memberNicknameList = new ArrayList<>();
            for (SmartUserInfo smartUserInfo : smartUserInfoList) {
                if (!TextUtils.isEmpty(smartUserInfo.getUserJid())) {
                    memberJidList.add(smartUserInfo.getUserJid());
                }
                memberNicknameList.add(smartUserInfo.getNickname());
            }
            if (!memberJidList.contains(inviterJid)) {
                memberJidList.add(inviterJid);
                memberNicknameList.add(userById.getUserNickName());
            }
            chatRoomEntity.setMemberJidList(TextUtils.join(Constant.SEPARATOR_CUSTOM, memberJidList));
            String sqlMemberName = TextUtils.join(Constant.SEPARATOR_CUSTOM, memberNicknameList);
            chatRoomEntity.setMemberNicknameList(sqlMemberName);
            DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                    .saveChatRoom(chatRoomEntity)
                    .subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onComplete() {
                            ConversationDao.getInstance().saveGroupConversation(groupId, roomInfo, smartMessage);
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {

                        }
                    });
        }*/
    }

    /**
     * 收到群成员vcard
     *
     * @param userInfo
     */
    @Override
    public void receivedMemberInfo(SmartUserInfo userInfo) {
        Disposable subscribe = DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .findMemberByGroupIdAndAccount(userInfo.getMemberAccount(), userInfo.getGroupId())
                .subscribe(new Consumer<List<GroupMember>>() {
                    @Override
                    public void accept(List<GroupMember> groupMembers) throws Throwable {
                        if (!groupMembers.isEmpty()) {
                            GroupMember memberByGroupIdAndAccount = groupMembers.get(0);
                            String nickName = userInfo.getNickname();
                            if (!TextUtils.isEmpty(nickName)) {
                                memberByGroupIdAndAccount.setMemberName(nickName);
                                DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                                        .saveGroupMember(memberByGroupIdAndAccount)
                                        .subscribe();
                            }
                            // 更新群成员的头像-为了更新群消息-群成员界面的头像
                            userInfo.setGroupId(userInfo.getGroupId());
                            userInfo.setUserId(memberByGroupIdAndAccount.getMemberOriginId());
                            AvatarGenerator.saveAvatarFileByUserInfo(userInfo, false);
                        }
                    }
                });

    }

    /**
     * 接收到头像hash的时候
     *
     * @param userId    收到群成员的的头像hash
     * @param photoHash 已存在的
     */
    @Override
    public void receivedMemberAvatarHash(String userId, String photoHash) {
        AvatarEntity avatarEntity = new AvatarEntity();
        avatarEntity.setUserId(userId);
        avatarEntity.setAvatarHash(photoHash);
        Trace.d("receivedMemberAvatarHash: " + photoHash);
        DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .saveAvatarOrUpdate(avatarEntity)
                .subscribe();
    }

    @Override
    public void updateMemberInfo(String groupId, SmartUserInfo user) {
        GroupMember groupMember = new GroupMember();
        groupMember.setMemberAccount(user.getMemberAccount());
        groupMember.setGroupId(groupId);
        groupMember.setBelongAccount(PreferencesUtil.getInstance().getUserId());
        groupMember.setAffiliation(user.getAffiliation());
        groupMember.setRole(user.getRole());
        // 匿名时 为null
        groupMember.setMemberRealUserId(user.getUserId());
        groupMember.setMemberName(user.getNickname());
        Trace.d("更新群成员状态: " + user.getMemberAccount(),
                user.getNickname(),
                user.getUserId());
        DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .saveGroupMember(groupMember)
                .subscribe();
    }

    @Override
    public void groupNameUpdated(String groupId, SmartGroupInfo roomInfo) {
        createOrUpdateChatRoom(groupId, roomInfo);
    }

    @Override
    public void groupDestroyed(String groupId, String reason) {
        Disposable subscribe = DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .getConversationByConversationId(PreferencesUtil.getInstance().getUserId(), groupId)
                .subscribe(conversationInfoList -> {
                    ConversationInfo conversationInfo;
                    if (conversationInfoList.isEmpty()) {
                        conversationInfo = null;
                    } else {
                        conversationInfo = conversationInfoList.get(0);
                    }
                    Trace.d("群聊解散: " + groupId,
                            "conversationById " + conversationInfo);
                    if (null != conversationInfo) {
                        conversationInfo.setAvailable(false);
                        DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                                .saveConversation(conversationInfo).subscribe(new CompletableObserver() {
                                    @Override
                                    public void onSubscribe(@NonNull Disposable d) {

                                    }

                                    @Override
                                    public void onComplete() {
                                        User user = PreferencesUtil.getInstance().getUser();
                                        ChatMessage textMsg = ChatMessage.createTextMsg(groupId,
                                                SmartConversationType.GROUP.name(),
                                                SmartContentType.SYSTEM,
                                                ActivityManager.getInstance().getApplication().getString(R.string.group_destroyed));
                                        if (ActivityManager.getInstance().containsActivity(ChatActivity.class)) {
                                            ChatEvent event = new ChatEvent(ChatEvent.GROUP_DESTROYED);
                                            Bundle bundle = new Bundle();
                                            bundle.putString(Constant.GROUP_ID, groupId);
                                            bundle.putString(Constant.MESSAGE_ORIGIN_ID, textMsg.getOriginId());
                                            event.bundle = bundle;
                                            EventBus.getDefault().post(event);
                                        }
                                    }

                                    @Override
                                    public void onError(@NonNull Throwable e) {

                                    }
                                });

                    }
                });

    }

    /**
     * @param groupId
     * @param oldUserName 金角大王
     * @param newAccount  xx@xx/金角大王2
     * @param newUserName 金角大王2
     * @description 过时的 离线的时候无法收到更改
     */
    @Override
    public void memberAccountChanged(String groupId, String oldUserName, String newAccount, String newUserName) {
        // 需要修改avatars表 chat_message表
        Trace.d(groupId,
                "memberAccountChanged: " + oldUserName, newAccount, newUserName);
        MessageDao.getInstance().memberAccountChanged(groupId, oldUserName, newAccount, newUserName);
    }
}
