package com.hjq.demo.chat.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.hjq.demo.chat.entity.AvatarEntity
import com.hjq.demo.utils.Trace
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

/**
 * @author r
 * @date 2024/9/3
 * @description Brief description of the file content.
 */
@Dao
interface AvatarDao {
    @Query(
        "select * from avatars where userId LIKE :userId || ',%' " +
                "OR userId LIKE '%,' || :userId " +
                "OR userId = :userId " +
                "OR userId LIKE '%,' || :userId || ',%'"
    )
    fun get(userId: String): Single<List<AvatarEntity>>

    @Query(
        "select * from avatars where userId = :conversationId"
    )
    fun getAvatarByConversationId(conversationId: String): Single<List<AvatarEntity>>


    @Query(
        "select * from avatars where userId LIKE :userId || ',%' " +
                "OR userId LIKE '%,' || :userId " +
                "OR userId = :userId " +
                "OR userId LIKE '%,' || :userId || ',%' or (avatarhash IS NOT NULL AND avatarhash <> '' and avatarHash = :avatarHash)"
    )
    fun getAvatarByIdOrHash(userId: String, avatarHash: String): Single<List<AvatarEntity>>

    @Query(
        "select * from avatars where userId LIKE :userId || ',%' " +
                "OR userId LIKE '%,' || :userId " +
                "OR userId = :userId " +
                "OR userId LIKE '%,' || :userId || ',%' or (avatarhash IS NOT NULL AND avatarhash <> '' and avatarHash = :avatarHash) limit 1"
    )
    fun getAvatarByIdOrHashSync(userId: String, avatarHash: String): AvatarEntity?

    @Query(
        "select * from avatars where userId LIKE :userId || ',%' " +
                "OR userId LIKE '%,' || :userId " +
                "OR userId = :userId " +
                "OR userId LIKE '%,' || :userId || ',%' limit 1"
    )
    fun getAvatar(userId: String): AvatarEntity?

    @Query("select * from avatars where avatarHash = :avatarHash limit 1")
    fun getAvatarByHashSync(avatarHash: String?): AvatarEntity?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAvatar(avatarEntity: AvatarEntity): Completable


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAvatarSync(avatarEntity: AvatarEntity)


    @Transaction
    fun saveOrUpdateAvatar(avatarEntity: AvatarEntity) {
        val dbEntity = getAvatarByIdOrHashSync(avatarEntity.userId, avatarEntity.avatarHash)
        dbEntity?.let {
            // hash存储
            avatarEntity.id = dbEntity.id
            if (avatarEntity.avatarLocalPath == null) {
                if (avatarEntity.avatarHash != dbEntity.avatarHash) {
                    // 删除旧头像
                }else {
                    // 头像相同
                    avatarEntity.avatarLocalPath = dbEntity.avatarLocalPath
                }
            } else {
                avatarEntity.avatarLocalPath =
                    avatarEntity.avatarLocalPath.takeIf { !it.isNullOrEmpty() }
                        ?: dbEntity.avatarLocalPath
            }
            val userIdSet = avatarEntity.userId.split(",").toSet()
            val dbUserIdSet = dbEntity.userId.split(",").toSet()
            avatarEntity.userId = (userIdSet union dbUserIdSet).joinToString(",")
        }
//        Trace.d(dbEntity?.avatarHash,"real saveOrUpdateAvatar ${avatarEntity.avatarHash}")
        insertAvatarSync(avatarEntity)
    }

//    @Query("delete from avatars")
//    fun deleteAll()
}