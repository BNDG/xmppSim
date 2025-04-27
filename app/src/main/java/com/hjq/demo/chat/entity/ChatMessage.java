package com.hjq.demo.chat.entity;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.hjq.demo.R;
import com.hjq.demo.chat.adapter.SmartMessageAdapter;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.MessageDao;
import com.hjq.demo.chat.entity.enums.MessageStatus;
import com.hjq.demo.chat.utils.CommonUtil;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.utils.JsonParser;
import com.hjq.demo.utils.Trace;

import java.util.Date;

import com.bndg.smack.SmartCommHelper;
import com.bndg.smack.enums.SmartContentType;
import com.bndg.smack.enums.SmartConversationType;

/**
 * 本地聊天消息
 */
@Entity(tableName = "chat_message", indices = {@Index(value = {"belongAccount", "conversationId",
        "messageType", "messageContent", "lastTimeStamp", "smartMessageId"})})
public class ChatMessage implements MultiItemEntity {
    /**
     * 我们发现与上个方法不同，该方法没有标记 @Ignore 标签
     * 所以编译时该方法会被传入 Room 中相应的注解处理器，做相应处理
     * 这里的处理应该是 add 新数据
     */
    public ChatMessage(String belongAccount) {
        this.belongAccount = belongAccount;
    }

    /**
     * 构造方法
     * 设置为 @Ignore 将其忽视
     * 这样以来，这个注解方法就不会被传入 Room 中，做相应处理
     * 我们于是需要创建临时的 User 对象，但我们又不希望 @Entity 在我们调用构造方法时，就将其存入数据库。
     * 所以我们就有了这个被 @Ignore 的构造方法，用于创建不被自动存入数据库的临时对象，
     * 等到我们想将这个对象存入数据库时，调用ChatMessage(String userName) 即可。
     */
    @Ignore
    public ChatMessage() {
    }

    // 原始id 发消息的时候用
    @PrimaryKey
    @NonNull
    private String originId;
    // 消息类型
    @ColumnInfo(name = "messageType")
    private String messageType;
    // 消息体
    private String messageContent;
    // fromUserId 发送者的userid
    private String fromUserId;
    // 群消息的昵称
    private String fromUserName;
    // toUserId 接收者的userid
    private String toUserId;
    // 文件本地路径
    private String fileLocalPath;
    // 消息状态 失败 成功
    @ColumnInfo(index = true)  // 为这个字段添加索引
    private int status;
    // 接收消息时候时间戳
    private long timestamp;
    // 会话类型 单聊 群聊
    private String conversationType;
    // 会话id 单聊是对方id 群聊是groupId
    private String conversationId;
    // 消息记录属于于哪个账号
    private String belongAccount;
    // 前一条消息时间
    private long lastTimeStamp;
    // smart的消息id
    private String smartMessageId;
    // 存档id用于拉取消息
    private String archivedId;
    // 通话消息id
    private String callId;
    // 通话消息类型 语音通话 视频通话
    private String callType;
    // 发起通话的人的名字
    private String callCreatorInfo;

    private boolean isRead;
    // 附加信息
    private String extraData;
    @Ignore
    public int progress;

    /**
     * 是否是通话消息
     *
     * @param messageType
     * @return
     */
    public static boolean isCallMsgType(String messageType) {
        return Constant.MSG_TYPE_VIDEO_CALL.equals(messageType)
                || Constant.MSG_TYPE_VOICE_CALL.equals(messageType)
                || Constant.MSG_TYPE_END_CALL.equals(messageType)
                || Constant.MSG_TYPE_CALL_BUSY.equals(messageType)
                || Constant.MSG_TYPE_CANCEL_CALL.equals(messageType)
                || Constant.MSG_TYPE_CALL_REFUSE.equals(messageType)
                || Constant.MSG_TYPE_ACCEPT_CALL.equals(messageType);
    }

    /**
     * 创建文字消息
     *
     * @param conversationId   会话id 接收者jid
     * @param conversationType 单聊、群聊
     * @param msgType          消息类型
     * @param content          消息内容
     * @return chatMessage
     * @todo 群聊消息需要考虑发送者昵称 考虑群昵称
     */
    public static ChatMessage createTextMsg(String conversationId, String conversationType,
                                            String msgType, String content) {
        ChatMessage chatMessage = generateBasicMsg(conversationId, conversationType, msgType);
        chatMessage.setMessageContent(content);
        return chatMessage;
    }

