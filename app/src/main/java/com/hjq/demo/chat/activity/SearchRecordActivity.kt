package com.hjq.demo.chat.activity

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.blankj.utilcode.util.KeyboardUtils
import com.chad.library.adapter.base.listener.OnLoadMoreListener
import com.hjq.demo.R
import com.hjq.demo.chat.adapter.SearchRecordAdapter
import com.hjq.demo.chat.cons.Constant
import com.hjq.demo.chat.dao.MessageDao
import com.hjq.demo.chat.entity.ChatMessage
import com.hjq.demo.chat.widget.LQRRecyclerView
import com.hjq.demo.utils.Trace

/**
 * @author r
 * @date 2024/9/5
 * @description 搜索聊天记录
 */
class SearchRecordActivity : ChatBaseActivity(), OnLoadMoreListener {
    private var userId: String? = null
    private lateinit var etSearch: EditText
    private lateinit var rvList: LQRRecyclerView
    private val flLayout: View? by lazy { findViewById(R.id.fl_layout) }
    private val tvImage: View? by lazy { findViewById(R.id.tv_image) }
    private var mAdapter: SearchRecordAdapter = SearchRecordAdapter()
    var conversationId: String? = null
    var conversationTitle: String? = null
    var conversationType: String? = null
    var pageCount = 0

    companion object {
        @JvmStatic
        fun start(
            context: Context,
            conversationType: String,
            conversationId: String,
            conversationTitle: String
        ) {
            val starter = Intent(context, SearchRecordActivity::class.java)
                .putExtra(Constant.CONVERSATION_ID, conversationId)
                .putExtra(Constant.CONVERSATION_TITLE, conversationTitle)
                .putExtra(Constant.CONVERSATION_TYPE, conversationType)
            context.startActivity(starter)
        }

        @JvmStatic
        fun start(
            context: Context,
            conversationType: String,
            conversationId: String,
            conversationTitle: String,
            userId: String
        ) {
            val starter = Intent(context, SearchRecordActivity::class.java)
                .putExtra(Constant.CONVERSATION_ID, conversationId)
                .putExtra(Constant.CONVERSATION_TITLE, conversationTitle)
                .putExtra(Constant.CONVERSATION_TYPE, conversationType)
                .putExtra(Constant.CONTACT_ID, userId)
            context.startActivity(starter)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.search_record_activity
    }

    override fun initView() {
        etSearch = findViewById(R.id.et_search)
        rvList = findViewById(R.id.rv_list)
        setOnClickListener(R.id.tv_cancel, R.id.tv_image)
    }

    override fun initData() {
        rvList.adapter = mAdapter
        mAdapter.setOnItemClickListener { helper, view, itemPosition ->
            val item: ChatMessage = mAdapter.getItem(itemPosition)
            val originId = item.originId
            // 查询item应该在第几页
            MessageDao.getInstance().queryMessagePosition(
                this@SearchRecordActivity,
                conversationId,
                originId,
                object : MessageDao.MessageDaoCallback {

                    override fun queryMessagePosition(ints: Array<out Int>?) {
                        ints?.let {
                            val count = ints[0]
                            val position = ints[1]
                            // 页码 先判断消息在哪一页
                            val page = position / MessageDao.PAGE_SIZE
                            // 计算行号在当前页
                            val startOfPage = page * 40
                            // 当前页上的消息数量
                            val messagesOnCurrentPage =
                                Math.min(40, count - startOfPage)
                            // 倒序行号
                            val lineNumDesc =
                                messagesOnCurrentPage - (position - startOfPage) - 1
                            // 正常行号
                            val lineNum = position - startOfPage + 1
                            Trace.d(
                                "position: $position",
                                "page: $page",
                                "lineNum: $lineNum",
                                "lineNumDesc: $lineNumDesc",
                                "totalMsg: $count"
                            )
                            ChatActivity.startFromSearch(
                                this@SearchRecordActivity,
                                conversationType,
                                conversationId,
                                conversationTitle,
                                page,
                                lineNumDesc
                            )
                        }
                    }
                })
        }
        conversationType = intent.getStringExtra(Constant.CONVERSATION_TYPE)
        conversationId = intent.getStringExtra(Constant.CONVERSATION_ID)
        conversationTitle = intent.getStringExtra(Constant.CONVERSATION_TITLE)
        userId = intent.getStringExtra(Constant.CONTACT_ID)
        if (userId != null) {
            findViewById<View>(R.id.searchLayout).visibility = View.GONE
            findViewById<TextView>(R.id.tv_title).text = conversationTitle
            searchChannel(isSearchByUserId = !userId.isNullOrEmpty())
        } else {
            // 是搜索聊天记录
            findViewById<View>(R.id.rl_title_user).visibility = View.GONE
            flLayout?.visibility = View.VISIBLE
        }
        etSearch.setImeOptions(EditorInfo.IME_ACTION_SEARCH)
        etSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId === EditorInfo.IME_ACTION_SEARCH) {
                KeyboardUtils.hideSoftInput(v)
                true
            } else {
                false
            }
        }

