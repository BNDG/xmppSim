package com.hjq.demo.chat.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hjq.demo.R
import com.hjq.demo.chat.entity.ChannelItem
import com.hjq.demo.chat.utils.AvatarGenerator

/**
 * @author r
 * @date 2024/9/19
 * @description Brief description of the file content.
 */
class ChannelDiscoverAdapter(data: MutableList<ChannelItem>) :
    BaseQuickAdapter<ChannelItem, BaseViewHolder>(
        R.layout.item_channel, data
    ) {
    override fun convert(holder: BaseViewHolder, item: ChannelItem) {
        holder.setText(R.id.name, item.name)
            .setText(R.id.nusers, buildString {
                append("在线人数 (")
                append(item.nusers.toString())
                append(")")
            })
            .setText(R.id.description, item.description)
            .setText(R.id.language, item.language)
            .setText(R.id.room_address, item.address)
            .setGone(R.id.description, item.description.isNullOrEmpty())

        AvatarGenerator.loadAvatar(
            context,
            item.address,
            item.name,
            holder.getView(R.id.avatar),
            true
        )
    }
}