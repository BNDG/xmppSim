package com.bndg.smack.impl;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import org.jivesoftware.smack.ReconnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.sasl.SASLError;
import org.jivesoftware.smack.sasl.SASLErrorException;
import org.jivesoftware.smack.sasl.packet.SaslNonza;
import org.jivesoftware.smackx.blocking.BlockingCommandManager;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.omemo.OmemoManager;
import org.jivesoftware.smackx.omemo.internal.OmemoDevice;
import org.jivesoftware.smackx.omemo.trust.OmemoFingerprint;
import org.jivesoftware.smackx.pep.PepEventListener;
import org.jivesoftware.smackx.pep.PepManager;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.bndg.smack.R;
import com.bndg.smack.SmartCommHelper;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.callback.IFriendListCallback;
import com.bndg.smack.callback.ILoginCallback;
import com.bndg.smack.callback.ISmartCallback;
import com.bndg.smack.callback.IUserInfoCallBack;
import com.bndg.smack.callback.IUserInfoCallback2;
import com.bndg.smack.constant.SmartConstants;
import com.bndg.smack.contract.ISmartCommUser;
import com.bndg.smack.exceptions.SIMConnectionException;
import com.bndg.smack.extensions.VCardUpdateExtension;
import com.bndg.smack.extensions.avatar.AvatarData;
import com.bndg.smack.extensions.avatar.AvatarMetadata;
import com.bndg.smack.model.SmartUserInfo;
import com.bndg.smack.utils.BitmapUtils;
import com.bndg.smack.utils.OtherUtil;
import com.bndg.smack.utils.SmartTrace;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class DefaultSmartCommUserImpl extends BaseXmppImpl implements ISmartCommUser {

    private static final long CACHE_EXPIRATION_TIME = 10 * 60 * 1000; // 毫秒为单位
    // 用户是否在线
    private Presence.Type isOnLine;

    // 缓存用户信息
    HashMap<String, String> userInfoMap = new HashMap<>();

    private final PublishSubject<String> requestSubject = PublishSubject.create();
    private Disposable queryAvatarSubscribe;
    private PepManager pepManager;
    private HashMap<String, String> trustMap = new HashMap<>();

    public DefaultSmartCommUserImpl() {

    }

    public void initData() {
        pepManager = PepManager.getInstanceFor(SmartIMClient.getInstance().getConnection());
        pepManager.addPepEventListener(AvatarMetadata.NAMESPACE, AvatarMetadata.class, new PepEventAvatarListener());
        queryAvatarSubscribe = requestSubject
                .concatMap(jidString ->
                        Single.fromCallable(() -> {
                                    if (!SmartIMClient.getInstance().isAuthenticated()) {
                                        return false;
                                    }
                                    Jid from = JidCreate.from(jidString);
                                    IQ vCardRequest = new IQ("vCard", "vcard-temp") {
                                        @Override
                                        protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
                                            xml.setEmptyElement();
                                            return xml;
                                        }
                                    };
                                    vCardRequest.setTo(from);
                                    vCardRequest.setType(IQ.Type.get);
                                    SmartIMClient.getInstance().getConnection().sendStanza(vCardRequest);
                                    SmartTrace.d("请求用户头像 queryAvatarByUserId: " + jidString);
                                    return true;
                                })
                                .toObservable() // 将 Single 转换为 Observable
                                .delay(250, TimeUnit.MILLISECONDS) // 添加50ms延迟
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            // 处理成功
                        },
                        throwable -> {
                            // 处理错误
                        }
                );
    }

    /**
     * 登录
     *
     * @param userName 用户名
     * @param passWord 密码
     * @return
     */
    @Override
    public void login(String userName, String passWord, ILoginCallback iLoginCallback) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        SmartTrace.d(SmartIMClient.getInstance().getConnection()
                                .getXMPPServiceDomain() + "登陆开始: login start >--------------------------------------------<");
                        if (SmartIMClient.getInstance().getConnection() == null) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        if (!SmartIMClient.getInstance().isAuthenticated()) { // 判断是否登录
                            SmartTrace.d(
                                    "登录中 >--------------------------------------------<");
                            SmartCommHelper.getInstance().setAccount(userName);
                            SmartCommHelper.getInstance().setPassword(passWord);
                            Resourcepart resourcepart = Resourcepart.from(userName + OtherUtil.getAndroidId());
                            SmartIMClient.getInstance().getConnection().login(userName, passWord, resourcepart);
                            // 接收离线信息，设为离线状态
                            // 登录成功后，配置自动重连
                            ReconnectionManager instanceFor = ReconnectionManager.getInstanceFor(
                                    SmartIMClient.getInstance().getConnection());
                            // 重联间隔5秒
                            instanceFor.setFixedDelay(20);
                            // 开启重联机制
                            instanceFor.enableAutomaticReconnection();
                            instanceFor.addReconnectionListener(new ReconnectionListener() {
                                @Override
                                public void reconnectingIn(int i) {
                                    SmartTrace.w("reconnectingIn = " + i);
                                    if (i == 10) {
                                        SmartTrace.file("reconnectingIn 准备重连");
                                    }
                                }

                                @Override
                                public void reconnectionFailed(Exception e) {
                                    SmartTrace.w("reconnectionFailed");
                                }
                            });
                        }
                        SmartIMClient.getInstance().addRosterListener();
                        SmartTrace.d("onClick: 登录结束 >--------------------------------------------<");
                        return true;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(sucess -> {
                    if (sucess) {
                        iLoginCallback.onSuccess();
                    } else {
                        iLoginCallback.onError(SmartConstants.Error.LOGIN_FAILURE_OTHER,
                                SmartIMClient.getInstance().getString(R.string.no_connection));
                    }
                }, error -> {
                    SmartTrace.file("登陆错误: error = " + error);
                    //SmackException$NoResponseException: No response received within reply timeout. Timeout was 5000ms (~5s)
                    if (error instanceof SASLErrorException) {
                        SASLErrorException saslErrorException = (SASLErrorException) error;
                        SaslNonza.SASLFailure saslFailure = saslErrorException.getSASLFailure();
                        SASLError saslError = saslFailure.getSASLError();
                        if (saslError == SASLError.account_disabled) {
                            // 用户帐户被禁用，无法进行身份验证。
                            iLoginCallback.onError(SmartConstants.Error.ACCOUNT_DISABLED,
                                    SmartIMClient.getInstance().getString(R.string.account_disabled));
                        } else if (saslError == SASLError.not_authorized) {
                            iLoginCallback.onError(SmartConstants.Error.AUTHENTICATION_FAILURE,
                                    SmartIMClient.getInstance().getString(R.string.authentication_failure));
                        }
                    } else {
                        iLoginCallback.onError(SmartConstants.Error.LOGIN_FAILURE_OTHER,
                                SmartIMClient.getInstance().getString(R.string.no_connection));
                    }
                });

    }

    /**
     * 创建用户
     *
     * @param userName 用户名
     * @param passWord 密码
     */
    @Override
    public void createAccount(String userName, String passWord, String nickName, ISmartCallback smartCallback) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        try {
                            SmartTrace.d(
                                    "start: 注册开始 >--------------------------------------------<");
                            // 设置本地JID
                            AccountManager accountManager = AccountManager.getInstance(
                                    SmartIMClient.getInstance().getConnection());
                            Localpart usernameAsLocalpart = Localpart.from(userName);
                            Map<String, String> attributes = new HashMap<>();
                            attributes.put("email", userName + "@" + SmartIMClient.getInstance().getConnection().getXMPPServiceDomain());
                            accountManager.createAccount(usernameAsLocalpart, passWord, attributes);
                            // todo 可以修改密码
                            SmartTrace.d("end: 注册结束 end" + usernameAsLocalpart + "password" + passWord);
                            return true;
                        } catch (SmackException | InterruptedException | XMPPException |
                                 XmppStringprepException e) {
                            SmartTrace.d(
                                    "注册错误 error: " + e);
                            throw e;
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess -> {
                    if (onSuccess) {
                        smartCallback.onSuccess();
                    } else {
                        smartCallback.onFailed(SmartConstants.Error.CREATE_ACCOUNT_FAILED,
                                SmartIMClient.getInstance().getString(R.string.CREATE_ACCOUNT_FAILED));
                    }
                }, onError -> {
                    if (onError instanceof XMPPException.XMPPErrorException) {
                        // 处理 XMPPErrorException 异常
                        smartCallback.onFailed(SmartConstants.Error.CREATE_ACCOUNT_EXISTS_FAILED,
                                SmartIMClient.getInstance().getString(R.string.CREATE_ACCOUNT_EXISTS_FAILED));
                    } else {
                        smartCallback.onFailed(SmartConstants.Error.CREATE_ACCOUNT_FAILED,
                                SmartIMClient.getInstance().getString(R.string.CREATE_ACCOUNT_FAILED));
                    }
                });
    }

    /**
     * 修改用户头像
     *
     * @param file file
     */
    @Override
    public void changeImage(File file, IUserInfoCallback2 vCardCallback) {
        Disposable subscribe = Single.fromCallable(new Callable<SmartUserInfo>() {
                    @Override
                    public SmartUserInfo call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        byte[] bytes = BitmapUtils.getFileBytes(file);
                        VCardManager vCardManager = VCardManager.getInstanceFor(SmartIMClient.getInstance().getConnection());
                        VCard vcard = vCardManager.loadVCard(); // 加载当前用户的VCard
                        // 将头像数据设置到vCard中
                        vcard.setAvatar(bytes, "image/jpeg");
                        // 保存vCard
                        vCardManager.saveVCard(vcard);
                        String avatarHash = vcard.getAvatarHash();
                        avatarHash = avatarHash == null ? BitmapUtils.getAvatarHash(bytes) : avatarHash;
                        SmartTrace.d("头像hash = " + avatarHash);
                        Presence presence = new Presence(Presence.Type.available);
                        final VCardUpdateExtension vCardUpdate = new VCardUpdateExtension(avatarHash);
                        presence.addExtension(vCardUpdate);
                        SmartIMClient.getInstance().getConnection().sendStanza(presence);
                        // 实现XEP-0384
                        AvatarData avatar = new AvatarData(bytes);
                        PayloadItem<AvatarData> item = new PayloadItem<>(avatarHash, avatar);
                        String node = AvatarData.NAMESPACE;
                        pepManager.publish(node, item);

                        String[] imageMetaInfo = BitmapUtils.getImageMetaInfo(file);
                        if (imageMetaInfo != null) {
                            AvatarMetadata metadata = new AvatarMetadata();
                            AvatarMetadata.Info info = new AvatarMetadata.Info(avatarHash, imageMetaInfo[2],
                                    bytes.length);
                            info.setWidth(Integer.parseInt(imageMetaInfo[0]));
                            info.setHeight(Integer.parseInt(imageMetaInfo[1]));
                            metadata.addInfo(info);
                            PayloadItem<AvatarMetadata> metadataItem = new PayloadItem<>(avatarHash, metadata);
                            String nodeMetadata = AvatarMetadata.NAMESPACE;
                            pepManager.publish(nodeMetadata, metadataItem);
                        }
                        SmartUserInfo userInfo = new SmartUserInfo();
                        userInfo.setUserId(SmartCommHelper.getInstance().getUserId());
                        userInfo.setUserAvatar(bytes);
                        userInfo.setUserAvatarHash(avatarHash);
                        return userInfo;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(vCardCallback::onSuccess, onError -> {
                    vCardCallback.onFailed(SmartConstants.Error.UPDATE_VCARD_FAILED, SmartIMClient.getInstance().getString(R.string.update_vcard_failed));
                });

    }

    /**
     * 注销当前用户
     *
     * @return true成功
     */
    @Override
    public boolean deleteAccount() {
        if (SmartIMClient.getInstance().getConnection() == null)
            return false;
        try {
            AccountManager.getInstance(SmartIMClient.getInstance().getConnection()).deleteAccount();
            return true;
        } catch (XMPPException | SmackException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 修改密码
     *
     * @return true成功
     */
    @Override
    public boolean changePassword(String pwd) {
        if (SmartIMClient.getInstance().getConnection() == null)
            return false;
        try {
            AccountManager.getInstance(SmartIMClient.getInstance().getConnection()).changePassword(pwd);
            return true;
        } catch (SmackException | InterruptedException | XMPPException.XMPPErrorException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 判断OpenFire用户的状态 strUrl :
     * url格式 - http://my.openfire.com:9090/plugins/presence
     * /status?jid=user1@SERVER_NAME&type=xml
     * 返回值 : 0 - 用户不存在; 1 - 用户在线; 2 - 用户离线
     * 说明 ：必须要求 OpenFire加载 presence 插件，同时设置任何人都可以访问
     */
    @Override
    public int IsUserOnLine(String user) {
        String url = "http://" + SmartIMClient.getInstance().getConnection()
                .getHost() + ":9090/plugins/presence/status?" + "jid=" + user + "@" + SmartIMClient.getInstance().getConnection()
                .getHost() + "&type=xml";
        int shOnLineState = 0; // 不存在
        try {
            URL oUrl = new URL(url);
            URLConnection oConn = oUrl.openConnection();
            if (oConn != null) {
                BufferedReader oIn = new BufferedReader(
                        new InputStreamReader(oConn.getInputStream()));
                String strFlag = oIn.readLine();
                oIn.close();
                System.out.println("strFlag" + strFlag);
                if (strFlag.contains("type=\"unavailable\"")) {
                    shOnLineState = 2;
                }
                if (strFlag.contains("type=\"error\"")) {
                    shOnLineState = 0;
                } else if (strFlag.contains("priority") || strFlag.contains("id=\"")) {
                    shOnLineState = 1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return shOnLineState;
    }

    /**
     * 设置在线、离线等状态
     *
     * @param type
     */
    @Override
    public void setOnLine(Presence.Type type) {
        SmartTrace.w("设置在线状态 " + type);
        Disposable subscribe = Single.create(new SingleOnSubscribe<Void>() {
                    @Override
                    public void subscribe(@NonNull SingleEmitter<Void> emitter) throws Throwable {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        Presence presence = new Presence(type);
                        SmartIMClient.getInstance().getConnection().sendStanza(presence);
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

                }, onError -> {

                });

    }

    /**
     * 更改用户状态
     */
    @Override
    public void setPresence(int code) {
        XMPPConnection con = SmartIMClient.getInstance().getConnection();
        if (con == null)
            return;
        Presence presence;
        try {
            switch (code) {
                case 0:
                    presence = new Presence(Presence.Type.available);
                    con.sendStanza(presence);
                    Log.v("state", "设置在线");
                    break;
                case 1:
                    presence = new Presence(Presence.Type.available);
                    presence.setMode(Presence.Mode.chat);
                    con.sendStanza(presence);
                    Log.v("state", "设置Q我吧");
                    break;
                case 2:
                    presence = new Presence(Presence.Type.available);
                    presence.setMode(Presence.Mode.dnd);
                    con.sendStanza(presence);
                    Log.v("state", "设置忙碌");
                    break;
                case 3:
                    presence = new Presence(Presence.Type.available);
                    presence.setMode(Presence.Mode.away);
                    con.sendStanza(presence);
                    Log.v("state", "设置离开");
                    break;
                case 4:
                    //                    Roster roster = con.getRoster();
                    //                    Collection<RosterEntry> entries = roster.getEntries();
                    //                    for (RosterEntry entry : entries) {
                    //                        presence = new Presence(Presence.Type.unavailable);
                    //                        presence.setPacketID(Packet.ID_NOT_AVAILABLE);
                    //                        presence.setFrom(con.getUser());
                    //                        presence.setTo(entry.getUser());
                    //                        con.sendPacket(presence);
                    //                        Log.v("state", presence.toXML());
                    //                    }
                    //                    // 向同一用户的其他客户端发送隐身状态
                    //                    presence = new Presence(Presence.Type.unavailable);
                    //                    presence.setPacketID(Packet.ID_NOT_AVAILABLE);
                    //                    presence.setFrom(con.getUser());
                    //                    presence.setTo(StringUtils.parseBareAddress(con.getUser()));
                    //                    con.sendStanza(presence);
                    //                    Log.v("state", "设置隐身");
                    //                    break;
                case 5:
                    presence = new Presence(Presence.Type.unavailable);
                    con.sendStanza(presence);
                    Log.v("state", "设置离线");
                    break;
                default:
                    break;
            }
        } catch (SmackException.NotConnectedException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取VCard 是一个耗时的请求 会阻塞
     *
     * @param entityBareJidString xxx@xxx.xx
     * @param callback
     */
    @Override
    public void getUserInfo(String entityBareJidString, IUserInfoCallback2 callback) {
        Disposable subscribe = Single.fromCallable(new Callable<SmartUserInfo>() {
                    @Override
                    public SmartUserInfo call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        long start = System.currentTimeMillis();
                        SmartTrace.w(
                                "获取VCard 开始 userId = " + entityBareJidString);
                        VCard vCard = VCardManager.getInstanceFor(SmartIMClient.getInstance().getConnection())
                                .loadVCard(JidCreate.entityBareFrom(entityBareJidString));
                        EntityBareJid entityBareJid = SmartIMClient.getInstance().getConnection().getUser().asEntityBareJid();
                        if (entityBareJid.toString().equals(entityBareJidString)) {
                            // 当前用户
                            SmartTrace.d("当前用户的vcard");
                            SmartCommHelper.getInstance().setNickName(vCard.getNickName());
                        }
                        SmartTrace.w("获取VCard 结束-- 耗时" + (System.currentTimeMillis() - start) + "ms");
                        SmartUserInfo userInfo = new SmartUserInfo();
                        userInfo.setUserId(entityBareJidString);
                        if (vCard != null) {
                            userInfo.setUserAvatar(vCard.getAvatar());
                            userInfo.setUserAvatarHash(vCard.getAvatarHash());
                            userInfo.setNickname(vCard.getNickName());
                            userInfoMap.put(entityBareJidString, vCard.getNickName());
                        } else {

                        }
                        return userInfo;
                    }
                }).onErrorResumeNext(throwable -> {
                    if (throwable instanceof SIMConnectionException) {
                        SmartIMClient.getInstance().checkConnection();
                    }
                    throw throwable;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(vCard -> {
                    if (null != vCard) {
                        callback.onSuccess(vCard);
                    } else {
                        callback.onFailed(SmartConstants.Error.NO_VCARD, SmartIMClient.getInstance().getString(R.string.no_vcard));
                    }
                }, onError -> {
                    callback.onFailed(SmartConstants.Error.NO_VCARD, SmartIMClient.getInstance().getString(R.string.no_vcard));
                });
        addDisposable(subscribe);
    }

    /**
     * 获取用户VCard信息
     *
     * @param userJid userJid
     * @return VCard
     */
    @Override
    public VCard getUserInfo(String userJid) {
        if (!SmartIMClient.getInstance().isAuthenticated()) {
            return null;
        }
        try {
            return VCardManager.getInstanceFor(SmartIMClient.getInstance().getConnection())
                    .loadVCard(JidCreate.entityBareFrom(userJid));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void getUserNickname(String userJid, IUserInfoCallBack userInfoCallBack) {
        if (userInfoMap.containsKey(userJid)) {
            userInfoCallBack.getNickname(userInfoMap.get(userJid));
            return;
        }
        SmartIMClient.getInstance().getSmartCommUserManager().getUserInfo(userJid, new IUserInfoCallback2() {
            @Override
            public void onSuccess(SmartUserInfo userInfo) {
                String nickName = userInfo.getNickname();
                if (!TextUtils.isEmpty(nickName)) {
                    userInfoMap.put(userJid, nickName);
                }
                userInfoCallBack.getNickname(TextUtils.isEmpty(nickName) ? "" : nickName);
            }

            @Override
            public void onFailed(int code, String desc) {
                userInfoCallBack.getNicknameError(SmartConstants.Error.NO_USER_FOUND, "no user");
            }
        });

    }

    @Override
    public void getUserAvatar(BareJid jid, IUserInfoCallback2 userVCardCallback) {
        Disposable subscribe = Single.fromCallable(new Callable<Bitmap>() {
                    @Override
                    public Bitmap call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        ByteArrayInputStream bais = null;
                        VCard vcard = new VCard();
                        // 加入这句代码，解决No VCard for
                        ProviderManager.addIQProvider("vCard", "vcard-temp",
                                new org.jivesoftware.smackx.vcardtemp.provider.VCardProvider());
                        VCardManager.getInstanceFor(SmartIMClient.getInstance().getConnection())
                                .loadVCard(JidCreate.entityBareFrom(
                                        jid));
                        if (vcard.getAvatar() == null) {
                            return null;
                        }
                        bais = new ByteArrayInputStream(vcard.getAvatar());
                        return BitmapUtils.getInstance().InputStream2Bitmap(bais);
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(bitmap -> {
                    if (bitmap != null) {
                        userVCardCallback.onAvatarBitmapReceived(bitmap);
                    }
                }, onError -> {
                    userVCardCallback.onFailed(SmartConstants.Error.GET_AVATAR_FAILED, SmartIMClient.getInstance().getString(R.string.get_avatar_failed));
                });
    }

    @Override
    public void getUserIsOnLine(String userJid, IUserInfoCallBack userInfoCallBack) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        Roster instanceFor = Roster.getInstanceFor(SmartIMClient.getInstance().getConnection());
                        Presence presence = instanceFor.getPresence(JidCreate.entityBareFrom(userJid));
                        if (presence.isAvailable()) {
                            return true;
                        }
                        return false;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(onSuccess -> {
                    userInfoCallBack.isOnLine(onSuccess);
                }, onError -> {

                });
    }

    @Override
    public void setMyUserInfo(SmartUserInfo userInfo, IUserInfoCallback2 callBack) {
        Disposable subscribe = Single.fromCallable(new Callable<VCard>() {
                    @Override
                    public VCard call() throws Exception {
                        VCardManager vCardManager = VCardManager.getInstanceFor(
                                SmartIMClient.getInstance().getConnection());
                        VCard vCard = vCardManager.loadVCard(); // 加载当前用户的VCard
                        if (vCard == null) {
                            vCard = new VCard(); // 创建新的 VCard
                        }
                        if (!TextUtils.isEmpty(userInfo.getNickname())) {
                            vCard.setNickName(userInfo.getNickname()); // 设置昵称
                            SmartCommHelper.getInstance().setNickName(userInfo.getNickname());
                        }
                        if (userInfo.getUserAvatar() != null) {
                            vCard.setAvatar(userInfo.getUserAvatar());
                        }
                        // 设置其他属性...
                        vCardManager.saveVCard(vCard); // 保存 VCard 到服务器
                        return vCard;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(vCard -> {
                    callBack.onSuccess(userInfo);
                }, onError -> {
                    callBack.onFailed(SmartConstants.Error.UPDATE_VCARD_FAILED, SmartIMClient.getInstance().getString(R.string.update_vcard_failed));
                });

    }

    @Override
    public void requestAvatarByUserId(String jidString) {
        if (!requestSubject.hasObservers()) {
            initData();
        }
        requestSubject.onNext(jidString);
    }

    /**
     * 注意 释放后下次再使用需要初始化
     */
    @Override
    public void release() {
        if (queryAvatarSubscribe != null && !queryAvatarSubscribe.isDisposed()) {
            queryAvatarSubscribe.dispose();
        }
        clearDisposable();
    }

    @Override
    public String getUserNicknameSync(String userJid, boolean needQueryCard) {
        if (userInfoMap.containsKey(userJid)) {
            return userInfoMap.get(userJid);
        }
        try {
            RosterEntry entry = Roster.getInstanceFor(SmartIMClient.getInstance().getConnection())
                    .getEntry(JidCreate.entityBareFrom(userJid));
            if (entry != null) {
                userInfoMap.put(userJid, entry.getName());
                return entry.getName();
            }
        } catch (XmppStringprepException e) {

        }
        if (needQueryCard) {
            VCard userVCard = getUserInfo(userJid);
            if (userVCard != null && !TextUtils.isEmpty(userVCard.getNickName())) {
                userInfoMap.put(userJid, userVCard.getNickName());
                return userVCard.getNickName();
            } else {
                return SmartCommHelper.getAccountFromJid(userJid);
            }
        }
        return SmartCommHelper.getAccountFromJid(userJid);
    }

    @Override
    public void getBlockList(IFriendListCallback callback) {
        Disposable subscribe = Single.fromCallable(new Callable<Set<SmartUserInfo>>() {
                    @Override
                    public Set<SmartUserInfo> call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        TreeSet<SmartUserInfo> lists = new TreeSet<>();
                        BlockingCommandManager blockingCommandManager = BlockingCommandManager.getInstanceFor(SmartIMClient.getInstance().getConnection());
                        List<Jid> blockList = blockingCommandManager.getBlockList();
                        for (Jid jid : blockList) {
                            SmartUserInfo userInfo = new SmartUserInfo();
                            userInfo.setUserId(jid.toString());
                            // 如果对方拉黑了我 那么查不到对方的名片的
                            userInfo.setNickname(getUserNicknameSync(jid.toString(), false));
                            lists.add(userInfo);
                        }
                        return lists;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(callback::onSuccess, onError -> {

                });

    }

    @Override
    public void checkTrustDevice(String conversationId, ISmartCallback callback) {
        if (trustMap.get(conversationId) != null) {
            // 对方离线是否清除？
            callback.onSuccess();
            return;
        }
        OmemoManager omemoManager = OmemoManager.getInstanceFor(SmartIMClient.getInstance().getConnection());
        try {
            DomainBareJid serverJid = SmartIMClient.getInstance().getConnection().getXMPPServiceDomain();
            /*boolean serverSupportsOmemo = OmemoManager.serverSupportsOmemo(SmartIMClient.getInstance().getConnection(), serverJid);
            if(!serverSupportsOmemo) {
                // 服务器不支持omemo
                callback.onFailed(1, "服务器不支持omemo");
                return;
            }*/
            BareJid contact = JidCreate.bareFrom(conversationId);
            // 如果联系人在 deviceList 中发布了任何活动设备，则返回 true。
           /* HashMap<OmemoDevice, OmemoFingerprint> activeFingerprints = omemoManager.getActiveFingerprints(contact);
            if (activeFingerprints.isEmpty()) {
                callback.onFailed(1, "联系人不支持omemo");
                return;
            }*/
            // 向联系人请求 deviceList 更新。
            omemoManager.requestDeviceListUpdateFor(contact);
            Set<OmemoDevice> devices = omemoManager.getDevicesOf(contact);
            if (devices.isEmpty()) {
                callback.onFailed(1, "联系人不支持omemo");
                return;
            }
            for (OmemoDevice device : devices) {
                // 获取联系人设备的指纹。
                OmemoFingerprint fingerprint = omemoManager.getFingerprint(device);
                SmartTrace.d("checkTrustDevice: " + fingerprint);
                if (omemoManager.isTrustedOmemoIdentity(device, fingerprint)) {
                    trustMap.put(conversationId, fingerprint.toString());
                    callback.onSuccess();
                    return;
                } else {
                    // 相信指纹属于 OmemoDevice。
                    SmartTrace.w("checkTrustDevice: " + fingerprint);
                    omemoManager.trustOmemoIdentity(device, fingerprint);
                    trustMap.put(conversationId, fingerprint.toString());
                    callback.onSuccess();
                }
            }
        } catch (Exception e) {
            SmartTrace.w("Exception: " + e);
            callback.onFailed(1, "omemo error");
        }
    }

    private static class PepEventAvatarListener implements PepEventListener<AvatarMetadata> {
        /**
         * 处理接收到的 onPepEvent 事件。如果是来自自己的事件，则进行特殊处理。
         * 否则，用户的头像（作为联系人实体添加）将不会被更新。
         *
         * @param from    发送者
         * @param event   包含子元素 'info' 的 AvatarMetadata 元素
         * @param id      节点项 ID
         * @param message 如果有的话，承载消息
         */
        @Override
        public void onPepEvent(EntityBareJid from, AvatarMetadata event, String id, Message message) {
            boolean isOwnSelf = SmartIMClient.getInstance().getConnection().getUser().isParentOf(from);
            List<AvatarMetadata.Info> infos = event.getInfo();
            if (!infos.isEmpty()) {
                AvatarMetadata.Info info = infos.get(0);
                SmartTrace.d("收到个人发布事件: " + from,
                        info.getId(),
                        message);
                String newAvatarId = info.getId();
            }
        }
    }
}
