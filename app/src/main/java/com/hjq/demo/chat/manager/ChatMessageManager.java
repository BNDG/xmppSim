package com.hjq.demo.chat.manager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.ArraySet;

import androidx.lifecycle.LifecycleOwner;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.hjq.demo.R;
import com.hjq.demo.chat.activity.ChatActivity;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.AppDatabase;
import com.hjq.demo.chat.dao.ConversationDao;
import com.hjq.demo.chat.dao.DBManager;
import com.hjq.demo.chat.dao.MessageDao;
import com.hjq.demo.chat.entity.CardInfoBean;
import com.hjq.demo.chat.entity.ChatFileBean;
import com.hjq.demo.chat.entity.ChatImageBean;
import com.hjq.demo.chat.entity.ChatMessage;
import com.hjq.demo.chat.entity.ChatRoomEntity;
import com.hjq.demo.chat.entity.ChatVideoBean;
import com.hjq.demo.chat.entity.ChatVoiceBean;
import com.hjq.demo.chat.entity.ConversationInfo;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.entity.enums.MessageStatus;
import com.hjq.demo.chat.extensions.AitExtension;
import com.hjq.demo.chat.extensions.CallExtension;
import com.hjq.demo.chat.extensions.CardInfoExtension;
import com.hjq.demo.chat.extensions.FileInfoExtension;
import com.hjq.demo.chat.extensions.ImageSizeExtension;
import com.hjq.demo.chat.extensions.VideoInfoExtension;
import com.hjq.demo.chat.extensions.VoiceInfoExtension;
import com.hjq.demo.chat.listener.ChatMsgCallback;
import com.hjq.demo.chat.listener.DownloadFileListener;
import com.hjq.demo.chat.model.ait.AtContactsModel;
import com.hjq.demo.chat.utils.AvatarGenerator;
import com.hjq.demo.chat.utils.CommonUtil;
import com.hjq.demo.chat.utils.ConversationHelper;
import com.hjq.demo.chat.utils.FileUtil;
import com.hjq.demo.chat.utils.GetFilePathFromUri;
import com.hjq.demo.chat.utils.JimUtil;
import com.hjq.demo.chat.utils.MessageHelper;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.utils.JsonParser;
import com.hjq.demo.utils.Trace;
import com.hjq.http.EasyHttp;
import com.hjq.http.listener.OnDownloadListener;
import com.hjq.http.model.HttpMethod;
import com.hjq.toast.ToastUtils;
import com.tencent.mmkv.MMKV;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bndg.smack.SmartCommHelper;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.callback.IMsgCallback;
import com.bndg.smack.callback.ISimpleMsgListener;
import com.bndg.smack.entity.SmartMessage;
import com.bndg.smack.enums.SmartContentType;
import com.bndg.smack.enums.SmartConversationType;
import com.bndg.smack.extensions.base.IExtension;
import com.bndg.smack.model.SmartUserInfo;
import com.bndg.smack.utils.SIMXmlParser;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.processors.FlowableProcessor;
import io.reactivex.rxjava3.processors.UnicastProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @author r
 * @date 2024/5/20
 * @description 消息管理
 */
public class ChatMessageManager implements ISimpleMsgListener {
    private static volatile ChatMessageManager instance;
    private Handler handler;
    private ArraySet<ChatMsgCallback> msgCallbacks = new ArraySet<>();
    private Disposable disposable; // 订阅消息的 Disposable
    private final FlowableProcessor<SmartMessage> messageProcessor = UnicastProcessor.create();

    private ChatMessageManager() {
        handler = new Handler(Looper.getMainLooper());
        initMessageProcessor();
    }

    private void initMessageProcessor() {
        disposable = messageProcessor
                .onBackpressureBuffer() // 或者其他背压处理策略
                .observeOn(Schedulers.io()) // 切换到 IO 线程处理
                .onErrorResumeNext(throwable -> {
                    Trace.file("ChatMessageManager: 发生了错误" + throwable);
                    return Flowable.empty();
                })
                .subscribe(
                        msg -> {
                            try {
                                processReceivedMessage(msg);
                            } catch (Exception e) {
                                Trace.file("processReceivedMessage: 发生了错误" + e);
                            }
                        },
                        throwable -> {
                            Trace.e("ERROR", "Error occurred", throwable);
                        },
                        () -> {
                            Trace.d("DEBUG", "Completed");
                        }
                );
    }

