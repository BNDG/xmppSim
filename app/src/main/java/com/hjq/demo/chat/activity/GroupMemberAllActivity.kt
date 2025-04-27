package com.hjq.demo.chat.activity

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnLoadMoreListener
import com.hjq.demo.R
import com.hjq.demo.chat.adapter.GroupMemberAdapter
import com.hjq.demo.chat.cons.Constant
import com.hjq.demo.chat.dao.DBManager
import com.hjq.demo.chat.dao.MessageDao
import com.hjq.demo.chat.dao.UserDao
import com.hjq.demo.chat.entity.ChatEvent
import com.hjq.demo.chat.entity.GroupMember
import com.hjq.demo.chat.entity.User
import com.hjq.demo.chat.listener.ContactCallback
import com.hjq.demo.chat.utils.PreferencesUtil
import com.rxjava.rxlife.life
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * @author r
 * @date 2024/9/9
 * @description 所有群成员
 */
class GroupMemberAllActivity : ChatBaseActivity(), OnLoadMoreListener {

    companion object {
        @JvmStatic
        fun start(context: Context, groupId: String) {
            val starter = Intent(context, GroupMemberAllActivity::class.java)
                .putExtra(Constant.GROUP_ID, groupId)
            context.startActivity(starter)
        }
    }

    private lateinit var allGroupMembers: List<GroupMember>
    private var pageNum: Int = 0
    private var mAdapter: GroupMemberAdapter? = null
    private var groupId: String = ""
    override fun getLayoutId(): Int {
        return R.layout.group_member_all_activity;
    }

    override fun initView() {
        findViewById<TextView>(R.id.tv_title).text = getString(R.string.group_chats)
        val rv_grid = findViewById<RecyclerView>(R.id.rv_grid)
        mAdapter = GroupMemberAdapter(mutableListOf())
        rv_grid.layoutManager = GridLayoutManager(this, 5)
        rv_grid.adapter = mAdapter
        mAdapter!!.setOnItemClickListener { baseQuickAdapter: BaseQuickAdapter<*, *>, view: View?, i: Int ->
            // 个人页面
            val item = baseQuickAdapter.getItem(i) as GroupMember?
            if (item != null) {
                val memberRealUserId = item.memberRealUserId
                // 如果有realUserId 进入个人界面
                if (!TextUtils.isEmpty(memberRealUserId)) {
                    if (memberRealUserId == myUserInfo.userId) {
                        // 是自己
                        val intent = Intent(
                            this@GroupMemberAllActivity,
                            UserInfoMyActivity::class.java
                        )
                        startActivity(intent)
                    } else {
                        UserDao.getInstance().getUserById(
                            this@GroupMemberAllActivity,
                            memberRealUserId,
                            object : ContactCallback {
                                override fun getUser(userById: User?) {
                                    if (userById != null && userById.isFriend()) {
                                        UserInfoActivity.start(
                                            this@GroupMemberAllActivity,
                                            memberRealUserId
                                        )
                                    } else {
                                        val userById2 = User()
                                        userById2.belongAccount =
                                            PreferencesUtil.getInstance().userId
                                        userById2.userNickName = item.memberName
                                        userById2.userId = memberRealUserId
                                        UserDao.getInstance().saveOrUpdateContact(userById2)
                                        UserInfoActivity.start(
                                            this@GroupMemberAllActivity,
                                            memberRealUserId,
                                            Constant.CONTACTS_FROM_WX_ID
                                        )
                                    }
                                }
                            })

                    }
                }
            }
        }
    }

    override fun initData() {
        groupId = intent.getStringExtra(Constant.GROUP_ID).toString()
        initLoadMore()
        request(true)
        EventBus.getDefault().register(this)
    }

    fun initLoadMore() {
        mAdapter?.let {
            it.animationEnable = true
            it.loadMoreModule.setOnLoadMoreListener(this)
            it.loadMoreModule.isAutoLoadMore = true
            it.loadMoreModule.isEnableLoadMore = true
            it.loadMoreModule.isEnableLoadMoreIfNotFullPage = false
        }
    }

    override fun initListener() {
    }

    override fun onLoadMore() {
        request(false)
    }

    private fun request(isRefresh: Boolean) {
        if (isRefresh) {
            pageNum = 0;
            DBManager.getInstance(this)
                .getGroupMemberByGroupId(groupId)
                ?.life(this)
                ?.subscribe { groupMembers: List<GroupMember> ->
                    allGroupMembers = groupMembers
                    realGetData(isRefresh)
                }
        } else {
            realGetData(isRefresh)
        }

    }

    private fun realGetData(isRefresh: Boolean) {
        val subGroupMembers =
            allGroupMembers.subList(
                MessageDao.PAGE_SIZE * pageNum,
                Math.min(
                    MessageDao.PAGE_SIZE * (pageNum + 1),
                    allGroupMembers.size
                )
            )
        if (isRefresh) {
            mAdapter?.setList(subGroupMembers)
        } else {
            mAdapter?.addData(subGroupMembers)
        }
        if (subGroupMembers.size < MessageDao.PAGE_SIZE) {
            mAdapter?.loadMoreModule?.isEnableLoadMore = false
            mAdapter?.loadMoreModule?.loadMoreEnd()
        } else {
            mAdapter?.loadMoreModule?.isEnableLoadMore = true
            mAdapter?.loadMoreModule?.loadMoreComplete()
        }
        pageNum++
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ChatEvent) {
        if (ChatEvent.REFRESH_GROUP_MEMBER_AVATAR == event.what) {
            val memberUserId = event.obj.toString()
            mAdapter?.let {
                for (i in it.data.indices) {
                    val groupMember: GroupMember = it.data[i]
                    if (groupMember.memberOriginId == memberUserId) {
                        it.notifyItemChanged(i)
                        break
                    }
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}