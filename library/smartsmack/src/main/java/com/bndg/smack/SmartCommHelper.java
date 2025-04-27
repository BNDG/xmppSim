package com.bndg.smack;

import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;


import org.jetbrains.annotations.Nullable;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.android.AndroidSmackInitializer;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.XmlEnvironment;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.xml.XmlPullParser;
import org.jivesoftware.smack.xml.XmlPullParserException;
import org.jivesoftware.smackx.bookmarks.BookmarkedConference;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.disco.packet.DiscoverItems;
import org.jivesoftware.smackx.formtypes.FormFieldRegistry;
import org.jivesoftware.smackx.forward.packet.Forwarded;
import org.jivesoftware.smackx.forward.provider.ForwardedProvider;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;
import org.jivesoftware.smackx.reference.ReferenceManager;
import org.jivesoftware.smackx.reference.element.ReferenceElement;
import org.jivesoftware.smackx.reference.provider.ReferenceProvider;
import org.jivesoftware.smackx.xdata.FormField;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.bndg.smack.callback.IBookmarkedConferenceCallback;
import com.bndg.smack.callback.IChatRoomCallback;
import com.bndg.smack.callback.IConnectionListener;
import com.bndg.smack.callback.IServiceCallback;
import com.bndg.smack.callback.ISmartCallback;
import com.bndg.smack.constant.SmartConstants;
import com.bndg.smack.enums.ConnectionState;
import com.bndg.smack.exceptions.SIMConnectionException;
import com.bndg.smack.extensions.ArchivedExtension;
import com.bndg.smack.extensions.MessageTypeExtension;
import com.bndg.smack.extensions.OobDataExtension;
import com.bndg.smack.extensions.SenderInfoExtension;
import com.bndg.smack.extensions.TestGroupMembersExtension;
import com.bndg.smack.extensions.VCardUpdateExtension;
import com.bndg.smack.extensions.avatar.AvatarData;
import com.bndg.smack.extensions.avatar.AvatarDataProvider;
import com.bndg.smack.extensions.avatar.AvatarMetadata;
import com.bndg.smack.extensions.avatar.AvatarMetadataProvider;
import com.bndg.smack.extensions.base.BaseExtensionElement;
import com.bndg.smack.extensions.base.BaseExtensionElementProvider;
import com.bndg.smack.extensions.base.IExtensionProvider;
import com.bndg.smack.extensions.base.ProviderFactory;
import com.bndg.smack.muc.RoomChat;
import com.bndg.smack.muc.RoomState;
import com.bndg.smack.service.SmartIMService;
import com.bndg.smack.utils.NetworkStateListener;
import com.bndg.smack.utils.OtherUtil;
import com.bndg.smack.utils.SmartTrace;
import com.bndg.smack.utils.StorageUtils;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @author r
 * @date 2024/5/17
 * @description
 */

public class SmartCommHelper {
    public static final String SEPARATOR_COMM = ":@#:";
    private HashMap<String, Integer> retryJoinMap = new HashMap<>();
    private LinkedList<EntityBareJid> leaveBeforeJoinMap = new LinkedList<>();
    private NetworkStateListener networkStateListener;
    private ConnectionState connectionState = ConnectionState.UNKNOWN;

    public Application getApplication() {
        return application;
    }

    private Application application;
    private CompositeDisposable disposable = new CompositeDisposable();

    private SmartCommHelper() {

    }

    private static volatile SmartCommHelper INSTANCE;

