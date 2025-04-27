package com.hjq.demo.chat.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author r
 * @date 2024/8/2
 * @description 群聊实体类
 */
@Entity(tableName = "chat_room")
class ChatRoomEntity {
    @PrimaryKey
    var chatRoomJid: String = ""
    var chatRoomName: String? = null
    var memberJidList: String? = null
    var memberNicknameList: String? = null
    var roomOwnerJid: String? = null

    // 显示群成员昵称
    var isShowNickname = false

    // 我在群里的昵称
    var selfDisplayName: String? = null
    var chatRoomSubject: String? = null
    var moderated: Boolean? = false
}
