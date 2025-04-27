package com.hjq.demo.chat.dao;

import androidx.core.util.Pair;
import androidx.lifecycle.LifecycleOwner;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.entity.ChatMessage;
import com.hjq.demo.chat.entity.enums.MessageStatus;
import com.hjq.demo.chat.listener.ChatMsgCallback;
import com.hjq.demo.chat.listener.SimpleResultCallback;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.utils.Trace;
import com.rxjava.rxlife.RxLife;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import com.bndg.smack.SmartIMClient;
import com.bndg.smack.entity.SmartMessage;
import com.bndg.smack.enums.SmartContentType;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 消息
 *
 * @author zhou
 */
public class MessageDao {
    public static final int PAGE_SIZE = 40;
    public static final int SEARCH_PAGE_SIZE = 40;
    private static volatile MessageDao instance;

    private MessageDao() {
        // 防止反射攻击
    }

    public static MessageDao getInstance() {
        if (instance == null) {
            synchronized (MessageDao.class) {
                if (instance == null) {
                    instance = new MessageDao();
                }
            }
        }
        return instance;
    }

    /**
     * 根据originId获取消息
     *
     * @param owner
     * @param originId
     * @param callback
     */
    public void getMessageByOriginId(LifecycleOwner owner, String originId, MessageDaoCallback callback) {
        Disposable subscribe = Single.fromCallable(new Callable<ChatMessage>() {
                    @Override
                    public ChatMessage call() throws Exception {
                        if (originId == null) {
                            return null;
                        }
                        return AppDatabase.getInstance(ActivityManager.getInstance().getApplication()).messageDao().getMessageByOriginId(originId);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(RxLife.to(owner))
                .subscribe(chatMessage -> {
                    if (chatMessage != null) {
                        callback.getMessageByOriginId(chatMessage);
                    }
                }, onError -> {

                });
    }

    /**
     * 根据smartMessageId获取消息
     *
     * @param messageId
     * @return
     */
    public ChatMessage getMessageBySmartMessageId(String messageId) {
        if (messageId == null) {
            return null;
        }
        return AppDatabase.getInstance(ActivityManager.getInstance().getApplication())
                .messageDao()
                .getMessageBySmartMessageId(messageId);
    }

    /**
     * 根据会话ID获取消息列表
     *
     * @param conversationId 会话ID
     * @return 消息列表
     */
    public void getMsgsByConversationId(LifecycleOwner owner, String conversationId, MessageDaoCallback callback) {
        Disposable subscribe = Single.fromCallable(new Callable<List<ChatMessage>>() {
                    @Override
                    public List<ChatMessage> call() throws Exception {
                        Trace.d("getMessageListByUserId: ==== " + conversationId);
                        List<ChatMessage> messageList = AppDatabase.getInstance(ActivityManager.getInstance().getApplication()).messageDao()
                                .getMsgsByConversationId(conversationId, PreferencesUtil.getInstance().getUserId());
                        return messageList;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(RxLife.to(owner))
                .subscribe(chatMessages -> {
                    callback.getMessagesByConversationId(chatMessages);
                }, onError -> {

                });
    }

    public void queryImgMsgPreNext(LifecycleOwner owner, String conversationId, String originId,
                                   boolean canGetPrevious, boolean canGetNext, MessageDaoCallback callback) {

        Disposable subscribe = Single.fromCallable(new Callable<Pair<List<ChatMessage>, List<ChatMessage>>>() {
                    @Override
                    public Pair<List<ChatMessage>, List<ChatMessage>> call() throws Exception {
                        List<ChatMessage> previousImages = new ArrayList<>();
                        List<ChatMessage> nextImages = new ArrayList<>();
                        if (canGetPrevious) {
                            previousImages = AppDatabase.getInstance(ActivityManager.getInstance().getApplication()).messageDao()
                                    .getPreviousImages(originId, conversationId, PreferencesUtil.getInstance().getUserId());
                            // 1 2 3 4 5
                          /*  if (!previousImages.isEmpty()) {
                                previousImages.remove(0);
                            }*/
                            // 倒序 5 4 3 2 1
                            Collections.reverse(previousImages);
                        }
                        if (canGetNext) {
                            nextImages = AppDatabase.getInstance(ActivityManager.getInstance().getApplication()).messageDao()
                                    .getNextImages(originId, conversationId, PreferencesUtil.getInstance().getUserId());
                            // 移除第一条
                            /*if (!nextImages.isEmpty()) {
                                nextImages.remove(0);
                            }*/
                        }
                        return new Pair<>(previousImages, nextImages);
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(RxLife.to(owner))
                .subscribe(pairList -> {
                    callback.getImageMsgs(pairList);
                }, onError -> {

                });
    }

    /**
     * 根据会话ID获取消息列表
     *
     * @param conversationId 会话ID
     * @return 消息列表
     */
    public void getMessagesByConversationId(LifecycleOwner owner, String conversationId, int pageNumber, MessageDaoCallback callback) {
        Disposable subscribe = Single.fromCallable(new Callable<List<ChatMessage>>() {
                    @Override
                    public List<ChatMessage> call() throws Exception {
                        int limit = MessageDao.PAGE_SIZE; // 每页记录 LIMIT
                        int offset = pageNumber * limit; // 计算偏移量 OFFSET
                        Trace.d("getMessageListByUserId: ==== " + conversationId, pageNumber,
                                offset);
                        List<ChatMessage> messageList = AppDatabase.getInstance(ActivityManager.getInstance().getApplication()).messageDao()
                                .getMessageListByConversationId(conversationId, PreferencesUtil.getInstance().getUserId(), limit, offset);
                        // 倒序
                        Collections.reverse(messageList);
                        return messageList;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(RxLife.to(owner))
                .subscribe(chatMessages -> {
                    callback.getMessagesByConversationId(chatMessages);
                }, onError -> {

                });
    }

    /**
     * 根据会话ID获取消息列表
     *
     * @param conversationId 会话ID
     * @return 消息列表
     */
    public void getMessagesByConversationIdLoadMore(LifecycleOwner owner, String conversationId, int pageNumber, MessageDaoCallback callback) {
        Disposable subscribe = Single.fromCallable(new Callable<List<ChatMessage>>() {
                    @Override
                    public List<ChatMessage> call() throws Exception {
                        int pageSize = MessageDao.PAGE_SIZE; // 每页50条记录
                        int offset = pageNumber * pageSize; // 计算偏移量
                        Trace.d("getMessageListByUserId: ==== " + conversationId, pageNumber,
                                offset);
                        List<ChatMessage> messageList = AppDatabase.getInstance(ActivityManager.getInstance().getApplication()).messageDao()
                                .getMessageListByConversationId(conversationId, PreferencesUtil.getInstance().getUserId(), pageSize, offset);
                        Collections.reverse(messageList);
                        return messageList;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(RxLife.to(owner))
                .subscribe(chatMessages -> {
                    callback.getMessagesByConversationId(chatMessages);
                }, onError -> {

                });
    }

    /**
     * 根据会话ID获取最后一条记录
     *
     * @param conversationId
     * @return
     */
    public ChatMessage getEarliestMessageByConversationId(String conversationId) {
        return AppDatabase.getInstance(ActivityManager.getInstance().getApplication()).messageDao()
                .getEarliestMessageByConversationId(conversationId, PreferencesUtil.getInstance().getUserId());
    }

    /**
     * 根据会话ID获取最后一条记录
     *
     * @return
     */
    public ChatMessage getLatestMessageByConversationId(String conversationId) {
        return AppDatabase.getInstance(ActivityManager.getInstance().getApplication()).messageDao()
                .getLatestMessageByConversationId(conversationId, PreferencesUtil.getInstance().getUserId());
    }

    public void getMessagesAfterTime(LifecycleOwner owner, String conversationId, long timestamp, MessageDaoCallback callback) {
        Disposable subscribe = Single.fromCallable(new Callable<List<ChatMessage>>() {
                    @Override
                    public List<ChatMessage> call() throws Exception {
                        return AppDatabase.getInstance(ActivityManager.getInstance().getApplication()).messageDao()
                                .getMessagesAfterTime(conversationId, PreferencesUtil.getInstance().getUserId(), MessageStatus.SENDING.value(), timestamp);
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(RxLife.to(owner))
                .subscribe(chatMessages -> {
                    callback.getMessagesAfterTime(chatMessages);
                }, onError -> {

                });
    }

    /**
     * 根据群组ID删除消息
     * 使用场景: 群会话中清空聊天记录
     *
     * @param conversationId 群组ID
     */
    public void deleteMessageByConversationId(String conversationId) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        AppDatabase.getInstance(ActivityManager.getInstance().getApplication()).messageDao()
                                .deleteMessageByGroupId(conversationId, PreferencesUtil.getInstance().getUserId());
                        return true;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(onSuccess -> {
                    if (onSuccess) {

                    } else {

                    }
                }, onError -> {

                });

    }


    /**
     * 获取某个会话中最新一条call消息
     *
     * @return
     */
    public void getLatestCallMsgById(LifecycleOwner owner, String conversationId, MessageDaoCallback callback) {
        Disposable subscribe = Single.fromCallable(new Callable<ChatMessage>() {
                    @Override
                    public ChatMessage call() throws Exception {
                        return AppDatabase.getInstance(ActivityManager.getInstance().getApplication()).messageDao()
                                .getLatestCallMsgByConversationId(conversationId, PreferencesUtil.getInstance().getUserId(),
                                        Constant.MSG_TYPE_VIDEO_CALL, Constant.MSG_TYPE_VOICE_CALL);
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(RxLife.to(owner))
                .subscribe(chatMessage -> {
                    callback.getLatestCallMsgById(chatMessage);
                }, onError -> {

                });

    }

    /**
     * 根据callId获取消息
     *
     * @param callId
     * @return
     */
    public void getMessageByCallId(String callId, MessageDaoCallback callback) {
        if (callId == null) {
            callback.getMessageByCallId(null);
            return;
        }
        Disposable subscribe = Single.fromCallable(new Callable<ChatMessage>() {
                    @Override
                    public ChatMessage call() throws Exception {
                        return AppDatabase.getInstance(ActivityManager.getInstance().getApplication()).messageDao()
                                .getMessageByCallId(callId);
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatMessage -> {
                    callback.getMessageByCallId(chatMessage);
                }, onError -> {

                });
    }

    public void searchMessageRecord(String conversationId, String keyword, int pageNum, MessageDaoCallback callback) {
        Disposable subscribe = Single.fromCallable(new Callable<List<ChatMessage>>() {
                    @Override
                    public List<ChatMessage> call() throws Exception {
//                        conversationId, PreferencesUtil.getInstance().getUserId(), keyword, msgType
                        String query = "SELECT * FROM chat_message " +
                                "WHERE belongAccount = ? " +
                                "AND conversationId = ? " +
                                "AND messageType = 'TEXT' " +
                                "AND messageContent LIKE ? " +
                                "ORDER BY timestamp DESC " +
                                "LIMIT ? OFFSET ?";
                        String[] args = new String[]{PreferencesUtil.getInstance().getUserId(),
                                conversationId, "%" + keyword + "%", String.valueOf(SEARCH_PAGE_SIZE), String.valueOf(pageNum * SEARCH_PAGE_SIZE)};
                        SupportSQLiteQuery sqliteQuery = new SimpleSQLiteQuery(query, args);
                        return AppDatabase.getInstance(ActivityManager.getInstance().getApplication())
                                .messageDao().searchMessages(sqliteQuery);
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatMessages -> {
                    callback.getSearchMessages(chatMessages);
                }, onError -> {

                });
    }

    /**
     * 根据用户ID搜索消息
     *
     * @param conversationId
     * @param userId
     * @param callback
     */
    public void searchMessageRecordByUserId(String conversationId, String userId, int pageNum, MessageDaoCallback callback) {
        Disposable subscribe = Single.fromCallable(new Callable<List<ChatMessage>>() {
                    @Override
                    public List<ChatMessage> call() throws Exception {
                        String query = "SELECT * FROM chat_message " +
                                "WHERE belongAccount = ? " +
                                "AND conversationId = ? " +
                                "AND fromUserId = ? " +
                                "ORDER BY timestamp DESC " +
                                "LIMIT ? OFFSET ?";
                        String[] args = new String[]{PreferencesUtil.getInstance().getUserId(),
                                conversationId, userId, String.valueOf(SEARCH_PAGE_SIZE), String.valueOf(pageNum * SEARCH_PAGE_SIZE)};
                        SupportSQLiteQuery sqliteQuery = new SimpleSQLiteQuery(query, args);
                        return AppDatabase.getInstance(ActivityManager.getInstance().getApplication())
                                .messageDao().searchMessages(sqliteQuery);
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatMessages -> {
                    callback.getSearchMessages(chatMessages);
                }, onError -> {

                });
    }

    /**
     * 根据用户ID搜索消息
     *
     * @param conversationId
     * @param callback
     */
    public void searchMediaMsgByConversationId(String conversationId, int pageNum, MessageDaoCallback callback) {
        Disposable subscribe = Single.fromCallable(new Callable<List<ChatMessage>>() {
                    @Override
                    public List<ChatMessage> call() throws Exception {
                        String query = "SELECT * FROM chat_message " +
                                "WHERE belongAccount = ? " +
                                "AND conversationId = ? " +
                                "AND messageType IN ('VIDEO', 'IMAGE')" +
                                "ORDER BY timestamp DESC " +
                                "LIMIT ? OFFSET ?";
                        String[] args = new String[]{PreferencesUtil.getInstance().getUserId(),
                                conversationId, String.valueOf(SEARCH_PAGE_SIZE), String.valueOf(pageNum * SEARCH_PAGE_SIZE)};
                        SupportSQLiteQuery sqliteQuery = new SimpleSQLiteQuery(query, args);
                        List<ChatMessage> chatMessages = AppDatabase.getInstance(ActivityManager.getInstance().getApplication())
                                .messageDao().searchMessages(sqliteQuery);
                        Collections.reverse(chatMessages);
                        return chatMessages;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatMessages -> {
                    callback.getSearchMessages(chatMessages);
                }, onError -> {

                });
    }

    public void save(ChatMessage chatMessage) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        ChatMessage message = MessageDao.getInstance()
                                .getLatestMessageByConversationId(chatMessage.getConversationId());
                        if (null != message) {
                            chatMessage.setLastTimeStamp(message.getTimestamp());
                        }
                        AppDatabase.getInstance(ActivityManager.getInstance().getApplication()).messageDao().saveChatMessage(chatMessage);
                        return true;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(onSuccess -> {
                }, onError -> {
                });

    }

    /**
     * 根据originId更新消息
     *
     * @param originId
     * @param msgEntity
     */
    public void updateBySmartMessage(String originId, SmartMessage msgEntity, ChatMsgCallback callback) {
        Disposable subscribe = Single.fromCallable(new Callable<ChatMessage>() {
                    @Override
                    public ChatMessage call() throws Exception {
                        ChatMessage chatMessage = AppDatabase.getInstance(ActivityManager.getInstance().getApplication())
                                .messageDao()
                                .getMessageByOriginId(originId);
                        if (chatMessage != null) {
                            if (null == msgEntity) {
                                chatMessage.setStatus(MessageStatus.SEND_FAIL.value());
                            } else {
                                chatMessage.setSmartMessageId(msgEntity.getMessageId());
                                chatMessage.setStatus(MessageStatus.SEND_SUCCESS.value());
                                // 文件消息 需要更新发送后的内容
                                if (ChatMessage.isFileType(chatMessage.getMessageType())) {
                                    chatMessage.setMessageContent(msgEntity.getMessageContent());
                                }
                            }
                            AppDatabase.getInstance(ActivityManager.getInstance().getApplication()).messageDao().saveChatMessage(chatMessage);
                        } else {
                            Trace.d("call: 消息为null");
                        }
                        return chatMessage;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(chatMessage -> {
                    if (chatMessage != null) {
                        callback.updateMsgSuccess(chatMessage);
                    }
                }, onError -> {
                    callback.updateMsgFailed();
                });
    }

    /**
     * 保存消息
     *
     * @param chatMessage
     */
    public void saveAndSetLastTimeStamp(ChatMessage chatMessage) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        ChatMessage message = MessageDao.getInstance()
                                .getLatestMessageByConversationId(chatMessage.getConversationId());
                        if (null != message) {
                            chatMessage.setLastTimeStamp(message.getTimestamp());
                        }
                        AppDatabase.getInstance(ActivityManager.getInstance().getApplication()).messageDao().saveChatMessage(chatMessage);
                        return true;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(onSuccess -> {
                    if (onSuccess) {
                    } else {

                    }
                }, onError -> {

                });
    }


    /**
     * 保存消息
     *
     * @param chatMessage
     */
    public void saveAndSetLastTimeStamp(ChatMessage chatMessage, SimpleResultCallback callback) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        ChatMessage message = MessageDao.getInstance()
                                .getLatestMessageByConversationId(chatMessage.getConversationId());
                        if (null != message) {
                            chatMessage.setLastTimeStamp(message.getTimestamp());
                        }
                        AppDatabase.getInstance(ActivityManager.getInstance().getApplication()).messageDao().saveChatMessage(chatMessage);
                        return true;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(onSuccess -> {
                    if (onSuccess) {
                        callback.onResult(true);
                    }
                }, onError -> {
                });
    }

    public void disableMessageByOriginId(String originId) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        ChatMessage messageByOriginId = AppDatabase
                                .getInstance(ActivityManager.getInstance().getApplication()).messageDao()
                                .getMessageByOriginId(originId);
                        if (messageByOriginId != null) {
                            messageByOriginId.setStatus(MessageStatus.DISABLE.value());
                            AppDatabase
                                    .getInstance(ActivityManager.getInstance().getApplication()).messageDao()
                                    .saveChatMessage(messageByOriginId);
                        }
                        return true;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(res -> {
                }, onError -> {

                });
    }

    /**
     * 获取文件消息
     *
     * @param received
     */
    public void getFileMsg(LifecycleOwner owner, boolean received, MessageDaoCallback callback) {
        Disposable subscribe = Single.fromCallable(new Callable<List<ChatMessage>>() {
                    @Override
                    public List<ChatMessage> call() throws Exception {
                        if (received) {
                            return AppDatabase.getInstance(ActivityManager.getInstance().getApplication()).messageDao()
                                    .getReceivedFileMsgs(PreferencesUtil.getInstance().getUserId(), SmartContentType.FILE);
                        } else {
                            return AppDatabase.getInstance(ActivityManager.getInstance().getApplication()).messageDao()
                                    .getSentFileMsgs(PreferencesUtil.getInstance().getUserId(), SmartContentType.FILE);
                        }
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(RxLife.to(owner))
                .subscribe(callback::getFileMsgs, onError -> {

                });
    }

    public void queryUnreadMsgCount(LifecycleOwner owner, String conversationId, long timestamp,
                                    MessageDaoCallback callback) {
        Disposable subscribe = Single.fromCallable(new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        return AppDatabase.getInstance(ActivityManager.getInstance().getApplication()).messageDao()
                                .queryUnreadMsg(PreferencesUtil.getInstance().getUserId(), conversationId, timestamp);
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(RxLife.to(owner))
                .subscribe(callback::queryUnreadMsgCount, onError -> {

                });

    }

    public void queryMessagePosition(@NotNull LifecycleOwner owner,
                                     @Nullable String conversationId,
                                     @Nullable String originId, @NotNull MessageDao.MessageDaoCallback callback) {
        Disposable subscribe = Single.fromCallable(new Callable<Integer[]>() {
                    @Override
                    public Integer[] call() throws Exception {
                        int count = AppDatabase.getInstance(ActivityManager.getInstance().getApplication()).messageDao()
                                .queryMessageCount(PreferencesUtil.getInstance().getUserId(), conversationId);
                        int position = AppDatabase.getInstance(ActivityManager.getInstance().getApplication()).messageDao()
                                .queryMessagePosition(PreferencesUtil.getInstance().getUserId(), conversationId, originId);
                        return new Integer[]{count, position};
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(RxLife.to(owner))
                .subscribe(callback::queryMessagePosition, onError -> {

                });
    }

    public void memberAccountChanged(String groupId, String oldAccount, String newAccount, String userName) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        AppDatabase.getInstance(ActivityManager.getInstance().getApplication()).messageDao()
                                .memberAccountChanged(PreferencesUtil.getInstance().getUserId(), groupId, oldAccount, newAccount, userName);
                        return true;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void deleteMessageByUserId(LifecycleOwner owner, String fromUserId, SimpleResultCallback callback) {
        Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        AppDatabase.getInstance(ActivityManager.getInstance().getApplication()).messageDao()
                                .deleteMessageByUserId(fromUserId);
                        return true;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(RxLife.to(owner))
                .subscribe(onSuccess -> {
                    callback.onResult(true);
                }, onError -> {

                });
    }

    public void markAsRead(ChatMessage chatMessage) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        AppDatabase.getInstance(ActivityManager.getInstance().getApplication()).messageDao()
                                .markAsRead(chatMessage.getOriginId());
                        // 查询之前的消息 都置为已读
                        AppDatabase.getInstance(ActivityManager.getInstance().getApplication()).messageDao()
                                .markAsReadAtConversation(chatMessage.getConversationId(), chatMessage.getTimestamp(),
                                        PreferencesUtil.getInstance().getUserId());
                        SmartIMClient.getInstance().getSmartCommMsgManager()
                                .sendReceipt(chatMessage.getFromUserId(), chatMessage.getSmartMessageId());
                        return true;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void updateSendingToFailed() {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        AppDatabase.getInstance(ActivityManager.getInstance().getApplication()).messageDao()
                                .updateSendingToFailed(PreferencesUtil.getInstance().getUserId());
                        return true;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public interface MessageDaoCallback {
        default void getMessageByCallId(ChatMessage chatMessage) {
        }

        default void getMessageByOriginId(ChatMessage chatMessage) {
        }

        default void getMessagesByConversationId(List<ChatMessage> chatMessages) {
        }

        default void getMessagesAfterTime(List<ChatMessage> chatMessages) {
        }

        default void getLatestCallMsgById(ChatMessage chatMessage) {
        }

        default void getSearchMessages(List<ChatMessage> chatMessages) {
        }

        default void getFileMsgs(List<ChatMessage> chatMessages) {
        }

        default void queryUnreadMsgCount(int aLong) {
        }

        default void queryMessagePosition(Integer[] ints) {
        }

        default void getImageMsgs(Pair<List<ChatMessage>, List<ChatMessage>> chatMessages) {
        }
    }
}
