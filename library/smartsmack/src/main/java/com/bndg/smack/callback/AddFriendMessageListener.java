package com.bndg.smack.callback;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.SubscribeListener;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;

import java.util.concurrent.Callable;

import com.bndg.smack.R;
import com.bndg.smack.SmartCommHelper;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.constant.SmartConstants;
import com.bndg.smack.exceptions.SIMConnectionException;
import com.bndg.smack.model.SmartUserInfo;
import com.bndg.smack.utils.SmartTrace;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @author r
 * @date 2024/11/22
 * @description sdk使用 处理好友请求
 */

public class AddFriendMessageListener implements SubscribeListener {

    /**
     * 处理订阅消息
     *
     * @param from
     * @param subscribeRequest
     * @return
     */
    @Override
    public SubscribeAnswer processSubscribe(Jid from, Presence subscribeRequest) {
        if (!SmartIMClient.getInstance().isAuthenticated()) {
            return null;
        }
        Roster roster = Roster.getInstanceFor(
                SmartIMClient.getInstance().getConnection());
        try {
            // 强制刷新好友列表 获取最新订阅请求
            roster.reloadAndWait();
        } catch (Exception e) {
        }
        String friendRequestIntro = subscribeRequest.getStatus();
        // 被添加的时候没有entry
        RosterEntry entry = roster.getEntry(from.asBareJid());
        if (null != entry) {
            // entry name 这里延迟有问题
            SmartTrace.d("是否可以查看对方信息: " + entry.canSeeHisPresence(),
                    "对方是否可以查看我的信息" + entry.canSeeMyPresence(),
                    "订阅类型 " + entry.getType(),
                    "好友昵称 " + entry.getName());
            if (entry.canSeeHisPresence()) {
                SmartTrace.d("我先添加的对方, 直接通过对方的好友请求");
                handleFriendRequestAccepted(entry);
                return SubscribeAnswer.ApproveAndAlsoRequestIfRequired;
            }
        } else {
            SmartTrace.d("entry is null");
        }
        String fromJid = from.asBareJid().toString();
        SmartTrace.d(fromJid + " 申请添加我为好友");
        SmartIMClient.getInstance().getSmartCommUserManager().getUserInfo(fromJid, new IUserInfoCallback2() {
            @Override
            public void onSuccess(SmartUserInfo userInfo) {
                processAddMe(userInfo, friendRequestIntro, fromJid);
            }

            @Override
            public void onFailed(int code, String desc) {
                SmartUserInfo userInfo = new SmartUserInfo();
                userInfo.setNickname(fromJid);
                processAddMe(userInfo, friendRequestIntro, fromJid);
            }
        });
        return null;
    }

    private void handleFriendRequestAccepted(RosterEntry entry) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        VCard vCard = VCardManager.getInstanceFor(SmartIMClient.getInstance().getConnection())
                                .loadVCard(JidCreate.entityBareFrom(entry.getJid()));
                        String userName = vCard == null ? entry.getName() : vCard.getNickName();
                        Roster.getInstanceFor(SmartIMClient.getInstance().getConnection())
                                .createItem(JidCreate.entityBareFrom(entry.getJid()), userName, null);
                        SmartUserInfo info = new SmartUserInfo();
                        info.setUserId(entry.getJid().toString());
                        info.setNickname(userName);
                        info.setSubscribeStatus(entry.getType().name());
                        SmartIMClient.getInstance().getFriendshipManager().receivedFriendAdded(info);
                        return true;
                    }
                }).onErrorResumeNext(throwable -> {
                    if (throwable instanceof SIMConnectionException) {
                        SmartIMClient.getInstance().checkConnection();
                    }
                    throw throwable;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(onSuccess -> {
                    if (onSuccess) {

                    } else {

                    }
                }, onError -> {

                });
    }

    /**
     * 收到有人添加我的监听
     *
     * @param userInfo
     * @param fromJid
     */
    private void processAddMe(SmartUserInfo userInfo, String friendRequestIntro, String fromJid) {
        String nickName = userInfo.getNickname();
        SmartTrace.d("收到好友申请 userid = " + fromJid + ", 昵称 = " + nickName);
        SmartUserInfo info = new SmartUserInfo();
        info.setUserId(fromJid);
        info.setNickname(nickName);
        info.setUserAvatar(userInfo.getUserAvatar());
        info.setUserAvatarHash(userInfo.getUserAvatarHash());
        info.setFriendRequestIntro(friendRequestIntro);
        SmartIMClient.getInstance().getFriendshipManager().receivedFriendRequest(info);
    }
}