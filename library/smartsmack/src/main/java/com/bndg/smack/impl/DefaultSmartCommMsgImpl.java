package com.bndg.smack.impl;


import android.text.TextUtils;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.MessageBuilder;
import org.jivesoftware.smack.packet.MessageView;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.StanzaBuilder;
import org.jivesoftware.smackx.chat_markers.element.ChatMarkersElements;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.httpfileupload.HttpFileUploadManager;
import org.jivesoftware.smackx.httpfileupload.UploadProgressListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jivesoftware.smackx.omemo.OmemoManager;
import org.jivesoftware.smackx.omemo.OmemoMessage;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.bndg.smack.OmemoHelper;
import com.bndg.smack.R;
import com.bndg.smack.SmartCommHelper;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.callback.IChatRoomCallback;
import com.bndg.smack.callback.IFileCallback;
import com.bndg.smack.callback.IMsgCallback;
import com.bndg.smack.callback.ISmartCallback;
import com.bndg.smack.constant.SmartConstants;
import com.bndg.smack.contract.ISmartCommMsg;
import com.bndg.smack.entity.SmartMessage;
import com.bndg.smack.enums.SmartContentType;
import com.bndg.smack.exceptions.SIMConnectionException;
import com.bndg.smack.exceptions.SIMGroupNotJoinedException;
import com.bndg.smack.exceptions.SIMNotFriendException;
import com.bndg.smack.extensions.MessageTypeExtension;
import com.bndg.smack.extensions.OobDataExtension;
import com.bndg.smack.extensions.SenderInfoExtension;
import com.bndg.smack.extensions.base.ElementFactory;
import com.bndg.smack.extensions.base.IExtension;
import com.bndg.smack.muc.RoomChat;
import com.bndg.smack.muc.RoomState;
import com.bndg.smack.utils.SIMJsonUtil;
import com.bndg.smack.utils.SmartTrace;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class DefaultSmartCommMsgImpl extends BaseXmppImpl implements ISmartCommMsg {

    PublishSubject<MessageData> messageSubject = PublishSubject.create();
    AtomicInteger messageCount = new AtomicInteger(0);
    // 暂存消息
    private LinkedList<MessageData> tempMessageList = new LinkedList<>();

    public DefaultSmartCommMsgImpl() {
        initObserver();
    }

    private void initObserver() {
        messageSubject
                .concatMap(message -> {
                    // 在这里获取当前的消息数量
                    int count = messageCount.get(); // 这里不自增
                    long delay = Math.max(count, 0) * 100L; // 使用当前计数来设置延迟
                    SmartTrace.d("延迟发送 " + delay);
                    return Single.timer(delay, TimeUnit.MILLISECONDS)
                            .flatMap(ignore -> Single.fromCallable(() -> {
                                if (message.isSingleMsg) {
                                    realSendSingleMessage(
                                            message.toContactJid,
                                            message.msgType,
                                            message.messageContent,
                                            message.extensions,
                                            message.iMsgCallback
                                    );
                                } else {
                                    realSendGroupMessage(
                                            message.toContactJid,
                                            message.msgType,
                                            message.messageContent,
                                            message.extensions,
                                            message.iMsgCallback
                                    );
                                }

                                return true;
                            }))
                            .toObservable()
                            .doOnSubscribe(disposable -> messageCount.incrementAndGet()) // 在订阅时增加计数
                            .doFinally(() -> messageCount.decrementAndGet()); // 完成后减少计数
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    private void realSendGroupMessage(String groupId, String msgType, String messageContent, List<IExtension> extensions, IMsgCallback msgCallback) {
        Disposable subscribe = Single.fromCallable(new Callable<SmartMessage>() {
                    @Override
                    public SmartMessage call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        MultiUserChat muc = SmartIMClient.getInstance().getSmartCommChatRoomManager().getMucIns(groupId);
                        if (muc == null || !muc.isJoined()) {
                            throw new SIMGroupNotJoinedException();
                        }
                        MessageBuilder messageBuilder = MessageBuilder.buildMessage(UUID.randomUUID().toString());
                        messageBuilder.setBody(messageContent);
                        // 添加自定义扩展，包含发送者的信息
                        if (!SmartCommHelper.getInstance().isDeveloperMode()) {
                            messageBuilder.addExtension(new SenderInfoExtension(SmartCommHelper.getInstance().getUserId(), SmartCommHelper.getInstance().getNickname()));
                        }
                        messageBuilder.addExtension(new MessageTypeExtension(msgType)); // 自定义扩展，传递消息类型
                        for (IExtension element : extensions) {
                            messageBuilder.addExtension(ElementFactory.createBaseExtensionElement(element));
                        }
                        // obb支持xmpp其他客户端显示图片
                        if (SIMJsonUtil.isFileType(msgType)) {
                            messageBuilder.addExtension(new OobDataExtension(messageContent));
                        }
                        MessageView messageView = muc.sendMessage(messageBuilder);
                        messageView.getStanzaId();
                        SmartTrace.file(messageBuilder + " 发送群消息 ",
                                messageBuilder.getStanzaId());
                        return SmartMessage.createSendGroupMessage(messageBuilder.getStanzaId(),
                                groupId,
                                SmartCommHelper.getInstance().getUserId(),
                                SmartCommHelper.getInstance().getNickname(),
                                msgType,
                                messageContent); // Void类型，表示没有返回值;
                    }
                }).onErrorResumeNext(throwable -> {
                    if (throwable instanceof SIMConnectionException) {
                        SmartIMClient.getInstance().checkConnection();
                    }
                    if (throwable instanceof SIMGroupNotJoinedException) {
                        // 发送消息失败 可能是连接中断 暂时存储消息 重新入群后发送
                        MessageData messageData = new MessageData(false, groupId, msgType,
                                messageContent, extensions, msgCallback);
                        tempMessageList.add(messageData);
                        RoomChat roomChat = SmartIMClient.getInstance().getSmartCommChatRoomManager().getRoomChat(groupId);
                        if (roomChat == null) {
                            roomChat = new RoomChat();
                        } else if (roomChat.getState() == RoomState.joining) {
                            return Single.error(throwable);
                        } else if (!roomChat.canJoin()) {
                            ((SIMGroupNotJoinedException) throwable).setCanNotJoin(true);
                            return Single.error(throwable); // 可替换为其他逻辑
                        }
                        roomChat.setState(RoomState.joining);
                        SmartIMClient.getInstance().getSmartCommChatRoomManager().rejoinRoom(groupId, new IChatRoomCallback() {
                            @Override
                            public void joinRoomSuccess(String groupId) {
                                // 重新入群成功，发送暂存消息
                                for (MessageData messageData : tempMessageList) {
                                    messageSubject.onNext(messageData);
                                }
                            }

                            @Override
                            public void joinRoomFailed(int code, String groupId, String desc) {
                                msgCallback.onError(SmartConstants.Error.SEND_FAILED_CANT_JOIN_GROUP,
                                        SmartIMClient.getInstance().getString(R.string.SEND_FAILED_CANT_JOIN_GROUP));
                            }
                        });
                        return Single.error(throwable); // 可替换为其他逻辑
                    }
                    throw throwable;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(smartMessage -> {
                    if (smartMessage != null) {
                        msgCallback.onSuccess(smartMessage);
                    } else {
                        SmartTrace.file("发送失败 msg为null");
                        msgCallback.onError(SmartConstants.Error.SEND_GROUP_MSG_FAILED,
                                SmartIMClient.getInstance().getString(R.string.SEND_GROUP_MSG_FAILED));
                    }
                }, onError -> {
                    SmartTrace.file("发送失败 " + onError);
                    if (onError instanceof SIMGroupNotJoinedException) {
                        SIMGroupNotJoinedException exception = (SIMGroupNotJoinedException) onError;
                        if (exception.isCanNotJoin()) {
                            msgCallback.onError(SmartConstants.Error.SEND_FAILED_CANT_JOIN_GROUP,
                                    SmartIMClient.getInstance().getString(R.string.SEND_FAILED_CANT_JOIN_GROUP));
                        }
                    } else {
                        msgCallback.onError(SmartConstants.Error.SEND_GROUP_MSG_FAILED,
                                SmartIMClient.getInstance().getString(R.string.SEND_GROUP_MSG_FAILED));
                    }
                });
    }

    /**
     * 发送单聊消息
     *
     * @param toContactJid
     * @param msgType
     * @param messageContent
     * @param extensions
     * @param iMsgCallback
     */
    private void realSendSingleMessage(String toContactJid, String msgType, String messageContent, List<IExtension> extensions, IMsgCallback iMsgCallback) {
        if (SmartContentType.TEXT.equals(msgType) && OmemoHelper.getInstance().isEnableEncrypt(toContactJid)) {
            sendOmemoMessage(toContactJid, msgType, messageContent, extensions, iMsgCallback);
            return;
        }
        Disposable subscribe = Single.fromCallable(new Callable<SmartMessage>() {
                    @Override
                    public SmartMessage call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        SmartTrace.d(
                                "发送单聊消息 " + SmartIMClient.getInstance().getConnection(),
                                " <toContactJid> " + toContactJid);
                        // 判断是否是好友 单向好友也不允许发送？
                        boolean isFriend = SmartIMClient.getInstance().getFriendshipManager().checkIsFriend(toContactJid);
                        if (!isFriend) {
                            throw new SIMNotFriendException();
                        }
                        Message message = SmartIMClient.getInstance().getConnection()
                                .getStanzaFactory()
                                .buildMessageStanza()
                                .to(toContactJid)
                                .setBody(messageContent)
                                .build();
                        message.setType(Message.Type.chat);
                        for (IExtension element : extensions) {
                            message.addExtension(ElementFactory.createBaseExtensionElement(element));
                        }
                        message.addExtension(new MessageTypeExtension(msgType));
                        if (SIMJsonUtil.isFileType(msgType)) {
                            message.addExtension(new OobDataExtension(messageContent));
                        }
                        SmartIMClient.getInstance().getConnection().sendStanza(message);
                        // 发送消息
                        String toUserId = message.getTo().asBareJid().toString();
                        return SmartMessage.createSendSingleMessage(message.getStanzaId(),
                                SmartCommHelper.getInstance().getUserId(),
                                toUserId, msgType, messageContent); // Void类型，表示没有返回值
                    }
                }).onErrorResumeNext(throwable -> {
                    if (throwable instanceof SIMConnectionException) {
                        SmartIMClient.getInstance().checkConnection();
                    }
                    throw throwable;
                })
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(msg -> {
                    if (null != msg) {
                        iMsgCallback.onSuccess(msg);
                    }
                }, error -> {
                    if (error.getCause() instanceof SIMNotFriendException) {
                        iMsgCallback.onError(SmartConstants.Error.SEND_MSG_FAILED_NO_FRIEND,
                                SmartIMClient.getInstance().getString(R.string.NOT_FRIEND));
                    } else {
                        iMsgCallback.onError(SmartConstants.Error.SEND_MSG_FAILED,
                                SmartIMClient.getInstance().getInstance().getInstance().getInstance().getString(R.string.SEND_MSG_FAILED));
                    }
                });
    }

    public void sendOmemoMessage(String toContactJid, String msgType, String messageContent, List<IExtension> extensions, IMsgCallback iMsgCallback) {
        SmartIMClient.getInstance().getSmartCommUserManager().checkTrustDevice(toContactJid, new ISmartCallback() {
            @Override
            public void onSuccess() {
                Disposable subscribe = Single.fromCallable(new Callable<SmartMessage>() {
                            @Override
                            public SmartMessage call() throws Exception {
                                if (!SmartIMClient.getInstance().isAuthenticated()) {
                                    throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                                }
                                SmartTrace.d(
                                        "发送omemo加密消息: " + SmartIMClient.getInstance().getConnection(),
                                        " <toContactJid> " + toContactJid);
                                boolean isFriend = SmartIMClient.getInstance().getFriendshipManager().checkIsFriend(toContactJid);
                                if (!isFriend) {
                                    throw new SIMNotFriendException();
                                }
                                OmemoManager omemoManager = OmemoManager.getInstanceFor(SmartIMClient.getInstance().getConnection());
                                OmemoMessage.Sent encrypt = omemoManager.encrypt(JidCreate.bareFrom(toContactJid), messageContent);
                                MessageBuilder messageBuilder = MessageBuilder.buildMessage();
                                Message message = encrypt.buildMessage(messageBuilder, JidCreate.from(toContactJid));
                                for (IExtension element : extensions) {
                                    message.addExtension(ElementFactory.createBaseExtensionElement(element));
                                }
                                message.addExtension(new MessageTypeExtension(msgType));
                                SmartIMClient.getInstance().getConnection().sendStanza(message);
                                // 发送消息
                                String toUserId = message.getTo().asBareJid().toString();
                                return SmartMessage.createSendSingleMessage(message.getStanzaId(),
                                        SmartCommHelper.getInstance().getUserId(),
                                        toUserId, msgType, messageContent); // Void类型，表示没有返回值
                            }
                        }).onErrorResumeNext(throwable -> {
                            if (throwable instanceof SIMConnectionException) {
                                SmartIMClient.getInstance().checkConnection();
                            }
                            throw throwable;
                        })
                        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(msg -> {
                            if (null != msg) {
                                iMsgCallback.onSuccess(msg);
                            }
                        }, error -> {
                            if (error.getCause() instanceof SIMNotFriendException) {
                                iMsgCallback.onError(SmartConstants.Error.SEND_MSG_FAILED,
                                        SmartIMClient.getInstance().getString(R.string.NOT_FRIEND));
                            } else {
                                iMsgCallback.onError(SmartConstants.Error.SEND_MSG_FAILED,
                                        SmartIMClient.getInstance().getInstance().getInstance().getInstance().getString(R.string.SEND_MSG_FAILED));
                            }
                        });
            }

            @Override
            public void onFailed(int code, String desc) {
                iMsgCallback.onError(SmartConstants.Error.SEND_MSG_FAILED,
                        "无法验证对方设备");
            }
        });
    }

    /**
     * 发送群组消息
     *
     * @param groupId
     * @param msgType
     * @param messageContent
     * @param extensions
     * @param msgCallback
     */
    @Override
    public void sendGroupMessage(String groupId, String msgType, String messageContent, List<IExtension> extensions, IMsgCallback msgCallback) {
        MessageData messageData = new MessageData(false, groupId, msgType, messageContent, extensions, msgCallback);
        if (!messageSubject.hasObservers()) {
            initObserver();
        }
        messageSubject.onNext(messageData);
        //发送消息
    }

    /**
     * 发送文件
     *
     * @param user
     * @param filePath
     */
    public void sendFile(String user, String filePath) {
        if (SmartIMClient.getInstance().getConnection() == null)
            return;
        // 创建文件传输管理器
        FileTransferManager manager = FileTransferManager.getInstanceFor(
                SmartIMClient.getInstance().getConnection());

        // 创建输出的文件传输
        OutgoingFileTransfer transfer = null;
        try {
            transfer = manager.createOutgoingFileTransfer(JidCreate.entityFullFrom(user));
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }

        // 发送文件
        try {
            if (transfer != null)
                transfer.sendFile(new File(filePath), "You won't believe this!");
        } catch (SmackException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param toContactJid
     * @param messageContent
     * @param msgType
     * @param extensions
     * @param iMsgCallback
     */
    public void sendSingleMessage(String toContactJid, String msgType, String messageContent, List<IExtension> extensions, IMsgCallback iMsgCallback) {
        MessageData messageData = new MessageData(true, toContactJid, msgType, messageContent, extensions, iMsgCallback);
        if (!messageSubject.hasObservers()) {
            initObserver();
        }
        messageSubject.onNext(messageData);
    }

    /**
     * 发送文件
     *
     * @param filePath
     * @throws SmackException.NotConnectedException
     * @throws XMPPException.XMPPErrorException
     * @throws SmackException.NoResponseException
     * @throws InterruptedException
     */
    public void sendFileByOriginalMeans(String filePath, IFileCallback iFileCallback) {
        Disposable subscribe = Single.fromCallable(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            throw new SIMConnectionException(SmartConstants.Error.NO_CONNECTION, SmartIMClient.getInstance().getString(R.string.no_connection));
                        }
                        ServiceDiscoveryManager discoManager = ServiceDiscoveryManager.getInstanceFor(
                                SmartIMClient.getInstance().getConnection());
                        List<DomainBareJid> services;
                        services = discoManager.findServices(HttpFileUploadManager.NAMESPACE_0_2, true,
                                true);
                        if (services.isEmpty()) {
                            services = discoManager.findServices(HttpFileUploadManager.NAMESPACE, true,
                                    true);
                        }
                        if (!services.isEmpty()) {
                            final DomainBareJid uploadServerUrl = services.get(0);
                            SmartTrace.d("上传文件服务: " + uploadServerUrl);
                            String fileMimeType = "image/png"; // 待上传文件的 MIME 类型
                            // 初始化 ServiceDiscoveryManager
                            ServiceDiscoveryManager discoveryManager = ServiceDiscoveryManager.getInstanceFor(
                                    SmartIMClient.getInstance().getConnection());
                            // 查询上传服务的特性信息
                            DiscoverInfo info = discoveryManager.discoverInfo(uploadServerUrl);
                            // 查找支持的特性
                            boolean supportsHttpUpload = info.containsFeature(
                                    HttpFileUploadManager.NAMESPACE);
                            if (supportsHttpUpload) {
                                // 支持 HTTP 文件上传，获取上传 URL
                                HttpFileUploadManager uploadManager = HttpFileUploadManager.getInstanceFor(
                                        SmartIMClient.getInstance().getConnection());
                                URL url = uploadManager.uploadFile(new File(filePath),
                                        new UploadProgressListener() {
                                            @Override
                                            public void onUploadProgress(long uploadedBytes, long totalBytes) {
                                                // 计算进度百分比
                                                double progressPercent = (double) uploadedBytes / totalBytes * 100;
                                                // 根据需求处理进度，例如更新UI或打印日志
                                                SmartTrace.d("UploadProgress",
                                                        "Uploaded: " + uploadedBytes + " of " + totalBytes + " bytes. Progress: " + progressPercent + "%");
                                            }
                                        });
                                // 处理上传 URL
                                return url.toString();
                            } else {
                                // 不支持 HTTP 文件上传
                                SmartTrace.w("HTTP File Upload not supported");
                            }
                        } else {
                            SmartTrace.d("discoverSupport: services.isEmpty()");
                        }
                        return "";
                    }
                }).onErrorResumeNext(throwable -> {
                    if (throwable instanceof SIMConnectionException) {
                        SmartIMClient.getInstance().checkConnection();
                    }
                    throw throwable;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(iFileCallback::onSuccess, error -> {
                    iFileCallback.onFailed(SmartConstants.Error.UPLOAD_FILE_FAILED,
                            SmartIMClient.getInstance().getString(R.string.UPLOAD_FILE_FAILED));
                });

    }

    private Map<String, PublishSubject<String>> queries = new HashMap<>();

    /**
     * 发送回执
     *
     * @param toJid
     * @param messageId
     */
    @Override
    public void sendReceipt(String toJid, String messageId) {
        if (TextUtils.isEmpty(toJid) || TextUtils.isEmpty(messageId)) {
            return;
        }
        PublishSubject<String> subject = queries.get(toJid);
        if (subject == null) subject = createSubject(toJid);
        subject.onNext(messageId);
        /*Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                      *//*  Message receipt = new Message(JidCreate.from(toJid));
                        receipt.addExtension(new DeliveryReceipt(messageId));
                        // the key problem is Thread - smack does not keep it in auto reply
                        receipt.setType(Message.Type.chat);
                        SmartIMClient.getInstance().getConnection().sendStanza(receipt);*//*

                        return true;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(onSuccess -> {
                }, onError -> {

                });*/
    }

    private PublishSubject<String> createSubject(final String toJid) {
        PublishSubject<String> subject = PublishSubject.create();
        subject.debounce(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull String messageId) {
                        if (!SmartIMClient.getInstance().isAuthenticated()) {
                            return;
                        }
                        try {
                            Message displayed = new Message(JidCreate.from(toJid));
                            displayed.addExtension(new ChatMarkersElements.DisplayedExtension(messageId));
                            displayed.setType(Message.Type.chat);
                            SmartIMClient.getInstance().getConnection().sendStanza(displayed);
                        } catch (XmppStringprepException e) {
                        } catch (SmackException.NotConnectedException e) {
                        } catch (InterruptedException e) {
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
        queries.put(toJid, subject);
        return subject;
    }

    class MessageData {
        boolean isSingleMsg;
        String toContactJid;
        String msgType;
        String messageContent;
        List<IExtension> extensions;
        IMsgCallback iMsgCallback;

        public MessageData(boolean isSingleMsg, String toContactJid, String msgType, String messageContent,
                           List<IExtension> extensions, IMsgCallback iMsgCallback) {
            this.isSingleMsg = isSingleMsg;
            this.toContactJid = toContactJid;
            this.msgType = msgType;
            this.messageContent = messageContent;
            this.extensions = extensions;
            this.iMsgCallback = iMsgCallback;
        }
    }
}
