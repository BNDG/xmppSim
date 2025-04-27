package com.hjq.demo.chat.activity

import android.content.Context
import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Patterns
import android.view.View
import android.widget.TextView
import com.bndg.smack.SmartIMClient
import com.bndg.smack.callback.IChatRoomCallback
import com.bndg.smack.model.SmartGroupInfo
import com.hjq.demo.R
import com.hjq.demo.aop.Log
import com.hjq.demo.chat.cons.Constant
import com.hjq.demo.chat.dao.DBManager.Companion.getInstance
import com.hjq.demo.chat.entity.ChatRoomEntity
import com.hjq.demo.chat.listener.CustomClickableSpan
import com.hjq.demo.utils.CheckUtil
import com.hjq.demo.utils.Trace
import com.rxjava.rxlife.life
import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2018/10/18
 * desc   : 群资料
 */
class GroupInfoActivity : ChatBaseActivity() {
    private var tv_subject: TextView? = null
    private var tv_other: TextView? = null
    private var groupId: String? = null
    override fun getLayoutId(): Int {
        return R.layout.group_info_activity
    }

    override fun initView() {
        val tv_title = findViewById<TextView>(R.id.tv_title)
        tv_title.text = "群信息"
        tv_subject = findViewById(R.id.tv_subject)
        tv_other = findViewById(R.id.tv_other)
    }

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

    override fun initData() {
        groupId = intent.getStringExtra(Constant.GROUP_ID)
        SmartIMClient.getInstance().getSmartCommChatRoomManager().getRoomInfo(
            groupId,
            object : IChatRoomCallback {
                override fun getGroupInfo(roomInfo: SmartGroupInfo) {
                    post {
                        val sb = StringBuilder()
                        sb.append("groupid -> $groupId")
                            .append("\r\n")
                            .append("room name -> ")
                            .append(roomInfo.groupName)
                            .append("\r\n")
                        tv_other!!.text = sb.toString()
                    }
                }

                override fun getGroupInfoFailed() {}
            }
        )

        getInstance(this)
            .getChatRoomByRoomId(groupId!!)
            ?.life(this)
            ?.subscribe { chatRoomEntities: List<ChatRoomEntity> ->
                if (!chatRoomEntities.isEmpty()) {
                    val chatRoomEntity = chatRoomEntities[0]

                    val text = CheckUtil.getNotNullString(chatRoomEntity.chatRoomSubject)
                    val spannableString = SpannableString(text)

// 正则表达式匹配所有链接
                    val pattern = Patterns.WEB_URL
                    val matcher = pattern.matcher(text)

                    while (matcher.find()) {
                        val start = matcher.start()
                        val end = matcher.end()
                        //最主要的一点

                        val group = matcher.group()

                        //最主要的一点
                        val myURLSpan =
                            CustomClickableSpan(
                                group,
                                object : CustomClickableSpan.OnLinkClickListener {
                                    override fun onLinkClick(view: View?) {
                                        Trace.d("onLinkClick: $group")
                                    }
                                })
                        spannableString.setSpan(
                            myURLSpan,
                            start,
                            end,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    tv_subject!!.text = spannableString
                }
            }

    }


    override fun initListener() {}

    companion object {
        @JvmStatic
        @Log
        fun start(context: Context, groupId: String?) {
            val intent = Intent(context, GroupInfoActivity::class.java)
            intent.putExtra(Constant.GROUP_ID, groupId)
            context.startActivity(intent)
        }
    }
}