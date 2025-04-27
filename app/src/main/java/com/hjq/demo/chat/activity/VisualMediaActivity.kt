package com.hjq.demo.chat.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.bndg.smack.enums.SmartContentType
import com.chad.library.adapter.base.listener.OnUpFetchListener
import com.hjq.demo.R
import com.hjq.demo.chat.adapter.VisualMediaAdapter
import com.hjq.demo.chat.cons.Constant
import com.hjq.demo.chat.dao.MessageDao
import com.hjq.demo.chat.entity.ChatMessage
import com.hjq.demo.chat.widget.LQRRecyclerView
import com.hjq.demo.ui.activity.ImagePreviewMsgActivity
import com.hjq.demo.utils.Trace

/**
 * @author r
 * @date 2024/12/25
 * @description Brief description of the file content.
 */
class VisualMediaActivity : ChatBaseActivity(), OnUpFetchListener {
    private val rvList: LQRRecyclerView by lazy { findViewById(R.id.rv_list) }
    private var adapter: VisualMediaAdapter = VisualMediaAdapter(mutableListOf())
    private var conversationId: String? = null
    var pageCount = 0
    override fun getLayoutId(): Int {
        return R.layout.data_list_activity2
    }

    override fun initView() {

    }

    override fun initData() {
        rvList.adapter = adapter
        adapter.setOnItemClickListener { _, imgView, position ->
            val item = adapter.getItem(position)
            if (item.messageType == SmartContentType.IMAGE) {
                // 生成转场动画的bundle对象
                val bundle = Bundle()
                // 如果有参数传递，可以这么添加
                bundle.putString(Constant.MESSAGE_FILE_LOCAL, item.fileLocalPath)
                bundle.putString(Constant.MESSAGE_ORIGIN_ID, item.originId)
                bundle.putString(Constant.MESSAGE_CONTENT, item.messageContent)
                bundle.putString(Constant.CONVERSATION_ID, conversationId)
                ImagePreviewMsgActivity.start(context, bundle)
            }
        }
        conversationId = intent.getStringExtra(Constant.CONVERSATION_ID)
        searchRecord()
        initLoadMore()
    }

    fun initLoadMore() {
        adapter.let {
            it.animationEnable = true
            it.upFetchModule.setOnUpFetchListener(this)
        }
    }

    override fun onUpFetch() {
        searchRecord(false)
    }
    private fun searchRecord(isRefresh: Boolean = true) {
        if (isRefresh) {
            pageCount = 0
        }
        MessageDao.getInstance().searchMediaMsgByConversationId(
            conversationId,
            pageCount,
            object : MessageDao.MessageDaoCallback {
                override fun getSearchMessages(chatMessages: MutableList<ChatMessage>?) {
                    Trace.d(">>>>", "d: $pageCount ${chatMessages?.size}")
                    chatMessages?.let {
                        if (isRefresh) {
                            adapter.setList(it)
                            rvList.scrollToPosition(it.size - 1)
                        } else {
                            adapter.addData(0, it)
                        }
                        if (chatMessages.size < MessageDao.SEARCH_PAGE_SIZE) {
                            Trace.d("loadMoreEnd")
                            adapter.upFetchModule.isUpFetchEnable = false
                            adapter.upFetchModule.isUpFetching = false
                        } else {
                            adapter.upFetchModule.isUpFetchEnable = true
                            pageCount++
                        }
                    }
                }
            })
    }

    override fun initListener() {
    }

    companion object {

        fun start(context: Context, extras: Bundle?) {
            val intent = Intent(context, VisualMediaActivity::class.java)
            extras?.let {
                intent.putExtras(it)
            }
            context.startActivity(intent)
        }
    }

}