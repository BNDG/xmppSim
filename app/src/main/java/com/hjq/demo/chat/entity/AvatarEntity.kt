package com.hjq.demo.chat.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author r
 * @date 2024/9/3
 * @description Brief description of the file content.
 */
@Entity(tableName = "avatars")
class AvatarEntity {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    var userId: String = ""
    var avatarHash: String = ""
    var avatarLocalPath: String? = null
    var photoHash: String? = null

}