    public static ChatMessageManager getInstance() {
        if (instance == null) {
            synchronized (ChatMessageManager.class) {
                if (instance == null) {
                    instance = new ChatMessageManager();
                }
            }
        }
        return instance;
    }

    /**
     * 接收到消息
     *
     * @param msgEntity
     */
    @Override
    public void receivedSmartMessage(SmartMessage msgEntity) {
        if (!messageProcessor.hasSubscribers()) {
            Trace.file("出现了没有订阅者的情况: ");
            initMessageProcessor();
        }
        messageProcessor.onNext(msgEntity);
    }

    public static void downLoadFile(LifecycleOwner owner, File saveFile, String fileUrl, DownloadFileListener listener) {
        EasyHttp.download(owner)
                .method(HttpMethod.GET)
                .file(saveFile)
                .url(fileUrl)
                .listener(new OnDownloadListener() {

                    @Override
                    public void onDownloadStart(File file) {

                    }

                    @Override
                    public void onDownloadProgressChange(File file, int i) {

                    }

                    @Override
                    public void onDownloadSuccess(File file) {
                        if (listener != null) {
                            listener.onComplete(file);
                        }
                    }

                    @Override
                    public void onDownloadFail(File file, Throwable e) {
                        if (listener != null) {
                            listener.onError(file, e);
                        }
                    }

                    @Override
                    public void onDownloadEnd(File file) {

                    }
                })
                .start();
    }

    /**
     * 将收到的消息包装为数据库对象
     *
     * @param msg {from='m1918', to='m1920@tigase.bndg.cn', msg='7'}
     * @return 消息
     */
    public ChatMessage wrapperToChatMessage(SmartMessage msg) {
        ChatMessage chatMessage = new ChatMessage(PreferencesUtil.getInstance().getUserId());
        chatMessage.setOriginId(msg.getMessageId());
        // 收到消息的stanzaId 如果是通话消息 要把这个id携带 返回
        chatMessage.setSmartMessageId(msg.getMessageId());
        chatMessage.setArchivedId(msg.getArchivedId());
        chatMessage.setFromUserId(msg.getFromUserId());
        // 消息接收者信息
        chatMessage.setToUserId(msg.getToUserId());
        String conversationId;
        if (msg.isSingle()) {
            chatMessage.setFromUserName(msg.getFromNickname());
            // 消息发送者信息
            chatMessage.setConversationType(SmartConversationType.SINGLE.name());
            if (chatMessage.getIsSent()) {
                // 如果是我发送的 以对方id为会话id
                conversationId = msg.getToUserId();
            } else {
                conversationId = msg.getFromUserId();
            }
            chatMessage.setConversationId(conversationId);
        } else {
            conversationId = msg.getGroupId();
            // 群聊的昵称 todo 用member 去查询
            String groupSenderNickname = msg.getGroupSenderNickname();
            chatMessage.setFromUserName(groupSenderNickname);
            chatMessage.setConversationType(SmartConversationType.GROUP.name());
            chatMessage.setConversationId(conversationId);
        }
        // 消息类型 文字--语音--图片--之类的
        chatMessage.setMessageType(msg.getMessageType());
        chatMessage.setTimestamp(msg.getCreateTime());
        chatMessage.setMessageContent(msg.getMessageContent());
        return chatMessage;
    }

    public void notifyMsgCallback(SmartMessage msgEntity, ChatMessage chatMessage) {
        for (ChatMsgCallback msgCallback : msgCallbacks) {
            msgCallback.receivedMsg(msgEntity, chatMessage);
        }
    }

    /**
     * 收到消息刷新会话页面
     *
     * @param msgEntity
     */
    public void receivedUpdateConversation(ChatMessage chatMessage, SmartMessage msgEntity) {
        if (msgEntity.isSingle()) {
            ConversationDao.getInstance()
                    .saveSingleConversation(msgEntity, false);
        } else {
            ConversationDao.getInstance().saveGroupConversation(chatMessage.getConversationId(), null, msgEntity, false);
        }
        // 通知提示
        if (!ActivityManager.getInstance().isForeground()) {
            // 如果栈里面有chatactivity 交由chat处理
            if (!ActivityManager.getInstance().containsActivity(ChatActivity.class)) {
                if (!msgEntity.isHistoryMsg()) {
                    Trace.d("receivedUpdateConversation: 需要chatactivity判断是否弹出通知？");
                    createMsgNotification(msgEntity);
                }
            }
        }
        // 让chat处理 Chat界面在的时候 不会有大量消息
        notifyMsgCallback(msgEntity, chatMessage);

    }

