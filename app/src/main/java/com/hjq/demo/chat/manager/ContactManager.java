package com.hjq.demo.chat.manager;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.UserDao;
import com.hjq.demo.chat.entity.ChatEvent;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.listener.ContactCallback;
import com.hjq.demo.chat.listener.SimpleResultCallback;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.utils.Trace;
import com.tencent.mmkv.MMKV;

import org.greenrobot.eventbus.EventBus;

import com.bndg.smack.model.SmartUserInfo;

/**
 * @author r
 * @date 2024/9/29
 * @description Brief description of the file content.
 */
public class ContactManager {
    private static volatile ContactManager instance;

    private ContactManager() {
    }

    public static ContactManager getInstance() {
        if (instance == null) {
            synchronized (ContactManager.class) {
                if (instance == null) {
                    instance = new ContactManager();
                }
            }
        }
        return instance;
    }

    /**
     * 解除好友关系
     *
     * @param contactId
     */
    public void unbindFriendship(LifecycleOwner owner, String contactId) {
        UserDao.getInstance().getUserById(owner, contactId, new ContactCallback() {
            @Override
            public void getUser(@Nullable User userById) {
                if (userById != null) {
                    userById.setIsFriend(Constant.DELETE_ME);
                    UserDao.getInstance().saveOrUpdateContact(userById);
                }
            }
        });
    }

    /**
     * 数据库中添加好友
     * @param isNewAdded 新的好友
     */
    public void addContact(LifecycleOwner owner, SmartUserInfo userInfo, boolean isNewAdded) {
        UserDao.getInstance().getUserById(owner, userInfo.getUserId(), new ContactCallback() {
            @Override
            public void getUser(@Nullable User userById) {
                if (userById != null) {
                } else {
                    userById = new User();
                    userById.setUserId(userInfo.getUserId());
                }
                userById.setBelongAccount(PreferencesUtil.getInstance().getUserId());
                userById.setUserNickName(userInfo.getNickname());
                userById.setSubscribeStatus(userInfo.getSubscribeStatus());
                // 默认没有加入黑名单禁言
                String isFriend = userInfo.isFriend() ? Constant.IS_FRIEND : Constant.IS_NOT_FRIEND;
                userById.setIsFriend(isFriend);
                Trace.d("addContact: " + userById.getUserNickName(),
                        "isFriend? " + userById.getIsFriend(),
                        "sub " + userById.getSubscribeStatus());
                // 缓存会话标题
                MMKV.defaultMMKV().putString(userById.getUserId() + "_" + Constant.CONVERSATION_TITLE, userById.getConversationTitle());
                UserDao.getInstance().saveOrUpdateContact(userById, new SimpleResultCallback() {
                    @Override
                    public void onResult(boolean isSuccess) {
                        if (isNewAdded) {
                            // 通知联系人更新
                            ChatEvent event = new ChatEvent(ChatEvent.REFRESH_CONTACT);
                            Bundle bundle = new Bundle();
                            bundle.putString(Constant.CONTACT_ID, userInfo.getUserId());
                            bundle.putBoolean(Constant.FRIEND_ADDED, false);
                            event.bundle = bundle;
                            EventBus.getDefault().post(event);
                        }
                    }
                });
            }
        });

        /*UserDao.getInstance().saveOrUpdateContact(contact, new SimpleResultCallback() {
            @Override
            public void onResult(boolean isSuccess) {
                if (isNewAdded) {
                    // 通知联系人更新
                    ChatEvent event = new ChatEvent(ChatEvent.REFRESH_CONTACT_LIST);
                    event.obj = false;
                    EventBus.getDefault().post(event);
                }
                *//*if (contact.isFriendBoth() || contact.inMyFriendList()) {
                    SmartIMClient.getInstance().getSmartCommUserManager().getUserIsOnLine(contact.getUserJid(), new IUserInfoCallBack() {
                        @Override
                        public void isOnLine(boolean isOnLine) {
                            UserDao.getInstance().getUserById(MainActivity.this, contact.getUserJid(), new ContactCallback() {
                                @Override
                                public void getUser(@Nullable User userById) {
                                    if (userById == null) {
                                        return;
                                    }
                                    userById.setPresence(isOnLine ? Constant.ONLINE : Constant.OFFLINE);
                                    DBManager.Companion.getInstance(MainActivity.this)
                                            .saveOrUpdateContact(userById)
                                            .to(RxLife.to(MainActivity.this))
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
                            });
                        }
                    });
                }*//*
            }
        });*/
    }
}
