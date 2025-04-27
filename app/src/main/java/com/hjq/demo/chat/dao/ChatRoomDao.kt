package com.hjq.demo.chat.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.hjq.demo.chat.entity.ChatRoomEntity
import com.hjq.demo.chat.entity.MemberVoiceEntity
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

/**
 * @author r
 * @date 2024/8/2
 * @description Brief description of the file content.
 */
@Dao
interface ChatRoomDao {
    @Query("select * from chat_room")
    fun getAllChatRoom(): Single<List<ChatRoomEntity>>

    @Query("select * from chat_room where chatRoomJid = :chatRoomJid")
    fun getChatRoomByRoomId(chatRoomJid: String): Single<List<ChatRoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChatRoom(chatRoomEntity: ChatRoomEntity): Completable

    @Query("select * from chat_room where chatRoomJid = :chatRoomJid limit 1")
    fun getChatRoomByRoomIdSync(chatRoomJid: String): ChatRoomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChatRoomSync(chatRoomEntity: ChatRoomEntity?)

    @Transaction
    fun saveOrUpdateChatRoom(chatRoomEntity: ChatRoomEntity) {
        val chatRoomByRoomId = getChatRoomByRoomIdSync(chatRoomEntity.chatRoomJid)
        chatRoomByRoomId?.let {
            // 如果记录存在，更新待插入实体的字段
            chatRoomEntity.chatRoomName = chatRoomEntity.chatRoomName.takeIf { !it.isNullOrEmpty() }
                ?: it.chatRoomName
            chatRoomEntity.chatRoomSubject =
                chatRoomEntity.chatRoomSubject.takeIf { !it.isNullOrEmpty() }
                    ?: it.chatRoomSubject
        }
        insertChatRoomSync(chatRoomEntity)
    }

    @Query("select cr.moderated, gm.role from chat_room cr join group_member gm on gm.groupId = cr.chatRoomJid " +
            "where cr.chatRoomJid = :chatRoomJid and gm.memberAccount = :account")
    fun getMemberVoiceInGroup(chatRoomJid: String, account: String): Single<List<MemberVoiceEntity>>?
}