    public void createMsgNotification(SmartMessage msgEntity) {
        if (TextUtils.isEmpty(msgEntity.getMessageContent())) {
            return;
        }
        String conversationId = msgEntity.isSingle() ? msgEntity.getFromUserId() : msgEntity.getGroupId();
        boolean isMuted = MMKV.defaultMMKV().getBoolean(conversationId + Constant.MUTE_KEY, false);
        if (isMuted) {
            Trace.d("createMsgNotify: 开启了免打扰");
            return;
        }
        if (!msgEntity.isSingle()) {
            // 需要去查询群名
            Disposable subscribe = DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                    .getConversationByConversationId(PreferencesUtil.getInstance().getUserId(),
                            msgEntity.getGroupId())
                    .subscribe(conversationInfoList -> {
                        ConversationInfo conversationInfo;
                        if (conversationInfoList.isEmpty()) {
                            conversationInfo = null;
                        } else {
                            conversationInfo = conversationInfoList.get(0);
                        }
                        String conversationTitle = "";
                        if (conversationInfo != null) {
                            conversationTitle = conversationInfo.getConversationTitle();
                        }
                        conversationTitle = TextUtils.isEmpty(conversationTitle) ? ActivityManager.getInstance().getApplication()
                                .getString(R.string.group_chats) : conversationTitle;
                        int id = msgEntity.getGroupId().hashCode();
                        String fromNickname = msgEntity.getGroupSenderNickname();
                        String content = fromNickname + ": " + JimUtil.getMessageContent(msgEntity.getMessageType(), msgEntity.getMessageContent());
                        PushNotificationHelper.INSTANCE.notifyMessage(id, false, conversationTitle, content, msgEntity);
                    });
        } else {
            int id = msgEntity.getFromUserId().hashCode();
            String content = JimUtil.getMessageContent(msgEntity.getMessageType(), msgEntity.getMessageContent());
            PushNotificationHelper.INSTANCE.notifyMessage(id, true, msgEntity.getFromNickname(), content, msgEntity);
        }
    }

    int countSendMsg = 0;

    /**
     * 发送单聊消息
     *
     * @param conversationId    会话id
     * @param conversationTitle 会话标题
     * @param content           消息内容
     * @param msgType           消息类型
     * @param elements          扩展信息
     * @param messageIndex      发送消息索引-更新界面
     * @param originId          消息id
     */
    public void sendSingleMessage(
            String conversationId,
            String conversationTitle,
            String content,
            String msgType,
            List<IExtension> elements,
            int messageIndex,
            String originId) {
        SmartIMClient.getInstance().getSmartCommMsgManager().sendSingleMessage(conversationId, msgType, content, elements, new IMsgCallback() {
            @Override
            public void onSuccess(SmartMessage msgEntity) {
                countSendMsg++;
                Trace.d("sendSingleMessage: " + countSendMsg);
                // 消息发送成功后更新会话内容
                sendMsgUpdateConversation(msgEntity);
                // 消息发送后更新本地消息状态
                MessageDao.getInstance().updateBySmartMessage(originId, msgEntity, new ChatMsgCallback() {
                    @Override
                    public void updateMsgSuccess(ChatMessage chatMessage) {
                        for (ChatMsgCallback msgCallback : msgCallbacks) {
                            msgCallback.sendSingleMsgSuccess(originId, msgEntity, chatMessage, messageIndex);
                        }
                    }
                });

            }

            @Override
            public void onError(int code, String desc) {
                // 消息发送后更新本地消息状态
                MessageDao.getInstance().updateBySmartMessage(originId, null, new ChatMsgCallback() {
                    @Override
                    public void updateMsgSuccess(ChatMessage chatMessage) {
                        for (ChatMsgCallback msgCallback : msgCallbacks) {
                            msgCallback.sendSingleMsgFailed(originId, code, desc, messageIndex);
                        }
                    }
                });
            }
        });
    }

    /**
     * 发送消息后更新会话列表显示的会话内容
     */
    private void sendMsgUpdateConversation(SmartMessage message) {
        Trace.d("updateConversation saveConversation: 发送消息后更新会话内容");
        if (message.isSingle()) {
            ConversationDao.getInstance().saveSingleConversation(message, true);
        } else {
            ConversationDao.getInstance()
                    .saveGroupConversation(message.getGroupId(), null, message, true);
        }
    }

