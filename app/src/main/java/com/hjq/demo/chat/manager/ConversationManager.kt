package com.hjq.demo.chat.manager

import com.hjq.demo.chat.dao.DBManager
import com.hjq.demo.chat.entity.ConversationInfo
import com.hjq.demo.chat.utils.PreferencesUtil
import com.hjq.demo.manager.ActivityManager
import io.reactivex.rxjava3.functions.Consumer

/**
 * @author r
 * @date 2024/9/6
 * @description Brief description of the file content.
 */
object ConversationManager {
    private val conversationMap: MutableMap<String, ConversationInfo> = mutableMapOf()

    // 根据conversationId查找
    fun getConversationInfo(conversationId: String, callback: ResultCallback) {
        val conversationInfo = conversationMap[conversationId]
        if (conversationInfo == null) {
            val subscribe = DBManager.getInstance(ActivityManager.getInstance().application)
                .getConversationByConversationId(
                    PreferencesUtil.getInstance().userId,
                    conversationId
                )
                ?.subscribe(Consumer {
                    if (it.isNotEmpty()) {
                        addConversationInfo(it[0])
                        callback.onResult(it[0])
                    }
                })
        } else {
            callback.onResult(conversationInfo)
        }
    }

    private fun addConversationInfo(conversationInfo: ConversationInfo) {
        conversationMap[conversationInfo.conversationId] = conversationInfo
    }
}

interface ResultCallback {
    fun onResult(conversationInfo: ConversationInfo?)
}
