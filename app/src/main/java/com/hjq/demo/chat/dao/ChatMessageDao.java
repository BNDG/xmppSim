package com.hjq.demo.chat.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.hjq.demo.chat.entity.ChatMessage;
import com.hjq.demo.ui.activity.ImagePreviewMsgActivity;

import java.util.List;

/**
 * 地址
 *
 * @author zhou
 */
@Dao
public interface ChatMessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveChatMessage(ChatMessage chatMessage);

    //  如果通过 Entity 来删除数据，传进来的参数需要包含主键
    @Delete
    void delete(ChatMessage chatMessage);

    @Query("SELECT * FROM chat_message WHERE originId=(:originId)")
    ChatMessage getMessageByOriginId(String originId);

    @Query("SELECT * FROM chat_message WHERE smartMessageId=(:messageId) ")
    ChatMessage getMessageBySmartMessageId(String messageId);

    @Query("SELECT * FROM chat_message WHERE conversationId=(:conversationId) AND belongAccount = (:belongAccount) and status <> '4'" +
            "order by timestamp desc limit (:limit) offset (:offset)")
    List<ChatMessage> getMessageListByConversationId(String conversationId, String belongAccount, int limit, int offset);

    @Query("SELECT * FROM chat_message WHERE conversationId=(:conversationId) AND belongAccount = (:belongAccount) and status <> '4'" +
            "order by timestamp desc")
    List<ChatMessage> getMsgsByConversationId(String conversationId, String belongAccount);

    @Query("select * from chat_message where conversationId = (:conversationId) AND belongAccount = (:belongAccount) order by timestamp ASC LIMIT 1")
    ChatMessage getEarliestMessageByConversationId(String conversationId, String belongAccount);

    @Query("select * from chat_message where  conversationId = (:conversationId) AND belongAccount = (:belongAccount) order by timestamp DESC LIMIT 1")
    ChatMessage getLatestMessageByConversationId(String conversationId, String belongAccount);

    @Query("select * from chat_message where conversationId = (:conversationId) AND belongAccount =(:belongAccount) and status != (:status) and timestamp > (:timestamp) order by timestamp")
    List<ChatMessage> getMessagesAfterTime(String conversationId, String belongAccount, int status, long timestamp);

    @Query("delete from chat_message where conversationId =  (:conversationId) AND belongAccount =(:belongAccount)")
    void deleteMessageByGroupId(String conversationId, String belongAccount);

    @Query("select * from chat_message where  conversationId =  (:conversationId) AND belongAccount =(:belongAccount)" +
            " AND (callType = (:voiceType) or callType = (:videoType)) order by timestamp DESC LIMIT 1")
    ChatMessage getLatestCallMsgByConversationId(String conversationId, String belongAccount, String voiceType, String videoType);

    @Query("SELECT * FROM chat_message WHERE callId=(:callId)")
    ChatMessage getMessageByCallId(String callId);

    @RawQuery
    List<ChatMessage> searchMessages(SupportSQLiteQuery query);

    @Query("SELECT * FROM chat_message WHERE belongAccount = (:userId) AND messageType = (:msgType) and fromUserId = (:userId) and fileLocalPath IS NOT NULL ")
    List<ChatMessage> getSentFileMsgs(String userId, String msgType);

    @Query("SELECT * FROM chat_message WHERE belongAccount = (:userId) AND messageType = (:msgType) and fromUserId != (:userId) and fileLocalPath IS NOT NULL ")
    List<ChatMessage> getReceivedFileMsgs(String userId, String msgType);

    @Query("select count(*) from chat_message where conversationId = :conversationId and belongAccount = :userId and timestamp > :timestamp")
    int queryUnreadMsg(String userId, String conversationId, long timestamp);

    @Query("select count(*) from chat_message where conversationId = :conversationId and belongAccount = :userId and timestamp > (SELECT timestamp FROM chat_message WHERE originId = :originId)")
    int queryMessagePosition(String userId, String conversationId, String originId);

    @Query("select count(*) from chat_message where conversationId = :conversationId and belongAccount = :userId ")
    int queryMessageCount(String userId, String conversationId);

    @Query("UPDATE chat_message SET fromUserId = :newAccount, fromUserName = :userName WHERE fromUserName = :oldUserName and conversationId = :groupId and belongAccount = :belongAccount")
    void memberAccountChanged(String belongAccount, String groupId, String oldUserName, String newAccount, String userName);

    @Query("DELETE from chat_message where fromUserId = :fromUserId")
    void deleteMessageByUserId(String fromUserId);

    @Query("UPDATE chat_message SET isRead = 1 WHERE originId = :originId")
    void markAsRead(String originId);

    @Query("UPDATE chat_message SET isRead = 1 WHERE conversationId = :conversationId and timestamp < :timestamp and isRead = 0 and belongAccount = :belongAccount and fromUserId <> :belongAccount ")
    void markAsReadAtConversation(String conversationId, long timestamp, String belongAccount);

    @Query("UPDATE chat_message SET status = '3' WHERE belongAccount = :belongAccount and status = '1'")
    void updateSendingToFailed(String belongAccount);

    @Query("SELECT * FROM chat_message " +
            "WHERE conversationId = :conversationId " +
            "AND belongAccount = :userId " +
            "AND messageType = 'IMAGE' " +
            "AND status <> '4' " +
            "AND timestamp < (SELECT timestamp FROM chat_message " +
            "WHERE originId = :originId)" +
            "ORDER BY timestamp DESC LIMIT " + ImagePreviewMsgActivity.IMG_PAGE_SIZE)
    List<ChatMessage> getPreviousImages(String originId, String conversationId, String userId);

    @Query("SELECT * FROM chat_message " +
            "WHERE conversationId = :conversationId " +
            "AND belongAccount = :userId " +
            "AND messageType = 'IMAGE' " +
            "AND status <> '4' " +
            "AND timestamp > (SELECT timestamp FROM chat_message " +
            "WHERE originId = :originId)" +
            "ORDER BY timestamp ASC LIMIT " + ImagePreviewMsgActivity.IMG_PAGE_SIZE)
    List<ChatMessage> getNextImages(String originId, String conversationId, String userId);

}