    public void addMsgCallback(ChatMsgCallback sendMsgCallback) {
        this.msgCallbacks.add(sendMsgCallback);
    }

    public void removeMsgCallback(ChatMsgCallback sendMsgCallback) {
        this.msgCallbacks.remove(sendMsgCallback);
    }


    /**
     * 发送群消息
     *
     * @param groupId
     * @param messageType
     * @param body
     * @param elements
     * @param messageIndex
     * @param originId
     */
    public void sendGroupMessage(
            String groupId,
            String messageType,
            String body,
            List<IExtension> elements,
            int messageIndex,
            String originId) {
        SmartIMClient.getInstance().getSmartCommMsgManager()
                .sendGroupMessage(groupId, messageType, body,
                        elements, new IMsgCallback() {
                            @Override
                            public void onSuccess(SmartMessage msgEntity) {
                                sendMsgUpdateConversation(msgEntity);
                                MessageDao.getInstance().updateBySmartMessage(originId, msgEntity, new ChatMsgCallback() {
                                    @Override
                                    public void updateMsgSuccess(ChatMessage chatMessage) {
                                        for (ChatMsgCallback msgCallback : msgCallbacks) {
                                            msgCallback.sendGroupMsgSuccess(originId, msgEntity, chatMessage, messageIndex);
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onError(int code, String desc) {
                                MessageDao.getInstance().updateBySmartMessage(originId, null, new ChatMsgCallback() {
                                    @Override
                                    public void updateMsgSuccess(ChatMessage chatMessage) {
                                        for (ChatMsgCallback msgCallback : msgCallbacks) {
                                            msgCallback.sendGroupMsgFailed(originId, code, desc, messageIndex);
                                        }
                                    }
                                });
                            }
                        });
    }


    /**
     * 处理接收到的消息
     *
     * @param msgEntity
     */
    private void processReceivedMessage(SmartMessage msgEntity) {
        // 查询本地是否已经有此消息
        ChatMessage messageBySmartMessageId = MessageDao.getInstance().getMessageBySmartMessageId(msgEntity.getMessageId());
        boolean isExist = messageBySmartMessageId != null;
        if (isExist) {
            Trace.d("processReceivedMessage: 本地已经有此消息，直接返回");
            return;
        }
        ArraySet<CharSequence> extensionsXml = msgEntity.getExtensionsXml();
        String extraData = "";
        for (CharSequence xmlRepresentation : extensionsXml) {
            String xmlString = xmlRepresentation.toString();
            if (TextUtils.isEmpty(xmlString)) {
                continue;
            }
            if (xmlString.contains(CallExtension.NAMESPACE)) {
                CallExtension callExtension = (CallExtension) SIMXmlParser.getInstance().parseXml(xmlRepresentation, new CallExtension.Provider());
                if (callExtension != null && !TextUtils.isEmpty(callExtension.getCallId())) {
                    // 通话消息特殊处理
                    CallManager.getInstance().processCallMsg(msgEntity, callExtension);
                    return;
                }
            } else if (xmlString.contains(AitExtension.NAMESPACE)) {
                if (!msgEntity.isHistoryMsg()) {
                    // 提醒消息 历史消息不处理 再次入群可以拉取到
                    AtContactsModel aitBlockFromMsg = MessageHelper.getAitBlockFromMsg(xmlString);
                    if (aitBlockFromMsg != null) {
                        List<String> atTeamMember = aitBlockFromMsg.getAtTeamMember();
                        for (String atTeamMemberItem : atTeamMember) {
                            if (PreferencesUtil.getInstance().getUserId().equals(atTeamMemberItem)
                                    || AtContactsModel.ACCOUNT_ALL.equals(atTeamMemberItem)) {
                                ConversationHelper.updateAitInfo(msgEntity.getGroupId(), true);
                            }
                        }
                    }
                }
            } else if (xmlString.contains(ImageSizeExtension.NAMESPACE)) {
                ImageSizeExtension imageSizeExtension = (ImageSizeExtension)
                        SIMXmlParser.getInstance().parseXml(xmlRepresentation, new ImageSizeExtension.Provider());
                ChatImageBean imageBean = new ChatImageBean();
                imageBean.setImageWidth(imageSizeExtension.getImageWidth());
                imageBean.setImageHeight(imageSizeExtension.getImageHeight());
                extraData = JsonParser.serializeToJson(imageBean);
            } else if (xmlString.contains(FileInfoExtension.NAMESPACE)) {
                FileInfoExtension fileInfoExtension = (FileInfoExtension) SIMXmlParser.getInstance().parseXml(
                        xmlRepresentation, new FileInfoExtension.Provider());
                ChatFileBean fileBean = new ChatFileBean();
                fileBean.fileName = fileInfoExtension.getFileName();
                fileBean.fileSize = fileInfoExtension.getFileSize();
                extraData = JsonParser.serializeToJson(fileBean);
            } else if (xmlString.contains(VoiceInfoExtension.NAMESPACE)) {
                VoiceInfoExtension voiceInfoExtension = (VoiceInfoExtension) SIMXmlParser.getInstance().parseXml(
                        xmlRepresentation, new VoiceInfoExtension.Provider());
                ChatVoiceBean voiceBean = new ChatVoiceBean();
                voiceBean.voiceDuration = voiceInfoExtension.getVoiceDuration();
                extraData = JsonParser.serializeToJson(voiceBean);
            } else if (xmlString.contains(VideoInfoExtension.NAMESPACE)) {
                VideoInfoExtension videoInfoExtension = (VideoInfoExtension) SIMXmlParser.getInstance().parseXml(
                        xmlRepresentation, new VideoInfoExtension.Provider());
                ChatVideoBean videoBean = new ChatVideoBean();
                videoBean.setThumbnailUrl(videoInfoExtension.getThumbnailUrl());
                videoBean.setDuration(videoInfoExtension.getDuration());
                videoBean.setThumbnailWidth(videoInfoExtension.getThumbnailWidth());
                videoBean.setThumbnailHeight(videoInfoExtension.getThumbnailHeight());
                extraData = JsonParser.serializeToJson(videoBean);
            } else if (xmlString.contains(CardInfoExtension.NAMESPACE)) {
                CardInfoExtension cardInfoExtension = (CardInfoExtension) SIMXmlParser.getInstance().parseXml(
                        xmlRepresentation, new CardInfoExtension.Provider());
                CardInfoBean cardInfoBean = new CardInfoBean();
                cardInfoBean.setUserId(cardInfoExtension.getUserId());
                cardInfoBean.setNickName(cardInfoExtension.getNickName());
                extraData = JsonParser.serializeToJson(cardInfoBean);
            }
        }
        if (TextUtils.isEmpty(msgEntity.getMessageContent())) {
            // 处理没有body的消息 代表入群消息-系统通知-通话消息-群主题
            if (!msgEntity.isSingle()) {
                if (!TextUtils.isEmpty(msgEntity.getGroupSubject())) {
                    // 保存群主题消息 -有可能还没有ChatRoomEntity
                    ChatRoomEntity chatRoomEntity = new ChatRoomEntity();
                    chatRoomEntity.setChatRoomJid(msgEntity.getGroupId());
                    chatRoomEntity.setChatRoomSubject(msgEntity.getGroupSubject());
                    DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                            .saveOrUpdateChatRoom(chatRoomEntity)
                            .subscribe();
                }
            }
            return;
        }
        ChatMessage chatMessage = wrapperToChatMessage(msgEntity);
        chatMessage.setExtraData(extraData);
        if (SmartContentType.TEXT.equals(chatMessage.getMessageType())) {
            String regex = ">\\s*(https?://[^\\s\"]+\\.(jpg|jpeg|png|gif|bmp))";
            List<String> results = new ArrayList<>();
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(msgEntity.getMessageContent());
            while (matcher.find()) {
                // 使用 matcher.group(1) 获取第一个捕获组，即 URL 部分
                results.add(matcher.group(1));
            }
            if (!results.isEmpty()) {
                String newContent = msgEntity.getMessageContent().replaceAll(regex +"\n", "");
                chatMessage.setMessageContent(newContent);
                chatMessage.setMessageType(SmartContentType.QUOTE_IMAGE);
                chatMessage.setExtraData(results.get(0));
            }
        }
        AvatarGenerator.checkAvatar(chatMessage.getFromUserId());
        ChatMessage message = MessageDao.getInstance()
                .getLatestMessageByConversationId(chatMessage.getConversationId());
        if (null != message) {
            chatMessage.setLastTimeStamp(message.getTimestamp());
        }
        AppDatabase.getInstance(ActivityManager.getInstance().getApplication()).messageDao().saveChatMessage(chatMessage);
        if (msgEntity.isHistoryMsg()) {
            // 拉取历史消息
            notifyMsgCallback(msgEntity, chatMessage);
            return;
        }
        receivedUpdateConversation(chatMessage, msgEntity);
//        MessageDao.getInstance().saveAndSetLastTimeStamp(chatMessage, isSuccess -> {});
    }

    public void cleanup() {
        handler.removeCallbacksAndMessages(null); // 取消所有挂起的任务
    }

    /**
     * 接收到邀请入群
     *
     * @param inviterJid
     * @param groupId
     * @param memberIds
     * @param memberNicknames
     */
    @Override
    public void receivedTestGroupMembers(String inviterJid, String groupId, String memberIds, String memberNicknames) {
        Trace.d("receivedTestGroupMembers: why  process " + inviterJid);
        Disposable subscribe = DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .getUserById(inviterJid)
                .subscribe(new Consumer<List<User>>() {
                    @Override
                    public void accept(List<User> users) throws Throwable {
                        if (!users.isEmpty()) {
                            User userById = users.get(0);
                            String content = String.format(ActivityManager.getInstance().getApplication().getString(
                                            R.string.received_room_invite),
                                    userById.getUserNickName());
                            SmartMessage smartMessage = SmartMessage.newRoomInviteMsg(
                                    "", userById.getUserNickName(), PreferencesUtil.getInstance().getUserId(),
                                    content);
                            ChatMessage textMsg = ChatMessage.createTextMsg(groupId,
                                    SmartConversationType.GROUP.name(),
                                    SmartContentType.SYSTEM,
                                    content);
                            MessageDao.getInstance().saveAndSetLastTimeStamp(textMsg, isSuccess -> {
                            });
                            ArrayList<SmartUserInfo> smartUserInfoList = new ArrayList<>();
                            String[] idSplit = memberIds.split(SmartCommHelper.SEPARATOR_COMM);
                            String[] nameSplit = memberNicknames.split(SmartCommHelper.SEPARATOR_COMM);
                            for (int i = 0; i < idSplit.length; i++) {
                                SmartUserInfo userInfo = new SmartUserInfo();
                                userInfo.setUserId(idSplit[i]);
                                userInfo.setMemberAccount(User.getAccountById(idSplit[i]));
                                userInfo.setNickname(nameSplit[i]);
                                smartUserInfoList.add(userInfo);
                            }
                            ChatRoomEntity chatRoomEntity = new ChatRoomEntity();
                            chatRoomEntity.setChatRoomJid(groupId);
                            List<String> memberJidList = new ArrayList<>();
                            List<String> memberNicknameList = new ArrayList<>();
                            for (SmartUserInfo smartUserInfo : smartUserInfoList) {
                                if (!TextUtils.isEmpty(smartUserInfo.getUserId())) {
                                    memberJidList.add(smartUserInfo.getUserId());
                                }
                                memberNicknameList.add(smartUserInfo.getNickname());
                            }
                            if (!memberJidList.contains(inviterJid)) {
                                SmartUserInfo userInfo = new SmartUserInfo();
                                memberJidList.add(inviterJid);
                                memberNicknameList.add(userById.getUserNickName());
                                userInfo.setUserId(inviterJid);
                                userInfo.setMemberAccount(User.getAccountById(inviterJid));
                                userInfo.setNickname(userById.getUserNickName());
                                smartUserInfoList.add(userInfo);
                            }
                            DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                                    .saveChatRoom(chatRoomEntity)
                                    .subscribe(new CompletableObserver() {
                                        @Override
                                        public void onSubscribe(@NonNull Disposable d) {

                                        }

                                        @Override
                                        public void onComplete() {
                                            ConversationDao.getInstance().saveGroupConversation(groupId, null, smartMessage, false);
                                        }

                                        @Override
                                        public void onError(@NonNull Throwable e) {

                                        }
                                    });
                        }
                    }
                });
    }

    /**
     * 创建文件消息
     *
     * @param uri
     */
    public ChatFileBean createFileMsg(Context context, Uri uri, String conversationId, String conversationTitle, String conversationType) {
        String size = FileUtil.getUrlFileSize(context, uri);
        if (FileUtil.fileSizeLimit(Long.parseLong(size))) {
            String fileSizeLimit = String.valueOf(FileUtil.getFileLimitSize());
            String limitText =
                    String.format(context.getString(R.string.chat_message_file_size_limit_tips), fileSizeLimit);
            ToastUtils.show(limitText);
            return null;
        }
        context.getContentResolver().takePersistableUriPermission(uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // 在Android10上，文件会被复制到沙盒 路径就是沙盒路径
        String fileAbsolutePath = GetFilePathFromUri.getFileAbsolutePath(
                context, uri);
        ChatFileBean chatFileBean = new ChatFileBean();
        chatFileBean.conversationId = conversationId;
        chatFileBean.contactTitle = conversationTitle;
        chatFileBean.conversationType = conversationType;
        chatFileBean.fileName = FileUtil.getUrlFileName(context, uri);
        chatFileBean.fileSize = size;
        chatFileBean.fileLocalPath = fileAbsolutePath;
        chatFileBean.msgType = SmartContentType.FILE;
        Trace.d("onActivityResult: " + uri,
                "fileabs " + fileAbsolutePath);
        return chatFileBean;
    }

    /**
     * 语音消息封装
     *
     * @param conversationType
     * @param conversationId
     * @param userId
     * @param audioPath
     * @param audioDuration
     * @return
     */
    public ChatMessage createVoiceMsg(String conversationType, String conversationId,
                                      String userId,
                                      String audioPath, int audioDuration) {
        String messageId = CommonUtil.generateId();
        ChatMessage chatMessage = new ChatMessage(PreferencesUtil.getInstance().getUserId());
        chatMessage.setOriginId(messageId);
        chatMessage.setFromUserId(userId);
        if (SmartConversationType.GROUP.name().equals(conversationType)) {
            // 获取我在
            chatMessage.setFromUserName(SmartCommHelper.getInstance().getAccountIdInGroup(conversationId));
        } else {
            chatMessage.setFromUserName(PreferencesUtil.getInstance().getUser().getUserNickName());
        }
        chatMessage.setToUserId(conversationId);
        chatMessage.setConversationId(conversationId);
        chatMessage.setConversationType(conversationType);
        chatMessage.setTimestamp(new Date().getTime());
        chatMessage.setStatus(MessageStatus.SENDING.value());
        chatMessage.setMessageType(SmartContentType.VOICE);
        chatMessage.setFileLocalPath(audioPath);
        MessageDao.getInstance().saveAndSetLastTimeStamp(chatMessage);
        return chatMessage;
    }

    public ChatVoiceBean createVoiceBean(String conversationType, String conversationId, String audioPath, int audioDuration) {
        ChatVoiceBean chatVoiceBean = new ChatVoiceBean();
        chatVoiceBean.conversationId = conversationId;
        chatVoiceBean.conversationType = conversationType;
        chatVoiceBean.fileName = FileUtils.getFileName(audioPath);
        chatVoiceBean.fileLocalPath = audioPath;
        chatVoiceBean.msgType = SmartContentType.VOICE;
        chatVoiceBean.setVoiceDuration(audioDuration);
        return chatVoiceBean;
    }

    /**
     * 准备文件消息
     *
     * @param conversationType
     * @param conversationId
     * @param msgType
     * @param localPath
     * @return
     */
    public ChatMessage prepareFileMsg(String conversationType, String conversationId, String msgType, String localPath,
                                      ChatFileBean fileBean) {
        User userInfo = PreferencesUtil.getInstance().getUser();
        String originId = CommonUtil.generateId();
        ChatMessage chatMessage = new ChatMessage(userInfo.getUserId());
        chatMessage.setOriginId(originId);
        chatMessage.setConversationType(conversationType);
        chatMessage.setFromUserId(userInfo.getUserId());
        chatMessage.setFromUserName(userInfo.getUserNickName());
        chatMessage.setToUserId(conversationId);
        chatMessage.setTimestamp(new Date().getTime());
        chatMessage.setStatus(MessageStatus.SENDING.value());
        // 群组
        chatMessage.setConversationId(conversationId);
        chatMessage.setMessageType(msgType);
        chatMessage.setMessageContent("");
        chatMessage.setFileLocalPath(localPath);
        if (fileBean != null) {
            chatMessage.setExtraData(JsonParser.serializeToJson(fileBean));
        }
        MessageDao.getInstance().save(chatMessage);
        return chatMessage;

    }

    @Override
    public void receivedErrorMessage(String smartMessageId, int code, String desc) {
        ChatMessage messageBySmartMessageId = MessageDao.getInstance().getMessageBySmartMessageId(smartMessageId);
        if (messageBySmartMessageId != null) {
            messageBySmartMessageId.setStatus(MessageStatus.SEND_FAIL.value());
            MessageDao.getInstance().save(messageBySmartMessageId);
            for (ChatMsgCallback msgCallback : msgCallbacks) {
                msgCallback.sendGroupMsgFailed(messageBySmartMessageId.getOriginId(), code, desc, -1);
            }
        }

    }

    /**
     * 上传文件成功后
     *
     * @param intent
     */
    public void uploadFileSuccess(Intent intent) {
        String fileType = intent.getStringExtra(Constant.FILE_TYPE);
        String content = "";
        String conversationId = "";
        String contactTitle = "";
        String originId = "";
        String conversationType = "";
        String msgType = SmartContentType.VIDEO;
        Trace.d("onReceive: " + fileType);
        ArrayList<IExtension> elements = new ArrayList<>();
        if (Constant.FILE_TYPE_VIDEO.equals(fileType)) {
            ChatVideoBean videoBean = intent.getParcelableExtra(Constant.FILE_BEAN);
            if (videoBean != null) {
                content = videoBean.getVideoUrl();
                conversationId = videoBean.conversationId;
                contactTitle = videoBean.contactTitle;
                originId = videoBean.originId;
                conversationType = videoBean.conversationType;
                elements.add(new VideoInfoExtension(videoBean.getThumbnailUrl(), videoBean.getDuration(), videoBean.getThumbnailWidth(),
                        videoBean.getThumbnailHeight()));
            }
        } else if (SmartContentType.FILE.equals(fileType)) {
            msgType = SmartContentType.FILE;
            ChatFileBean fileBean = intent.getParcelableExtra(Constant.FILE_BEAN);
            if (fileBean != null) {
                content = fileBean.fileUrl;
                conversationId = fileBean.conversationId;
                contactTitle = fileBean.contactTitle;
                originId = fileBean.originId;
                conversationType = fileBean.conversationType;
                elements.add(new FileInfoExtension(fileBean.fileName, fileBean.fileSize));
            }
        } else if (SmartContentType.VOICE.equals(fileType)) {
            msgType = SmartContentType.VOICE;
            ChatVoiceBean fileBean = intent.getParcelableExtra(Constant.FILE_BEAN);
            if (fileBean != null) {
                content = fileBean.fileUrl;
                conversationId = fileBean.conversationId;
                contactTitle = fileBean.contactTitle;
                originId = fileBean.originId;
                conversationType = fileBean.conversationType;
                elements.add(new VoiceInfoExtension(fileBean.getVoiceDuration()));
            }
        } else if (SmartContentType.IMAGE.equals(fileType)) {
            msgType = SmartContentType.IMAGE;
            ChatImageBean fileBean = intent.getParcelableExtra(Constant.FILE_BEAN);
            if (fileBean != null) {
                content = fileBean.fileUrl;
                conversationId = fileBean.conversationId;
                contactTitle = fileBean.contactTitle;
                originId = fileBean.originId;
                conversationType = fileBean.conversationType;
                if (fileBean.getImageWidth() != 0) {
                    elements.add(new ImageSizeExtension(fileBean.getImageWidth(), fileBean.getImageHeight()));
                }
            }
        }
        if (SmartConversationType.SINGLE.name().equals(conversationType)) {
            ChatMessageManager.getInstance().sendSingleMessage(
                    conversationId,
                    contactTitle,
                    content,
                    msgType,
                    elements,
                    -1,
                    originId);
        } else if (SmartConversationType.GROUP.name().equals(conversationType)) {
            ChatMessageManager.getInstance().sendGroupMessage(
                    conversationId,
                    msgType,
                    content,
                    elements,
                    -1,
                    originId);
        }
    }

    public ChatImageBean createImageFileBean(String conversationId, String conversationTitle, String conversationType, String fileAbsolutePath) {
        ChatImageBean chatImageBean = new ChatImageBean();
        chatImageBean.conversationId = conversationId;
        chatImageBean.contactTitle = conversationTitle;
        chatImageBean.conversationType = conversationType;
        chatImageBean.fileLocalPath = fileAbsolutePath;
        chatImageBean.msgType = SmartContentType.IMAGE;
        int[] size = ImageUtils.getSize(fileAbsolutePath);
        chatImageBean.setImageWidth(size[0]);
        chatImageBean.setImageHeight(size[1]);
        return chatImageBean;
    }
}