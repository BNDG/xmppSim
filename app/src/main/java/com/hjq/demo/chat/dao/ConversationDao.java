package com.hjq.demo.chat.dao;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.hjq.demo.chat.adapter.SmartConversationAdapter;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.entity.ChatEvent;
import com.hjq.demo.chat.entity.ConversationInfo;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.listener.ContactCallback;
import com.hjq.demo.chat.utils.JimUtil;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.utils.Trace;
import com.tencent.mmkv.MMKV;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import com.bndg.smack.entity.SmartMessage;
import com.bndg.smack.enums.SmartContentType;
import com.bndg.smack.enums.SmartConversationType;
import com.bndg.smack.model.SmartGroupInfo;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * @author r
 * @date 2024/5/21
 * @description Brief description of the file content.
 */
public class ConversationDao {
    private static volatile ConversationDao instance;

    private ConversationDao() {

    }

    public static ConversationDao getInstance() {
        if (instance == null) {
            synchronized (ConversationDao.class) {
                if (instance == null) {
                    instance = new ConversationDao();
                }
            }
        }
        return instance;
    }


    /**
     * 收到消息或发送消息后，多线程保存或更新单聊会话
     *
     * @param latestMessage
     * @param isSent
     */
    public void saveSingleConversation(SmartMessage latestMessage, boolean isSent) {
        // 对方的信息 conversationId 也就是对方的userId
        String conversationId = isSent ? latestMessage.getToUserId() : latestMessage.getFromUserId();
        String title = MMKV.defaultMMKV().getString(conversationId + "_" + Constant.CONVERSATION_TITLE, "");
        if (TextUtils.isEmpty(title)) {
            UserDao.getInstance().getUserById(conversationId, new ContactCallback() {
                @Override
                public void getUser(@Nullable User userById) {
                    if (userById != null) {
                        realRefreshConversation(latestMessage, userById.getConversationTitle(), isSent, conversationId);
                    }
                }
            });
        } else {
            realRefreshConversation(latestMessage, title, isSent, conversationId);
        }
    }

    private void realRefreshConversation(SmartMessage latestMessage, String conversationTitle, boolean isSent, String conversationId) {
        Trace.w(" 收到消息多线程保存或更新单聊会话 conversationId: >" + conversationId,
                "conversationTitle " + conversationTitle);
        Disposable subscribe = DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .getConversationByConversationId(PreferencesUtil.getInstance().getUserId(),
                        conversationId)
                .subscribe(conversationInfoList -> {
                    ConversationInfo conversationInfo;
                    if (conversationInfoList.isEmpty()) {
                        conversationInfo = null;
                        // 单聊使用对方的昵称或备注
                    } else {
                        conversationInfo = conversationInfoList.get(0);
                    }
                    long msgTimes = latestMessage.getCreateTime();
                    if (null != conversationInfo) {
                        // 会话已存在，更新基本信息
                        if (msgTimes < conversationInfo.getLastMsgDate()) {
                            Trace.w("msgTimes: " + latestMessage.getCreateTime(),
                                    "时间早于存在的-conversationInfo " + conversationInfo.getLastMsgDate());
                            return;
                        }
                        conversationInfo.setConversationType(SmartConversationType.SINGLE.name());
                        conversationInfo.setBelongAccount(PreferencesUtil.getInstance().getUserId());
                        conversationInfo.setDigest(JimUtil.getMessageContent(latestMessage.getMessageType(), latestMessage.getMessageContent()));
                        conversationInfo.setLastMsgDate(msgTimes);
                        conversationInfo.setConversationTitle(conversationTitle);
                        conversationInfo.setConversationId(conversationId);
                        if (!isSent) {
                            conversationInfo.setUnReadNum(1);
                        }
                    } else {
                        // 不存在,插入sqlite
                        conversationInfo = new ConversationInfo();
                        conversationInfo.setConversationType(SmartConversationType.SINGLE.name());
                        conversationInfo.setBelongAccount(PreferencesUtil.getInstance().getUserId());
                        conversationInfo.setDigest(JimUtil.getMessageContent(latestMessage.getMessageType(), latestMessage.getMessageContent()));
                        if (!isSent) {
                            conversationInfo.setUnReadNum(1);
                        }
                        conversationInfo.setLastMsgDate(msgTimes);
                        conversationInfo.setConversationTitle(conversationTitle);
                        conversationInfo.setConversationId(conversationId);
                    }
                    DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                            .saveOrUpdateConversation(conversationInfo).subscribe(new CompletableObserver() {
                                @Override
                                public void onSubscribe(@NonNull Disposable d) {

                                }

                                @Override
                                public void onComplete() {
                                    // 单聊更新会话内容
                                    ChatEvent event = new ChatEvent(ChatEvent.CONVERSATION_ITEM_CONTENT_UPDATE);
                                    event.obj = conversationId;
                                    EventBus.getDefault().post(event);
                                }

                                @Override
                                public void onError(@NonNull Throwable e) {

                                }
                            });
                });
    }

