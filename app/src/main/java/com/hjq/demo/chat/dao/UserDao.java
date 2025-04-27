package com.hjq.demo.chat.dao;

import androidx.lifecycle.LifecycleOwner;

import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.listener.ContactCallback;
import com.hjq.demo.chat.listener.SimpleResultCallback;
import com.hjq.demo.chat.utils.CommonUtil;
import com.hjq.demo.chat.utils.PinyinComparator;
import com.hjq.demo.manager.ActivityManager;
import com.rxjava.rxlife.RxLife;

import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * 用户DAO
 *
 * @author zhou
 * 迁移到Dbmanager
 */
public class UserDao {
    private static volatile UserDao instance;

    private UserDao() {
        // 防止反射攻击
    }

    public static UserDao getInstance() {
        if (instance == null) {
            synchronized (UserDao.class) {
                if (instance == null) {
                    instance = new UserDao();
                }
            }
        }
        return instance;
    }

    /**
     * 保存用户
     * 不存在则新建，存在则更新
     * 唯一标识(userId)
     *
     * @param user 用户
     */
    public void saveOrUpdateContact(User user) {
        user.setUserHeader(CommonUtil.generateUserHeader(user.getUserNickName()));
        DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .saveOrUpdateContact(user)
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

                    }
                });
    }

    public void saveOrUpdateContact(User user, SimpleResultCallback callback) {
        user.setUserHeader(CommonUtil.generateUserHeader(user.getUserNickName()));
        DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .saveOrUpdateContact(user)
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        callback.onResult(true);
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

                    }
                });
    }

    /**
     * 通过用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    public void getUserById(LifecycleOwner lifecycleOwner, String userId, ContactCallback contactCallback) {
        DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .getUserById(userId)
                .to(RxLife.to(lifecycleOwner))
                .subscribe(new Consumer<List<User>>() {
                    @Override
                    public void accept(List<User> users) throws Throwable {
                        if (!users.isEmpty()) {
                            contactCallback.getUser(users.get(0));
                        } else {
                            contactCallback.getUser(null);
                        }
                    }
                });
    }

    /**
     * 通过用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    public void getUserById(String userId, ContactCallback contactCallback) {
        Disposable subscribe = DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .getUserById(userId)
                .subscribe(new Consumer<List<User>>() {
                    @Override
                    public void accept(List<User> users) throws Throwable {
                        if (!users.isEmpty()) {
                            contactCallback.getUser(users.get(0));
                        } else {
                            contactCallback.getUser(null);
                        }
                    }
                });
    }

    /**
     * 获取所有的好友列表
     *
     * @return 所有的好友列表
     */
    public void getAllFriendList(LifecycleOwner lifecycleOwner, ContactCallback contactCallback) {
        DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .getAllFriendList()
                .to(RxLife.to(lifecycleOwner))
                .subscribe(new Consumer<List<User>>() {
                    @Override
                    public void accept(List<User> users) throws Throwable {
                        contactCallback.getContactList(users);
                    }
                });
    }

    /**
     * 获取所有的黑名单用户列表
     *
     * @return 所有的好友列表
     */
    public void getAllBlockedUserList(LifecycleOwner lifecycleOwner, ContactCallback contactCallback) {
        DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .getAllBlockedUserList()
                .to(RxLife.to(lifecycleOwner))
                .subscribe(new Consumer<List<User>>() {
                    @Override
                    public void accept(List<User> users) throws Throwable {
                        contactCallback.getAllBlockedUserList(users);
                    }
                });
    }

    /**
     * 获取所有的星标好友列表
     * 按好友昵称或备注首字母排序并设置特殊header
     *
     * @return 所有的星标好友列表
     */
    public void getAllStarredContactList(LifecycleOwner lifecycleOwner, ContactCallback contactCallback) {
        DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .getAllStarredContactList()
                .to(RxLife.to(lifecycleOwner))
                .subscribe(new Consumer<List<User>>() {
                    @Override
                    public void accept(List<User> users) throws Throwable {
                        Collections.sort(users, new PinyinComparator() {
                        });
                        for (User starredContact : users) {
                            starredContact.setUserHeader(Constant.STAR_FRIEND);
                        }
                        contactCallback.getStarredContactList(users);
                    }
                });
    }

    public void deleteUser(User user) {
        DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .deleteContact(user);
    }
}