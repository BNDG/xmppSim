package com.bndg.smack.impl;


import android.text.TextUtils;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.packet.RosterPacket;
import org.jivesoftware.smackx.blocking.BlockingCommandManager;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.xdata.BooleanFormField;
import org.jivesoftware.smackx.xdata.SingleValueFormField;
import org.jivesoftware.smackx.xdata.packet.DataForm;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import com.bndg.smack.R;
import com.bndg.smack.SmartCommHelper;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.callback.IFriendListCallback;
import com.bndg.smack.callback.ISmartCallback;
import com.bndg.smack.callback.IUserInfoCallBack;
import com.bndg.smack.callback.IFriendListener;
import com.bndg.smack.constant.SmartConstants;
import com.bndg.smack.contract.ISmartCommFriend;
import com.bndg.smack.exceptions.SIMConnectionException;
import com.bndg.smack.model.SmartUserInfo;
import com.bndg.smack.utils.SmartTrace;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DefaultSmartCommFriendImpl extends BaseXmppImpl implements ISmartCommFriend {

    private Set<IFriendListener> iFriendListeners = new HashSet<>();
    private HashMap<String, Boolean> tempStatusMap = new HashMap<>();

    /**
     * 添加好友 无分组
     *
     * @param userId       userName
     * @param userNickname name
     * @return boolean
     */
    @Override
    public void addFriend(String userId, String friendRequestIntro, String userNickname, IFriendListener callback) {
        Disposable subscribe = Single.fromCallable(() -> {
                    if (!SmartIMClient.getInstance().isAuthenticated()) {
                        throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                    }
                    if (TextUtils.isEmpty(userNickname)) {
                        return false;
                    }
                    Presence presencePacket = SmartIMClient.getInstance().getConnection().getStanzaFactory().buildPresenceStanza()
                            .ofType(Presence.Type.subscribe)
                            .to(JidCreate.entityBareFrom(userId))
                            .setStatus(friendRequestIntro)
                            .build();
                    SmartIMClient.getInstance().getConnection().sendStanza(presencePacket);
//                    Roster.getInstanceFor(SmartIMClient.getInstance().getConnection())
//                            .createEntry(JidCreate.entityBareFrom(userJid), nickName, null);
                    return true;
                }).onErrorResumeNext(throwable -> {
                    if (throwable instanceof SIMConnectionException) {
                        SmartIMClient.getInstance().checkConnection();
                    }
                    throw throwable;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess -> {
                    if (onSuccess) {
                        callback.onSendRequestSuccess();
                    } else {
                        callback.onSendRequestFailed();
                    }
                }, onError -> {
                    callback.onSendRequestFailed();
                });


    }

    /**
     * 添加好友 有分组
     *
     * @param userName  userName
     * @param name      name
     * @param groupName groupName
     * @return boolean
     */
    @Override
    public boolean addFriend(String userName, String name, String groupName) {
        if (SmartIMClient.getInstance().getConnection() == null)
            return false;
        try {
            Presence subscription = new Presence(Presence.Type.subscribed);
            subscription.setTo(JidCreate.entityBareFrom(userName));
            userName += "@" + SmartIMClient.getInstance().getConnection().getHost();
            SmartIMClient.getInstance().getConnection().sendStanza(subscription);
            Roster.getInstanceFor(SmartIMClient.getInstance().getConnection())
                    .createEntry(JidCreate.entityBareFrom(userName), name, new String[]{groupName});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void getFriendList(IFriendListCallback friendListCallback) {
        Disposable subscribe = Single.create(new SingleOnSubscribe<Set<SmartUserInfo>>() {
                    @Override
                    public void subscribe(final SingleEmitter<Set<SmartUserInfo>> emitter) throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        Set<SmartUserInfo> friendList = new TreeSet<>();
                        Roster roster = Roster.getInstanceFor(SmartIMClient.getInstance().getConnection());
                        roster.reloadAndWait(); // 强制刷新好友列表
                        Set<RosterEntry> entries = roster.getEntries();
                        for (RosterEntry entry : entries) {
                            String jid = entry.getJid().asBareJid().toString();
                            if (jid.contains("conference.")) {
                                continue;
                            }
                            if (TextUtils.isEmpty(entry.getName())) {
                                VCard userVCard = SmartIMClient.getInstance().getSmartCommUserManager().
                                        getUserInfo(entry.getJid().toString());
                                if (null != userVCard) {
                                    entry.setName(userVCard.getNickName());
                                }
                            }
                            SmartUserInfo userInfo = new SmartUserInfo();
                            userInfo.setUserId(jid);
                            userInfo.setNickname(entry.getName());
                            userInfo.setSubscribeStatus(entry.getType().toString());
                            if(entry.getType() == RosterPacket.ItemType.from && entry.isSubscriptionPending()) {
                                // 我在对方的列表中 并且是待处理的好友请求  显示
                                SmartIMClient.getInstance().getFriendshipManager().receivedFriendRequest(userInfo);
                            }
                            friendList.add(userInfo);
                        }
                        emitter.onSuccess(friendList);
                    }
                })
                // 在IO线程执行网络操作
                .subscribeOn(Schedulers.io())
                // 将结果转换在主线程处理
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(friendListCallback::onSuccess, onError -> {
                    friendListCallback.onFailed(SmartConstants.Error.GET_FRIEND_LIST_FAILED,
                            SmartIMClient.getInstance().getString(R.string.GET_FRIEND_LIST_FAILED));
                });
        addDisposable(subscribe);
    }

    @Override
    public void addFriendListener(IFriendListener instance) {
        this.iFriendListeners.add(instance);
    }

    @Override
    public void removeFriendListener(IFriendListener instance) {
        this.iFriendListeners.remove(instance);
    }

    /**
     * 收到好友申请
     *
     * @param info
     */
    @Override
    public void receivedFriendRequest(SmartUserInfo info) {
        for (IFriendListener listener : iFriendListeners) {
            listener.onNewFriendRequest(info);
        }
    }

    @Override
    public void receivedFriendAdded(SmartUserInfo info) {
        for (IFriendListener listener : iFriendListeners) {
            listener.onFriendAdded(info);
        }
    }

    @Override
    public void receivedFriendDeleted(String userId) {
        for (IFriendListener listener : iFriendListeners) {
            listener.onFriendDeleted(userId);
        }
    }

    @Override
    public void release() {
        clearDisposable();
    }

    /**
     * 删除好友
     *
     * @param userJid userName 这里判断了有没有@
     * @return boolean
     */
    @Override
    public void deleteFriend(String userJid, ISmartCallback smartCallback) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        RosterEntry entry = Roster.getInstanceFor(SmartIMClient.getInstance().getConnection())
                                .getEntry(JidCreate.entityBareFrom(userJid));
                        Roster.getInstanceFor(SmartIMClient.getInstance().getConnection())
                                .removeEntry(entry);
                        SmartTrace.d("删除好友 userId = " + userJid);
                        return true;
                    }
                }).onErrorResumeNext(throwable -> {
                    if (throwable instanceof SIMConnectionException) {
                        SmartIMClient.getInstance().checkConnection();
                    }
                    throw throwable;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess -> {
                    if (onSuccess) {
                        smartCallback.onSuccess();
                    } else {
                        smartCallback.onFailed(SmartConstants.Error.REMOVE_FRIEND_FAILED,
                                SmartIMClient.getInstance().getString(R.string.REMOVE_FRIEND_FAILED));
                    }
                }, onError -> {
                    smartCallback.onFailed(SmartConstants.Error.REMOVE_FRIEND_FAILED,
                            SmartIMClient.getInstance().getString(R.string.REMOVE_FRIEND_FAILED));
                });
    }

    /**
     * 查询用户 unused
     *
     * @param userName userName
     * @return List<HashMap < String, String>>
     */
    @Override
    public void searchFriends(String userName, IUserInfoCallBack infoCallback) {
        if (SmartIMClient.getInstance().getConnection() == null) {
            infoCallback.getNicknameError(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
            return;
        }
        try {
            UserSearchManager usm = new UserSearchManager(
                    SmartIMClient.getInstance().getConnection());
            //本例用的smack:4.3.4版本，getSearchForm方法传的是DomainBareJid类型，而之前的版本是String类型，大家在使用的时候需要特别注意
            //而转换DomainBareJid的方式如下面的例子所示：JidCreate.domainBareFrom("search." + getConnection().getXMPPServiceDomain())
            // 设置搜索域，通常为你的XMPP服务器域名
            DataForm searchForm = usm.getSearchForm(JidCreate.domainBareFrom("search." + SmartIMClient.getInstance().getConnection().getXMPPServiceDomain()));
            if (searchForm == null) {
                infoCallback.getNicknameError(SmartConstants.Error.NO_USER_FOUND, SmartIMClient.getInstance().getString(R.string.NO_USER_FOUND));
                return;
            }
            // 由 domainpart 组成的 XMPP 地址 （JID）。例如“xmpp.org”。
            // 获取搜索表单
            //这里设置了Username为true代码是根据用户名查询用户，search代表查询字段
            //smack:4.3.4版本是下面的字段，但之前的版本会有些不一样，所以在用的时候最好看下xmpp交互的log，里面有相应的字段值
            DataForm.Builder builder = DataForm.builder();
            BooleanFormField.Builder firstname = SingleValueFormField.booleanBuilder("firstname");
            firstname.setValue(true);
            builder.addField(firstname.build());
            BooleanFormField.Builder lastname = SingleValueFormField.booleanBuilder("lastname");
            lastname.setValue(true);
            builder.addField(lastname.build());
            builder.addField(lastname.build());
            ReportedData data = usm.getSearchResults(builder.build(), JidCreate.domainBareFrom("search." + SmartIMClient.getInstance().getConnection().getXMPPServiceDomain()));
            List<ReportedData.Row> rowList = data.getRows();

            //此处返回的字段名如下所示，之前的版本可能有所变化，使用的时候需要注意
            for (ReportedData.Row row : rowList) {
                String jid = row.getValues("jid").toString();
                String username = row.getValues("Username").toString();
                String name = row.getValues("Name").toString();
                String email = row.getValues("Email").toString();
                // 若存在，则有返回,UserName一定非空，其他两个若是有设，一定非空
            }
        } catch (SmackException | InterruptedException | XmppStringprepException |
                 XMPPException e) {
            e.printStackTrace();
            infoCallback.getNicknameError(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
        }
    }

    /**
     * 接受好友请求
     *
     * @param userJid
     * @param targetNickname
     * @param smartCallback
     */
    @Override
    public void acceptFriendReq(String userJid, String targetNickname, ISmartCallback smartCallback) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        // 同意对方的请求
                        unblockContacts(userJid, new ISmartCallback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onFailed(int code, String desc) {

                            }
                        });
                        Presence presence = new Presence(Presence.Type.subscribed);
                        presence.setTo(JidCreate.entityBareFrom(userJid));
                        SmartIMClient.getInstance().getConnection().sendStanza(presence);
                        // 发送请求对方
                        Roster.getInstanceFor(SmartIMClient.getInstance().getConnection())
                                .createEntry(JidCreate.entityBareFrom(userJid), targetNickname, null);
                       /* addFriend(userJid, targetNickname, new IAddFriendCallback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onFailed() {

                            }
                        });*/
                        return true;
                    }
                }).onErrorResumeNext(throwable -> {
                    if (throwable instanceof SIMConnectionException) {
                        SmartIMClient.getInstance().checkConnection();
                    }
                    throw throwable;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess -> {
                    if (onSuccess) {
                        smartCallback.onSuccess();
                    } else {
                        smartCallback.onFailed(SmartConstants.Error.ACCEPT_PRESENCE_FAILED,
                                SmartIMClient.getInstance()
                                        .getString(R.string.ACCEPT_PRESENCE_FAILED));
                    }
                }, onError -> {
                    smartCallback.onFailed(SmartConstants.Error.ACCEPT_PRESENCE_FAILED,
                            SmartIMClient.getInstance().getString(R.string.ACCEPT_PRESENCE_FAILED));
                });
    }

    /**
     * 拒绝好友请求
     *
     * @param to
     * @return
     */
    @Override
    public void rejectPresence(String to, ISmartCallback smartCallback) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        Presence presence = new Presence(Presence.Type.unsubscribed);
                        presence.setTo(JidCreate.entityBareFrom(to));
                        SmartIMClient.getInstance().getConnection().sendStanza(presence);
                        return true;
                    }
                }).onErrorResumeNext(throwable -> {
                    if (throwable instanceof SIMConnectionException) {
                        SmartIMClient.getInstance().checkConnection();
                    }
                    throw throwable;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess -> {
                    if (onSuccess) {
                        smartCallback.onSuccess();
                    } else {
                        smartCallback.onFailed(SmartConstants.Error.REJECT_PRESENCE_FAILED,
                                SmartIMClient.getInstance()
                                        .getString(R.string.REJECT_PRESENCE_FAILED));
                    }
                }, onError -> {
                    smartCallback.onFailed(SmartConstants.Error.REJECT_PRESENCE_FAILED,
                            SmartIMClient.getInstance().getString(R.string.REJECT_PRESENCE_FAILED));
                });
    }

    public void blockContact(String userJid, ISmartCallback callback) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        BlockingCommandManager blockingCommandManager = BlockingCommandManager.getInstanceFor(SmartIMClient.getInstance().getConnection());
                        SmartTrace.d("是否支持黑名单服务 = " + blockingCommandManager.isSupportedByServer());
                        List<Jid> blockList = blockingCommandManager.getBlockList();
                        List<Jid> newList = new ArrayList<>(blockList);
                        if (!newList.contains(JidCreate.from(userJid))) {
                            newList.add(JidCreate.from(userJid));
                            blockingCommandManager.blockContacts(newList);
                        }
                        return true;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(onSuccess -> {
                    callback.onSuccess();
                }, onError -> {

                });
    }

    @Override
    public void unblockContacts(String jid, ISmartCallback iSmartCallback) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        BlockingCommandManager blockingCommandManager = BlockingCommandManager.getInstanceFor(SmartIMClient.getInstance().getConnection());
                        List<Jid> blockList = blockingCommandManager.getBlockList();
                        List<Jid> newList = new ArrayList<>(blockList);
                        if (newList.contains(JidCreate.from(jid))) {
                            newList.remove(JidCreate.from(jid));
                            blockingCommandManager.unblockContacts(newList);
                        }
                        return true;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(onSuccess -> {
                    iSmartCallback.onSuccess();
                }, onError -> {

                });
    }

    @Override
    public boolean checkIsFriend(String toContactJid) {
        try {
            if (toContactJid.equals(SmartCommHelper.getInstance().getUserId())) {
                return true;
            }
            RosterEntry entry = Roster.getInstanceFor(SmartIMClient.getInstance().getConnection())
                    .getEntry(JidCreate.entityBareFrom(toContactJid));
            if (entry == null) {
                return false;
            }
            SmartTrace.d("检查是否订阅了对方 = " + entry.canSeeHisPresence(),
                    "对方是否订阅了我 = " + entry.canSeeMyPresence());
            return entry.canSeeMyPresence();
        } catch (XmppStringprepException e) {
            return false;
        }
    }

    @Override
    public void receivedFriendPresent(Presence presence, RosterEntry entry) {
        SmartTrace.d("联系人的在线状态发生变化 = " + presence,
                presence.getType(),
                entry);
        tempStatusMap.put(entry.getJid().toString(), presence.getType() == Presence.Type.available);
        if (presence.getType() == Presence.Type.unavailable || presence.getType() == Presence.Type.available) {
            for (IFriendListener listener : iFriendListeners) {
                SmartCommHelper.getInstance().executeWithDelay(1500,() -> {
                    listener.receivedFriendStatus(entry.getJid().toString(), presence.getType() == Presence.Type.available);
                });
            }
        }
    }

    @Override
    public boolean isOnline(String userId) {
        if (tempStatusMap.containsKey(userId)) {
            return Boolean.TRUE.equals(tempStatusMap.get(userId));
        }
        return false;
    }
}
