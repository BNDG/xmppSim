package com.hjq.demo.chat.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hjq.demo.chat.entity.FriendApply
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

/**
 * @author r
 * @date 2024/9/18
 * @description Brief description of the file content.
 */
@Dao
interface FriendRequestDao {

    @Query("SELECT * FROM friendApply WHERE applyId = :applyId")
    fun getFriendApplyByApplyId(applyId: String): Single<List<FriendApply>>

    @Query("SELECT * FROM friendApply WHERE belongAccount = :belongAccount")
    fun getFriendApplyByBelongAccount(belongAccount: String): Single<List<FriendApply>>

    @Query("SELECT * FROM friendApply WHERE friendUserId = :friendUserId and belongAccount = :belongAccount")
    fun getFriendApplyByUserId(
        friendUserId: String,
        belongAccount: String
    ): Single<List<FriendApply>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveOrUpdateFriendApply(friendApply: FriendApply)

    @Query("SELECT * FROM friendApply where belongAccount = :belongAccount")
    fun getFriendApplies(belongAccount: String): Single<List<FriendApply>>?

    @Query("SELECT * FROM friendApply where status = :status and  belongAccount = :belongAccount")
    fun getUnReadFriendApplies(status: String, belongAccount: String): Single<List<FriendApply>>?

    @Delete
    fun deleteFriendApply(friendApply: FriendApply)
}