    /**
     * 是否是结束通话的类型
     *
     * @param messageType
     * @return
     */
    public static boolean isCloseCallType(String messageType) {
        return Constant.MSG_TYPE_CALL_BUSY.equals(messageType)
                || Constant.MSG_TYPE_END_CALL.equals(messageType)
                || Constant.MSG_TYPE_CANCEL_CALL.equals(messageType)
                || Constant.MSG_TYPE_CALL_REFUSE.equals(messageType);
    }

    /**
     * 是否是结束通话的类型
     *
     * @param messageType
     * @return
     */
    public static boolean isAnswerCallType(String messageType) {
        return Constant.MSG_TYPE_CALL_BUSY.equals(messageType)
                || Constant.MSG_TYPE_CANCEL_CALL.equals(messageType)
                || Constant.MSG_TYPE_CALL_REFUSE.equals(messageType);
    }

    /**
     * 是否是通话消息
     *
     * @param messageType
     * @return
     */
    public static boolean isStartCallType(String messageType) {
        return Constant.MSG_TYPE_VOICE_CALL.equals(messageType)
                || Constant.MSG_TYPE_VIDEO_CALL.equals(messageType);
    }


    /**
     * 创建一条通话消息
     *
     * @param isInitiator      是否是发起者
     * @param conversationId   会话id
     * @param conversationType 会话类型
     * @param msgType          消息类型
     * @param fromUserId       发送者id
     * @param toUserId         接收者id
     * @param smartMessageId   smart消息id
     * @param callId           通话id
     * @param callType         通话类型
     * @param callCreatorInfo  发起通话方的信息
     * @return
     */
    public static ChatMessage createCallMsg(boolean isInitiator,
                                            String conversationId,
                                            String conversationType,
                                            String msgType,
                                            String fromUserId,
                                            String toUserId,
                                            String smartMessageId,
                                            String callId,
                                            String callType,
                                            String callCreatorInfo) {
        ChatMessage chatMessage = new ChatMessage(PreferencesUtil.getInstance().getUserId());
        chatMessage.setOriginId(CommonUtil.generateId());
        chatMessage.setSmartMessageId(smartMessageId);
        chatMessage.setCallId(callId);
        chatMessage.setCallType(callType);
        chatMessage.setCallCreatorInfo(callCreatorInfo);
        chatMessage.setConversationId(conversationId);
        chatMessage.setConversationType(conversationType);
        chatMessage.setMessageType(msgType);
        chatMessage.setFromUserId(fromUserId);
        chatMessage.setFromUserName(PreferencesUtil.getInstance().getUser().getUserNickName());
        chatMessage.setToUserId(toUserId);
        chatMessage.setTimestamp(new Date().getTime());
        // 创建通话消息的内容
        chatMessage.setCallMsgContentByType(isInitiator, msgType, true, "");
        MessageDao.getInstance().saveAndSetLastTimeStamp(chatMessage);
        return chatMessage;
    }

    /**
     * 创建一条文件消息
     *
     * @param conversationId
     * @param conversationType
     * @param localPath
     * @param fileUrl
     * @return
     */
    public static ChatMessage createForwardFileMsg(String conversationId, String conversationType,
                                                   String messageType, String localPath, String fileUrl, String extraData) {
        ChatMessage chatMessage = generateBasicMsg(conversationId, conversationType, messageType);
        chatMessage.setStatus(MessageStatus.SEND_SUCCESS.value());
        chatMessage.setMessageContent(fileUrl);
        chatMessage.setExtraData(extraData);
        chatMessage.setFileLocalPath(localPath);
        MessageDao.getInstance().saveAndSetLastTimeStamp(chatMessage);
        return chatMessage;
    }

