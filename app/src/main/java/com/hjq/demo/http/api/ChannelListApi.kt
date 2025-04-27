package com.hjq.demo.http.api

import com.hjq.demo.chat.entity.ChannelItem
import com.hjq.http.config.IRequestApi
import com.hjq.http.config.IRequestServer

/**
 * @author r
 * @date 2024/9/19
 * @description Brief description of the file content.
 */
class ChannelListApi : IRequestApi, IRequestServer {
    data class Response(
        val items: List<ChannelItem>,
        val npages: Int,
        val page: Int,
        val total: Int
    )

    override fun getApi(): String {
        return "api/1.0/rooms/unsafe"
    }

    private var p: String = "1"
    fun setP(p: String): ChannelListApi {
        this.p = p
        return this
    }

    override fun getHost(): String {
        return "https://search.jabber.network/"
    }


}