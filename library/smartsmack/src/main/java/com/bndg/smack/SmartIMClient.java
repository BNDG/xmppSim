package com.bndg.smack;

import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bndg.smack.callback.AddFriendMessageListener;
import com.bndg.smack.callback.IChatRoomCallback;
import com.bndg.smack.callback.IConnectionListener;
import com.bndg.smack.callback.IGroupMemberCallback;
import com.bndg.smack.callback.ILoginCallback;
import com.bndg.smack.callback.ISimpleMsgListener;
import com.bndg.smack.callback.ISmartCallback;
import com.bndg.smack.callback.ReceiverFriendStatusListener;
import com.bndg.smack.constant.SmartConstants;
import com.bndg.smack.contract.ISmartComm;
import com.bndg.smack.contract.ISmartCommChatRoom;
import com.bndg.smack.contract.ISmartCommConfig;
import com.bndg.smack.contract.ISmartCommFriend;
import com.bndg.smack.contract.ISmartCommMsg;
import com.bndg.smack.contract.ISmartCommUser;
import com.bndg.smack.entity.FetchEntity;
import com.bndg.smack.entity.LoginEventInfo;
import com.bndg.smack.entity.SmartMessage;
import com.bndg.smack.enums.ConnectionState;
import com.bndg.smack.enums.SmartContentType;
import com.bndg.smack.enums.SmartConversationType;
import com.bndg.smack.extensions.ArchivedExtension;
import com.bndg.smack.extensions.MessageTypeExtension;
import com.bndg.smack.extensions.OobDataExtension;
import com.bndg.smack.extensions.SenderInfoExtension;
import com.bndg.smack.extensions.TestGroupMembersExtension;
import com.bndg.smack.extensions.VCardUpdateExtension;
import com.bndg.smack.extensions.bookmarks.BookmarksManager;
import com.bndg.smack.impl.DefaultSmartCommChatRoomImpl;
import com.bndg.smack.impl.DefaultSmartCommFriendImpl;
import com.bndg.smack.impl.DefaultSmartCommImpl;
import com.bndg.smack.impl.DefaultSmartCommMsgImpl;
import com.bndg.smack.impl.DefaultSmartCommUserImpl;
import com.bndg.smack.impl.DefaultXmppConfigImpl;
import com.bndg.smack.model.SmartGroupInfo;
import com.bndg.smack.model.SmartUserInfo;
import com.bndg.smack.muc.RoomChat;
import com.bndg.smack.utils.BitmapUtils;
import com.bndg.smack.utils.OtherUtil;
import com.bndg.smack.utils.SmartTrace;
import com.bndg.smack.utils.StorageUtils;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.packet.StanzaError;
import org.jivesoftware.smack.parsing.ExceptionLoggingCallback;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.roster.SubscribeListener;
import org.jivesoftware.smack.sm.StreamManagementException;
import org.jivesoftware.smack.sm.packet.StreamManagement;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MUCAffiliation;
import org.jivesoftware.smackx.muc.MUCRole;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.Occupant;
import org.jivesoftware.smackx.muc.packet.MUCItem;
import org.jivesoftware.smackx.muc.packet.MUCUser;
import org.jivesoftware.smackx.omemo.element.OmemoElement_VAxolotl;
import org.jivesoftware.smackx.sid.element.OriginIdElement;
import org.jivesoftware.smackx.sid.element.StanzaIdElement;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

/**
 * author : r
 * time   : 2024/6/8 10:32 AM
 * desc   :
 */
public class SmartIMClient {
    private XMPPTCPConnection connection;
    // 连接状态监听器
    private SmartCommConnectionListener smartCommConnectionListener;
    //Xmpp 配置信息
    private ISmartCommConfig smartCommConfig;
    //用户管理
    private ISmartCommUser smartCommUserManager;
    // 好友管理
    private ISmartCommFriend smartCommFriendManager;
    //分组管理
    private ISmartComm smartCommGroupManager;
    // 聊天室管理
    private ISmartCommChatRoom smartCommChatRoomManager;
    //消息管理
    private ISmartCommMsg smartCommMsgManager;
    //好友在线状态改变
    private ReceiverFriendStatusListener mReceiverFriendStatusListener;
    //添加自己的信息监听
    private AddFriendMessageListener mAddFriendMessageListener;
    // 消息监听
    private IncomingChatMessageListener mIncomingChatMessageListener;
    // 心跳帮助类
    private SmartCommHeartbeat smartCommHeartbeat;
    // 消息监听器
    private ISimpleMsgListener simpleMsgListener;
    // 聊天室事件监听器
    private IChatRoomCallback chatRoomListener;

    public IConnectionListener getSimConnectionListener() {
        return iConnectionListener;
    }

    private IConnectionListener iConnectionListener;
    private boolean isRunning;

    public XMPPTCPConnection getConnection() {
        return connection;
    }

    private static volatile SmartIMClient instance;

    private PublishSubject<LoginEventInfo> mySubject = PublishSubject.create();

    private SmartIMClient() {
        // 5秒后获取最新状态判断是否在另一端登陆 5秒避免两个服务器发布用户状态的冲突
        Disposable subscribe = mySubject
                .debounce(5000, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<LoginEventInfo>() {
                    @Override
                    public void accept(LoginEventInfo info) {
                        SmartTrace.d("我的状态更新: myResource = " + info.myResoucerce,
                                "receivedResource " + info.receivedResoucerce,
                                "type " + info.presenceType);
                        if (!info.myResoucerce.equals(info.receivedResoucerce)) {
                            if (info.presenceType == Presence.Type.available) {
                                SmartTrace.d("在另一个设备上线了 需要退出");
                                if (iConnectionListener != null) {
                                    iConnectionListener.onKickedOffline();
                                }
                            }
                        }
                    }
                }, onError -> {
                });
    }

