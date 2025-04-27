package com.hjq.demo.chat.entity

/**
 * @author r
 * @date 2024/9/19
 * @description Brief description of the file content.
 */
data class ChannelItem(
    val address: String,
    val anonymity_mode: String,
    val description: String?,
    val is_open: Boolean,
    val language: String,
    val name: String,
    val nusers: Int
) {
    fun isChinese(): Boolean {
        return name.any { it in '\u4e00'..'\u9fa5' }
    }
}
