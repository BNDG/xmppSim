package com.hjq.demo.chat.adapter

import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hjq.demo.R
import com.hjq.demo.chat.entity.GroupMember
import com.hjq.demo.chat.utils.AvatarGenerator

/**
 * @author r
 * @date 2024/11/11
 * @description Brief description of the file content.
 */
class GroupAdministratorAdapter(data: MutableList<GroupMember>) :
    BaseQuickAdapter<GroupMember, BaseViewHolder>(
        R.layout.item_administrator, data
    ) {

    override fun convert(holder: BaseViewHolder, item: GroupMember) {
        holder.setText(R.id.tv_name, item.memberName)
        AvatarGenerator.loadAvatar(
            context,
            item.memberOriginId,
            item.getMemberName(),
            holder.getView<ImageView>(R.id.sdv_avatar),
            false
        )

    }
}