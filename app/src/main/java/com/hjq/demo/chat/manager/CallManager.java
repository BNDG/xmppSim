package com.hjq.demo.chat.manager;

import android.content.Context;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.hjq.demo.R;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.DBManager;
import com.hjq.demo.chat.dao.MessageDao;
import com.hjq.demo.chat.entity.CallCreatorInfo;
import com.hjq.demo.chat.entity.ChatEvent;
import com.hjq.demo.chat.entity.ChatMessage;
import com.hjq.demo.chat.entity.ConversationInfo;
import com.hjq.demo.chat.extensions.CallExtension;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.other.AppConfig;
import com.hjq.demo.utils.JsonParser;
import com.hjq.demo.utils.Trace;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import com.bndg.smack.SmartCommHelper;
import com.bndg.smack.entity.SmartMessage;
import com.bndg.smack.enums.SmartConversationType;
import com.bndg.smack.extensions.base.IExtension;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * @author r
 * @date 2024/11/7
 * @description 通话消息管理类
 */

public class CallManager {
    // 当前通话创建者信息
    public static String CURRENT_CREATOR_INFO = "";
    // 当前通话id
    public static String CURRENT_CALL_ID;
    private static volatile CallManager instance;

    private CallManager() {
    }

    public static CallManager getInstance() {
        if (instance == null) {
            synchronized (CallManager.class) {
                if (instance == null) {
                    instance = new CallManager();
                }
            }
        }
        return instance;
    }

    public void processCallMsg(SmartMessage msgEntity, CallExtension callExtension) {
        String callId = callExtension.getCallId();
        String callType = callExtension.getType();
        String callCreatorInfo = callExtension.getCallCreatorInfo();
        String callServiceUrl = callExtension.getServiceUrl();
        // 收到通话消息 不是历史消息,
        if (msgEntity.isOfflineMsg() || msgEntity.isHistoryMsg()) {
            // 历史通话消息 不处理
            Trace.d(">>>>历史通话消息: 不处理>>>>");
            return;
        }
        Trace.d(callId,
                "处理通话消息 " + callType,
                "是否是单聊通话 " + msgEntity.isSingle());
        if (!msgEntity.isSingle()) {
            processGroupCallMsg(msgEntity, callExtension);
            return;
        }
        if (ChatMessage.isStartCallType(msgEntity.getMessageType())) {
            // 收到通话请求 创建一次callId通话消息
            ChatMessage callMsg = ChatMessage.createCallMsg(false,
                    msgEntity.getFromUserId(),
                    msgEntity.getConversationType(),
                    msgEntity.getMessageType(),
                    msgEntity.getFromUserId(),
                    msgEntity.getToUserId(),
                    msgEntity.getMessageId(),
                    callId,
                    callType,
                    callCreatorInfo);
            // 如果已经在通话中 或者已经有通话通知了 代表忙
            String calling = SPUtils.getInstance().getString(Constant.CURRENT_CALL_ID);
            // 通知chatActivity进行刷新
            ChatMessageManager.getInstance().notifyMsgCallback(msgEntity, callMsg);
        } else if (ChatMessage.isCloseCallType(callType)) {
            // 单聊更新本次通话的消息内容
            updateDbCallMsg(callId, callType,
                    false,
                    true,
                    "",
                    new MessageDao.MessageDaoCallback() {
                        @Override
                        public void getMessageByCallId(ChatMessage chatMessage) {
                            if (chatMessage == null) {
                                ChatMessage callMsg = ChatMessage.createCallMsg(false,
                                        msgEntity.getFromUserId(),
                                        msgEntity.getConversationType(),
                                        msgEntity.getMessageType(),
                                        msgEntity.getFromUserId(),
                                        msgEntity.getToUserId(),
                                        msgEntity.getMessageId(),
                                        callId,
                                        callType,
                                        callCreatorInfo);
                            }
                            // 收到对方忙线 取消通话 拒绝通话  结束通话 如果已经在通话界面需要关闭 都会携带关联的通话id
                            EventBus.getDefault().post(new ChatEvent(ChatEvent.CLOSE_CALL));
                            // 通知chatActivity进行刷新
                            ChatMessageManager.getInstance().notifyMsgCallback(msgEntity, chatMessage);
                            NotificationCompatUtil.Companion.cancelCallNotify(ActivityManager.getInstance().getApplication(), callId.hashCode());
                            CallManager.CURRENT_CALL_ID = "";
                            CallManager.CURRENT_CREATOR_INFO = "";
                        }
                    });
        } else if (Constant.MSG_TYPE_ACCEPT_CALL.equals(msgEntity.getMessageType())) {
            // 收到接听方同意通话
            EventBus.getDefault().post(new ChatEvent(ChatEvent.ACCEPT_CALL));
        }
    }