    /**
     * 发送或收到消息-保存或更新群聊会话
     *
     * @param groupId
     * @param roomInfo      不为null 需要刷新标题 为null 是更新新消息
     * @param latestMessage
     */
    public void saveGroupConversation(String groupId, SmartGroupInfo roomInfo, SmartMessage latestMessage, boolean isSent) {
        // 消息监听到了有可能roominfo还没有
        ConversationInfo conversationInfo = new ConversationInfo();
        String label = "";
        if (null != latestMessage) {
            String groupSenderNickname = latestMessage.getGroupSenderNickname();
            String label_suffix = ": ";
            if (!SmartContentType.CUSTOM.equals(latestMessage.getMessageType())) {
                label = groupSenderNickname + label_suffix;
            }
            String digest = label + JimUtil.getMessageContent(latestMessage.getMessageType(), latestMessage.getMessageContent());
            conversationInfo.setFromJid(latestMessage.getFromUserId());
            conversationInfo.setDigest(digest);
            conversationInfo.setLastMsgDate(latestMessage.getCreateTime());
            if (!isSent) {
                conversationInfo.setUnReadNum(1);
            }
        }
        conversationInfo.setConversationType(SmartConversationType.GROUP.name());
        conversationInfo.setBelongAccount(PreferencesUtil.getInstance().getUserId());
        conversationInfo.setAvailable(true);
        conversationInfo.setConversationId(groupId);
        if (null != roomInfo) {
            conversationInfo.setConversationTitle(roomInfo.getGroupName());
        }
        DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .saveOrUpdateConversation(conversationInfo).subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        // 群聊更新会话内容
                        ChatEvent event = new ChatEvent(ChatEvent.CONVERSATION_ITEM_CONTENT_UPDATE);
                        if (null != roomInfo) {
                            Bundle bundle = new Bundle();
                            ArrayList<String> refreshList = new ArrayList<>();
                            refreshList.add(SmartConversationAdapter.REFRESH_TITLE);
                            bundle.putStringArrayList(SmartConversationAdapter.PAYLOADS, refreshList);
                            event.bundle = bundle;
                        }
                        event.obj = groupId;
                        EventBus.getDefault().post(event);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }
                });
    }


    /**
     * 清除会话的未读数
     *
     * @param conversationId
     */
    public void clearUnreadMsgCount(String conversationId) {
        Disposable subscribe = DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .getConversationByConversationId(PreferencesUtil.getInstance().getUserId(),
                        conversationId)
                .subscribe(conversationInfoList -> {
                    ConversationInfo conversationInfo;
                    if (conversationInfoList.isEmpty()) {
                        conversationInfo = null;
                    } else {
                        conversationInfo = conversationInfoList.get(0);
                    }
                    if (null != conversationInfo) {
                        conversationInfo.setUnReadNum(0);
                        DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                                .saveConversation(conversationInfo).subscribe(new CompletableObserver() {
                                    @Override
                                    public void onSubscribe(@NonNull Disposable d) {

                                    }

                                    @Override
                                    public void onComplete() {
                                        // 清除会话未读数后更新内容
                                        ChatEvent event = new ChatEvent(
                                                ChatEvent.CONVERSATION_ITEM_CONTENT_UPDATE);
                                        event.obj = conversationId;
                                        EventBus.getDefault().post(event);
                                    }

                                    @Override
                                    public void onError(@NonNull Throwable e) {

                                    }
                                });
                    }
                });
    }

    /**
     * 创建群聊
     *
     * @param groupId
     */
    public void createGroupConversation(String groupId, SmartMessage latestMessage) {
        ConversationInfo newSession = new ConversationInfo();
        newSession.setConversationType(SmartConversationType.GROUP.name());
        newSession.setBelongAccount(PreferencesUtil.getInstance().getUserId());
        newSession.setDigest(JimUtil.getMessageContent(latestMessage.getMessageType(), latestMessage.getMessageContent()));
        newSession.setLastMsgDate(latestMessage.getCreateTime());
        // 创建群聊的时候是未命名状态
        newSession.setConversationTitle("");
        newSession.setConversationId(groupId);
        DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .saveConversation(newSession).subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onComplete() {
//                      创建群聊后新增会话
                        ChatEvent event = new ChatEvent(ChatEvent.CONVERSATION_ITEM_CONTENT_UPDATE);
                        event.obj = groupId;
                        EventBus.getDefault().post(event);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }
                });
    }
}
