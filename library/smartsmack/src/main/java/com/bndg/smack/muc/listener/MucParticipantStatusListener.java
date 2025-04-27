package com.bndg.smack.muc.listener;

import androidx.annotation.Nullable;

import org.jivesoftware.smackx.muc.MUCAffiliation;
import org.jivesoftware.smackx.muc.MUCRole;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;

import java.util.UUID;
import java.util.concurrent.Callable;

import com.bndg.smack.R;
import com.bndg.smack.SmartCommHelper;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.entity.SmartMessage;
import com.bndg.smack.enums.SmartContentType;
import com.bndg.smack.model.SmartUserInfo;
import com.bndg.smack.utils.SmartTrace;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @author r
 * @date 2024/9/26
 * @description muc 参与者状态监听
 */
public class MucParticipantStatusListener implements ParticipantStatusListener {
    private String groupId;
    private int tempCount = 0;

    @Override
    public void parted(EntityFullJid participant) {
    }

    public MucParticipantStatusListener(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public void joined(EntityFullJid participant) {
        // 初次加入muc 也会收到许多joined监听 也会收到自己的加入提示 或者群成员更改昵称后
        SmartTrace.d(this, "群成员加入joined: >> " + participant,
                tempCount++);
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        SmartUserInfo smartUserInfo = getSmartUserInfo(participant);
                        if (null != smartUserInfo) {
                            if (SmartIMClient.getInstance().getChatRoomListener() != null) {
                                SmartIMClient.getInstance()
                                        .getChatRoomListener()
                                        .memberJoined(groupId, smartUserInfo);
                            }
                        }
                        return true;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Boolean -> {
                }, onError -> {

                });
    }

    @Nullable
    private SmartUserInfo getSmartUserInfo(EntityFullJid participant) {
        SmartUserInfo smartUserInfo = null;
        Resourcepart resourceOrNull = participant.getResourceOrNull();
        if (null != resourceOrNull) {
            smartUserInfo = new SmartUserInfo();
            // 获取每个成员的 Occupant 对象
            MultiUserChat mucIns = SmartIMClient.getInstance().getSmartCommChatRoomManager().getMucIns(groupId);
            Occupant occupant = mucIns.getOccupant(participant);
            if (occupant == null) {
                return null;
            }
            MUCAffiliation affiliation = occupant.getAffiliation();
            MUCRole role = occupant.getRole();
            Jid jid = occupant.getJid();
            Resourcepart nick = occupant.getNick();
            smartUserInfo.setRole(role.name());
            smartUserInfo.setAffiliation(affiliation.name());
            smartUserInfo.setMemberAccount(String.valueOf(nick));
            if (jid != null) {
                EntityBareJid bareJid = jid.asEntityBareJidIfPossible();
                smartUserInfo.setUserId(String.valueOf(bareJid));
                smartUserInfo.setNickname(SmartIMClient.getInstance().getSmartCommUserManager()
                        .getUserNicknameSync(bareJid.toString(), true));
            } else {
                smartUserInfo.setNickname(String.valueOf(nick));
//                smartUserInfo.setUserId(participant.toString());
            }
        }
        return smartUserInfo;
    }

