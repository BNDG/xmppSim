package com.hjq.demo.chat.activity

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bndg.smack.SmartIMClient
import com.bndg.smack.callback.IChatRoomCallback
import com.bndg.smack.model.SmartUserInfo
import com.hjq.base.BaseDialog
import com.hjq.demo.R
import com.hjq.demo.chat.adapter.GroupAdministratorAdapter
import com.hjq.demo.chat.cons.Constant
import com.hjq.demo.chat.dao.DBManager
import com.hjq.demo.chat.entity.GroupMember
import com.hjq.demo.chat.widget.LQRRecyclerView
import com.hjq.demo.ui.dialog.MessageDialog
import com.rxjava.rxlife.life

/**
 * @author r
 * @date 2024/11/11
 * @description Brief description of the file content.
 */
class ChatGroupAdministratorsActivity : ChatBaseActivity() {
    private lateinit var rvList: LQRRecyclerView
    private var mAdapter: GroupAdministratorAdapter = GroupAdministratorAdapter(mutableListOf())
    private var adminRealJids: StringBuilder = StringBuilder()
    private var adminMembers: MutableList<GroupMember> = mutableListOf()
    private lateinit var allGroupMembers: List<GroupMember>
    private var groupId: String? = ""
    override fun getLayoutId(): Int {
        return R.layout.data_list_activity
    }

    companion object {
        @JvmStatic
        fun start(context: Context, conversationId: String) {
            val starter = Intent(context, ChatGroupAdministratorsActivity::class.java)
                .putExtra(Constant.CONVERSATION_ID, conversationId)
            context.startActivity(starter)
        }
    }

    override fun initView() {
        findViewById<TextView>(R.id.tv_title).text = getString(R.string.group_administrator)
        rvList = findViewById(R.id.rv_list)
    }

    override fun initData() {
        rvList.adapter = mAdapter
        groupId = intent.getStringExtra(Constant.CONVERSATION_ID)
        val footView = layoutInflater.inflate(R.layout.item_administrator, rvList, false)
        footView.findViewById<ImageView>(R.id.sdv_avatar)
            .setImageResource(R.drawable.baseline_add_circle_outline_24)
        footView.findViewById<TextView>(R.id.tv_name).text = getString(R.string.add_administrator)
        footView.findViewById<View>(R.id.tv_remove).visibility = View.INVISIBLE
        footView.setOnClickListener {
            addAdministrator(groupId)
        }
        mAdapter.addFooterView(footView, 0)
        getAdmins(groupId!!)
        getAllMembers(groupId!!)
        mAdapter.apply {
            addChildClickViewIds(R.id.tv_remove)
            setOnItemChildClickListener { _, _, position ->
                val item = mAdapter.getItem(position)
                MessageDialog.Builder(this@ChatGroupAdministratorsActivity)
                    .setTitle(getString(R.string.tips))
                    .setMessage(getString(R.string.remove_group_administrator, item.memberName))
                    // 确定按钮文本
                    .setConfirm(getString(R.string.remove))
                    // 设置 null 表示不显示取消按钮
                    .setCancel(getString(R.string.common_cancel))
                    .setListener(object : MessageDialog.OnListener {
                        override fun onConfirm(dialog: BaseDialog?) {
                            showDialog()
                            SmartIMClient.getInstance().smartCommChatRoomManager.revokeAdmin(
                                item.memberRealUserId,
                                groupId,
                                object : IChatRoomCallback {
                                    override fun revokeAdminSuccess() {
                                        hideDialog()
                                        toast(getString(R.string.success))
                                        getAdmins(groupId!!)
                                    }
                                })
                        }

                        override fun onCancel(dialog: BaseDialog?) {
                        }
                    }).show()
            }
        }
    }

    private fun getAllMembers(groupId: String) {
        DBManager.getInstance(this)
            .getGroupMemberByGroupId(groupId)
            ?.life(this)
            ?.subscribe { groupMembers: List<GroupMember> ->
                allGroupMembers = groupMembers
            }
    }

    /**
     * 获取群管理员
     */
    private fun getAdmins(groupId: String) {
        SmartIMClient.getInstance().smartCommChatRoomManager.getGroupAdministrators(
            groupId,
            object : IChatRoomCallback {
                override fun getAdmins(smartUserInfos: MutableList<SmartUserInfo>?) {
                    for (item in smartUserInfos!!) {
                        val groupMember = GroupMember()
                        groupMember.groupId = groupId
                        groupMember.memberRealUserId = item.userId
                        if (adminRealJids.length > 1) {
                            adminRealJids.append(",")
                        }
                        for (member in allGroupMembers) {
                            if (member.memberRealUserId == item.userId) {
                                groupMember.memberName = member.memberName
                                break
                            }
                        }
                        groupMember.memberName =
                            groupMember.memberName.takeIf { it?.isNotEmpty() == true }
                                ?: item.userId
                        adminRealJids.append(item.userId)
                        adminMembers.add(groupMember)
                    }
                    mAdapter.setList(adminMembers)
                }
            })
    }

    private fun addAdministrator(groupId: String?) {
        // 传递id list过去还有昵称
        GroupMemberListActivity.start(
            this@ChatGroupAdministratorsActivity, groupId, adminRealJids.toString(),
            object : GroupMemberListActivity.OnRemoveListener {
                override fun checkedAccounts(realJidList: ArrayList<String>?) {
                    if (!realJidList.isNullOrEmpty()) {
                        SmartIMClient.getInstance().smartCommChatRoomManager.grantAdmin(
                            realJidList,
                            groupId,
                            object : IChatRoomCallback {
                                override fun grantAdminSuccess() {
                                    toast(getString(R.string.success))
                                    getAdmins(groupId!!)
                                }
                            })
                    }
                }
            })
    }

    override fun initListener() {
    }
}