    public static SmartIMClient getInstance() {
        if (instance == null) {
            synchronized (SmartIMClient.class) {
                if (instance == null) {
                    instance = new SmartIMClient();
                }
            }
        }
        return instance;
    }

    public void setSimpleMsgListener(ISimpleMsgListener iMsgProcessor) {
        this.simpleMsgListener = iMsgProcessor;
    }

    public ISimpleMsgListener getSimpleMsgListener() {
        return simpleMsgListener;
    }

    public IChatRoomCallback getChatRoomListener() {
        if (chatRoomListener == null) {
            chatRoomListener = new IChatRoomCallback() {
            };
        }
        return chatRoomListener;
    }

    /**
     * 设置群组监听器
     *
     * @param chatRoomListener
     */
    public void setChatRoomListener(IChatRoomCallback chatRoomListener) {
        this.chatRoomListener = chatRoomListener;
    }

    public void setConnectionListener(IConnectionListener callback) {
        this.iConnectionListener = callback;
    }

    public void removeConnectionCallback(IConnectionListener callback) {
        this.iConnectionListener = null;
    }


    /**
     * 加入群聊失败
     *
     * @param code
     * @param reason
     */
    public void joinRoomFailed(int code, String groupId, String reason) {
        SmartTrace.d("joinRoomFailed");
    }

    public String getString(int res_id) {
        return SmartCommHelper.getInstance().getApplication().getString(res_id);
    }