        etSearch.addTextChangedListener { text ->
            if (text.isNullOrEmpty()) {
                flLayout?.visibility = View.VISIBLE
                mAdapter.setList(mutableListOf())
            } else {
                flLayout?.visibility = View.GONE
                searchChannel(text.toString())
            }
        }
        initLoadMore()
    }

    fun initLoadMore() {
        mAdapter?.let {
            it.animationEnable = true
            it.loadMoreModule.setOnLoadMoreListener(this)
            it.loadMoreModule.isAutoLoadMore = true
            it.loadMoreModule.isEnableLoadMoreIfNotFullPage = false
        }
    }

    override fun onLoadMore() {
        searchChannel(etSearch.text.toString(), !userId.isNullOrEmpty(), false)
    }

    private fun searchChannel(
        text: String = "",
        isSearchByUserId: Boolean = false,
        isRefresh: Boolean = true
    ) {
        if (isRefresh) {
            pageCount = 0
        }
        conversationId?.let { it ->
            mAdapter.searchKey = text
            if (isSearchByUserId) {
                MessageDao.getInstance().searchMessageRecordByUserId(
                    conversationId,
                    userId,
                    pageCount,
                    object : MessageDao.MessageDaoCallback {
                        override fun getSearchMessages(chatMessages: MutableList<ChatMessage>?) {
                            chatMessages?.let {
                                if (isRefresh) {
                                    mAdapter.setList(it)
                                } else {
                                    mAdapter.addData(it)
                                }
                                mAdapter.loadMoreModule.isEnableLoadMore = true
                                if (chatMessages.size < MessageDao.SEARCH_PAGE_SIZE) {
                                    Trace.d("loadMoreEnd")
                                    mAdapter.loadMoreModule.loadMoreEnd(true)
                                } else {
                                    mAdapter.loadMoreModule.loadMoreComplete()
                                    pageCount++
                                }
                            }
                        }
                    })
            } else {
                MessageDao.getInstance().searchMessageRecord(
                    it,
                    text,
                    pageCount,
                    object : MessageDao.MessageDaoCallback {
                        override fun getSearchMessages(chatMessages: MutableList<ChatMessage>?) {
                            chatMessages?.let {
                                if (isRefresh) {
                                    mAdapter.setList(it)
                                } else {
                                    mAdapter.addData(it)
                                }
                                mAdapter.loadMoreModule.isEnableLoadMore = true
                                if (chatMessages.size < MessageDao.SEARCH_PAGE_SIZE) {
                                    mAdapter.loadMoreModule.loadMoreEnd(true)
                                } else {
                                    pageCount++
                                }
                            }
                        }
                    })
            }
        }
    }

    override fun initListener() {
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.tv_cancel -> finish()
            R.id.tv_image -> {
                VisualMediaActivity.start(this, intent.extras)
            }
        }
    }
}
