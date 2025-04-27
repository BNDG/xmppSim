package com.hjq.demo.chat.dao;

import com.blankj.utilcode.util.StringUtils;
import com.hjq.demo.R;
import com.hjq.demo.chat.entity.GroupMember;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.manager.ThreadPoolManager;
import com.hjq.demo.utils.Trace;

import java.util.LinkedHashMap;
import java.util.List;

import com.bndg.smack.SmartCommHelper;

/**
 * @author r
 * @date 2024/6/3
 * @description Brief description of the file content.
 */
public class GroupMemberDao {
    private static volatile GroupMemberDao instance;

    private GroupMemberDao() {
        // 防止反射攻击
    }

    public static GroupMemberDao getInstance() {
        if (instance == null) {
            synchronized (GroupMemberDao.class) {
                if (instance == null) {
                    instance = new GroupMemberDao();
                }
            }
        }
        return instance;
    }

    /**
     * 创建群 邀请其他人的时候 保存一次群成员
     * 不适用于xmpp的匿名muc
     *
     * @param pickedUserList
     * @param groupId
     */
    public void createGroupAddMembers(List<User> pickedUserList, String groupId) {
        for (User user : pickedUserList) {
            GroupMember groupMember = new GroupMember();
            groupMember.setGroupId(groupId);
            groupMember.setBelongAccount(PreferencesUtil.getInstance().getUserId());

            groupMember.setMemberRealUserId(user.getUserId());
            groupMember.setMemberAccount(User.getAccountById(user.getUserId()));
            groupMember.setMemberName(user.getUserNickName());
            Trace.d("createGroupAddMembers: 邀请者会用userid加入群聊");
            DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                    .saveGroupMember(groupMember)
                    .subscribe();
        }
    }

    /**
     * @param groupId
     * @param fromUserId          匿名的时候 是groupId/memberAccount 开放的时候是realUserId
     * @param groupMemberCallback
     */
    public void getOperationList(String groupId, String fromUserId, GroupMemberCallback groupMemberCallback) {
        ThreadPoolManager.getInstance().execute(() -> {
            GroupMember myInfo = AppDatabase.getInstance(ActivityManager.getInstance().getApplication())
                    .groupMemberDao().getMemberByAccount(SmartCommHelper.getInstance().getAccountIdInGroup(groupId),
                            groupId, PreferencesUtil.getInstance().getUserId());
            GroupMember targetMember = null;
            if (SmartCommHelper.getInstance().isDeveloperMode()) {
                if (fromUserId.contains("/")) {
                    targetMember = AppDatabase.getInstance(ActivityManager.getInstance().getApplication())
                            .groupMemberDao().getMemberByAccount(SmartCommHelper.getMemberAccountFromUserId(fromUserId),
                                    groupId, PreferencesUtil.getInstance().getUserId());
                } else {
                    targetMember = AppDatabase.getInstance(ActivityManager.getInstance().getApplication())
                            .groupMemberDao().getMemberByRealId(fromUserId, groupId, PreferencesUtil.getInstance().getUserId());
                }
            } else {
                targetMember = AppDatabase.getInstance(ActivityManager.getInstance().getApplication())
                        .groupMemberDao().getMemberByRealId(fromUserId, groupId, PreferencesUtil.getInstance().getUserId());
            }
            LinkedHashMap<String, Integer> list = new LinkedHashMap<>();
            list.put(StringUtils.getString(R.string.view_speech_record), 100);
            if (null == myInfo || null == targetMember) {
            } else {
                list.put("@Ta", 101);
                if (myInfo.isOwner() || myInfo.isAdmin()) {
                    if (targetMember.isVisitor()) {
                        // 解除禁言
                        list.put(StringUtils.getString(R.string.cancel_mute), 103);
                    } else if (targetMember.isParticipant()) {
                        // 可以禁言对方
                        list.put(StringUtils.getString(R.string.mute_member), 102);
                    }
                } else {
                }
            }
            groupMemberCallback.getOperationList(list, targetMember);
        });
    }

    public interface GroupMemberCallback {
        void getOperationList(LinkedHashMap<String, Integer> strings, GroupMember targetMember);
    }

    /**
     * 保存群成员
     * 当前仅收到邀请入群的时候
     *
     * @param smartUserInfoList
     * @param groupId
     */
   /* public void updateMembers(List<SmartUserInfo> smartUserInfoList, String groupId) {
        ThreadPoolManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                for (SmartUserInfo user : smartUserInfoList) {
                    GroupMember groupMemberFind = findMemberByGroupIdAndAccount(groupId, user.getMemberAccount());
                    if (null == groupMemberFind) {
                        GroupMember groupMember = new GroupMember();
                        groupMember.setGroupId(groupId);
                        groupMember.setBelongAccount(PreferencesUtil.getInstance().getUserId());
                        groupMember.setRole(user.getRole());
                        groupMember.setAffiliation(user.getAffiliation());
                        groupMember.setMemberRealUserId(user.getUserJid());
                        groupMember.setMemberAccount(user.getMemberAccount());
                        groupMember.setMemberName(user.getNickname());
                        Trace.d("updateMembers: 收到邀请的时候 新增--->>>> " + user.getUserJid());
                        groupMember.save();
                    } else {
                        groupMemberFind.setGroupId(groupId);
                        groupMemberFind.setBelongAccount(PreferencesUtil.getInstance().getUserId());
                        groupMemberFind.setRole(user.getRole());
                        groupMemberFind.setMemberRealUserId(user.getUserJid());
                        groupMemberFind.setAffiliation(user.getAffiliation());
                        groupMemberFind.setMemberAccount(user.getMemberAccount());
                        groupMemberFind.setMemberName(user.getNickname());
                        Trace.d("updateMembers: 收到邀请的时候 已有--->>>> " + user.getUserJid());
                        groupMemberFind.save();
                    }
                }
            }
        });
    }*/

}