    /**
     * 检查连接状态
     */
    public synchronized void checkConnection() {
        if (isAuthenticated()) {
            return;
        }
        if (!isRunning) {
            String account = SmartCommHelper.getInstance().getAccount();
            String password = SmartCommHelper.getInstance().getPassword();
            if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(password)) {
                if (connection == null || !connection.isConnected() || !connection.isAuthenticated()) {
                    isRunning = true;
                    // 这里早于Mainactivity
                    SmartTrace.file("检查连接状态, 重新连接到xmpp服务器");
                    connectAndLogin(account, password, new ISmartCallback() {
                        @Override
                        public void onSuccess() {
                            isRunning = false;
                            if (iConnectionListener != null) {
                                iConnectionListener.onServerConnected();
                            }
                        }

                        @Override
                        public void onFailed(int code, String desc) {
                            isRunning = false;
                        }
                    });
                }
            }
        }
    }

    /**
     * 获取XmppConfig
     *
     * @return
     */
    public ISmartCommConfig getSmartCommConfig() {
        if (null == smartCommConfig) {
            smartCommConfig = new DefaultXmppConfigImpl();
        }
        return smartCommConfig;
    }

    /**
     * 设置XmppConfig
     *
     * @param _xmppConfig
     */
    public void setSmartCommConfig(ISmartCommConfig _xmppConfig) {
        this.smartCommConfig = _xmppConfig;
    }

    /**
     * 设置User管理类
     *
     * @param _xmppUserManager
     */
    public void setSmartCommUserManager(ISmartCommUser _xmppUserManager) {
        this.smartCommUserManager = _xmppUserManager;
    }

    /**
     * 获取用户管理对象
     *
     * @return
     */
    public ISmartCommUser getSmartCommUserManager() {
        if (null == smartCommUserManager) {
            smartCommUserManager = new DefaultSmartCommUserImpl();
        }
        return smartCommUserManager;
    }

    /**
     * 获取好友管理类
     *
     * @return
     */
    public ISmartCommFriend getFriendshipManager() {
        if (null == smartCommFriendManager) {
            smartCommFriendManager = new DefaultSmartCommFriendImpl();
        }
        return smartCommFriendManager;
    }

    /**
     * 设置好友管理类
     *
     * @param smartCommFriendManager
     */
    public void setSmartCommFriendManager(ISmartCommFriend smartCommFriendManager) {
        this.smartCommFriendManager = smartCommFriendManager;
    }

    /**
     * 获取分组管理类
     *
     * @return
     */
    public ISmartComm getSmartCommGroupManager() {
        if (null == smartCommGroupManager) {
            smartCommGroupManager = new DefaultSmartCommImpl();
        }
        return smartCommGroupManager;
    }

    /**
     * 设置分组管理类
     *
     * @param smartCommGroupManager
     */
    public void setSmartCommGroupManager(ISmartComm smartCommGroupManager) {
        this.smartCommGroupManager = smartCommGroupManager;
    }

    /**
     * 获取聊天室管理类
     *
     * @return
     */
    public ISmartCommChatRoom getSmartCommChatRoomManager() {
        if (null == smartCommChatRoomManager) {
            smartCommChatRoomManager = new DefaultSmartCommChatRoomImpl();
        }
        return smartCommChatRoomManager;
    }

    /**
     * 设置聊天室管理类
     *
     * @param smartCommChatRoomManager
     */
    public void setSmartCommChatRoomManager(ISmartCommChatRoom smartCommChatRoomManager) {
        this.smartCommChatRoomManager = smartCommChatRoomManager;
    }

    /**
     * 获取消息管理类
     *
     * @return
     */
    public ISmartCommMsg getSmartCommMsgManager() {
        if (null == smartCommMsgManager) {
            smartCommMsgManager = new DefaultSmartCommMsgImpl();
        }
        return smartCommMsgManager;
    }

    /**
     * 设置消息管理类
     *
     * @param smartCommMsgManager
     */
    public void setSmartCommMsgManager(ISmartCommMsg smartCommMsgManager) {
        this.smartCommMsgManager = smartCommMsgManager;
    }

    /**
     * 连接并登录
     *
     * @param userName
     * @param passWord
     * @param iSmartCallback
     */
    public void connectAndLogin(String userName, String passWord, ISmartCallback iSmartCallback) {
        showConnecting();
        connect(new ISmartCallback() {
            @Override
            public void onSuccess() {
                SmartCommHelper.getInstance().setConnectionState(ConnectionState.CONNECTED);
                if (iConnectionListener != null) {
                    iConnectionListener.onServerConnected();
                }
                getSmartCommUserManager().login(userName, passWord, new ILoginCallback() {
                    @Override
                    public void onSuccess() {
                        iSmartCallback.onSuccess();
                    }

                    @Override
                    public void onError(int code, String desc) {
                        iSmartCallback.onFailed(code, desc);
                        if (iConnectionListener != null) {
                            iConnectionListener.onLoginFailed(code, desc);
                        }
                    }
                });
            }

            @Override
            public void onFailed(int code, String desc) {
                SmartCommHelper.getInstance().setConnectionState(ConnectionState.DISCONNECTED);
                if (iConnectionListener != null) {
                    iConnectionListener.onServerConnectFailed(desc);
                }
                iSmartCallback.onFailed(code, desc);
            }
        });
    }

    public void showConnecting() {
        SmartCommHelper.getInstance().setConnectionState(ConnectionState.CONNECTING);
        if (iConnectionListener != null) {
            iConnectionListener.onServerConnecting();
        }
    }

    /**
     * 通过流获取离线消息
     */
    public void getHistoryMsgByStream(ISmartCallback smartCallback) {
        Disposable subscribe = Single.create(new SingleOnSubscribe<Void>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<Void> emitter) throws Throwable {
                boolean resumed = connection.streamWasResumed();
                boolean enabled = connection.isSmEnabled();
                if (resumed || enabled) {
                    try {
                        connection.sendSmAcknowledgement();
                    } catch (SmackException.NotConnectedException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (StreamManagementException.StreamManagementNotEnabledException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        connection.sendNonza(new StreamManagement.Enable(true));
                    } catch (SmackException.NotConnectedException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                emitter.onSuccess(null);
            }
        }).subscribeOn(Schedulers.io()).subscribe(onSuccess -> {
            getSmartCommUserManager().setOnLine(Presence.Type.available);
            smartCallback.onSuccess();
        }, onError -> {
            getSmartCommUserManager().setOnLine(Presence.Type.available);
            smartCallback.onSuccess();
        });

    }

    /**
     * 初始化SDK
     *
     * @param application
     */
    public void initSDK(Application application) {
        SmartCommHelper.getInstance().init(application, true);
        getSmartCommMsgManager();
    }

    public void fetchMessages() {
        checkConnection();
    }

    /**
     * 服务器连接监听
     */
    private class SmartCommConnectionListener implements ConnectionListener {
        @Override
        public void connected(XMPPConnection connection) {
            SmartTrace.w("connected: 连接成功>>>>" + isAuthenticated());
            // 配置重连管理器
            //            ReconnectionManager.getInstanceFor(getConnection()).enableAutomaticReconnection();
        }

        @Override
        public void authenticated(XMPPConnection connection, boolean resumed) {
            smartCommHeartbeat.startHeartDump();
            if (iConnectionListener != null) {
                iConnectionListener.onAuthenticated();
            }
            SmartCommHelper.getInstance().onAuthenticated();
            // 获取 OMEMO 管理器
            SmartTrace.file("登录成功: ");
        }


        @Override
        public void connectionClosed() {
            SmartTrace.w("connectionClosed: -----");
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            SmartTrace.file("connectionClosedOnError: 掉线了..." + e);
            // 这里把连接中的muc 强制离开
            getSmartCommChatRoomManager().checkMucAndForceLeave();
            getSmartCommUserManager().release();
            if ("conflict".contains(e.getMessage())) {
            } else {

            }

        }

        @Override
        public void connecting(XMPPConnection connection) {
            SmartTrace.w("connecting");
        }
    }

    /**
     * 添加好友变化监听
     */
    public void addRosterListener() {
        smartCommUserManager.initData();
        addFriendListener();
        addIncomingMessageListener();
        // 添加一个Presence状态监听器 会监听到多种状态信息 包括muc的
        connection.addStanzaListener(new StanzaListener() {
            @Override
            public void processStanza(Stanza stanza) {
                if (!isAuthenticated()) {
                    return;
                }
                if (stanza instanceof Presence) {
                    Presence presence = (Presence) stanza;
                    Presence.Type type = presence.getType();
                    Jid fromJid = presence.getFrom();
                    EntityBareJid fromBareJid = fromJid.asEntityBareJidIfPossible();
                    EntityFullJid fromEntityFullJid = fromJid.asEntityFullJidIfPossible();
                    String resourceName = "";
                    if (fromEntityFullJid != null) {
                        resourceName = fromEntityFullJid.getResourcepart().toString();
                    }
                    EntityBareJid myBareJid = connection.getUser().asEntityBareJid();
                    if (fromBareJid.equals(myBareJid)) {
                        if (fromEntityFullJid != null) {
                            LoginEventInfo loginEventInfo = new LoginEventInfo();
                            Resourcepart resourcepart = connection.getUser().getResourcepart();
                            loginEventInfo.myResoucerce = resourcepart.toString();
                            loginEventInfo.receivedResoucerce = resourceName;
                            loginEventInfo.presenceType = type;
                            mySubject.onNext(loginEventInfo);
                        }
                        // 这是你自己的状态更新
                        if (type == Presence.Type.available) {
                            // 表示自己处于在线状态
                            Intent intent = new Intent();
                            intent.setAction(SmartConstants.ACCOUNT_STATUS);
                            intent.putExtra(SmartConstants.CURRENT_STATUS,
                                    SmartCommHelper.getInstance().getApplication().getString(R.string.status_online));
                            LocalBroadcastManager.getInstance(SmartCommHelper.getInstance().getApplication())
                                    .sendBroadcast(intent);
                        } else if (type == Presence.Type.unavailable) {
                            Intent intent = new Intent();
                            intent.setAction(SmartConstants.ACCOUNT_STATUS);
                            intent.putExtra(SmartConstants.CURRENT_STATUS,
                                    SmartCommHelper.getInstance().getApplication().getString(R.string.status_offline));
                            LocalBroadcastManager.getInstance(SmartCommHelper.getInstance().getApplication())
                                    .sendBroadcast(intent);
                        } else {
                            // 其他状态
                        }
                    } else {
                        MUCUser mucUser = MUCUser.from(presence);
                        if (mucUser != null) {
                            // 收到了群成员的状态
                            MUCItem item = mucUser.getItem();
                            if (getChatRoomListener() != null && presence.isAvailable()) {
                                Jid jid = item.getJid();
                                if (jid != null) {
                                    // 更新自己的状态 角色变化了 岗位变化了 账号变化了 改名过了
                                    BareJid bareJid = jid.asBareJid();
                                    if (connection.getUser().asBareJid().equals(bareJid)) {
                                        SmartUserInfo smartUserInfo = new SmartUserInfo();
                                        smartUserInfo.setUserId(jid.asBareJid().toString());
                                        MUCRole role = item.getRole();
                                        MUCAffiliation affiliation = item.getAffiliation();
                                        smartUserInfo.setRole(role.name());
                                        smartUserInfo.setAffiliation(affiliation.name());
                                        smartUserInfo.setMemberAccount(resourceName);
                                        smartUserInfo.setNickname(SmartCommHelper.getInstance().getNickname());
                                        getChatRoomListener().updateMemberInfo(fromBareJid.toString(), smartUserInfo);
                                    }
                                }
                            }
                            VCardUpdateExtension vCardTemp = presence.getExtension(VCardUpdateExtension.class);
                            if (vCardTemp != null) {
                                // fromEntityFullJid不为null 不能说明是群成员的头像 因为好友也会发
                                String photoHash = vCardTemp.getPhotoHash();
                                if (!TextUtils.isEmpty(photoHash)) {
                                    // 群成员的avatarHash
                                    SmartCommHelper.getInstance().executeWithDelay(SmartConstants.SHORT_DELAY, () -> {
                                        if (getChatRoomListener() != null) {
                                            getChatRoomListener().receivedMemberAvatarHash(fromEntityFullJid.toString(), photoHash);
                                        }
                                    });
                                }
                            }
                        } else {
                            // unavailable unsubscribe unsubscribed离线或者解除好友unsubscribe 好友注销账号unsubscribe
                            // 这里也可以监听离线 头像更新
                            SmartTrace.d("监听到好友的 状态  >> " + stanza);
                            if (presence.getType() == Presence.Type.unsubscribe) {
                                // 好友关系被解除
                                SmartTrace.d("presenceChanged 好友关系被解除");
                                SmartIMClient.getInstance().getFriendshipManager().receivedFriendDeleted(fromBareJid.toString());
                            } else {
                                VCardUpdateExtension vCardTemp = presence.getExtension(VCardUpdateExtension.class);
                                if (vCardTemp != null) {
                                    // fromEntityFullJid不为null 不能说明是群成员的头像 因为好友也会发
                                    String photoHash = vCardTemp.getPhotoHash();
                                    if (!TextUtils.isEmpty(photoHash)) {
                                        // 群成员的avatarHash
                                        SmartIMClient.getInstance().getSmartCommUserManager().requestAvatarByUserId(fromBareJid.toString());
                                    }
                                    // 好友的头像更新了
                                    SmartTrace.d("好友的头像更新了 >> ");
                                }
                            }

                        }

                    }
                }
            }
        }, new StanzaTypeFilter(Presence.class));
        // 监听IQ消息
        connection.addStanzaListener(new StanzaListener() {
            @Override
            public void processStanza(Stanza stanza) {
                if (stanza instanceof IQ) {
                    IQ iq = (IQ) stanza;
                    // 处理IQ消息
                    if (stanza instanceof VCard) {
                        VCard card = (VCard) stanza;
                        if (getChatRoomListener() != null) {
                            String nickName = card.getNickName();
                            byte[] avatar = card.getAvatar();
                            if (avatar != null) {
                                Jid from = iq.getFrom();
                                if (from != null) {
                                    try {
                                        String groupId = from.asBareJid().toString();
                                        if (from.toString().contains("/")) {
                                            EntityFullJid entityFullJid = JidCreate.entityFullFrom(from);
                                            Resourcepart resourceOrNull = entityFullJid.getResourceOrNull();
                                            SmartTrace.w("接收到用户名片: 用户 -> " + resourceOrNull,
                                                    card.getAvatarHash());
                                            if (resourceOrNull != null) {
                                                String memberAccount = resourceOrNull.toString();
                                                SmartUserInfo userInfo = new SmartUserInfo();
                                                userInfo.setUserAvatar(card.getAvatar());
                                                userInfo.setUserAvatarHash(card.getAvatarHash());
                                                userInfo.setMemberAccount(memberAccount);
                                                userInfo.setGroupId(groupId);
                                                userInfo.setMemberAccount(memberAccount);
                                                getChatRoomListener().receivedMemberInfo(userInfo);
                                            }
                                        } else {
                                            // 也会接到jid的头像 这里有问题
                                            String avatarHash = BitmapUtils.getAvatarHash(avatar);
                                            SmartTrace.w("接收到id名片: -> " + groupId,
                                                    card.getAvatarHash(),
                                                    avatarHash);
                                            SmartUserInfo userInfo = new SmartUserInfo();
                                            userInfo.setUserAvatar(card.getAvatar());
                                            userInfo.setUserAvatarHash(card.getAvatarHash());
                                            getChatRoomListener().updateGroupAvatar(userInfo, groupId);
                                        }
                                    } catch (XmppStringprepException e) {
                                        SmartTrace.w("processStanza: " + e);
                                    }
                                }
                            }
                        }
                    } else {
                    }
                }
            }
        }, new StanzaTypeFilter(IQ.class));

        // 添加 StanzaListener 以监听错误消息
        connection.addAsyncStanzaListener(new StanzaListener() {
            @Override
            public void processStanza(Stanza stanza) {
                if (stanza instanceof Message) {
                    Message message = (Message) stanza;
                    if (message.getType() == Message.Type.error) {
                        if (message.getError().getCondition() == StanzaError.Condition.not_acceptable) {
                            // 提示消息发送错误 应该是不在房间 那么需要重新加入房间
                            String groupId = message.getFrom().toString();
                            if (getSmartCommChatRoomManager().getMucIns(groupId) != null) {
                                SmartTrace.d("提示消息发送错误");
                                getSmartCommChatRoomManager().rejoinRoom(groupId, new IChatRoomCallback() {
                                });
                                getSimpleMsgListener().receivedErrorMessage(message.getStanzaId(),
                                        0,
                                        SmartCommHelper.getInstance().getString(R.string.SEND_FAILED_CANT_JOIN_GROUP));
                            }

                        } else if (message.getError().getCondition() == StanzaError.Condition.service_unavailable) {
                            // 被对方拉黑会触发这个错误
                            SmartTrace.d("processStanza: 被对方拉黑了");
                            getSimpleMsgListener().receivedErrorMessage(message.getStanzaId(),
                                    SmartConstants.Error.BLOCKED_BY_USER,
                                    SmartCommHelper.getInstance().getString(R.string.BLOCKED_BY_USER));
                        } else if (message.getError().getCondition() == StanzaError.Condition.forbidden) {
                            getSimpleMsgListener().receivedErrorMessage(message.getStanzaId(),
                                    SmartConstants.Error.FORBIDDEN,
                                    SmartCommHelper.getInstance().getString(R.string.send_failed_forbidden));
                        }
                    }
                }
            }
        }, new StanzaTypeFilter(Message.class));
        OmemoHelper.getInstance().publishDevice();
    }


    /**
     * 连接xmpp服务
     *
     * @return
     */
    public @NonNull void connect(ISmartCallback smartCallback) {
        Disposable subscribe = Observable.fromCallable(() -> {
                    // 初始化连接配置
                    try {
                        if (null != connection) {
                            SmartTrace.w(
                                    "should be remove connectionlistener......remove xmppConnectionListener");
                            // 移除监听
                            //                            release();
                        }
                        SmartTrace.d(
                                getSmartCommConfig().getDomainName() + ">>>>start connect------------------------------------------------ ",
                                getSmartCommConfig().getSecurityMode());
                        KeyStore keystore = SSLHelper.getKeyStoreFromResources(SmartCommHelper.getInstance().getApplication());
                        // Set up SSL context
                        SSLContext sslContext = SSLContext.getInstance("TLS");
                        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                                TrustManagerFactory.getDefaultAlgorithm());
                        trustManagerFactory.init(keystore);
                        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                                KeyManagerFactory.getDefaultAlgorithm());
                        keyManagerFactory.init(keystore, "111111".toCharArray());
                        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
                        XMPPTCPConnectionConfiguration.Builder builder = XMPPTCPConnectionConfiguration.builder();
                        builder.setXmppDomain(getSmartCommConfig().getDomainName())
                                .setHost(getSmartCommConfig().getHostAddress())
                                // 默认端口，根据实际情况调整
                                .setPort(getSmartCommConfig().getPort())
                                // 根据需要调整安全模式
                                .setSecurityMode(getSmartCommConfig().getSecurityMode())
                                // 不发送在线状态 以离线方式登录,以便获取离线消息
                                .setSendPresence(false)
                                .setResource(OtherUtil.getAndroidID())
                                .setConnectTimeout(20 * 1000)
                                .setCustomSSLContext(sslContext)
                                .setCustomX509TrustManager(new X509TrustManager() {
                                    @Override
                                    public X509Certificate[] getAcceptedIssuers() {
                                        return new X509Certificate[0];
                                    }

                                    @Override
                                    public void checkClientTrusted(
                                            X509Certificate[] certs, String authType) {
                                    }

                                    @Override
                                    public void checkServerTrusted(
                                            X509Certificate[] certs, String authType) {
                                    }
                                });

                        if (BuildConfig.DEBUG || true) {
                            builder.enableDefaultDebugger(); // 启用调试模式
                        }
                        XMPPTCPConnectionConfiguration config = builder.build();
                        // 创建连接
                        connection = new XMPPTCPConnection(config);
                        // 启用流管理支持。如果服务器支持，SMACK 将启用 SM，因此不需要进行额外的检查。
                        connection.setUseStreamManagement(true);
                        connection.setUseStreamManagementResumption(false);
                        // 设置回复超时10秒
                        connection.setReplyTimeout(10 * 1000);
                        // 默认情况下，Smack 在解析错误时会断开连接
                        connection.setParsingExceptionCallback(new ExceptionLoggingCallback());
                        //设置需要经过同意才可以加为好友
                        Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);
                        // 连接到服务器并登录
                        connection.connect();
                        // 连接, 可设置监听
                        smartCommConnectionListener = new SmartCommConnectionListener();
                        connection.addConnectionListener(smartCommConnectionListener);
                        smartCommHeartbeat = new SmartCommHeartbeat();
                        OmemoHelper.getInstance().init(connection);
                        return connection;
                    } catch (SmackException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (XMPPException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(xmpptcpConnection -> {
                    if (null != xmpptcpConnection) {
                        smartCallback.onSuccess();
                    } else {
                        smartCallback.onFailed(SmartConstants.Error.CONNECTION_FAILED,
                                SmartCommHelper.getInstance().getApplication().getString(R.string.CONNECTION_FAILED));
                    }
                }, onError -> {
                    SmartTrace.d("onError " + onError);
                    smartCallback.onFailed(SmartConstants.Error.CONNECTION_FAILED,
                            SmartCommHelper.getInstance().getApplication().getString(R.string.CONNECTION_FAILED));
                });
    }

    /**
     * 添加单聊一对一消息监听器
     */
    public void addIncomingMessageListener() {
        if (isAuthenticated()) {
            ChatManager chatManager = ChatManager.getInstanceFor(connection);
            if (mIncomingChatMessageListener != null) {
                chatManager.removeIncomingListener(mIncomingChatMessageListener); // 先移除已有的监听器
            }
            mIncomingChatMessageListener = new IncomingChatMessageListener() {
                @Override
                public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
                    processMsg(false, message);
                }
            };
            SmartTrace.d("addIncomingMessageListener: 添加消息监听器 ");
            chatManager.addIncomingListener(mIncomingChatMessageListener);
            MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);
            manager.addInvitationListener(new InvitationListener() {
                /**
                 *
                 * @param conn- 收到邀请的 XMPPConnection。
                 * @param room- 邀请所指的房间。
                 * @param inviter- 发送邀请的邀请者。（例如 crone1@shakespeare.lit）。
                 * @param reason- 邀请人发送邀请的原因。
                 * @param password- 加入聊天室时使用的密码。
                 * @param message- 邀请者用来发送邀请的消息。
                 * @param invitation- 随消息一起收到的原始邀请。
                 */
                @Override
                public void invitationReceived(XMPPConnection conn,
                                               MultiUserChat room,
                                               EntityJid inviter,
                                               String reason,
                                               String password, Message message,
                                               MUCUser.Invite invitation) {
                    // 收到群聊邀请自动加入房间
                    String toJid = connection.getUser().asEntityBareJidString();
                    SmartTrace.d(
                            "Received invitation from " + inviter + " to join " + room.getRoom(),
                            " reason: " + reason,
                            toJid);
                    String inviterJid = inviter.asEntityBareJid().toString();
                    // 先加入群
                    getSmartCommChatRoomManager().realJoinRoom(room.getRoom().toString(),
                            new IChatRoomCallback() {
                                @Override
                                public void joinRoomSuccess(String groupId) {
                                    // 获取房间信息 这里会重复 因为回调给了app 也会获取roominfo
                                    // 按微信逻辑 收到邀请直接是加群的逻辑
                                    // 现在用的是私聊邀请入群

                                   /* getSmartCommChatRoomManager().getRoomInfo(multiUserChat.getRoom().toString(), new IChatRoomCallback() {
                                        @Override
                                        public void getRoomInfo(RoomInfo roomInfo) {
                                            preInvitation(roomInfo, multiUserChat, inviterJid, reason, password, message, invitation);
                                        }

                                        @Override
                                        public void getRoomInfoFailed() {
                                            preInvitation(null, multiUserChat, inviterJid, reason, password, message, invitation);
                                        }
                                    });*/
                                }
                            });
                }
            });
        } else {
            // 处理连接未建立的情况
            SmartTrace.d("addIncomingMessageListener: 处理连接未建立的情况");
        }
    }

    /**
     * 预处理邀请
     *
     * @param roomInfo
     * @param multiUserChat
     * @param inviterJid
     * @param reason
     * @param password
     * @param message
     * @param invitation
     */
    private void preInvitation(SmartGroupInfo roomInfo, MultiUserChat multiUserChat,
                               String inviterJid, String reason, String password, Message message, MUCUser.Invite invitation) {
        // 收到邀请加入房间后查询群成员
        getSmartCommChatRoomManager().getGroupMemberList(multiUserChat.getRoom().toString(), new IGroupMemberCallback() {
            @Override
            public void onSuccess(List<SmartUserInfo> smartUserInfoList) {
                // 也得传roominfo
//                getChatRoomListener().invitationReceived(multiUserChat, roomInfo, inviterJid, reason, password,
//                        message, invitation, smartUserInfoList);
            }
        });
    }

    public void processMsg(boolean isHistoryMsg, Message message) {
        boolean isGroupMsg = message.getType() == Message.Type.groupchat;
        boolean isSingleMsg = message.getType() == Message.Type.chat;
        // 群聊消息
        boolean isGroupPrivateMsg = false;
        if (isGroupMsg || isSingleMsg) {
            // 更新或添加会话到您的列表 {from='m1918', to='m1920@tigase.bndg.cn', msg='7'}
            // 检查消息是否包含 <archived> 扩展
            String archivedId = "";
            // 获取发送时间的扩展-有可能没有
            Date timestamp = null;
            // 获取消息类型
            String messageType = SmartContentType.TEXT;
            // 获取Jid 群聊可能携带
            String senderUserId = "";
            // 获取群聊发送者昵称
            String senderNickname = "";
            TestGroupMembersExtension testGroupMembersExtension = null;
            // 处理收到的消息 单聊是entitybare  群聊是entityfull
            Jid messageFromJid = message.getFrom();
            SmartMessage msgEntity = new SmartMessage();
            List<ExtensionElement> messageExtensions = message.getExtensions();
            String oobUrl = "";
            for (ExtensionElement extension : messageExtensions) {
                if (ArchivedExtension.ELEMENT.equals(extension.getElementName())) {
                    ArchivedExtension archivedExtension = (ArchivedExtension) extension;
                    archivedId = archivedExtension.getId();
                } else if (DelayInformation.ELEMENT.equals(extension.getElementName())) {
                    DelayInformation delayInformation = (DelayInformation) extension;
                    timestamp = delayInformation.getStamp();
                } else if (MessageTypeExtension.ELEMENT.equals(extension.getElementName())) {
                    MessageTypeExtension typeExtension = (MessageTypeExtension) extension;
                    messageType = typeExtension.getType();
                } else if (SenderInfoExtension.ELEMENT_NAME.equals(extension.getElementName())) {
                    SenderInfoExtension senderInfoExtension = (SenderInfoExtension) extension;
                    senderUserId = senderInfoExtension.getSenderBareJid();
                    senderNickname = senderInfoExtension.getSenderNickname();
                } else if (OobDataExtension.NAMESPACE.equals(extension.getNamespace())) {
                    OobDataExtension oobDataExtension = (OobDataExtension) extension;
                    oobUrl = oobDataExtension.getUrl();
                } else if (TestGroupMembersExtension.ELEMENT_NAME.equals(extension.getElementName())) {
                    testGroupMembersExtension = (TestGroupMembersExtension) extension;
                } else if (OmemoElement_VAxolotl.NAME_ENCRYPTED.equals(extension.getElementName())) {
                    // 含 有encrypted 判断能否解密？
                    return;
                } else {
                    if (MUCUser.NAMESPACE.equals(extension.getNamespace())) {
                        MUCUser mucUser = (MUCUser) extension;
                        if (mucUser.getStatus() != null && mucUser.getStatus().contains(MUCUser.Status.create("104"))
                        ) {
                            // 'This room is not anonymous' 104也可能是销毁房间
                            // 处理房间名称更新
                            String groupId = messageFromJid.asBareJid().toString();
                            SmartCommHelper.getInstance().executeWithDelay(500, () -> {
                                RoomChat roomChat = getSmartCommChatRoomManager().getRoomChat(groupId);
                                if (roomChat != null && roomChat.getState().inUse()) {
                                    SmartTrace.d("处理房间状态更新");
                                    getSmartCommChatRoomManager().roomNameUpdated(groupId);
                                }
                            });
                            return;
                        }
                        if (isSingleMsg) {
                            // 证明是私信
                            isGroupPrivateMsg = true;
                        }
                    }
                    if (!Message.BODY.equals(extension.getElementName())) {
                        msgEntity.addExtensions(extension.toXML());
                    }
                }
            }
            // 有oob数据 并且类型是TEXT 说明是其他客户端发送的
            if (!TextUtils.isEmpty(oobUrl) && SmartContentType.TEXT.equals(messageType)) {
                messageType = OtherUtil.getMessageTypeByUrl(oobUrl);
            }
            if (testGroupMembersExtension != null) {
                if (timestamp != null) {
                    //timestamp != null 才是实时消息 书签里没有群聊才加入
                    boolean hasRoom = BookmarksManager.getInstance().hasRoom(testGroupMembersExtension.getRoomId());
                    if (hasRoom) {
                        SmartTrace.w("书签里已经存在该群聊");
                        return;
                    }
                    TestGroupMembersExtension finalTestGroupMembersExtension = testGroupMembersExtension;
                    SmartCommHelper.getInstance().executeWithDelay(500, () -> {
                        getSmartCommChatRoomManager().realJoinRoom(finalTestGroupMembersExtension.getRoomId(), new IChatRoomCallback() {
                            @Override
                            public void joinRoomSuccess(String groupId) {
                                getSimpleMsgListener()
                                        .receivedTestGroupMembers(messageFromJid.asEntityBareJidIfPossible().toString(),
                                                finalTestGroupMembersExtension.getRoomId(),
                                                finalTestGroupMembersExtension.getMemberIds(),
                                                finalTestGroupMembersExtension.getMemberNicknames());
                            }
                        });
                    });
                }
                return;
            }
            msgEntity.setArchivedId(archivedId);
            msgEntity.setHistoryMsg(isHistoryMsg);
            String stanzaId = message.getStanzaId();
            if (stanzaId != null) {
                msgEntity.setMessageId(stanzaId);
            } else {
                // 拉取的历史消息可能是null
                StanzaIdElement extensionStanzaId = StanzaIdElement.getStanzaId(message);
                if (extensionStanzaId != null) {
                    msgEntity.setMessageId(extensionStanzaId.getId());
                } else {
                    OriginIdElement originId = OriginIdElement.getOriginId(message);
                    if (originId != null) {
                        msgEntity.setMessageId(originId.getId());
                    } else {
                        msgEntity.setMessageId(System.currentTimeMillis() + "");
                    }
                }
            }
            // 在muc私信的时候 from是groupid 但是 type是chat
            Jid messageTo = message.getTo();
            String toUserId = messageTo == null ? "" : messageTo.asBareJid().toString();
            String fromUserId = msgEntity.getFromUserId();
            if (isGroupMsg || isGroupPrivateMsg) {
                EntityFullJid fromFullJid = messageFromJid.asEntityFullJidIfPossible();
                if (null == fromFullJid) {
                    return;
                }
                String groupId = fromFullJid.asEntityBareJid().toString();
                Resourcepart resourceOrNull = fromFullJid.getResourceOrNull();
                if (null == resourceOrNull) {
                    return;
                }
                String memberAccount = resourceOrNull.toString();
                String memberJid = fromFullJid.toString();
                MultiUserChat mucIns = getSmartCommChatRoomManager().getMucIns(groupId);
                if (mucIns != null) {
                    Occupant occupant = mucIns.getOccupant(fromFullJid);
                    if (occupant != null) {
                        // 如果对方已不再群中 可能是null
                        Jid jid = occupant.getJid();
                        if (jid != null) {
                            SmartTrace.d("获取到了jid = " + jid.toString());
                            memberJid = jid.asBareJid().toString();
                        }
                    }
                    msgEntity.setFromUserId(TextUtils.isEmpty(senderUserId) ? memberJid : senderUserId);
                    // 如果能获取到真实昵称
                    msgEntity.setGroupSenderNickname(TextUtils.isEmpty(senderNickname) ? memberAccount : senderNickname);
                    if (timestamp == null) {
                        if (JidCreate.entityFullFrom(mucIns.getRoom(), mucIns.getNickname()).equals(fromFullJid)) {
                            return;
                        }
                    }
                } else {
                    SmartTrace.file("出现了muc为null的情况");
                }
                // 群消息这里 to 也有可能是发给我的
                // mam拉取的消息没有to
                if (messageTo != null) {
                    msgEntity.setToUserId(toUserId);
                }
                msgEntity.setGroupId(groupId);
                msgEntity.setConversationType(SmartConversationType.GROUP.name());
                msgEntity.setGroupSubject(message.getSubject());
                if (isGroupPrivateMsg) {

                } else {
                    if (message.getBody() != null) {
                        FetchEntity fetchEntity = getSmartCommChatRoomManager().getFetchEntity(msgEntity.getGroupId());
                        if (fetchEntity == null) {
                            // 如果没有历史消息 新建一个
                            fetchEntity = new FetchEntity();
                            fetchEntity.archivedId = archivedId;
                            fetchEntity.createTime = timestamp == null ? System.currentTimeMillis() : timestamp.getTime();
                            fetchEntity.conversationId = msgEntity.getGroupId();
                        } else {
                            // 更新消息时间 存在拉取历史消息的时候收到新消息的情况
                            if (!fetchEntity.needPull) {
                                // 如果不需要拉取历史消息 更新为收到消息的时间
                                fetchEntity.createTime = timestamp == null ? System.currentTimeMillis() : timestamp.getTime();
                            }
                        }
                        getSmartCommChatRoomManager().putRecordMsg(msgEntity.getGroupId(), fetchEntity);
                    }
                }
            } else {
                String jid = messageFromJid.asEntityBareJidIfPossible().toString();
                // from 有可能是我发送的 因为是历史消息
                msgEntity.setFromUserId(jid);
                if (jid.equals(toUserId)) {
                    // 忽略自己发送的消息
                    return;
                }
                // 获取发送者昵称 如果是其他xmpp客户端发送的没有nickname
                if (TextUtils.isEmpty(senderNickname)) {
                    SmartTrace.w("no nickname");
                    senderNickname = getSmartCommUserManager().getUserNicknameSync(jid, true);
                }
                msgEntity.setFromNickname(senderNickname);
                msgEntity.setToUserId(toUserId);
                msgEntity.setConversationType(SmartConversationType.SINGLE.name());
            }
            // 离线消息 timestamp不是null
            msgEntity.setOfflineMsg(timestamp != null);
            msgEntity.setMessageContent(message.getBody());
            msgEntity.setMessageType(messageType);
            // 这个时间 由于对方设备时间不同 所以这里需要处理一下
            long time = new Date().getTime();
            long createTime = timestamp == null ? time : Math.min(timestamp.getTime(), time);
            msgEntity.setCreateTime(createTime);
            SmartTrace.d(messageType + ">>>>消息监听器>>>>" + message.getBody());
            if (SmartContentType.LEAVE_ROOM.equals(messageType)) {
                // 只处理在线的退群消息
                if (timestamp == null) {
                    if (SmartIMClient.getInstance().getChatRoomListener() != null) {
                        SmartIMClient.getInstance()
                                .getChatRoomListener()
                                .memberLeave(msgEntity.getGroupId(), SmartCommHelper.getAccountFromJid(fromUserId));
                    }
                }
                return;
            }
            getSimpleMsgListener().receivedSmartMessage(msgEntity);
        } else {
            SmartTrace.d("其他消息processmsg", message);
        }
    }

    /**
     * 关闭连接
     *
     * @return
     */
    public void logout(ISmartCallback iSmartCallback) {
        Disposable subscribe = Observable.fromCallable(() -> {
                    if (connection != null && connection.isConnected()) {
                        //设为下线
                        StorageUtils.getInstance(SmartConstants.SP_NAME).remove(SmartConstants.USER_NAME);
                        StorageUtils.getInstance(SmartConstants.SP_NAME).remove(SmartConstants.PASS_WORD);
                        Roster.getInstanceFor(connection)
                                .removeRosterListener(mReceiverFriendStatusListener);
                        Roster.getInstanceFor(connection)
                                .removeSubscribeListener(mAddFriendMessageListener);
                        smartCommHeartbeat.stopHeartbeat();
                        SmartCommHelper.getInstance().setAccount("");
                        if (smartCommChatRoomManager != null) {
                            smartCommChatRoomManager.release();
                        }
                        if (smartCommUserManager != null) {
                            smartCommUserManager.release();
                        }
                        if (smartCommFriendManager != null) {
                            smartCommFriendManager.release();
                        }
                        SmartCommHelper.getInstance().release();
                        iSmartCallback.onSuccess();
                        connection.disconnect();
                    }
                    return true;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess -> {
                    if (onSuccess) {
                        iSmartCallback.onSuccess();
                    } else {
                        iSmartCallback.onFailed(SmartConstants.Error.LOGOUT_FAILED,
                                SmartCommHelper.getInstance().getApplication().getString(R.string.LOGOUT_FAILED));
                    }
                }, onError -> {
                    iSmartCallback.onFailed(SmartConstants.Error.LOGOUT_FAILED,
                            SmartCommHelper.getInstance().getApplication().getString(R.string.LOGOUT_FAILED));
                });
    }

    /**
     * 添加关于好友的监听
     */
    private void addFriendListener() {
        SmartTrace.d("addFriendListener: 添加好友监听");
        // 获取当前角色
        Roster roster = Roster.getInstanceFor(connection);
        if (mReceiverFriendStatusListener != null) {
            roster.removeRosterListener(mReceiverFriendStatusListener);
        }
        if (mAddFriendMessageListener != null) {
            roster.removeSubscribeListener(mAddFriendMessageListener);
        }
        // 接收到好友信息变化
        mReceiverFriendStatusListener = new ReceiverFriendStatusListener();
        roster.addRosterListener((RosterListener) mReceiverFriendStatusListener);
        // 接收到添加好友信息
        mAddFriendMessageListener = new AddFriendMessageListener();
        roster.addSubscribeListener((SubscribeListener) mAddFriendMessageListener);
    }

    /**
     * 是否已经连接服务器
     *
     * @return
     */
    private boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    /**
     * 是否已经登录成功
     *
     * @return
     */
    public boolean isAuthenticated() {
        return connection != null && connection.isConnected() && connection.isAuthenticated();
    }

}
