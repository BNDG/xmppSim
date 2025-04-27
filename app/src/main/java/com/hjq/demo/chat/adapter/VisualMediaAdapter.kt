package com.hjq.demo.chat.adapter

import android.net.Uri
import android.text.TextUtils
import com.bndg.smack.enums.SmartContentType
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.UpFetchModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hjq.demo.R
import com.hjq.demo.chat.entity.ChatMessage
import java.io.File

/**
 * @author r
 * @date 2024/12/25
 * @description Brief description of the file content.
 */
class VisualMediaAdapter(data: MutableList<ChatMessage>) :
    BaseQuickAdapter<ChatMessage, BaseViewHolder>(R.layout.item_visual_media, data),
    UpFetchModule {
    override fun convert(holder: BaseViewHolder, chatMessage: ChatMessage) {
        val width = (holder.itemView.context.resources.displayMetrics.widthPixels / 4)
        val layoutParams = holder.itemView.layoutParams
        layoutParams.width = width
        layoutParams.height = width // 强制宽高相等
        holder.itemView.layoutParams = layoutParams
        if (chatMessage.messageType == SmartContentType.IMAGE) {
            val uri = if (!TextUtils.isEmpty(chatMessage.getFileLocalPath())) {
                // 本地读
                Uri.fromFile(File(chatMessage.getFileLocalPath()))
            } else {
                // 网络获取
                Uri.parse(chatMessage.getMessageContent())
            }

            // centerCrop 图片变形
            Glide.with(context)
                .load(uri)
                .centerCrop()
                .into(holder.getView(R.id.sdv_image_content))
        }
    }
}