    private static ChatMessage generateBasicMsg(String conversationId, String conversationType, String messageType) {
        String originId = CommonUtil.generateId();
        String userId = PreferencesUtil.getInstance().getUserId();
        ChatMessage chatMessage = new ChatMessage(userId);
        chatMessage.setOriginId(originId);
        chatMessage.setConversationId(conversationId);
        chatMessage.setConversationType(conversationType);
        chatMessage.setMessageType(messageType);
        chatMessage.setFromUserId(userId);
        chatMessage.setFromUserName(PreferencesUtil.getInstance().getUser().getUserNickName());
        chatMessage.setToUserId(conversationId);
        chatMessage.setTimestamp(new Date().getTime());
        chatMessage.setStatus(MessageStatus.SENDING.value());
        return chatMessage;
    }

    /**
     * 创建一条群通话消息
     *
     * @param isSend
     * @param conversationId
     * @param conversationType
     * @param msgType
     * @param fromUserId
     * @param toUserId
     * @param smartMessageId
     * @param callId
     * @return
     */
    public static ChatMessage createGroupCallMsg(boolean isSend,
                                                 String conversationId,
                                                 String conversationType,
                                                 String msgType,
                                                 String fromUserId,
                                                 String toUserId,
                                                 String smartMessageId,
                                                 String callId,
                                                 String callType,
                                                 String callCreatorInfo) {
        ChatMessage chatMessage = new ChatMessage(PreferencesUtil.getInstance().getUserId());
        chatMessage.setOriginId(CommonUtil.generateId());
        chatMessage.setSmartMessageId(smartMessageId);
        chatMessage.setCallId(callId);
        chatMessage.setCallType(callType);
        chatMessage.setCallCreatorInfo(callCreatorInfo);
        chatMessage.setConversationId(conversationId);
        chatMessage.setConversationType(conversationType);
        chatMessage.setMessageType(msgType);
        chatMessage.setFromUserId(fromUserId);
        chatMessage.setFromUserName(PreferencesUtil.getInstance().getUser().getUserNickName());
        chatMessage.setToUserId(toUserId);
        chatMessage.setTimestamp(new Date().getTime());
        // 之前的最后一条
        String callCreatorName = "";
        if (!TextUtils.isEmpty(callCreatorInfo)) {
            CallCreatorInfo infoBean = JsonParser.deserializeByJson(callCreatorInfo, CallCreatorInfo.class);
            if (infoBean != null) {
                callCreatorName = infoBean.creatorName;
            }
        }
        // 创建群聊通话消息的内容
        chatMessage.setCallMsgContentByType(isSend, msgType, false, callCreatorName);
        MessageDao.getInstance().saveAndSetLastTimeStamp(chatMessage);
        return chatMessage;
    }

    public static boolean isFileType(String messageType) {
        return SmartContentType.VIDEO.equals(messageType)
                || SmartContentType.IMAGE.equals(messageType)
                || SmartContentType.FILE.equals(messageType)
                || SmartContentType.VOICE.equals(messageType);
    }

    public static ChatMessage createCardInfoMsg(String conversationId, String conversationType,
                                                String messageType, String messageContent, String extraData) {
        ChatMessage chatMessage = generateBasicMsg(conversationId, conversationType, messageType);
        chatMessage.setMessageContent(messageContent);
        chatMessage.setExtraData(extraData);
        MessageDao.getInstance().saveAndSetLastTimeStamp(chatMessage);
        return chatMessage;
    }


    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    public String getCallCreatorInfo() {
        return callCreatorInfo;
    }

