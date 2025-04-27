package com.hjq.demo.chat.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.hjq.demo.chat.entity.ChatRoomEntity
import com.hjq.demo.chat.entity.ConversationInfo
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

/**
 * @author r
 * @date 2024/8/1
 * @description Brief description of the file content.
 */
@Dao
interface ChatConversationDao {
    /**
     * 获取改用户的会话列表
     *
     * @param belongAccount
     * @return 背压 Flowable<...> 类型的对象，这是为了防止表中数据过多，读取速率远大于接收数据，从而导致内存溢出的问题
     */
    @Query("select * from conversation_info where belongAccount = :belongAccount order by pinned desc, lastMsgDate desc")
    fun getConversationList(belongAccount: String?): Single<List<ConversationInfo>>?

    /**
     * 根据会话id获取会话
     *
     * @param belongAccount
     * @param conversationId
     * @return
     */
    @Query("select * from conversation_info where belongAccount = :belongAccount and conversationId = :conversationId LIMIT 1")
    fun getConversationById(
        belongAccount: String?,
        conversationId: String?
    ): Single<List<ConversationInfo>>?

    /**
     * @param conversationInfo
     * @return Completable ，该返回值是 RxJava 的基本类型，它只处理 onComplete onError 事件，可以看成是Rx的Runnable。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertConversation(conversationInfo: ConversationInfo?): Completable?

    @Query("select * from conversation_info where belongAccount = :belongAccount and conversationId = :conversationId LIMIT 1")
    fun getConversationByIdSync(belongAccount: String?, conversationId: String?): ConversationInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertConversationSync(conversationInfo: ConversationInfo?)

    /**
     * 多线程需要处理更新问题
     */
    @Transaction
    fun saveOrUpdateConversation(conversationInfo: ConversationInfo) {
        val existingConversation =
            getConversationByIdSync(conversationInfo.belongAccount, conversationInfo.conversationId)

        if (existingConversation != null) {
            conversationInfo.id = existingConversation.id // 更新 ID
            // 未读数++
            conversationInfo.unReadNum += existingConversation.unReadNum
            conversationInfo.lastMsgDate =
                conversationInfo.lastMsgDate.takeIf { it != 0L } ?: existingConversation.lastMsgDate
            conversationInfo.fromJid = conversationInfo.fromJid.takeIf { !it.isNullOrEmpty() }
                ?: existingConversation.fromJid
            conversationInfo.digest = conversationInfo.digest.takeIf { !it.isNullOrEmpty() }
                ?: existingConversation.digest
            conversationInfo.conversationTitle =
                conversationInfo.conversationTitle.takeIf { !it.isNullOrEmpty() }
                    ?: existingConversation.conversationTitle
            conversationInfo.isPinned = existingConversation.isPinned
        }
        insertConversationSync(conversationInfo)
    }

    /**
     * 根据会话类型获取用户的会话列表
     *
     * @param belongAccount
     * @param conversationType
     * @return 背压 Flowable<...> 类型的对象，这是为了防止表中数据过多，读取速率远大于接收数据，从而导致内存溢出的问题
     */
    @Query("select * from conversation_info where belongAccount = :belongAccount and conversationType = :conversationType")
    fun getConversationByConversationType(
        belongAccount: String?,
        conversationType: String?
    ): Single<List<ConversationInfo>>?

    /**
     * 根据会话id删除会话
     *
     * @param belongAccount
     * @param conversationId
     */
    @Query("delete from conversation_info where belongAccount = :belongAccount and conversationId = :conversationId")
    fun deleteConversationById(belongAccount: String?, conversationId: String?)

}
