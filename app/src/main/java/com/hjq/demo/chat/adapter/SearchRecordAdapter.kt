package com.hjq.demo.chat.adapter

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hjq.demo.R
import com.hjq.demo.chat.entity.ChatMessage
import com.hjq.demo.chat.utils.AvatarGenerator
import com.hjq.demo.chat.utils.JimUtil
import com.hjq.demo.chat.utils.TimestampUtil
import java.util.regex.Matcher
import java.util.regex.Pattern


class SearchRecordAdapter(var searchKey: String? = null) :
    BaseQuickAdapter<ChatMessage, BaseViewHolder>(R.layout.item_search_record), LoadMoreModule {

    override fun convert(holder: BaseViewHolder, item: ChatMessage) {
        holder.setText(R.id.tv_name, item.fromUserName)
            .setVisible(R.id.tv_name, !searchKey.isNullOrEmpty())
            .setText(R.id.tv_time, TimestampUtil.getTimeStringAutoShort2(item.getTimestamp(), true))
            .setText(
                R.id.tv_content,
                if (searchKey.isNullOrEmpty()) {
                    JimUtil.getMessageContent(item.messageType, item.messageContent)
                } else {
                    getSpannableContent(item.messageContent)
                }
            )
        AvatarGenerator.loadAvatar(
            context,
            item.fromUserId,
            item.fromUserName,
            holder.getView(R.id.iv_avatar),
            false
        )
    }

    private fun getSpannableContent(messageContent: String?): SpannableString {
        val searchKey = searchKey ?: return SpannableString(messageContent ?: "")
        return if (searchKey.isEmpty() || messageContent.isNullOrEmpty()) {
            SpannableString("")
        } else {
            matcherSearchText(messageContent, searchKey)
        }
    }


    /**
     * 正则匹配 返回值是一个SpannableString 即经过变色处理的数据
     */
    private fun matcherSearchText(text: String, keyword: String): SpannableString {
        val spannableString = SpannableString(text)
        //条件 keyword
        // 创建一个用于匹配的 Pattern 和 Matcher
        val pattern: Pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(text)
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            //ForegroundColorSpan 需要new 不然也只能是部分变色
            spannableString.setSpan(
                ForegroundColorSpan(context.getColor(R.color.primary_chat_user)),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        //返回变色处理的结果
        return spannableString
    }


}
