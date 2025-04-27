package com.hjq.demo.chat.listener

import com.hjq.demo.chat.entity.User

/**
 * @author r
 * @date 2024/9/18
 * @description Brief description of the file content.
 */
interface ContactCallback {
    @JvmDefault
    fun getContactList(users: MutableList<User>) {
    }

    @JvmDefault
    fun getUser(userById: User?) {
    }

    @JvmDefault
    fun getStarredContactList(users: MutableList<User>) {

    }

    @JvmDefault
    fun getAllBlockedUserList(users: MutableList<User>) {

    }
}