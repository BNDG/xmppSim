package com.bndg.smack.impl;

import android.text.TextUtils;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaCollector;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.FromMatchesFilter;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PossibleFromTypeFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.StanzaError;
import org.jivesoftware.smackx.bookmarks.BookmarkManager;
import org.jivesoftware.smackx.bookmarks.BookmarkedConference;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.jivesoftware.smackx.mam.MamManager;
import org.jivesoftware.smackx.mam.element.MamElements;
import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.MUCAffiliation;
import org.jivesoftware.smackx.muc.MUCRole;
import org.jivesoftware.smackx.muc.MucEnterConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.Occupant;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.jivesoftware.smackx.muc.packet.MUCOwner;
import org.jivesoftware.smackx.muc.packet.MUCUser;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.xdata.form.FillableForm;
import org.jivesoftware.smackx.xdata.form.Form;
import org.jivesoftware.smackx.xdata.packet.DataForm;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import com.bndg.smack.R;
import com.bndg.smack.SmartCommHelper;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.callback.IBookmarkedConferenceCallback;
import com.bndg.smack.callback.IChatRoomCallback;
import com.bndg.smack.callback.IGroupMemberCallback;
import com.bndg.smack.callback.IMsgCallback;
import com.bndg.smack.callback.IServiceCallback;
import com.bndg.smack.callback.IUserInfoCallBack;
import com.bndg.smack.constant.SmartConstants;
import com.bndg.smack.contract.ISmartCommChatRoom;
import com.bndg.smack.entity.FetchEntity;
import com.bndg.smack.entity.SmartMessage;
import com.bndg.smack.enums.SmartContentType;
import com.bndg.smack.exceptions.SIMConnectionException;
import com.bndg.smack.exceptions.SIMGroupNotJoinedException;
import com.bndg.smack.extensions.TestGroupMembersExtension;
import com.bndg.smack.extensions.VCardUpdateExtension;
import com.bndg.smack.extensions.avatar.AvatarData;
import com.bndg.smack.extensions.avatar.AvatarMetadata;
import com.bndg.smack.extensions.base.IExtension;
import com.bndg.smack.extensions.bookmarks.BookmarksManager;
import com.bndg.smack.model.SmartGroupInfo;
import com.bndg.smack.model.SmartUserInfo;
import com.bndg.smack.muc.RoomChat;
import com.bndg.smack.muc.RoomState;
import com.bndg.smack.muc.listener.MucMyStatusListener;
import com.bndg.smack.muc.listener.MucParticipantStatusListener;
import com.bndg.smack.utils.BitmapUtils;
import com.bndg.smack.utils.SIMJsonUtil;
import com.bndg.smack.utils.SmartTrace;
import com.bndg.smack.utils.StorageUtils;
import com.bndg.smack.utils.XmppUri;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.CompositeException;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DefaultSmartCommChatRoomImpl extends BaseXmppImpl implements ISmartCommChatRoom {

    private static final Integer FETCH_SIZE = 20;
    // 聊天室实例
    private HashMap<String, RoomChat> roomChatMap = new HashMap<>();
    // 聊天室消息监听
    private HashMap<String, MessageListener> mucMessageListenerMap = new HashMap<>();
    // 聊天室内成员状态监听
    private HashMap<String, ParticipantStatusListener> mucStatusListenerMap = new HashMap<>();
    // 聊天室内我的状态监听
    private HashMap<String, MucMyStatusListener> mucMyStatusListenerMap = new HashMap<>();
    private long joinTimes;
    private final int maxHistoryNum = 20;

    public DefaultSmartCommChatRoomImpl() {
    }


    /**
     * 创建聊天室
     *
     * @param roomCallback 回调
     */
    @Override
    public void createRoom(String roomName, IChatRoomCallback roomCallback) {
        // 查询支持的聊天室服务
        SmartCommHelper.getInstance().discoverMultiUserChatSupport(new IServiceCallback() {
            @Override
            public void onSuccess(String groupServer) {
                String groupJid = SmartCommHelper.getInstance().generateGroupId(groupServer);
                Disposable subscribe = Single.fromCallable(new Callable<MultiUserChat>() {
                            @Override
                            public MultiUserChat call() throws Exception {
                                if (!SmartIMClient.getInstance().isAuthenticated()) {
                                    throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                                }
                                // 创建一个MultiUserChat
                                EntityBareJid jid = JidCreate.entityBareFrom(groupJid);
                                MultiUserChat muc = MultiUserChatManager.getInstanceFor(SmartIMClient.getInstance().getConnection())
                                        .getMultiUserChat(jid);
                                // 创建聊天室
                                muc.create(Resourcepart.from(SmartCommHelper.getInstance().getAccountIdInGroup(groupJid)));
                                try {
                                    BookmarksManager.getInstance()
                                            .addConferenceToBookmarks(
                                                    roomName,
                                                    muc.getRoom(),
                                                    Resourcepart.from(SmartCommHelper.getInstance().getAccountIdInGroup(groupJid)));
                                } catch (XmppStringprepException e) {
                                    SmartTrace.d("error：" + e);
                                }
                                configRoom(roomName, muc);
                                return muc;
                            }
                        }).onErrorResumeNext(throwable -> {
                            if (throwable instanceof SIMConnectionException) {
                                SmartIMClient.getInstance().checkConnection();
                            }
                            throw throwable;
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(multiUserChat -> {
                            roomCallback.createSuccess(multiUserChat.getRoom().toString());
                            // 创建聊天室后addMucListener 延迟执行 等待app完成房间初始化 避免消息丢失
                            SmartCommHelper.getInstance().executeWithDelay(SmartConstants.SHORT_DELAY, () -> {
                                addMucListener(multiUserChat);
                            });
                        }, onError -> {
                            SmartTrace.w(onError,
                                    onError.getMessage());
                            roomCallback.createFailed(SmartConstants.Error.CREATE_MUC_FAILED, SmartIMClient.getInstance().getString(R.string.create_muc_failed));
                        });
            }

            @Override
            public void onFailed(int code, String desc) {
                roomCallback.createFailed(SmartConstants.Error.CREATE_MUC_FAILED, SmartIMClient.getInstance().getString(R.string.muc_unavailable));
            }
        });


    }

    /**
     * 创建群聊后 邀请用户加入群聊
     *
     * @param groupId           群实例对象
     * @param pickedUserIdList  被邀请的成员列表
     * @param iChatRoomCallback 群事件回调
     */
    @Override
    public void inviteUserToGroup(String groupId, List<String> pickedUserIdList, IChatRoomCallback iChatRoomCallback) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        inviteUsers(pickedUserIdList, groupId, "");
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
                        iChatRoomCallback.joinRoomSuccess(groupId);
                    } else {
                        iChatRoomCallback.joinRoomFailed(SmartConstants.Error.ROOM_JOIN_FAILED,
                                groupId,
                                SmartIMClient.getInstance().getString(R.string.room_join_failed));
                    }
                }, onError -> {
                    iChatRoomCallback.joinRoomFailed(SmartConstants.Error.ROOM_JOIN_FAILED,
                            groupId,
                            SmartIMClient.getInstance().getString(R.string.room_join_failed));
                });
    }

    /**
     * 适合无密码的邀请
     *
     * @param groupId
     * @param userInfos
     * @param reason
     * @param iChatRoomCallback
     */
    public void inviteUserToGroup2(String groupId, List<SmartUserInfo> userInfos, String reason, IChatRoomCallback iChatRoomCallback) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        inviteUsers2(userInfos, groupId, reason.toString());
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
                        iChatRoomCallback.joinRoomSuccess(groupId);
                    } else {
                        iChatRoomCallback.joinRoomFailed(SmartConstants.Error.ROOM_JOIN_FAILED,
                                groupId,
                                SmartIMClient.getInstance().getString(R.string.room_join_failed));
                    }
                }, onError -> {
                    iChatRoomCallback.joinRoomFailed(SmartConstants.Error.ROOM_JOIN_FAILED,
                            groupId,
                            SmartIMClient.getInstance().getString(R.string.room_join_failed));
                });
    }

    private void configRoom(String roomName, MultiUserChat muc) throws SmackException.NoResponseException, XMPPException.XMPPErrorException, InterruptedException, SmackException.NotConnectedException {
        // 获得聊天室的配置表单
//        Form form = muc.getRegistrationForm();
        MUCOwner iq = new MUCOwner();
        iq.setTo(muc.getRoom());
        iq.setType(IQ.Type.get);
        IQ answer = SmartIMClient.getInstance().getConnection().createStanzaCollectorAndSend(iq).nextResultOrThrow();
        Form form = new Form(answer.getExtension(DataForm.class));
        SmartTrace.d("=获得聊天室的配置表单=" + form);
        // 根据原始表单创建一个要提交的新表单。
        FillableForm submitForm = form.getFillableForm();
        // todo 向要提交的表单添加默认答复
        SmartTrace.d("createRoom: 向要提交的表单添加默认答复");
                        /*for (FormField formField : form.getDataForm().getFields()) {
                            if (FormField.Type.hidden == formField.getType()
                                    && formField.getVariable() != null) {
                                // 设置默认值作为答复
                                submitForm.setAnswer(formField.getVariable());
                            }
                        }*/
        // 设置聊天室的新拥有者
//                        List<String> owners = new ArrayList<>();
//                        owners.add(XmppService.getInstance().getConnection().getUser().asEntityBareJidString());// 用户JID
//                       submitForm.setAnswer("muc#roomconfig_roomowners", owners);
        //设置为公共房间
//                        submitForm.setAnswer("muc#roomconfig_publicroom", false);
        // 设置聊天室是持久聊天室，
        if (submitForm.getDataForm().hasField("muc#roomconfig_persistentroom")) {
            submitForm.setAnswer("muc#roomconfig_persistentroom", true);
        }
        // 房间仅对成员开放
        if (submitForm.getDataForm().hasField("muc#roomconfig_membersonly")) {
            submitForm.setAnswer("muc#roomconfig_membersonly", false);
        }
        // 允许占有者邀请其他人
        if (submitForm.getDataForm().hasField("muc#roomconfig_allowinvites")) {
            submitForm.setAnswer("muc#roomconfig_allowinvites", true);
        }
        // 需要密码才能进入房间
        if (submitForm.getDataForm().hasField("muc#roomconfig_passwordprotectedroom")) {
            submitForm.setAnswer("muc#roomconfig_passwordprotectedroom", false);
        }
                                /*if (!TextUtils.isEmpty(password)) {
                                    // 进入是否需要密码
                                    submitForm.setAnswer("muc#roomconfig_passwordprotectedroom",
                                            true);
                                    // 设置进入密码
                                    submitForm.setAnswer("muc#roomconfig_roomsecret", password);
                                } else {
                                    //进入不需要密码
                                }*/
        // 能够发现占有者真实 JID 的角色
        if (submitForm.getDataForm().hasField("muc#roomconfig_whois")) {
            submitForm.setAnswer("muc#roomconfig_whois", "anyone");
        }
        // 房间名称
        if (submitForm.getDataForm().hasField("muc#roomconfig_roomname")) {
            submitForm.setAnswer("muc#roomconfig_roomname", roomName);
        }
//      最大房间成员人数
        if (submitForm.getDataForm().hasField("muc#roomconfig_maxusers")) {
            submitForm.setAnswer("muc#roomconfig_maxusers", "none");
        }
//                        submitForm.setAnswer("muc#roomconfig_presencebroadcast", answers);
        // 昵称长度
//                        submitForm.setAnswer("muc#roomconfig_maxresources", "none");
        // 不支持不支持不支持登录房间对话 不支持
//                        submitForm.setAnswer("muc#roomconfig_enablelogging", true);
        // 仅允许注册的昵称登录
//                        submitForm.setAnswer("x-muc#roomconfig_reservednick", true);
        // 允许使用者修改昵称
//                        submitForm.setAnswer("x-muc#roomconfig_canchangenick", false);
        // 允许用户注册房间
//                        submitForm.setAnswer("x-muc#roomconfig_registration", false);
        // 发送已完成的表单（有默认值）到服务器来配置聊天室
        muc.sendConfigurationForm(submitForm);
        // 改变聊天室主题
//        muc.changeSubject("");
    }

    /**
     * 加入聊天室
     *
     * @param groupId  会议室名
     * @param callback
     */
    @Override
    public void realJoinRoom(String groupId, IChatRoomCallback callback) {
        String pwd = StorageUtils.getInstance(SmartConstants.SP_NAME).getString(groupId + SmartConstants.PASS_WORD);
        realJoinRoomWithPWD(groupId, pwd, callback);
    }

    @Override
    public void realJoinRoomWithPWD(String groupId, String pwd, IChatRoomCallback callback) {
        Disposable subscribe = Single.fromCallable(new Callable<MultiUserChat>() {
                    @Override
                    public MultiUserChat call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated())
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        SmartTrace.w("[" + groupId + "] 开始加入......");
                        SmartTrace.file("[" + groupId + "] 开始加入......");
                        SmartIMClient.getInstance().getChatRoomListener().startJoinGroup(groupId);
                        String nickName = SmartCommHelper.getInstance().getAccountIdInGroup(groupId);
                        // 使用XMPPConnection创建一个MultiUserChat窗口
                        MultiUserChatManager instanceFor = MultiUserChatManager.getInstanceFor(SmartIMClient.getInstance()
                                .getConnection());
                        MultiUserChat muc = instanceFor
                                .getMultiUserChat(JidCreate.entityBareFrom(groupId));
                        // 配置 MucEnterConfiguration 以不拉取历史消息
                        MucEnterConfiguration.Builder mucEnterConfigBuilder = muc.getEnterConfigurationBuilder(Resourcepart.from(nickName));
                        if (!TextUtils.isEmpty(pwd)) {
                            mucEnterConfigBuilder.withPassword(pwd);
                        }
                        // 如果没有历史消息或者历史消息时间是一天前的 就默认拉取49条
                        FetchEntity recordMsg = getFetchEntity(groupId);
                        if (recordMsg == null) {
                            SmartTrace.d("call: recordMsg == null");
                            mucEnterConfigBuilder.requestMaxStanzasHistory(maxHistoryNum);
                        } else if (System.currentTimeMillis() - recordMsg.createTime > 24 * 60 * 60 * 1000) {
                            SmartTrace.d("call: recordMsg > 24 * 60");
                            recordMsg.needPull = false;
                            putRecordMsg(groupId, recordMsg);
                            mucEnterConfigBuilder.requestMaxStanzasHistory(maxHistoryNum);
                        } else {
                            // 需要拉取
                            MamManager mam = MamManager.getInstanceFor(muc);
                            boolean supported = mam.isSupported();
                            if (supported) {
                                // 支持mam功能
                                mam.enableMamForAllMessages();
                                mucEnterConfigBuilder.requestNoHistory();
                                recordMsg.needPull = true;
                                SmartTrace.d("call: 服务器支持mam 拉取 ");
                                putRecordMsg(groupId, recordMsg);
                            } else {
                                SmartTrace.d("服务器不支持mam");
                                mucEnterConfigBuilder.requestMaxStanzasHistory(maxHistoryNum);
                            }
                        }
                        //设置加入 MUC 聊天室时使用的超时时间。
                        mucEnterConfigBuilder.timeoutAfter(10 * 1000);
                        MucEnterConfiguration mucEnterConfig = mucEnterConfigBuilder.build();
                        // 用户加入聊天室
                        joinTimes = System.currentTimeMillis();
                        // 加入群聊addMucListener
                        addMucListener(muc);
                        muc.join(mucEnterConfig);
                        return muc;
                    }
                }).onErrorResumeNext(throwable -> {
                    if (throwable instanceof SIMConnectionException) {
                        SmartIMClient.getInstance().checkConnection();
                    }
                    throw throwable;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(multiUserChat -> {
                    BookmarksManager.getInstance()
                            .addConferenceToBookmarks(
                                    multiUserChat.getRoom().getLocalpart().toString(),
                                    multiUserChat.getRoom(),
                                    Resourcepart.from(SmartCommHelper.getInstance().getAccountIdInGroup(groupId)));
                    if (!TextUtils.isEmpty(pwd)) {
                        StorageUtils.getInstance(SmartConstants.SP_NAME).put(groupId + SmartConstants.PASS_WORD, pwd);
                    }
                    RoomChat roomChat = SmartIMClient.getInstance().getSmartCommChatRoomManager().getRoomChat(groupId);
                    // roomChat一定不为空 因为监听器已经初始化
                    roomChat.setState(RoomState.available);
                    joinTimes = System.currentTimeMillis() - joinTimes;
                    SmartTrace.w(groupId,
                            "加入成功 用时 " + joinTimes + "ms");
                    SmartTrace.file(groupId,
                            "加入成功 用时 " + joinTimes + "ms");
                    callback.joinRoomSuccess(groupId);
                    if (SmartIMClient.getInstance().getChatRoomListener() != null) {
                        SmartIMClient.getInstance().getChatRoomListener().joinRoomSuccess(groupId);
                    }
                    FetchEntity recordMsg = getFetchEntity(groupId);
                    if (recordMsg != null && recordMsg.needPull) {
                        fetchHistory(recordMsg);
                    } else {
                        SmartTrace.w(recordMsg,
                                "recordMsg == null || !recordMsg.needPull");
                    }
                }, onError -> {
                    String desc = processJoinRoomError(groupId, onError);
                    callback.joinRoomFailed(SmartConstants.Error.ROOM_JOIN_FAILED,
                            groupId,
                            desc);
                    if (SmartIMClient.getInstance().getChatRoomListener() != null) {
                        SmartIMClient.getInstance().getChatRoomListener().joinRoomFailed(SmartConstants.Error.ROOM_JOIN_FAILED,
                                groupId,
                                desc);
                    }
                });
    }

    private String processJoinRoomError(String groupId, Throwable onError) {
        RoomChat roomChat = SmartIMClient.getInstance().getSmartCommChatRoomManager().getRoomChat(groupId);
        if(roomChat == null) {
            roomChat = new RoomChat();
            SmartTrace.file("roomChat == null");
        }
        roomChat.setState(RoomState.unavailable);
        if (onError instanceof CompositeException) {
            CompositeException compositeException = (CompositeException) onError;
            List<Throwable> exceptions = compositeException.getExceptions();
            for (Throwable throwable : exceptions) {
                SmartTrace.file("加群出错了" + throwable);
                if(throwable instanceof SmackException.NoResponseException) {
                    SmartTrace.file("NoResponseException");
                }
                if (throwable instanceof XMPPException.XMPPErrorException) {
                    SmartTrace.file("XMPPException");
                    XMPPException.XMPPErrorException xmppErrorException = (XMPPException.XMPPErrorException) throwable;
                    StanzaError stanzaError = xmppErrorException.getStanzaError();
                    if (stanzaError != null) {
                        if (stanzaError.getCondition() == StanzaError.Condition.item_not_found
                                || stanzaError.getCondition() == StanzaError.Condition.gone) {
                            //  群聊被解散
                            roomChat.setState(RoomState.destroyed);
                            try {
                                BookmarksManager.getInstance().removeConferenceFromBookmarks(JidCreate.entityBareFrom(groupId));
                            } catch (XmppStringprepException e) {
                                throw new RuntimeException(e);
                            }
                            return SmartCommHelper.getInstance().getString(R.string.room_destroyed);
                        } else if (stanzaError.getCondition() == StanzaError.Condition.forbidden) {
                            try {
                                BookmarksManager.getInstance().removeConferenceFromBookmarks(JidCreate.entityBareFrom(groupId));
                            } catch (XmppStringprepException e) {
                                throw new RuntimeException(e);
                            }
                            if (SmartIMClient.getInstance().getChatRoomListener() != null) {
                                SmartIMClient.getInstance().getChatRoomListener().memberKicked(groupId, SmartCommHelper.getInstance().getAccountIdInGroup(groupId), "normal", true);
                            }
                            // 被封禁
                            roomChat.setState(RoomState.forrbidden);
                            return SmartCommHelper.getInstance().getString(R.string.already_kicked);
                        } else if (stanzaError.getCondition() == StanzaError.Condition.not_authorized) {
                            // 需要密码
                            roomChat.setState(RoomState.error);
                            return SmartCommHelper.getInstance().getString(R.string.required_password);
                        } else if (stanzaError.getCondition() == StanzaError.Condition.remote_server_not_found) {
                            roomChat.setState(RoomState.error);
                            return SmartCommHelper.getInstance().getString(R.string.room_join_failed);
                        }
                    }
                }
            }
        }
        SmartTrace.w(onError,
                "MultiUserChat", "会议室【" + groupId + "】加入失败........");
        SmartTrace.file(onError.toString(), "会议室【" + groupId + "】加入失败........");
        return SmartCommHelper.getInstance().getString(R.string.room_join_failed);
    }

    /**
     * 添加监听器
     *
     * @param muc
     */
    public void addMucListener(MultiUserChat muc) {
        // 获取群聊参与者信息
        String roomJid = muc.getRoom().toString();
        RoomChat roomChat = roomChatMap.get(roomJid);
        if (roomChat == null) {
            SmartTrace.file("addMucListener 初始化roomChat");
            roomChat = new RoomChat();
        }
        roomChat.setMultiUserChat(muc);
        roomChatMap.put(roomJid, roomChat);
        MessageListener msgListener = mucMessageListenerMap.get(roomJid);
        if (msgListener != null) {
            muc.removeMessageListener(msgListener);
        }
        MessageListener messageListener = new MessageListener() {
            @Override
            public void processMessage(Message message) {
                if (message == null) {
                    return;
                }
//                SmartTrace.d("group newIncomingMessage:  = " + message,
//                        "body = " + message.getBody());
                // 输出消息的所有属性和元数据
                SmartIMClient.getInstance().processMsg(false, message);
            }
        };
        muc.addMessageListener(messageListener);
        mucMessageListenerMap.put(roomJid, messageListener);
        ParticipantStatusListener participantStatusListener = mucStatusListenerMap.get(roomJid);
        if (participantStatusListener != null) {
            muc.removeParticipantStatusListener(participantStatusListener);
        }
        MucParticipantStatusListener mucParticipantStatusListener = new MucParticipantStatusListener(roomJid);
        muc.addParticipantStatusListener(mucParticipantStatusListener);
        mucStatusListenerMap.put(roomJid, mucParticipantStatusListener);
        MucMyStatusListener mucMyStatusListener = mucMyStatusListenerMap.get(roomJid);
        if (mucMyStatusListener != null) {
            muc.removeUserStatusListener(mucMyStatusListener);
        }
        MucMyStatusListener myStatusListener = new MucMyStatusListener(roomJid);
        mucMyStatusListenerMap.put(roomJid, myStatusListener);
        muc.addUserStatusListener(myStatusListener);
    }

    /**
     * 成员被踢
     *
     * @param roomId
     * @param memberAccount
     * @param reason
     */
    @Override
    public void processMemberKicked(String roomId, String memberAccount, String reason) {

    }

    @Override
    public void release() {
        roomChatMap.clear();
        mucMessageListenerMap.clear();
        mucStatusListenerMap.clear();
    }

    /**
     * 重新加入
     *
     * @param groupId
     * @param callback
     */
    @Override
    public void rejoinRoom(String groupId, IChatRoomCallback callback) {
        Disposable subscribe = Single.fromCallable(new Callable<MultiUserChat>() {
                    @Override
                    public MultiUserChat call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated())
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        SmartTrace.w(
                                "MultiUserChat", "会议室【" + groupId + "】rejoin 开始........");
                        String nickName = SmartCommHelper.getInstance().getAccountIdInGroup(groupId);
                        MultiUserChat muc = MultiUserChatManager.getInstanceFor(SmartIMClient.getInstance()
                                        .getConnection())
                                .getMultiUserChat(JidCreate.entityBareFrom(groupId));
                        addMucListener(muc);
                        String pwd = StorageUtils.getInstance(SmartConstants.SP_NAME).getString(groupId + SmartConstants.PASS_WORD);
                        if (TextUtils.isEmpty(pwd)) {
                            muc.join(Resourcepart.from(nickName));
                        } else {
                            muc.join(Resourcepart.from(nickName), pwd);
                        }
                        return muc;
                    }
                }).onErrorResumeNext(throwable -> {
                    if (throwable instanceof SIMConnectionException) {
                        SmartIMClient.getInstance().checkConnection();
                    }
                    throw throwable;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(multiUserChat -> {
                    BookmarksManager.getInstance()
                            .addConferenceToBookmarks(
                                    multiUserChat.getRoom().getLocalpart().toString(),
                                    multiUserChat.getRoom(),
                                    Resourcepart.from(SmartCommHelper.getInstance().getAccountIdInGroup(groupId)));
                    callback.joinRoomSuccess(groupId);
                }, onError -> {
                    String desc = processJoinRoomError(groupId, onError);
                    callback.joinRoomFailed(SmartConstants.Error.ROOM_JOIN_FAILED,
                            groupId,
                            desc);
                });
    }

    @Override
    public void roomNameUpdated(String groupId) {
        getRoomInfo(groupId, new IChatRoomCallback() {
            @Override
            public void getGroupInfo(SmartGroupInfo roomInfo) {
                if (SmartIMClient.getInstance().getChatRoomListener() != null) {
                    SmartIMClient.getInstance().getChatRoomListener().groupNameUpdated(groupId, roomInfo);
                }
            }
        });
    }

    @Override
    public void putRecordMsg(String groupId, FetchEntity recordMsg) {
        StorageUtils.getInstance(SmartConstants.SP_NAME).put(groupId + SmartConstants.RECORD_KEY, SIMJsonUtil.serializeToJson(recordMsg));
    }

    /**
     * 获取房间名对应的聊天室实例
     *
     * @param groupId
     * @return
     */
    public MultiUserChat getMucIns(String groupId) {
        RoomChat roomChat = roomChatMap.get(groupId);
        if (null == roomChat) {
            return null;
        }
        return roomChat.getMultiUserChat();
    }

    @Override
    public boolean isJoined(String groupId) {
        RoomChat roomChat = roomChatMap.get(groupId);
        if (null == roomChat) {
            return false;
        }
        return roomChat.getMultiUserChat() != null;
    }

    @Override
    public RoomChat getRoomChat(String groupId) {
        return roomChatMap.get(groupId);
    }


    /**
     * 获取我的群权限
     *
     * @param muc
     * @param callBack
     */
    @Override
    public void getMyAffiliation(MultiUserChat muc, IUserInfoCallBack callBack) {
        Disposable subscribe = Single.fromCallable(new Callable<Occupant>() {
                    @Override
                    public Occupant call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        if (muc == null || !muc.isJoined()) {
                            throw new SIMGroupNotJoinedException();
                        }
                        return muc.getOccupant(JidCreate.entityFullFrom(muc.getRoom(), muc.getNickname()));
                    }
                }).onErrorResumeNext(throwable -> {
                    if (throwable instanceof SIMConnectionException) {
                        SmartIMClient.getInstance().checkConnection();
                    }
                    throw throwable;
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(occupant -> {
                    if (occupant != null) {
                        callBack.getOccupantInfo(occupant);
                    }
                }, onError -> {

                });
    }

    /**
     * 退出聊天
     *
     * @param roomId
     * @param roomCallback
     */
    @Override
    public void destroyRoom(String roomId, IChatRoomCallback roomCallback) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        MultiUserChat multiUserChat = getMucIns(roomId);
                        if (multiUserChat != null) {
                            // 先设为非持久的
                            MUCOwner iq = new MUCOwner();
                            iq.setTo(multiUserChat.getRoom());
                            iq.setType(IQ.Type.get);
                            IQ answer = SmartIMClient.getInstance().getConnection().createStanzaCollectorAndSend(iq).nextResultOrThrow();
                            Form form = new Form(answer.getExtension(DataForm.class));
                            SmartTrace.d("=获得聊天室的配置表单=" + form);
                            // 根据原始表单创建一个要提交的新表单。
                            FillableForm submitForm = form.getFillableForm();
                            submitForm.setAnswer("muc#roomconfig_persistentroom", false);
                            multiUserChat.sendConfigurationForm(submitForm);
                            String reason = String.format(SmartCommHelper.getInstance().getString(R.string.who_destory_group),
                                    SmartCommHelper.getInstance().getAccountIdInGroup(roomId));
                            RoomChat roomChat = roomChatMap.get(roomId);
                            if (roomChat != null) {
                                roomChat.setState(RoomState.unavailable);
                            }
                            multiUserChat.destroy(reason, null);
                            SmartTrace.d("解散群聊");
                            // 并且删除书签
                            BookmarksManager.getInstance().removeConferenceFromBookmarks(multiUserChat.getRoom());
                        }
                        return true;
                    }
                }).onErrorResumeNext(throwable -> {
                    if (throwable instanceof SIMConnectionException) {
                        SmartIMClient.getInstance().checkConnection();
                    }
                    throw throwable;
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(onSuccess -> {
                    if (onSuccess) {
                        roomCallback.deleteRoomSuccess();
                    } else {
                        roomCallback.deleteRoomFailed(SmartConstants.Error.ROOM_DELETED_FAILED, SmartIMClient.getInstance().getString(R.string.room_deleted_failed));
                    }
                }, onError -> {
                    roomCallback.deleteRoomFailed(SmartConstants.Error.ROOM_DELETED_FAILED, SmartIMClient.getInstance().getString(R.string.room_deleted_failed));
                });

    }

    /**
     * 邀请用户加入群聊
     *
     * @param pickedUserIdList
     * @param groupId
     * @param reason
     */
    @Override
    public void inviteUsers(List<String> pickedUserIdList, String groupId, String reason) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        EntityBareJid roomJid = JidCreate.entityBareFrom(groupId);
                        for (String userId : pickedUserIdList) {
                            EntityBareJid inviteeJid = JidCreate.entityBareFrom(userId);
                            // 创建邀请消息
                            Message message = new Message(roomJid);
                            MUCUser mucUser = new MUCUser();
                            MUCUser.Invite invite = new MUCUser.Invite(reason, null, inviteeJid);
                            mucUser.setInvite(invite);
                            message.addExtension(mucUser);
                            // 发送邀请消息
                            SmartIMClient.getInstance().getConnection().sendStanza(message);
                        }
                        return true;
                    }
                }).onErrorResumeNext(throwable -> {
                    if (throwable instanceof SIMConnectionException) {
                        SmartIMClient.getInstance().checkConnection();
                    }
                    throw throwable;
                }).subscribeOn(Schedulers.io())
                .subscribe(onSuccess -> {

                }, onError -> {

                });
    }

    /**
     * 邀请用户加入群聊
     *
     * @param userInfos
     * @param groupId
     * @param reason
     */
    @Override
    public void inviteUsers2(List<SmartUserInfo> userInfos, String groupId, String reason) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        EntityBareJid roomJid = JidCreate.entityBareFrom(groupId);
                        ArrayList<String> jids = new ArrayList<>();
                        ArrayList<String> names = new ArrayList<>();
                        for (int i = 0; i < userInfos.size(); i++) {
                            SmartUserInfo userInfo = userInfos.get(i);
                            jids.add(userInfo.getUserId());
                            names.add(userInfo.getNickname());
                            if (i == 8) {
                                break;
                            }
                        }
                        String joinJids = TextUtils.join(SmartCommHelper.SEPARATOR_COMM, jids);
                        String joinNames = TextUtils.join(SmartCommHelper.SEPARATOR_COMM, names);
                        TestGroupMembersExtension testGroupMembersExtension = new TestGroupMembersExtension(groupId, joinJids, joinNames);
                        RoomChat roomChat = getRoomChat(groupId);
                        boolean hasPermission = false;
                        MultiUserChat multiUserChat = null;
                        if (null != roomChat) {
                            multiUserChat = roomChat.getMultiUserChat();
                            if (multiUserChat != null) {
                                Occupant occupant = multiUserChat.getOccupant(JidCreate.entityFullFrom(roomJid, multiUserChat.getNickname()));
                                if (occupant.getAffiliation() == MUCAffiliation.owner || occupant.getAffiliation() == MUCAffiliation.admin) {
                                    hasPermission = true;
                                }
                            }
                        }
                        for (SmartUserInfo userInfo : userInfos) {
                            EntityBareJid inviteeJid = JidCreate.entityBareFrom(userInfo.getUserId());
                            // 创建邀请消息
                            Message message = new Message(roomJid);
                            MUCUser mucUser = new MUCUser();
                            MUCUser.Invite invite = new MUCUser.Invite(reason, null, inviteeJid);
                            mucUser.setInvite(invite);
                            message.addExtension(mucUser);
                            // 只有管理员才有权限
                            if (hasPermission) {
                                multiUserChat.grantMembership(inviteeJid);
                            }
                            // 发送邀请消息
                            SmartIMClient.getInstance().getConnection().sendStanza(message);
                            List<IExtension> exs = new ArrayList<>();
                            exs.add(testGroupMembersExtension);
                            SmartIMClient.getInstance().getSmartCommMsgManager().sendSingleMessage(inviteeJid.toString(),
                                    SmartContentType.GROUP_INVITATION_MESSAGE,
                                    String.format(SmartCommHelper.getInstance().getString(R.string.group_invitation), XmppUri.getRoomLinkly(groupId)),
                                    exs,
                                    new IMsgCallback() {
                                        @Override
                                        public void onSuccess(SmartMessage msgEntity) {

                                        }

                                        @Override
                                        public void onError(int code, String desc) {

                                        }
                                    });
                        }
                        return true;
                    }
                }).onErrorResumeNext(throwable -> {
                    if (throwable instanceof SIMConnectionException) {
                        SmartIMClient.getInstance().checkConnection();
                    }
                    throw throwable;
                }).subscribeOn(Schedulers.io())
                .subscribe(onSuccess -> {

                }, onError -> {

                });
    }

    /**
     * 离开聊天室
     *
     * @param groupId
     * @param roomCallback
     */
    @Override
    public void leaveRoom(String groupId, IChatRoomCallback roomCallback) {
        // 发送一条退群消息
        SmartIMClient.getInstance().getSmartCommMsgManager().sendGroupMessage(groupId,
                SmartContentType.LEAVE_ROOM,
                "", new ArrayList<>(), new IMsgCallback() {
                    @Override
                    public void onSuccess(SmartMessage msgEntity) {
                        realLeaveRoom(groupId, roomCallback);
                    }

                    @Override
                    public void onError(int code, String desc) {
                        realLeaveRoom(groupId, roomCallback);
                    }
                });

    }

    private void realLeaveRoom(String groupId, IChatRoomCallback roomCallback) {
        try {
            BookmarksManager.getInstance().removeConferenceFromBookmarks(JidCreate.entityBareFrom(groupId));
        } catch (Exception e) {
            SmartTrace.d("realLeaveRoom: " + e);
        }
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        MultiUserChat multiUserChat = SmartIMClient.getInstance().getSmartCommChatRoomManager().getMucIns(groupId);
                        SmartTrace.d("multiUserChat: " + multiUserChat);
                        // todo 是否移除监听?
                        if (multiUserChat != null && multiUserChat.isJoined()) {
                            // 并且删除书签 tigase 已经leave了 却阻塞
//                            multiUserChat.leave();
                            // 创建类的实例
                            // 获取私有字段
                            SmartTrace.d("leve process: ");
                            Field field = MultiUserChat.class.getDeclaredField("myRoomJid");
                            field.setAccessible(true); // 取消访问检查
                            // 获取字段值
                            EntityFullJid roomJid = (EntityFullJid) field.get(multiUserChat);
                            SmartTrace.d(roomJid);
                            Presence leavePresence = SmartIMClient.getInstance().getConnection().getStanzaFactory().buildPresenceStanza()
                                    .ofType(Presence.Type.unavailable)
                                    .to(roomJid)
                                    .build();
                            SmartIMClient.getInstance().getConnection().sendStanza(leavePresence);
                            // 获取私有方法
                            Method method = MultiUserChat.class.getDeclaredMethod("userHasLeft");
                            method.setAccessible(true); // 取消访问检查
                            // 调用私有方法
                            method.invoke(multiUserChat);
                            // 离开时还得移除roomChat实例
                            roomChatMap.remove(groupId);
                        } else {
                            return false;
                        }
                        return true;
                    }
                }).onErrorResumeNext(throwable -> {
                    if (throwable instanceof SIMConnectionException) {
                        SmartIMClient.getInstance().checkConnection();
                    }
                    throw throwable;
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess -> {
                    SmartCommHelper.getInstance().executeWithDelay(SmartConstants.DELAY_250, () -> {
                        StorageUtils.getInstance(SmartConstants.SP_NAME).remove(groupId + SmartConstants.RECORD_KEY);
                    });
                    if (onSuccess) {
                        roomCallback.leaveSuccess();
                    } else {
                        roomCallback.leaveFailed(SmartConstants.Error.LEAVE_ROOM_FAILED, SmartIMClient.getInstance().getString(R.string.leave_room_failed));
                    }
                }, onError -> {
                    roomCallback.leaveFailed(SmartConstants.Error.LEAVE_ROOM_FAILED, SmartIMClient.getInstance().getString(R.string.leave_room_failed));
                });
    }

    /**
     * 从书签获取保存的聊天室
     *
     * @param bookmarkedConferenceCallback
     */
    @Override
    public void getRoomsByBookmark(IBookmarkedConferenceCallback bookmarkedConferenceCallback) {
        Disposable subscribe = Single.fromCallable(new Callable<List<BookmarkedConference>>() {
                    @Override
                    public List<BookmarkedConference> call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        List<BookmarkedConference> conferences;
                        BookmarkManager bookmarkManager = BookmarkManager.getBookmarkManager(SmartIMClient.getInstance().getConnection());
                        conferences = bookmarkManager.getBookmarkedConferences();
                        return conferences;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(conferences -> {
                    bookmarkedConferenceCallback.onSuccess(conferences);
                }, onError -> {

                });
    }

    /**
     * 获取聊天室成员
     *
     * @param groupId
     * @param iGroupMemberCallback
     */
    @Override
    public void getGroupMemberList(String groupId, IGroupMemberCallback iGroupMemberCallback) {
        Disposable subscribe = Single.fromCallable(new Callable<List<SmartUserInfo>>() {
                    @Override
                    public List<SmartUserInfo> call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        List<SmartUserInfo> smartUserInfoList = new ArrayList<>();
                        MultiUserChat multiUserChat = getMucIns(groupId);
                        if (null != multiUserChat) {
                            List<EntityFullJid> occupants = multiUserChat.getOccupants();
                            for (EntityFullJid fullJid : occupants) {
                                Resourcepart resourceOrNull = fullJid.getResourceOrNull();
                                if (null != resourceOrNull) {
                                    // 获取每个成员的 Occupant 对象 这里没办法同时获取jid和昵称
                                    Occupant occupant = multiUserChat.getOccupant(fullJid);
                                    if (occupant == null) {
                                        continue;
                                    }
                                    SmartUserInfo smartUserInfo = new SmartUserInfo();
                                    MUCAffiliation affiliation = occupant.getAffiliation();
                                    MUCRole role = occupant.getRole();
                                    Jid jid = occupant.getJid();
                                    Resourcepart nick = occupant.getNick();
                                    smartUserInfo.setRole(role.name());
                                    smartUserInfo.setAffiliation(affiliation.name());
                                    smartUserInfo.setMemberAccount(String.valueOf(nick));
                                    if (jid != null) {
                                        // 真实的jid
                                        EntityBareJid bareJid = jid.asEntityBareJidIfPossible();
                                        smartUserInfo.setUserId(String.valueOf(bareJid));
                                        smartUserInfo.setNickname(SmartIMClient.getInstance().getSmartCommUserManager()
                                                .getUserNicknameSync(bareJid.toString(), true));
                                        SmartTrace.d("允许查看jid " + bareJid, smartUserInfo.getNickname());
                                    } else {
                                        smartUserInfo.setNickname(String.valueOf(nick));
                                        SmartTrace.d("匿名的 " );
//                                        smartUserInfo.setUserId(fullJid.toString());
                                    }
                                    smartUserInfoList.add(smartUserInfo);
                                }
                            }
                        }
                        return smartUserInfoList;
                    }
                }).onErrorResumeNext(throwable -> {
                    if (throwable instanceof SIMConnectionException) {
                        SmartIMClient.getInstance().checkConnection();
                    }
                    throw throwable;
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(userInfos -> {
                    iGroupMemberCallback.onSuccess(userInfos);
                }, onError -> {

                });
    }

    /**
     * 拉取历史消息
     */
    @Override
    public void fetchHistory(FetchEntity recordMsg) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        MamManager mamManager;
                        if (recordMsg.isSingle) {
                            mamManager = MamManager.getInstanceFor(SmartIMClient.getInstance().getConnection());
                        } else {
                            if (getMucIns(recordMsg.conversationId) == null) {
                                return false;
                            }
                            mamManager = MamManager.getInstanceFor(getMucIns(recordMsg.conversationId));
                        }
                        if (!mamManager.isSupported()) {
                            SmartTrace.w("不支持拉取");
                            return false;
                        }
                        Jid from = JidCreate.from(recordMsg.conversationId);
                        // 构建查询
                        MamManager.MamQueryArgs.Builder queryArgsBuilder;
                        if (recordMsg.isFetchHistory) {
                            queryArgsBuilder = MamManager.MamQueryArgs.builder()
                                    .beforeUid(recordMsg.archivedId)
                                    .setResultPageSize(FETCH_SIZE)
                                    .limitResultsToJid(from); // 设置每页结果数量
                        } else {
                            Date start = new Date(recordMsg.createTime);
                            queryArgsBuilder = MamManager.MamQueryArgs.builder()
                                    // 限制自该时间之后的消息
                                    .limitResultsSince(start)
                                    .setResultPageSize(FETCH_SIZE); // 设置每页结果数量
                            SmartTrace.w(recordMsg.conversationId,
                                    "拉取离线消息 从id : " + recordMsg.archivedId,
                                    "从时间 " + start,
                                    "isFetchHistory " + recordMsg.isFetchHistory);
                        }

                        /*if (lastMessageDate != null) {
                            // 如果有记录上次拉取的最后一条消息时间，从该时间之后开始拉取
                            queryArgsBuilder.limitResultsSince(lastMessageDate);
                        }*/
                        // 异步的查询
                        MamManager.MamQuery mamQuery = mamManager.queryArchive(queryArgsBuilder.build());
                        List<MamElements.MamResultExtension> results = mamQuery.getMamResultExtensions();
                        MultiUserChat multiUserChat = getMucIns(recordMsg.conversationId);
                        if (multiUserChat == null || !multiUserChat.isJoined()) {
                            return false;
                        }
                        SmartTrace.w("size" + results.size() + ">>>msg size =>>>" + mamQuery.getMessages().size());
                        if (mamQuery.getMessages().isEmpty()) {
                            FetchEntity fetchEntity = getFetchEntity(recordMsg.conversationId);
                            if (fetchEntity != null) {
                                SmartTrace.w("没有需要拉取的离线消息。");
                                fetchEntity.needPull = false;
                                putRecordMsg(recordMsg.conversationId, fetchEntity);
                            }
                            return true;
                        }
                        List<Message> historyMessages = new ArrayList<>();
                        for (MamElements.MamResultExtension result : results) {
                            Message message = result.getForwarded().getForwardedStanza();
                            // 获取DelayInformation
                            DelayInformation delayInformation = result.getForwarded().getDelayInformation();
                            if (delayInformation != null) {
                                // 将DelayInformation附加到Message
                                message.addExtension(delayInformation);
                            }
                            SmartTrace.w(message,
                                    " 离线消息 " + message.getBody());
                            historyMessages.add(message);
                        }
                        // 翻转列表 拉取才反转？
                        if (recordMsg.isFetchHistory) {
                            Collections.reverse(historyMessages);
                        }
                        for (Message message : historyMessages) {
                            SmartIMClient.getInstance().processMsg(recordMsg.isFetchHistory, message);
                        }
                        if (mamQuery.getMessages().size() >= FETCH_SIZE) {
                            // 时间更新为 最后一条消息的时间
                            Message lastMessage = historyMessages.get(historyMessages.size() - 1);
                            DelayInformation extension = lastMessage.getExtension(DelayInformation.class);
                            if (extension != null) {
                                // 时间更新为 最后一条消息的时间
                                recordMsg.createTime = extension.getStamp().getTime();
                                putRecordMsg(recordMsg.conversationId, recordMsg);
                            }
                            SmartCommHelper.getInstance().executeWithDelay(SmartConstants.SHORT_DELAY, () -> {
                                FetchEntity fetchEntity = getFetchEntity(recordMsg.conversationId);
                                if (fetchEntity != null) {
                                    MultiUserChat mucIns = getMucIns(recordMsg.conversationId);
                                    if (mucIns != null && mucIns.isJoined()) {
                                        SmartTrace.w(" 超过 还需要拉取 离线消息 ");
                                        fetchEntity.needPull = true;
                                        putRecordMsg(recordMsg.conversationId, fetchEntity);
                                        fetchHistory(fetchEntity);
                                    }
                                }
                            });
                        } else {
                            recordMsg.needPull = false;
                            SmartTrace.w(
                                    " 已拉取全部离线消息 ");
                            putRecordMsg(recordMsg.conversationId, recordMsg);
                        }
                        return true;
                    }
                }).onErrorResumeNext(throwable -> {
                    if (throwable instanceof SIMConnectionException) {
                        SmartIMClient.getInstance().checkConnection();
                    }
                    throw throwable;
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(onSuccess -> {
                    if (onSuccess) {

                    } else {

                    }
                }, onError -> {

                });

    }

    @Override
    public FetchEntity getFetchEntity(String conversationId) {
        String jsonRecordMsg = StorageUtils.getInstance(SmartConstants.SP_NAME).getString(conversationId + SmartConstants.RECORD_KEY);
        return SIMJsonUtil.deserializeByJson(jsonRecordMsg, FetchEntity.class);
    }

    @Override
    public void checkMucAndForceLeave() {
        if (SmartIMClient.getInstance().isAuthenticated()) {
            // 已经连接不重置
            SmartTrace.file("已经连接：");
            return;
        }
        for (String groupId : roomChatMap.keySet()) {
            MultiUserChat muc = getMucIns(groupId);
            if (muc != null && muc.isJoined()) {
                try {
                    // 获取 Class 对象
                    Class<?> clazz = muc.getClass();
                    // 获取私有方法 userHasLeft
                    Method method = clazz.getDeclaredMethod("userHasLeft");
                    // 设置可访问性为 true
                    method.setAccessible(true);
                    // 调用方法
                    method.invoke(muc);
                    SmartTrace.file("群组置为离开状态：" + groupId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 修改我在群里的昵称
     *
     * @param newNickname
     * @param groupId
     * @param roomCallback
     */
    @Override
    public void changeMyNickname(String newNickname, String groupId, IChatRoomCallback roomCallback) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        MultiUserChat muc = getMucIns(groupId);
                        if (muc == null || !muc.isJoined()) {
                            throw new SIMGroupNotJoinedException();
                        }
                        Resourcepart newName = Resourcepart.from(newNickname);
                        final EntityFullJid entityFullJid = JidCreate.entityFullFrom(JidCreate.entityBareFrom(groupId), newName);
                        Presence joinPresence = SmartIMClient.getInstance().getConnection()
                                .getStanzaFactory().buildPresenceStanza()
                                .to(entityFullJid)
                                .ofType(Presence.Type.available)
                                .build();
                        StanzaFilter responseFilter =
                                new AndFilter(
                                        FromMatchesFilter.createFull(entityFullJid),
                                        new StanzaTypeFilter(Presence.class));
                        StanzaCollector response = SmartIMClient.getInstance().getConnection()
                                .createStanzaCollectorAndSend(responseFilter, joinPresence);
                        response.nextResultOrThrow();
                        Method setNicknameMethod = MultiUserChat.class.getDeclaredMethod("setNickname", Resourcepart.class);
                        // 如果方法是 private，您需要设置 accessible 为 true
                        setNicknameMethod.setAccessible(true);
                        setNicknameMethod.invoke(muc, newName);
                        // 获取 messageListener 字段
                        try {
                            Field messageListenerField = MultiUserChat.class.getDeclaredField("messageListener");
                            messageListenerField.setAccessible(true);  // 允许访问私有字段
                            StanzaListener messageListener = (StanzaListener) messageListenerField.get(muc);
                            // 获取 presenceListener 字段
                            Field presenceListenerField = MultiUserChat.class.getDeclaredField("presenceListener");
                            presenceListenerField.setAccessible(true);  // 允许访问私有字段
                            StanzaListener presenceListener = (StanzaListener) presenceListenerField.get(muc);
                            FromMatchesFilter fromRoomFilter = FromMatchesFilter.create(JidCreate.entityBareFrom(groupId));
                            if (null != messageListener && null != presenceListener) {
                                SmartIMClient.getInstance().getConnection()
                                        .addStanzaListener(messageListener,
                                                new AndFilter(fromRoomFilter, MessageTypeFilter.GROUPCHAT));
                                StanzaFilter presenceFromRoomFilter = new AndFilter(fromRoomFilter,
                                        StanzaTypeFilter.PRESENCE,
                                        PossibleFromTypeFilter.ENTITY_FULL_JID);
                                SmartIMClient.getInstance().getConnection().addStanzaListener(presenceListener, presenceFromRoomFilter);
                            }
                        } catch (Exception e) {

                        }
                        return true;
                    }
                })
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof SIMConnectionException) {
                        SmartIMClient.getInstance().checkConnection();
                    }
                    throw throwable;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(isSuccess -> {
                    SmartCommHelper.getInstance().setAccountInGroup(groupId, newNickname);
                    roomCallback.changeNicknameInGroupSuccess();
                }, onError -> {
                    if (onError instanceof SIMGroupNotJoinedException) {
                    } else {
                        if (onError instanceof CompositeException) {
                            CompositeException compositeException = (CompositeException) onError;
                            List<Throwable> exceptions = compositeException.getExceptions();
                            for (Throwable throwable : exceptions) {
                                if (throwable instanceof XMPPException.XMPPErrorException) {
                                    XMPPException.XMPPErrorException xe = (XMPPException.XMPPErrorException) throwable;
                                    roomCallback.changeNicknameInGroupFailed(SmartConstants.Error.CHANGE_NICK_IN_GROUP_FAILED,
                                            xe.getStanzaError().getDescriptiveText());
                                    return;
                                }
                            }
                        }
                        String errorDesc = SmartIMClient.getInstance().getString(R.string.change_failed);
                        roomCallback.changeNicknameInGroupFailed(SmartConstants.Error.CHANGE_NICK_IN_GROUP_FAILED,
                                errorDesc);
                    }
                });
    }

    @Override
    public void getGroupAdministrators(String groupId, IChatRoomCallback callback) {
        Disposable subscribe = Single.fromCallable(new Callable<List<SmartUserInfo>>() {
                    @Override
                    public List<SmartUserInfo> call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        List<SmartUserInfo> smartUserInfoList = new ArrayList<>();
                        MultiUserChat multiUserChat = getMucIns(groupId);
                        if (null != multiUserChat) {
                            List<Affiliate> admins = multiUserChat.getAdmins();
                            for (Affiliate affiliate : admins) {
                                Jid fullJid = affiliate.getJid();
                                SmartUserInfo smartUserInfo = new SmartUserInfo();
                                smartUserInfo.setUserId(String.valueOf(fullJid));
                                SmartTrace.d("jid " + fullJid);
                                // 最好是muc 需要客户端去查询
                                smartUserInfo.setNickname(SmartIMClient.getInstance()
                                        .getSmartCommUserManager().getUserNicknameSync(fullJid.toString(), true));
                                smartUserInfoList.add(smartUserInfo);
                            }
                        }
                        return smartUserInfoList;
                    }
                }).onErrorResumeNext(throwable -> {
                    if (throwable instanceof SIMConnectionException) {
                        SmartIMClient.getInstance().checkConnection();
                    }
                    throw throwable;
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(userInfos -> {
                    callback.getAdmins(userInfos);
                }, onError -> {

                });
    }

    @Override
    public void grantAdmin(List<String> realJid, String groupId, IChatRoomCallback callback) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        List<Jid> jidList = new ArrayList<>();
                        for (String jid : realJid) {
                            jidList.add(JidCreate.from(jid));
                        }
                        List<SmartUserInfo> smartUserInfoList = new ArrayList<>();
                        MultiUserChat multiUserChat = getMucIns(groupId);
                        if (null != multiUserChat) {
                            multiUserChat.grantAdmin(jidList);
                        }
                        return true;
                    }
                }).onErrorResumeNext(throwable -> {
                    if (throwable instanceof SIMConnectionException) {
                        SmartIMClient.getInstance().checkConnection();
                    }
                    throw throwable;
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(s -> {
                    callback.grantAdminSuccess();
                }, onError -> {

                });
    }

    @Override
    public void revokeAdmin(String realJid, String groupId, IChatRoomCallback callback) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        MultiUserChat multiUserChat = getMucIns(groupId);
                        if (null != multiUserChat) {
                            multiUserChat.revokeAdmin(JidCreate.entityBareFrom(realJid));
                        }
                        return true;
                    }
                }).onErrorResumeNext(throwable -> {
                    if (throwable instanceof SIMConnectionException) {
                        SmartIMClient.getInstance().checkConnection();
                    }
                    throw throwable;
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(s -> {
                    callback.revokeAdminSuccess();
                }, onError -> {

                });
    }

    @Override
    public void checkGroupStatus(String groupId) {
        RoomChat roomChat = SmartIMClient.getInstance().getSmartCommChatRoomManager().getRoomChat(groupId);
        if (!SmartCommHelper.getInstance().checkMucJoined(groupId)) {
            if (roomChat != null && roomChat.canJoin() && roomChat.getState() != RoomState.joining) {
                rejoinRoom(groupId, new IChatRoomCallback() {
                    @Override
                    public void joinRoomSuccess(String groupId) {
                        if (SmartIMClient.getInstance().getChatRoomListener() != null) {
                            SmartIMClient.getInstance().getChatRoomListener().joinRoomSuccess(groupId);
                        }
                    }

                    @Override
                    public void joinRoomFailed(int code, String groupId, String desc) {
                        if (SmartIMClient.getInstance().getChatRoomListener() != null) {
                            SmartIMClient.getInstance().getChatRoomListener().joinRoomFailed(code, groupId, desc);
                        }
                    }
                });
            }
        }
    }

    /**
     * 获取群信息
     *
     * @param groupId
     * @param roomCallback
     */
    @Override
    public void getRoomInfo(String groupId, IChatRoomCallback roomCallback) {
        Disposable subscribe = Single.fromCallable(new Callable<SmartGroupInfo>() {
                    @Override
                    public SmartGroupInfo call() throws Exception {
                        // muc被销毁后 是无法获取信息的 而xabber能获取证明没有销毁
                        SmartTrace.w("获取群信息 getRoomInfo");
                        MultiUserChatManager mucManager = MultiUserChatManager.getInstanceFor(SmartIMClient.getInstance().getConnection());
                        RoomInfo roomInfo = mucManager.getRoomInfo(JidCreate.entityBareFrom(groupId));
                        SmartGroupInfo smartGroupInfo = new SmartGroupInfo();
                        smartGroupInfo.setGroupID(groupId);
                        smartGroupInfo.setGroupName(roomInfo.getName());
                        smartGroupInfo.setSubject(roomInfo.getSubject());
                        smartGroupInfo.setMembersOnly(roomInfo.isMembersOnly());
                        smartGroupInfo.setModerated(roomInfo.isModerated());
                        smartGroupInfo.setPasswordProtected(roomInfo.isPasswordProtected());
                        smartGroupInfo.setConf(roomInfo.getForm());
                        smartGroupInfo.setNonanonymous(roomInfo.isNonanonymous());
                        return smartGroupInfo;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(info -> {
                    if (info != null) {
                        roomCallback.getGroupInfo(info);
                    } else {
                        roomCallback.getGroupInfoFailed();
                    }
                }, onError -> {
                    roomCallback.getGroupInfoFailed();
                });
    }

    /**
     * 更改房间名称
     *
     * @param roomJid
     * @param roomName
     * @param callback
     */
    @Override
    public void changeRoomName(String roomJid, String roomName, IChatRoomCallback callback) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {

                        MultiUserChat muc = getMucIns(roomJid);
                        MUCOwner iq = new MUCOwner();
                        iq.setTo(muc.getRoom());
                        iq.setType(IQ.Type.get);
                        IQ answer = SmartIMClient.getInstance().getConnection().createStanzaCollectorAndSend(iq).nextResultOrThrow();
                        Form form = new Form(answer.getExtension(DataForm.class));
                        FillableForm submitForm = form.getFillableForm();
                        submitForm.setAnswer("muc#roomconfig_roomname", roomName);
                        muc.sendConfigurationForm(submitForm);
                       /* MultiUserChat mucChat = getMucIns(roomJid);
                        if (mucChat == null) {
                            return false;
                        }
                        Form configForm = mucChat.getConfigurationForm();
                        FillableForm answerForm = configForm.getFillableForm();
                        if (answerForm.hasField("muc#roomconfig_roomname")) {
                            answerForm.setAnswer("muc#roomconfig_roomname", roomName);
                            mucChat.sendConfigurationForm(answerForm);
                        } else {
                            return false;
                        }*/
                        return true;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(onSuccess -> {
                    if (onSuccess) {
                        callback.changeNameSuccess();
                    } else {
                        callback.changeNameFailed();
                    }
                }, onError -> {
                    callback.changeNameFailed();
                });
    }

    /**
     * 移除出群
     *
     * @param userInfos
     * @param groupId
     */
    @Override
    public void kickGroupMember(List<SmartUserInfo> userInfos, String groupId, IChatRoomCallback callback) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        MultiUserChat mucChat = getMucIns(groupId);
                        for (SmartUserInfo userInfo : userInfos) {
                            mucChat.kickParticipant(Resourcepart.from(userInfo.getMemberAccount()), SmartConstants.KICKED_REASON);
                            mucChat.banUser(JidCreate.from(userInfo.getUserId()), SmartConstants.KICKED_REASON);
                        }
                        return true;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(onSuccess -> {
                    if (onSuccess) {
                        callback.kickedSuccess();
                    } else {
                        callback.kickedFailed();
                    }
                }, onError -> {
                    callback.kickedFailed();
                });

    }

    @Override
    public void changeGroupAvatar(String groupId, File file, IChatRoomCallback iChatRoomCallback) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        byte[] bytes = BitmapUtils.getFileBytes(file);
                        VCard vCard = new VCard();
                        vCard.setAvatar(bytes, "image/jpeg");
                        // 假设你已经有一个MultiUserChat对象 muc
                        MultiUserChat muc = getMucIns(groupId);
                        if (muc == null || !muc.isJoined()) {
                            throw new SIMGroupNotJoinedException();
                        }
                        vCard.setTo(muc.getRoom());
                        vCard.setType(IQ.Type.set);
                        SmartIMClient.getInstance().getConnection().sendStanza(vCard);
                        return true;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(avatarHash -> {
                    iChatRoomCallback.changeGroupAvatarSuccess();
                }, onError -> {
                    iChatRoomCallback.changeGroupAvatarFailed();
                });
    }

    @Override
    public void muteMember(boolean muted, String groupId, String account, IChatRoomCallback callback) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        MultiUserChat multiUserChat = getMucIns(groupId);
                        if (null != multiUserChat) {
                            if (muted) {
                                multiUserChat.revokeVoice(Resourcepart.from(account));
                            } else {
                                multiUserChat.grantVoice(Resourcepart.from(account));
                            }
                        }
                        return true;
                    }
                }).onErrorResumeNext(throwable -> {
                    if (throwable instanceof SIMConnectionException) {
                        SmartIMClient.getInstance().checkConnection();
                    }
                    throw throwable;
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(s -> {
                    callback.muteMemberSuccess();
                }, onError -> {

                });
    }
}
