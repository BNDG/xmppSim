package com.hjq.demo.chat.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.hjq.demo.chat.entity.User;
import com.hjq.demo.utils.Trace;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/**
 * @author r
 * @date 2024/9/18
 * @description Brief description of the file content.
 */
@Dao
public interface ContactDao {
    @Query("select * from contact where userId = (:userId) and belongAccount = (:belongAccount)")
    Single<List<User>> getUserById(String userId, String belongAccount);

    @Query("select * from contact where userId = (:userId) and belongAccount = (:belongAccount)")
    User getUserByIdSync(String userId, String belongAccount);

    @Query("select * from contact where isFriend = (:isFriend) and  isBlocked = (:isBlocked) and belongAccount = (:belongAccount)")
    Single<List<User>> getAllFriendList(String isFriend, String isBlocked, String belongAccount);


    @Query("select * from contact where  isBlocked = (:isBlocked) and belongAccount = (:belongAccount)")
    Single<List<User>> getAllBlockedUserList(String isBlocked, String belongAccount);

    @Query("select * from contact where  userType = (:userType) and isBlocked = (:isBlocked) and belongAccount = (:belongAccount)")
    Single<List<User>> getContactsCount(String userType, String isBlocked, String belongAccount);

    @Query("select * from contact where  isStarred = (:isStarred) and isBlocked = (:isBlocked) and belongAccount = (:belongAccount)")
    Single<List<User>> getAllStarredContactList(String isStarred, String isBlocked, String belongAccount);


    @Query("select * from contact where  isFriend = (:isFriend) and userPhone = (:userPhone) and belongAccount = (:belongAccount)")
    Single<List<User>> checkIsFriendByPhone(String isFriend, String userPhone, String belongAccount);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSync(User contact);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveContact(User contact);

    @Delete
    Completable deleteContact(User user);

    @Transaction
    default void saveContactSync(User user) {
        User userByIdSync = getUserByIdSync(user.getUserId(), user.getBelongAccount());
        if (null != userByIdSync) {
            user.id = userByIdSync.id;
        }
        insertSync(user);
    }


}
