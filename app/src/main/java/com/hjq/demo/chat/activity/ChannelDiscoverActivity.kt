package com.hjq.demo.chat.activity

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bndg.smack.SmartIMClient
import com.bndg.smack.callback.IChatRoomCallback
import com.bndg.smack.enums.SmartConversationType
import com.hjq.base.BaseDialog
import com.hjq.demo.R
import com.hjq.demo.chat.adapter.ChannelDiscoverAdapter
import com.hjq.demo.http.api.ChannelListApi
import com.hjq.demo.manager.ActivityManager
import com.hjq.demo.ui.dialog.MessageDialog
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallbackProxy
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener
import com.scwang.smart.refresh.layout.listener.OnRefreshListener

/**
 * @author r
 * @date 2024/9/19
 * @description 发现频道
 */
class ChannelDiscoverActivity : ChatBaseActivity(), OnLoadMoreListener, OnRefreshListener {
    private lateinit var rvList: RecyclerView
    private lateinit var srl_layout: SmartRefreshLayout
    private var adapter: ChannelDiscoverAdapter? = null
    var pageNumber = 1
    override fun getLayoutId(): Int {
        return R.layout.activity_channel_discover
    }

    override fun initView() {
        findViewById<TextView>(R.id.tv_title).text = getString(R.string.channel_discover)
        rvList = findViewById(R.id.rv_list)
        srl_layout = findViewById(R.id.srl_layout)
        srl_layout.setOnLoadMoreListener(this)
        srl_layout.setEnableLoadMore(false)
        srl_layout.setOnRefreshListener(this)
        srl_layout.autoRefresh()
        srl_layout.autoRefreshAnimationOnly()
    }

    override fun initData() {
        adapter = ChannelDiscoverAdapter(mutableListOf())
        rvList.adapter = adapter
        adapter?.setOnItemClickListener { _, _, position ->
            val item = adapter?.getItem(position)
            if (item != null) {
                if (!item.is_open) {
                    toast("该群已关闭")
                    return@setOnItemClickListener
                }
                if (SmartIMClient.getInstance().smartCommChatRoomManager.isJoined(item.address)) {
                    toast("已加入该群")
                    return@setOnItemClickListener
                }
                MessageDialog.Builder(this)
                    .setTitle(getString(R.string.tips))
                    .setMessage(getString(R.string.join_group_tips, item.name))
                    // 确定按钮文本
                    .setConfirm(getString(R.string.join))
                    // 设置 null 表示不显示取消按钮
                    .setCancel(getString(R.string.common_cancel))
                    .setListener(object : MessageDialog.OnListener {
                        override fun onConfirm(dialog: BaseDialog?) {
                            showDialog()
                            SmartIMClient.getInstance().smartCommChatRoomManager.realJoinRoom(
                                item.address,
                                object : IChatRoomCallback {
                                    override fun joinRoomSuccess(goupId: String?) {
                                        hideDialog()
                                        ChatActivity.start(
                                            this@ChannelDiscoverActivity,
                                            SmartConversationType.GROUP.name,
                                            item.address,
                                            item.name
                                        )
                                        ActivityManager.getInstance().finishAllActivities(MainActivity::class.java, ChatActivity::class.java)
                                    }

                                    override fun joinRoomFailed(
                                        code: Int,
                                        groupId: String?,
                                        desc: String?
                                    ) {
                                        hideDialog()
                                        toast(desc)
                                    }
                                })
                        }

                        override fun onCancel(dialog: BaseDialog?) {
                        }
                    }).show()
            }
        }
    }

    override fun initListener() {
    }

    override fun onLoadMore(refreshLayout: RefreshLayout) {
        getData(false)
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        getData(true)
    }

    private fun getData(isRefresh: Boolean) {
        if (isRefresh) {
            pageNumber = 1
        }
        EasyHttp.get(this)
            .api(
                ChannelListApi()
                    .setP(pageNumber.toString())
            )
            .request(object : HttpCallbackProxy<ChannelListApi.Response>(this) {
                override fun onHttpSuccess(data: ChannelListApi.Response) {
                    val sortedWith = data.items.sortedWith(compareBy { !it.isChinese() })
                    if (isRefresh) {
                        srl_layout.finishRefresh()
                        adapter?.setList(sortedWith)
                    } else {
                        srl_layout.finishLoadMore()
                        adapter?.addData(sortedWith)
                    }
                    if (pageNumber >= data.npages) {
                        srl_layout.finishLoadMoreWithNoMoreData()
                    } else {
                        srl_layout.setEnableLoadMore(true)
                    }
                    pageNumber++
                }
            })
    }
}