    private void createCallNotify(SmartMessage msgEntity, String callId, String callType, String callCreatorInfo) {
        SPUtils.getInstance().put(Constant.CURRENT_CALL_ID, callId);
        Vibrator mVibrator = (Vibrator) ActivityManager.getInstance().getApplication().getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 1000, 1000};
        AudioAttributes audioAttributes;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION) //key
                    .build();
            mVibrator.vibrate(pattern, 0, audioAttributes);
        } else {
            mVibrator.vibrate(pattern, 0);
        }
        // 查询以下群聊标题
        if (!msgEntity.isSingle()) {
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
                        String conversationTitle = StringUtils.getString(R.string.group_chats);
                        if (conversationInfo != null) {
                            conversationTitle = conversationInfo.getConversationTitle();
                        }
                        conversationTitle = TextUtils.isEmpty(conversationTitle) ? StringUtils.getString(R.string.group_chats) : conversationTitle;
                        PushNotificationHelper.INSTANCE.notifyCall(callId.hashCode(), conversationTitle, StringUtils.getString(R.string.incoming_calls), false,
                                msgEntity.getGroupId(), callType, msgEntity.getMessageType(), callId, callCreatorInfo);
                    });
        } else {
            PushNotificationHelper.INSTANCE.notifyCall(callId.hashCode(), msgEntity.getFromNickname(), StringUtils.getString(R.string.incoming_calls), true,
                    msgEntity.getFromUserId(), callType, msgEntity.getMessageType(), callId, callCreatorInfo);
        }
    }

    /**
     * 处理群通话的消息
     *
     * @param msgEntity
     * @param callExtension
     */
    private void processGroupCallMsg(SmartMessage msgEntity, CallExtension callExtension) {
        String groupId = msgEntity.getGroupId();
        String callId = callExtension.getCallId();
        String callUserIds = callExtension.getCallUserIds();
        String callType = callExtension.getType();
        String callCreatorInfo = callExtension.getCallCreatorInfo();
        String callServiceUrl = callExtension.getServiceUrl();
        CallCreatorInfo infoBean;
        if (!TextUtils.isEmpty(callCreatorInfo)) {
            infoBean = JsonParser.deserializeByJson(callCreatorInfo, CallCreatorInfo.class);
        } else {
            infoBean = null;
        }
        boolean isFromCreator;
        boolean isCreator = false;
        if (infoBean != null) {
            isFromCreator = msgEntity.getFromUserId().equals(infoBean.creatorJid);
            isCreator = PreferencesUtil.getInstance().getUserId().equals(infoBean.creatorJid);
        } else {
            isFromCreator = false;
        }
        Trace.d("收到群通话消息: " + callId,
                "calluserIds " + callUserIds,
                "creinfo " + callCreatorInfo,
                "callType " + callType,
                "msgType: " + msgEntity.getMessageType());
        if (ChatMessage.isStartCallType(msgEntity.getMessageType())) {
            // 收到通话请求 创建一次callid通话消息
            ChatMessage callMsg = ChatMessage.createGroupCallMsg(false,
                    groupId,
                    msgEntity.getConversationType(),
                    msgEntity.getMessageType(),
                    msgEntity.getFromUserId(),
                    msgEntity.getToUserId(),
                    msgEntity.getMessageId(),
                    callId,
                    callType,
                    callCreatorInfo);
            if (callUserIds.contains(SmartCommHelper.getInstance().getAccountIdInGroup(msgEntity.getGroupId()))) {
                // 如果已经在通话中
                String calling = SPUtils.getInstance().getString(Constant.CURRENT_CALL_ID);
            }
            // 通知chatActivity进行刷新
            ChatMessageManager.getInstance().notifyMsgCallback(msgEntity, callMsg);
        } else if (ChatMessage.isCloseCallType(callType)) {
            // 收到对方忙线 取消通话 拒绝通话 结束通话
            if (ChatMessage.isAnswerCallType(callType)) {
                // 不处理其他群成员应答消息
                if (!isCreator && !isFromCreator) {
                    return;
                }
            }
            // 更新本次通话的消息内容
            String creatorName = infoBean == null ? "" : infoBean.creatorName;
        } else if (Constant.MSG_TYPE_ACCEPT_CALL.equals(msgEntity.getMessageType())) {
            // 收到接听方同意通话 只有发起通话者才需要处理
            if (isCreator) {
                ChatEvent event = new ChatEvent(ChatEvent.RECEIVED_ACCEPT_GROUP_CALL);
                Bundle bundle = new Bundle();
                bundle.putString(Constant.GROUP_ID, groupId);
                bundle.putString(Constant.CONTACT_ID, msgEntity.getFromUserId());
                event.bundle = bundle;
                EventBus.getDefault().post(event);
            }
        }
    }

    /**
     * 发起通话请求
     *
     * @param conversationId
     * @param ids              群聊所选的id
     * @param conversationType 会话类型
     * @param callExtension
     */
    public void initiateCall(String conversationId,
                             String contactName,
                             ArrayList<String> ids,
                             String conversationType,
                             CallExtension callExtension) {
        List<IExtension> extensionElements = new ArrayList<>();
        extensionElements.add(callExtension);
        String callCreatorInfo = callExtension.getCallCreatorInfo();
        String callId = callExtension.getCallId();
        String callType = callExtension.getType();
        Trace.d("initiateCall: " + callId);
        if (SmartConversationType.SINGLE.name().equals(conversationType)) {
            // 创建一条通话请求消息发送给接收方
            ChatMessage callMsg = ChatMessage.createCallMsg(true,
                    conversationId,
                    conversationType,
                    callType,
                    PreferencesUtil.getInstance().getUserId(),
                    conversationId,
                    "",
                    callId,
                    callType,
                    callCreatorInfo);
            ChatMessageManager.getInstance().sendSingleMessage(
                    conversationId,
                    contactName,
                    "",
                    callType,
                    extensionElements,
                    -1,
                    callMsg.getOriginId()
            );
        } else {
            ChatMessage callMsg = ChatMessage.createGroupCallMsg(false,
                    conversationId,
                    conversationType,
                    callType,
                    PreferencesUtil.getInstance().getUserId(),
                    conversationId,
                    "",
                    callId,
                    callType,
                    callCreatorInfo);
            ChatMessageManager.getInstance().sendGroupMessage(
                    conversationId,
                    callType,
                    "",
                    extensionElements,
                    -1,
                    callMsg.getOriginId());
        }
    }

    // 接收方处理通话请求
    public void handleCallRequest(CallExtension callMessage) {
        // 根据接收到的通话请求消息，决定接受或拒绝
        // 发送接听或拒绝消息给发起者
    }


    /**
     * 处理通话应答
     *
     * @param conversationId
     * @param isSingle
     * @param callId
     * @param callType
     * @param calUserIds
     * @param callCreatorInfo
     */
    public void handleAnswer(
            String conversationId,
            boolean isSingle,
            String callId,
            String callType,
            String calUserIds,
            String callCreatorInfo) {
        Trace.d("handleAnswer: " + callType);
        String creatorName = "";
        if (!TextUtils.isEmpty(callCreatorInfo)) {
            CallCreatorInfo infoBean = JsonParser.deserializeByJson(callCreatorInfo, CallCreatorInfo.class);
            creatorName = infoBean.creatorName;
        }
        updateDbCallMsg(callId, callType, true, isSingle, creatorName, new MessageDao.MessageDaoCallback() {
            @Override
            public void getMessageByCallId(ChatMessage messageByCallId) {
                if (messageByCallId != null) {
                    List<IExtension> extensionElements = new ArrayList<>();
                    CallExtension callExtension = new CallExtension(callId, callType, calUserIds, callCreatorInfo,
                            AppConfig.getJitsiUrl());
                    extensionElements.add(callExtension);
                    if (isSingle) {
                        ChatMessageManager.getInstance().sendSingleMessage(
                                conversationId,
                                messageByCallId.getFromUserName(),
                                messageByCallId.getMessageContent(),
                                callExtension.getType(),
                                extensionElements,
                                -1,
                                messageByCallId.getOriginId());
                    } else {
                        ChatMessageManager.getInstance().sendGroupMessage(
                                conversationId,
                                callExtension.getType(),
                                "",
                                extensionElements,
                                -1,
                                messageByCallId.getOriginId());
                    }
                }
            }
        });
    }

    // 通话状态更新（已接通、挂断等）
    public void updateCallStatus(CallExtension callMessage) {
        // 发送通话状态更新消息给对方
    }

    // 挂断通话
    public void endCall(String conversationId,
                        boolean isSingle,
                        CallExtension callExtension) {
        // 发送挂断通知给对方
        // 发送通话请求消息给接收方
        updateDbCallMsg(callExtension.getCallId(),
                callExtension.getType(),
                true,
                isSingle,
                "",
                new MessageDao.MessageDaoCallback() {
                    @Override
                    public void getMessageByCallId(ChatMessage callMessage) {
                        if (callMessage != null) {
                            List<IExtension> extensionElements = new ArrayList<>();
                            extensionElements.add(callExtension);
                            if (isSingle) {
                                ChatMessageManager.getInstance().sendSingleMessage(
                                        conversationId,
                                        callMessage.getFromUserName(),
                                        "",
                                        callExtension.getType(),
                                        extensionElements,
                                        -1,
                                        callMessage.getOriginId());
                            } else {
                                ChatMessageManager.getInstance().sendGroupMessage(
                                        conversationId,
                                        callExtension.getType(),
                                        "",
                                        extensionElements,
                                        -1,
                                        callMessage.getOriginId());
                            }
                        }
                    }
                });

    }

    public void updateDbCallMsg(String callId,
                                String callType,
                                boolean isSend,
                                boolean isSingle,
                                String creatorName,
                                MessageDao.MessageDaoCallback callback) {
        MessageDao.getInstance().getMessageByCallId(callId, new MessageDao.MessageDaoCallback() {
            @Override
            public void getMessageByCallId(ChatMessage messageByCallId) {
                if (messageByCallId != null) {
                    messageByCallId.setCallMsgContentByType(isSend, callType, isSingle, creatorName);
                    messageByCallId.setMessageType(callType);
                    MessageDao.getInstance().save(messageByCallId);
                    callback.getMessageByCallId(messageByCallId);
                } else {
                    callback.getMessageByCallId(null);
                }
            }
        });
    }
}