    public void setCallCreatorInfo(String callCreatorInfo) {
        this.callCreatorInfo = callCreatorInfo;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public String getBelongAccount() {
        return belongAccount;
    }

    public void setBelongAccount(String belongAccount) {
        this.belongAccount = belongAccount;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getFileLocalPath() {
        return fileLocalPath;
    }

    public void setFileLocalPath(String fileLocalPath) {
        this.fileLocalPath = fileLocalPath;
    }

    public String getConversationId() {
        return conversationId;
    }


    public String getArchivedId() {
        return archivedId;
    }

    public void setArchivedId(String archivedId) {
        this.archivedId = archivedId;
    }

    public void setLastTimeStamp(long lastTimeStamp) {
        this.lastTimeStamp = lastTimeStamp;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getSmartMessageId() {
        return smartMessageId;
    }

    public void setSmartMessageId(String smartMessageId) {
        this.smartMessageId = smartMessageId;
    }


    public String getOriginId() {
        return originId;
    }

    public void setOriginId(String originId) {
        this.originId = originId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getConversationType() {
        return conversationType;
    }

    public void setConversationType(String conversationType) {
        this.conversationType = conversationType;
    }

    @Override
    public int getItemType() {
        boolean isSent = getIsSent();
        int itemType = isSent ? SmartMessageAdapter.SENT_TEXT : SmartMessageAdapter.RECEIVED_TEXT;
        if (!TextUtils.isEmpty(messageType)) {
            switch (messageType) {
                case SmartContentType.TEXT:
                    return isSent ? SmartMessageAdapter.SENT_TEXT : SmartMessageAdapter.RECEIVED_TEXT;
                case SmartContentType.IMAGE:
                    return isSent ? SmartMessageAdapter.SENT_IMAGE : SmartMessageAdapter.RECEIVED_IMAGE;
                case SmartContentType.QUOTE_IMAGE:
                    return isSent ? SmartMessageAdapter.SENT_TEXT : SmartMessageAdapter.RECEIVED_QUOTE_IMAGE;
                case SmartContentType.FILE:
                    return isSent ? SmartMessageAdapter.SENT_FILE : SmartMessageAdapter.RECEIVED_FILE;
                case Constant.MSG_TYPE_LOCATION:
                    return isSent ? SmartMessageAdapter.SENT_LOCATION : SmartMessageAdapter.RECEIVED_LOCATION;
                case SmartContentType.VOICE:
                    return isSent ? SmartMessageAdapter.SENT_VOICE : SmartMessageAdapter.RECEIVED_VOICE;
                case Constant.MSG_TYPE_CARD_INFO:
                    return isSent ? SmartMessageAdapter.SENT_CARD_INFO : SmartMessageAdapter.RECEIVED_CARD_INFO;
                case SmartContentType.SYSTEM:
                    return SmartMessageAdapter.SYSTEM_MESSAGE;
                case Constant.MSG_TYPE_VOICE_CALL:
                case Constant.MSG_TYPE_VIDEO_CALL:
                case Constant.MSG_TYPE_END_CALL:
                case Constant.MSG_TYPE_ACCEPT_CALL:
                case Constant.MSG_TYPE_CALL_REFUSE:
                case Constant.MSG_TYPE_CALL_BUSY:
                case Constant.MSG_TYPE_CANCEL_CALL:
                    boolean isVoiceCall = Constant.MSG_TYPE_VOICE_CALL.equals(callType);
                    if (isSent) {
                        if (isGroupMsg()) {
                            return SmartMessageAdapter.GROUP_VIDEO_CALL;
                        } else {
                            return isVoiceCall ? SmartMessageAdapter.SENT_SINGLE_VOICE_CALL : SmartMessageAdapter.SENT_SINGLE_VIDEO_CALL;
                        }
                    } else {
                        if (isGroupMsg()) {
                            return SmartMessageAdapter.GROUP_VIDEO_CALL;
                        } else {
                            return isVoiceCall ? SmartMessageAdapter.RECEIVED_SINGLE_VOICE_CALL : SmartMessageAdapter.RECEIVED_SINGLE_VIDEO_CALL;
                        }
                    }
                case SmartContentType.VIDEO:
                    return isSent ? SmartMessageAdapter.SENT_VIDEO : SmartMessageAdapter.RECEIVED_VIDEO;
                case Constant.MSG_TYPE_SPANNABLE:
                    return isSent ? SmartMessageAdapter.RECEIVED_SPANNABLE_TEXT : SmartMessageAdapter.RECEIVED_SPANNABLE_TEXT;
            }
        }
        return itemType;
    }

    /**
     * 是否是群聊
     *
     * @return
     */
    public boolean isGroupMsg() {
        return SmartConversationType.GROUP.name().equals(conversationType);
    }

    /**
     * 是否是自己发送的
     *
     * @return
     */
    public boolean getIsSent() {
        // todo 群聊的时候 昵称相同就是自己 因为离线消息返回的id
        if (isGroupMsg()) {
            return PreferencesUtil.getInstance().getUserId().equals(getFromUserId());
        }
        return PreferencesUtil.getInstance().getUserId().equals(fromUserId);
    }

    /**
     * 是否需要显示时间
     *
     * @return
     */
    public boolean needShowTime() {
        return getTimestamp() - getLastTimeStamp() > 10 * 60 * 1000;
    }

    public long getLastTimeStamp() {
        return lastTimeStamp;
    }

    /**
     * 是否是结束通话
     *
     * @return
     */
    public boolean isEndCallMsg() {
        return Constant.MSG_TYPE_END_CALL.equals(messageType);
    }

    /**
     * 设置通话消息内容
     *
     * @param isSend
     * @param messageType
     * @param isSingle
     * @param creatorName 群通话显示发起者信息
     */
    public void setCallMsgContentByType(boolean isSend, String messageType, boolean isSingle, String creatorName) {
        if (Constant.MSG_TYPE_CALL_REFUSE.equals(messageType)) {
            // 收到对方拒绝通话
            if (isSingle) {
                if (isSend) {
                    setMessageContent(ActivityManager.getInstance().getApplication().getString(R.string.sent_refuse_call));
                } else {
                    setMessageContent(ActivityManager.getInstance().getApplication().getString(R.string.received_refuse_call));
                }
            }
            // 群通话 收到某个人的拒绝通话
        } else if (Constant.MSG_TYPE_CALL_BUSY.equals(messageType)) {
            if (isSingle) {
                // 收到对方忙线
                if (isSend) {
                    setMessageContent(ActivityManager.getInstance().getApplication().getString(R.string.sent_busy_call));
                } else {
                    setMessageContent(ActivityManager.getInstance().getApplication().getString(R.string.received_busy_call));
                }
            }
        } else if (Constant.MSG_TYPE_CANCEL_CALL.equals(messageType)) {
            if (isSingle) {
                // 收到对方取消
                if (isSend) {
                    setMessageContent(ActivityManager.getInstance().getApplication().getString(R.string.sent_cancel_call));
                } else {
                    setMessageContent(ActivityManager.getInstance().getApplication().getString(R.string.received_cancel_call));
                }
            } else {
                if (isSend) {
                    setMessageContent(ActivityManager.getInstance().getApplication().getString(R.string.group_call_end));
                } else {
                    setMessageContent(ActivityManager.getInstance().getApplication().getString(R.string.group_call_end));
                }
            }
        } else if (Constant.MSG_TYPE_END_CALL.equals(messageType)) {
            // 收到对方结束
            if (isSingle) {
                setMessageContent(ActivityManager.getInstance().getApplication().getString(R.string.end_call));
            } else {
                setMessageContent(ActivityManager.getInstance().getApplication().getString(R.string.group_call_end));
            }
        } else if (Constant.MSG_TYPE_VOICE_CALL.equals(messageType)) {
            if (isSingle) {
                setMessageContent(ActivityManager.getInstance().getApplication().getString(R.string.voice_call));
            } else {
                String createVoiceCall = ActivityManager.getInstance().getApplication().getString(R.string.created_voice_call);
                setMessageContent(String.format(createVoiceCall, creatorName));
            }
        } else if (Constant.MSG_TYPE_VIDEO_CALL.equals(messageType)) {
            if (isSingle) {
                setMessageContent(ActivityManager.getInstance().getApplication().getString(R.string.video_call));
            } else {
                String createVideoCall = ActivityManager.getInstance().getApplication().getString(R.string.created_video_call);
                setMessageContent(String.format(createVideoCall, creatorName));
            }
        } else if (Constant.MSG_TYPE_ACCEPT_CALL.equals(messageType)) {
            if (isSingle) {
                // 收到对方忙线
                if (isSend) {
                    setMessageContent(ActivityManager.getInstance().getApplication().getString(R.string.accept_call));
                } else {
                    setMessageContent(ActivityManager.getInstance().getApplication().getString(R.string.accept_call));
                }
            }
        }
        Trace.d("是否是单聊通话: " + isSingle,
                "messageType " + messageType,
                "更新后的消息内容 " + getMessageContent());
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isMediaMsg() {
        return SmartContentType.VIDEO.equals(messageType) || SmartContentType.IMAGE.equals(messageType);
    }
}
