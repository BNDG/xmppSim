package com.bndg.smack.muc.listener;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.UserStatusListener;
import org.jivesoftware.smackx.muc.packet.MUCUser;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.UUID;

import com.bndg.smack.R;
import com.bndg.smack.SmartCommHelper;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.entity.SmartMessage;
import com.bndg.smack.enums.SmartContentType;
import com.bndg.smack.extensions.bookmarks.BookmarksManager;
import com.bndg.smack.muc.RoomChat;
import com.bndg.smack.muc.RoomState;
import com.bndg.smack.utils.SmartTrace;

/**
 * @author r
 * @date 2024/9/26
 * @description Brief description of the file content.
 */
public class MucMyStatusListener implements UserStatusListener {
    private String groupId;

    public MucMyStatusListener(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public void kicked(Jid actor, String reason) {
        SmartTrace.d(groupId,
                actor,
                "主持人将您踢出聊天室 " + reason);
        MultiUserChat mucIns = SmartIMClient.getInstance().getSmartCommChatRoomManager().getMucIns(groupId);
        // 是自己 从书签中移除这个群聊
        BookmarksManager.getInstance().removeConferenceFromBookmarks(mucIns.getRoom());
        RoomChat roomChat = SmartIMClient.getInstance().getSmartCommChatRoomManager().getRoomChat(groupId);
        if (roomChat != null) {
            roomChat.setState(RoomState.forrbidden);
        }
        if (SmartIMClient.getInstance().getChatRoomListener() != null) {
            SmartIMClient.getInstance().getChatRoomListener().memberKicked(groupId, SmartCommHelper.getInstance().getAccountIdInGroup(groupId), reason, true);
        }

    }

    @Override
    public void voiceGranted() {
        SmartMessage sendGroupMessage = SmartMessage.createSendGroupMessage(UUID.randomUUID().toString(),
                groupId,
                groupId,
                groupId,
                SmartContentType.SYSTEM,
                String.format(SmartCommHelper.getInstance().getString(R.string.voice_granted), SmartCommHelper.getInstance().getString(R.string.you)));
        SmartIMClient.getInstance().getSimpleMsgListener().receivedSmartMessage(sendGroupMessage);
        if(SmartIMClient.getInstance().getChatRoomListener() != null) {
            SmartIMClient.getInstance().getChatRoomListener().voiceGranted(groupId, SmartCommHelper.getInstance().getAccountIdInGroup(groupId));
        }
        SmartTrace.d(groupId, "授予您发言权限");
    }

    @Override
    public void voiceRevoked() {
        SmartMessage sendGroupMessage = SmartMessage.createSendGroupMessage(UUID.randomUUID().toString(),
                groupId,
                groupId,
                groupId,
                SmartContentType.SYSTEM,
                String.format(SmartCommHelper.getInstance().getString(R.string.voice_revoked), SmartCommHelper.getInstance().getString(R.string.you)));
        SmartIMClient.getInstance().getSimpleMsgListener().receivedSmartMessage(sendGroupMessage);
        if(SmartIMClient.getInstance().getChatRoomListener() != null) {
            SmartIMClient.getInstance().getChatRoomListener().voiceRevoked(groupId, SmartCommHelper.getInstance().getAccountIdInGroup(groupId));
        }
        SmartTrace.d(groupId, "禁止您的发言权限");
    }

    @Override
    public void banned(Jid actor, String reason) {
        // 这里可以提示已经被踢出聊天室
        SmartTrace.d(groupId, "禁止您进入聊天室 " + reason);
    }

    @Override
    public void removed(MUCUser mucUser, Presence presence) {
        // 群解散的时候 或者是改名 或者时被踢出
        SmartTrace.w(groupId, "群解散的时候 被移除出聊天室 " + presence.getFrom(),
                presence.getType());
     /*   MultiUserChat mucIns = SmartIMClient.getInstance().getSmartCommChatRoomManager().getMucIns(groupId);
        // 是自己 从书签中移除这个群聊
        BookmarksManager.getInstance().removeConferenceFromBookmarks(mucIns.getRoom());
        if (SmartIMClient.getInstance().getChatRoomListener() != null) {
            SmartIMClient.getInstance().getChatRoomListener().memberKicked(groupId, SmartCommHelper.getInstance().getAccount(), "", true);
        }*/
    }

    @Override
    public void membershipGranted() {
        SmartTrace.d(groupId, "授予您的用户文件室成员资格时");
    }

    @Override
    public void membershipRevoked() {
        SmartTrace.d(groupId, "取消您的用户文件室成员资格时");
    }

    @Override
    public void moderatorGranted() {
        SmartTrace.d(groupId, "授予您主持人权限");
    }

    @Override
    public void moderatorRevoked() {
        SmartTrace.d(groupId, "取消您的主持人权限");
    }

    @Override
    public void ownershipGranted() {
        SmartMessage sendGroupMessage = SmartMessage.createSendGroupMessage(UUID.randomUUID().toString(),
                groupId,
                groupId,
                groupId,
                SmartContentType.SYSTEM,
                String.format(SmartCommHelper.getInstance().getString(R.string.owner_granted), SmartCommHelper.getInstance().getString(R.string.you)));
        SmartIMClient.getInstance().getSimpleMsgListener().receivedSmartMessage(sendGroupMessage);
        SmartTrace.d(groupId, "授予所有者权限");
    }

    @Override
    public void ownershipRevoked() {
        SmartMessage sendGroupMessage = SmartMessage.createSendGroupMessage(UUID.randomUUID().toString(),
                groupId,
                groupId,
                groupId,
                SmartContentType.SYSTEM,
                String.format(SmartCommHelper.getInstance().getString(R.string.owner_revoked), SmartCommHelper.getInstance().getString(R.string.you)));
        SmartIMClient.getInstance().getSimpleMsgListener().receivedSmartMessage(sendGroupMessage);
        SmartTrace.d(groupId, "取消所有者权限");
    }

    @Override
    public void adminGranted() {
        SmartMessage sendGroupMessage = SmartMessage.createSendGroupMessage(UUID.randomUUID().toString(),
                groupId,
                groupId,
                groupId,
                SmartContentType.SYSTEM,
                String.format(SmartCommHelper.getInstance().getString(R.string.admin_granted), SmartCommHelper.getInstance().getString(R.string.you)));
        SmartIMClient.getInstance().getSimpleMsgListener().receivedSmartMessage(sendGroupMessage);
        SmartTrace.d(groupId, "授予管理员权限");
    }

    @Override
    public void adminRevoked() {
        SmartMessage sendGroupMessage = SmartMessage.createSendGroupMessage(UUID.randomUUID().toString(),
                groupId,
                groupId,
                groupId,
                SmartContentType.SYSTEM,
                String.format(SmartCommHelper.getInstance().getString(R.string.admin_revoked), SmartCommHelper.getInstance().getString(R.string.you)));
        SmartIMClient.getInstance().getSimpleMsgListener().receivedSmartMessage(sendGroupMessage);
        SmartTrace.d(groupId, "取消管理员权限");
    }

    @Override
    public void roomDestroyed(MultiUserChat alternateMUC, String reason) {
        // alternateMUC 是null
        SmartTrace.d(groupId, "聊天室被销毁 " + reason);
        RoomChat roomChat = SmartIMClient.getInstance().getSmartCommChatRoomManager().getRoomChat(groupId);
        if (roomChat != null) {
            roomChat.setState(RoomState.destroyed);
        }
        if (SmartIMClient.getInstance().getChatRoomListener() != null) {
            SmartIMClient.getInstance().getChatRoomListener().groupDestroyed(groupId, reason);
        }
        // 是自己 从书签中移除这个群聊
        try {
            BookmarksManager.getInstance().removeConferenceFromBookmarks(JidCreate.entityBareFrom(groupId));
        } catch (XmppStringprepException e) {
        }
    }
}
