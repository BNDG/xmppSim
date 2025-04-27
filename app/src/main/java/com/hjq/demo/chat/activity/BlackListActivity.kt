package com.hjq.demo.chat.activity

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bndg.smack.SmartIMClient
import com.bndg.smack.callback.IFriendListCallback
import com.bndg.smack.model.SmartUserInfo
import com.hjq.demo.R
import com.hjq.demo.chat.adapter.SmartFriendsAdapter
import com.hjq.demo.chat.cons.Constant
import com.hjq.demo.chat.entity.User

/**
 * @author r
 * @date 2024/10/21
 * @description Brief description of the file content.
 */
class BlackListActivity : ChatBaseActivity() {
    private lateinit var rvList: RecyclerView
    private var adapter: SmartFriendsAdapter? = null
    override fun getLayoutId(): Int {
        return R.layout.data_list_activity
    }

    override fun initView() {
        findViewById<TextView>(R.id.tv_title).text = getString(R.string.mobile_blocked_list)
        rvList = findViewById(R.id.rv_list)
    }

    override fun initData() {
        adapter = SmartFriendsAdapter(mutableListOf())
        rvList.adapter = adapter
        adapter?.setOnItemClickListener { _, _, position ->
            val item = adapter?.getItem(position)
            item?.let {
                if (Constant.USER_TYPE_WEIXIN == it.userType) {
                    UserInfoActivity.start(activity, it.userId)
                } else {
                    UserInfoActivity.start(activity, it.userId)
                }
            }
        }
        getBlockList()
    }

    private fun getBlockList() {
        SmartIMClient.getInstance().smartCommUserManager.getBlockList(object : IFriendListCallback {
            override fun onSuccess(entries: MutableSet<SmartUserInfo>?) {
                post{
                    entries?.let {
                        val list = mutableListOf<User>()
                        for (entry in it) {
                            val user = User()
                            user.userId = entry.userId
                            user.userNickName = entry.nickname
                            list.add(user)
                        }
                        adapter?.setList(list)
                    }
                }
            }

            override fun onFailed(code: Int, desc: String?) {
            }
        })
    }

    override fun initListener() {
    }
}