    /**
     * 成员离开 当群成员离线的时候也会被认为是离开聊天室
     *
     * @param participant
     */
    @Override
    public void left(EntityFullJid participant) {
        SmartTrace.d("成员离开: " + participant);
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        Resourcepart resourceOrNull = participant.getResourceOrNull();
                        if (null != resourceOrNull) {
                            String memberAccount = resourceOrNull.toString();
                            if (SmartIMClient.getInstance().getChatRoomListener() != null) {
                                SmartIMClient.getInstance()
                                        .getChatRoomListener()
                                        .memberOffline(groupId, memberAccount);
                            }
                        }

                        return true;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Boolean -> {
                }, onError -> {

                });
    }

    @Override
    public void kicked(EntityFullJid participant, Jid actor, String reason) {
        String memberAccount = participant.getResourceOrNull().toString();
        SmartTrace.d("参与者 participant" + participant, "kicked: " + actor,
                "reason " + reason, "参与者 nickname" + memberAccount);
        // 被踢了无法获取信息 所以无法在这里判断自己被踢出
        SmartTrace.d("被踢出的account: " + memberAccount);
        if (SmartIMClient.getInstance().getChatRoomListener() != null) {
            SmartIMClient.getInstance().getChatRoomListener().memberKicked(groupId, memberAccount, reason, false);
        }
    }

    @Override
    public void voiceGranted(EntityFullJid participant) {
        // 更新角色为参与者
        SmartTrace.d(participant + "被批准发言了!");
        updateParticipantInfo(participant);
    }

    /**
     * @param participant the participant that was revoked voice from the room
     *                    (e.g. room@conference.jabber.org/nick).
     */
    @Override
    public void voiceRevoked(EntityFullJid participant) {
        // 更新角色为访客
        SmartTrace.d(participant + "被禁言了!");
        updateParticipantInfo(participant);
    }

    /**
     * @param participant 被禁止进入房间的参与者（例如：room@conference.jabber.org/nick）。
     * @param actor       执行禁止操作的管理员（例如：user@host.org）。
     * @param reason      管理员提供的禁止该参与者进入房间的原因。
     */
    @Override
    public void banned(EntityFullJid participant, Jid actor, String reason) {
        SmartTrace.d(participant + " 被ban了!");
        if (SmartIMClient.getInstance().getChatRoomListener() != null) {
            Resourcepart resourceOrNull = participant.getResourceOrNull();
            if (resourceOrNull != null) {
                // 模拟发出一条系统消息
                SmartMessage sendGroupMessage = SmartMessage.createSendGroupMessage(UUID.randomUUID().toString(),
                        groupId,
                        groupId,
                        groupId,
                        SmartContentType.SYSTEM,
                        String.format(SmartCommHelper.getInstance().getString(R.string.member_banned),resourceOrNull));
                SmartIMClient.getInstance().getSimpleMsgListener().receivedSmartMessage(sendGroupMessage);
                SmartIMClient.getInstance().getChatRoomListener().memberBanned(groupId, resourceOrNull.toString(), reason);
            }
        }


    }

    @Override
    public void membershipGranted(EntityFullJid participant) {
        updateParticipantInfo(participant);
    }

    /**
     * @param participant 成员资格
     *                    (e.g. room@conference.jabber.org/nick).
     */
    @Override
    public void membershipRevoked(EntityFullJid participant) {
        updateParticipantInfo(participant);
    }

    @Override
    public void moderatorGranted(EntityFullJid participant) {
        updateParticipantInfo(participant);
    }

    /**
     * @param participant 主持人
     *                    (e.g. room@conference.jabber.org/nick).
     */
    @Override
    public void moderatorRevoked(EntityFullJid participant) {
        updateParticipantInfo(participant);
    }

    @Override
    public void ownershipGranted(EntityFullJid participant) {
        updateParticipantInfo(participant);
    }

    /**
     * @param participant 取消参与者房间所有权
     *                    (e.g. room@conference.jabber.org/nick).
     */
    @Override
    public void ownershipRevoked(EntityFullJid participant) {
        updateParticipantInfo(participant);
    }

    @Override
    public void adminGranted(EntityFullJid participant) {
        updateParticipantInfo(participant);
    }

    /**
     * @param participant 被撤销管理员权限的参与者
     *                    (e.g. room@conference.jabber.org/nick).
     */
    @Override
    public void adminRevoked(EntityFullJid participant) {
        updateParticipantInfo(participant);
    }

    private void updateParticipantInfo(EntityFullJid participant) {
        // 更新群成员 角色变化了 岗位变化了 账号变化了 改名过了
        MultiUserChat mucIns = SmartIMClient.getInstance().getSmartCommChatRoomManager().getMucIns(groupId);
        if (mucIns == null) {
            return;
        }
        Occupant occupant = mucIns.getOccupant(participant);
        if (occupant == null) {
            return;
        }
        SmartUserInfo smartUserInfo = new SmartUserInfo();
        MUCRole role = occupant.getRole();
        MUCAffiliation affiliation = occupant.getAffiliation();
        smartUserInfo.setRole(role.name());
        smartUserInfo.setAffiliation(affiliation.name());
        Resourcepart resourceOrNull = participant.getResourceOrNull();
        if (resourceOrNull != null) {
            smartUserInfo.setMemberAccount(resourceOrNull.toString());
        } else {
            smartUserInfo.setMemberAccount(participant.toString());
        }
        Jid jid = occupant.getJid();
        if (jid != null) {
            smartUserInfo.setUserId(jid.asBareJid().toString());
        }
        SmartTrace.d(
                "fromEntityFullJid " + participant,
                "memberAccount " + smartUserInfo.getMemberAccount(),
                "userid " + smartUserInfo.getUserId());
        smartUserInfo.setNickname(participant.getResourcepart().toString());
        SmartIMClient.getInstance().getChatRoomListener().updateMemberInfo(groupId, smartUserInfo);
    }

    @Override
    public void nicknameChanged(EntityFullJid participant, Resourcepart newNickname) {
        SmartTrace.d(participant + " 更改昵称为: " + newNickname);
        EntityBareJid entityBareJid = JidCreate.entityBareFrom(participant.getLocalpart(), participant.getDomain());
        String oldUserName = participant.getResourceOrNull().toString();
        EntityFullJid entityFullJid = JidCreate.entityFullFrom(entityBareJid, newNickname);
        SmartIMClient.getInstance().getChatRoomListener().memberAccountChanged(entityBareJid.toString(), oldUserName, entityFullJid.toString(), newNickname.toString());
        // 判断是不是自己 如果是自己还需要主动加入
        left(participant);
    }

}