    public static SmartCommHelper getInstance() {
        if (INSTANCE == null) {
            synchronized (SmartCommHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SmartCommHelper();
                }
            }
        }
        return INSTANCE;
    }

    public static String getAccountFromJid(String jidString) {
        // 使用字符串处理方法提取用户名
        int atIndex = jidString.indexOf('@');
        return jidString.substring(0, atIndex);
    }

    public static String getMemberAccountFromUserId(String idString) {
        // 使用字符串处理方法提取用户名
        int atIndex = idString.indexOf('/');
        if (atIndex == -1) {
            return idString;
        }
        return idString.substring(atIndex + 1);
    }

    public void init(Application application, boolean isLogEnable) {
        this.application = application;
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        AndroidSmackInitializer.initialize(application);
                        ProviderManager.addExtensionProvider(MessageTypeExtension.ELEMENT,
                                MessageTypeExtension.NAMESPACE,
                                new ExtensionElementProvider<MessageTypeExtension>() {
                                    @Override
                                    public MessageTypeExtension parse(XmlPullParser parser, int initialDepth,
                                                                      XmlEnvironment xmlEnvironment) throws XmlPullParserException, IOException {
                                        String type = parser.nextText();
                                        return new MessageTypeExtension(type);
                                    }
                                });
                        ProviderManager.addExtensionProvider(SenderInfoExtension.ELEMENT_NAME,
                                SenderInfoExtension.NAMESPACE,
                                new SenderInfoExtension.Provider());
                        ProviderManager.addExtensionProvider(ArchivedExtension.ELEMENT, ArchivedExtension.NAMESPACE, new ArchivedExtension.Provider());
                        ProviderManager.addExtensionProvider(VCardUpdateExtension.ELEMENT_NAME, VCardUpdateExtension.NAMESPACE, new VCardUpdateExtension.Provider());
                        ProviderManager.addExtensionProvider(OobDataExtension.ELEMENT_NAME, OobDataExtension.NAMESPACE, new OobDataExtension.Provider());
                        ProviderManager.addExtensionProvider(ReferenceElement.ELEMENT, ReferenceManager.NAMESPACE, new ReferenceProvider());
                        ProviderManager.addExtensionProvider(Forwarded.ELEMENT, Forwarded.NAMESPACE, new ForwardedProvider());
                        // XEP-0084: User Avatar (metadata) + notify
                        ProviderManager.addExtensionProvider(AvatarMetadata.ELEMENT, AvatarMetadata.NAMESPACE,
                                new AvatarMetadataProvider());

                        // XEP-0084: User Avatar (data)
                        ProviderManager.addExtensionProvider(AvatarData.ELEMENT, AvatarData.NAMESPACE, new AvatarDataProvider());
                        FormFieldRegistry.register("http://jabber.org/protocol/muc#roominfo", "muc#roomconfig_presencebroadcast", FormField.Type.text_multi);
                        return true;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess -> {
                    if (onSuccess) {

                    } else {

                    }
                }, onError -> {

                });
        SmartTrace.d("AndroidSmackInitializer");
        SmartTrace.getConfig().setLogSwitch(isLogEnable)// 设置log总开关，默认开
                .setGlobalTag("SmartSmack")// 设置log全局标签，默认为空
                // 当全局标签不为空时，我们输出的log全部为该tag，
                // 为空时，如果传入的tag为空那就显示类名，否则显示tag
                .setSaveDays(7)
                .setLog2FileSwitch(false)// 打印log时是否存到文件的开关，默认关
                .setBorderSwitch(true);// 输出日志是否带边框开关，默认开
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Throwable {
                if (throwable instanceof SIMConnectionException) {
                    SmartTrace.w("sendSingleMessage: " + throwable.getMessage());
                }
            }
        });
        Intent serviceIntent = new Intent(application, SmartIMService.class);
        application.startService(serviceIntent);
        OmemoHelper.getInstance().setUp(application);
        networkStateListener = new NetworkStateListener(application);
        networkStateListener.registerNetworkCallback();
    }

    public void addExtensionProvider(String elementName, String nameSpace, IExtensionProvider<?> provider) {
        ProviderManager.addExtensionProvider(TestGroupMembersExtension.ELEMENT_NAME, TestGroupMembersExtension.NAMESPACE,
                ProviderFactory.createProvider(new TestGroupMembersExtension.Provider()));
        BaseExtensionElementProvider<BaseExtensionElement> providerIns = ProviderFactory.createProvider(provider);
        ProviderManager.addExtensionProvider(elementName, nameSpace, providerIns);
    }

    public void discoverMultiUserChatSupport(IServiceCallback callback) {
        Disposable subscribe = Single.fromCallable(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        if (SmartIMClient.getInstance().isAuthenticated()) {
                            ServiceDiscoveryManager discoManager = ServiceDiscoveryManager.getInstanceFor(SmartIMClient.getInstance().getConnection());
                            // 发送服务发现请求
                            DiscoverItems result = null;
                            try {
                                result = discoManager.discoverItems(JidCreate.domainBareFrom(SmartIMClient.getInstance().getConnection().getXMPPServiceDomain()));
                                // 解析返回结果
                                for (DiscoverItems.Item item : result.getItems()) {
                                    Jid entityJid = item.getEntityID();
                                    DiscoverInfo info = discoManager.discoverInfo(entityJid);
                                    // 检查是否支持多人聊天
                                    if (info.containsFeature("http://jabber.org/protocol/muc")) {
                                        return entityJid.toString();
                                    }
                                }
                                return "";
                            } catch (SmackException.NoResponseException | XmppStringprepException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        return "";
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mucService -> {
                    if (!TextUtils.isEmpty(mucService)) {
                        callback.onSuccess(mucService);
                    } else {
                        callback.onFailed(SmartConstants.Error.MUC_UNAVAILABLE, application.getString(R.string.muc_unavailable));
                    }
                }, onError -> {
                    callback.onFailed(SmartConstants.Error.MUC_UNAVAILABLE, application.getString(R.string.muc_unavailable));
                });
    }

    public void setAccount(String userName) {
        StorageUtils.getInstance(SmartConstants.SP_NAME).put(SmartConstants.USER_NAME, userName);
    }

    public void setPassword(String passWord) {
        StorageUtils.getInstance(SmartConstants.SP_NAME).put(SmartConstants.PASS_WORD, passWord);
    }

    /**
     * 设置昵称缓存 key是账户名+NICKNAME
     * @param nickName
     */
    public void setNickName(String nickName) {
        StorageUtils.getInstance(SmartConstants.SP_NAME).put(getUserId() + SmartConstants.NICKNAME, nickName);
    }

    /**
     * 获取IM的用户名 不包含域名
     * @return
     */
    public String getAccount() {
        return StorageUtils.getInstance(SmartConstants.SP_NAME).getString(SmartConstants.USER_NAME);
    }

    /**
     * 设置我在群聊中的账户Id
     * @param groupId
     * @param account
     */
    public void setAccountInGroup(String groupId, String account) {
        int hashKey = (getUserId() + groupId).hashCode();
        StorageUtils.getInstance(SmartConstants.SP_NAME).put(hashKey + SmartConstants.NICKNAME, account);
    }

    /**
     * 获取我在群聊中的账户Id
     *
     * @param groupId
     * @return
     */
    public String getAccountIdInGroup(String groupId) {
        int hashKey = (getUserId() + groupId).hashCode();
        String nickName = StorageUtils.getInstance(SmartConstants.SP_NAME).getString(hashKey + SmartConstants.NICKNAME);
        if (TextUtils.isEmpty(nickName)) {
            // 如果没有昵称 默认是账户名
            nickName = getAccount();
        }
        return nickName;
    }

    public String getUserId() {
        return getAccount() + "@" + SmartIMClient.getInstance().getSmartCommConfig().getDomainName();
    }

    public String getPassword() {
        return StorageUtils.getInstance(SmartConstants.SP_NAME).getString(SmartConstants.PASS_WORD);
    }

    public String getNickname() {
        String nickName = StorageUtils.getInstance(SmartConstants.SP_NAME).getString(getUserId() + SmartConstants.NICKNAME);
        if (TextUtils.isEmpty(nickName)) {
            return getAccount();
        } else {
            return nickName;
        }
    }

    /**
     * 生成一个不重复的群聊id
     *
     * @param serviceName
     * @return
     */
    public String generateGroupId(String serviceName) {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8); // 使用UUID生成唯一的后缀
        return getAccount() + "-" + uniqueSuffix + "@" + serviceName;
    }

    /**
     * 通过用户名获取userid
     *
     * @param account
     * @return
     */
    public String getUserIdByAccount(String account) {
        return account + "@" + SmartIMClient.getInstance().getSmartCommConfig().getDomainName();
    }

    /**
     * 检查是否在muc中 不准确
     *
     * @return true 在 false 不在
     */
    public boolean checkMucJoined(String groupId) {
        RoomChat roomChat = SmartIMClient.getInstance().getSmartCommChatRoomManager().getRoomChat(groupId);
        if (roomChat == null) {
            return false;
        }
        MultiUserChat multiUserChat = roomChat.getMultiUserChat();
        if (multiUserChat == null || !multiUserChat.isJoined()) {
            return false;
        }
        Resourcepart nickname = multiUserChat.getNickname();
        Occupant occupant = multiUserChat.getOccupant(JidCreate.entityFullFrom(multiUserChat.getRoom(), nickname));
        return null != occupant;
    }

    /**
     * 登录成功后拉取历史消息
     * 重连登录成功后
     * 进群
     */
    public void onAuthenticated() {
        setConnectionState(ConnectionState.LOADING);
        Disposable subscribe1 = Observable.just(true)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    // 缓存 SimConnectionListener 实例，避免重复调用
                    IConnectionListener listener = SmartIMClient.getInstance().getSimConnectionListener();
                    // 双重检查以确保线程安全
                    if (listener != null) {
                        listener.onAuthenticated();
                        listener.onChatDataLoading();
                    }
                }, error -> {
                    // 处理订阅过程中发生的错误
                });
        Disposable subscribe = Observable.just(true)
                .delay(50, TimeUnit.MILLISECONDS, Schedulers.io())
                .subscribe(new Consumer<Boolean>() {
                               @Override
                               public void accept(Boolean aBoolean) throws Throwable {
                                   if (SmartIMClient.getInstance().isAuthenticated()) {
                                       SmartIMClient.getInstance().getHistoryMsgByStream(new ISmartCallback() {
                                           @Override
                                           public void onSuccess() {
                                               SmartIMClient.getInstance().getSmartCommChatRoomManager()
                                                       .getRoomsByBookmark(
                                                               new IBookmarkedConferenceCallback() {
                                                                   @Override
                                                                   public void onSuccess(List<BookmarkedConference> conferences) {
                                                                       LinkedList<EntityBareJid> jids = new LinkedList<>();
                                                                       for (BookmarkedConference conference : conferences) {
                                                                           EntityBareJid jid = conference.getJid();
                                                                           jids.add(jid);
                                                                           SmartTrace.d("群 id = " + jid);
                                                                       }
                                                                       retryJoinMap.clear();
                                                                       leaveBeforeJoinMap.clear();
                                                                       SmartTrace.d(jids);
                                                                       joinSequentially(jids);
                                                                   }
                                                               });
                                           }

                                           @Override
                                           public void onFailed(int code, String desc) {
                                               setConnectionState(ConnectionState.LOADED);
                                               if (SmartIMClient.getInstance().getSimConnectionListener() != null) {
                                                   SmartIMClient.getInstance().getSimConnectionListener().onChatDataLoaded();
                                               }
                                           }
                                       });
                                   }
                               }
                           }
                );


    }

    /**
     * 检查muc的加入状态
     */
    private void checkMucConnection() {
        if (!SmartIMClient.getInstance().isAuthenticated()) {
            return;
        }
        if (!OtherUtil.isConnected()) {
            SmartTrace.file("当前网络未连接");
            return;
        }
        SmartTrace.file("检查muc的加入状态");
        SmartIMClient.getInstance().getSmartCommChatRoomManager()
                .getRoomsByBookmark(
                        new IBookmarkedConferenceCallback() {
                            @Override
                            public void onSuccess(List<BookmarkedConference> conferences) {
                                LinkedList<EntityBareJid> jids = new LinkedList<>();
                                for (BookmarkedConference conference : conferences) {
                                    EntityBareJid jid = conference.getJid();
                                    RoomChat roomChat = SmartIMClient.getInstance().getSmartCommChatRoomManager().getRoomChat(jid.toString());
                                    if (roomChat != null && roomChat.getMultiUserChat() != null) {
                                        if (!checkMucJoined(jid.toString())) {
                                            jids.add(jid);
                                        }
                                    }
                                }
                                if (!jids.isEmpty()) {
                                    joinSequentially(jids);
                                }
                            }
                        });
    }

    /**
     * 重新加入群聊
     *
     * @param jidList
     */
    private void joinSequentially(LinkedList<EntityBareJid> jidList) {
        if (!OtherUtil.isConnected()) {
            SmartTrace.file("无网络...");
            setConnectionState(ConnectionState.LOADED);
            if (SmartIMClient.getInstance().getSimConnectionListener() != null) {
                SmartIMClient.getInstance().getSimConnectionListener().onChatDataLoaded();
            }
            return;
        }
        if (jidList.isEmpty()) {
            SmartTrace.file("遍历加入完毕... ");
            setConnectionState(ConnectionState.LOADED);
            if (SmartIMClient.getInstance().getSimConnectionListener() != null) {
                SmartIMClient.getInstance().getSimConnectionListener().onChatDataLoaded();
            }
            // 2分钟后检查一下连接状态
            executeWithDelay(1000 * 60 * 2, () -> {
                checkMucConnection();
            });
            return;
        }
        EntityBareJid jid = jidList.removeFirst();
        String groupId = jid.toString();
        RoomChat roomChat = SmartIMClient.getInstance().getSmartCommChatRoomManager().getRoomChat(groupId);
        if (roomChat != null) {
            MultiUserChat multiUserChat = roomChat.getMultiUserChat();
            if (multiUserChat != null && multiUserChat.isJoined()) {
                // 已经在掉线的时候反射把状态设为离线 这里暂时未检测到发生
                SmartTrace.file(jid + " smack返回当前已是加入状态");
                joinSequentially(jidList);
                executeWithDelay(10, () -> {
                    try {
                        multiUserChat.leave();
                        // 这里会阻塞10秒左右 应该是smack问题
                        if (roomChat.getState() != RoomState.waiting) {
                            leaveBeforeJoinMap.add(jid);
                            roomChat.setState(RoomState.waiting);
                            joinSequentially(leaveBeforeJoinMap);
                        }
                    } catch (Exception e) {
                    }
                });
                return;
            } else {
            }
            if (roomChat.getState() == RoomState.joining) {
                SmartTrace.file(jid + " 已经在加入中... 加入下一个");
                joinSequentially(jidList);
                return;
            } else {
                roomChat.setState(RoomState.joining);
            }
        } else {
            // 首次加入 roomChat是null

        }
        if (!SmartIMClient.getInstance().isAuthenticated()) {
            SmartTrace.file("已断开服务器 停止加入...");
            if (roomChat != null) {
                roomChat.setState(RoomState.unavailable);
            }
            setConnectionState(ConnectionState.LOADED);
            if (SmartIMClient.getInstance().getSimConnectionListener() != null) {
                SmartIMClient.getInstance().getSimConnectionListener().onChatDataLoaded();
            }
            return;
        }

        SmartIMClient.getInstance().getSmartCommChatRoomManager().realJoinRoom(
                groupId, new IChatRoomCallback() {
                    @Override
                    public void joinRoomSuccess(String groupId) {
                        joinSequentially(jidList);
                    }

                    @Override
                    public void joinRoomFailed(int code, String groupId, String desc) {
                        // 加入失败后 重试1次 是否可以重试 如果被封禁了 不能重试
                        SmartTrace.file("加入失败 " + groupId + " " + desc);
                        if (!TextUtils.isEmpty(getAccount())) {
                            RoomChat roomChat = SmartIMClient.getInstance().getSmartCommChatRoomManager().getRoomChat(groupId);
                            if (roomChat != null && roomChat.canJoin()) {
                                roomChat.setState(RoomState.unavailable);
                                Integer retryJoinCount = retryJoinMap.get(groupId);
                                if (retryJoinCount == null) {
                                    // 如果没有重试过
                                    retryJoinMap.put(groupId, 1);
                                    jidList.add(jid);
                                    SmartTrace.file("加入失败 重试 " + jid);
                                } else {
                                    SmartTrace.file("重试后仍加入失败 " + jid);
                                    SmartIMClient.getInstance().joinRoomFailed(code, groupId, desc);
                                }
                                joinSequentially(jidList);
                            } else {
                                SmartTrace.file("群聊有错误跳过 groupid = " + jid);
                                joinSequentially(jidList);
                            }
                        }
                    }
                });
    }

    public void executeWithDelay(long delay, final Runnable runnable) {
        Disposable subscribe = Observable.timer(delay, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(aLong -> {
                    if (SmartIMClient.getInstance().isAuthenticated()) {
                        runnable.run();
                    }
                }, throwable -> {

                });
        disposable.add(subscribe);
    }

    public void queryIq(String room) {

        IQ iq = new IQ("query", "http://jabber.org/protocol/disco#info") {
            @Override
            protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
                xml.rightAngleBracket();
                return xml;
            }
        };
        iq.setType(IQ.Type.get);
        try {
            iq.setTo(JidCreate.from(room));
            SmartIMClient.getInstance().getConnection().sendStanza(iq);
        } catch (Exception e) {
        }
    }

    public void release() {
        disposable.clear();
    }


    public String getString(int resId) {
        return application.getString(resId);
    }

    public String getMucFormItemDesc(@Nullable String fieldName) {
        if (fieldName != null) {
            if (fieldName.equals("muc#roomconfig_roomdesc") || fieldName.equals("muc#roominfo_description")) {
                return "房间描述";
            } else if (fieldName.equals("muc#roomconfig_roomname")) {
                return "房间名称";
            } else if (fieldName.equals("muc#roomconfig_changesubject")) {
                return "房间主题";
            } else if (fieldName.equals("muc#roomconfig_publicroom")) {
                return "房间是否公开";
            } else if (fieldName.equals("muc#roomconfig_presencebroadcast")) {
                return "广播其存在的角色";
            } else if (fieldName.equals("muc#roomconfig_allowpm")) {
                return "允许发送私信";
            } else if (fieldName.equals("muc#roomconfig_membersonly")) {
                return "只有成员能进入";
            } else if (fieldName.equals("muc#roomconfig_allowinvites")) {
                return "允许成员邀请其他人";
            } else if (fieldName.equals("muc#roomconfig_canchangenick")) {
                return "允许成员修改昵称";
            } else if (fieldName.equals("muc#roominfo_occupants")) {
                return "当前在线人数";
            } else if (fieldName.equals("muc#roominfo_lang")) {
                return "语言";
            }
        }
        return fieldName;
    }

    public void closeDeveloperMode() {
        StorageUtils.getInstance(SmartConstants.SP_NAME).put(SmartConstants.DEVELOPER_MODE, false);
    }

    public void openDeveloperMode() {
        StorageUtils.getInstance(SmartConstants.SP_NAME).put(SmartConstants.DEVELOPER_MODE, true);
    }

    public boolean isDeveloperMode() {
        return true;
    }

    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    public boolean connectionIsLoading() {
        return connectionState == ConnectionState.LOADING || connectionState == ConnectionState.CONNECTING;
    }

    /*public void testBosh() {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        BOSHConfiguration config = BOSHConfiguration.builder()
                                .setUsernameAndPassword("user005", "pZbyT4")
                                .setXmppDomain("jitsi.bndg.cn")
                                .useHttps()
                                .setPort(5443)
                                .setFile("/http-bind")
                                .build();
                        XMPPBOSHConnection connection = new XMPPBOSHConnection(config);

                        connection.connect().login();
                        Message message = connection.getStanzaFactory()
                                .buildMessageStanza()
                                .to("m5000@tigase2.bndg.cn")
                                .setBody("Hi there 2")
                                .build();
                        connection.sendStanza(message);
                        connection.disconnect();
                        return true;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(onSuccess -> {
                    if (onSuccess) {
                        SmartTrace.d("success");
                    } else {
                        SmartTrace.d("fafafafaf");
                    }
                }, onError -> {
                    SmartTrace.d("onError" + onError);
                });

    }*/
}