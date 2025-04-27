package com.hjq.demo.chat.activity

import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.bndg.smack.SmartIMClient
import com.bndg.smack.callback.IChatRoomCallback
import com.bndg.smack.model.SmartGroupInfo
import com.hjq.base.BaseDialog
import com.hjq.demo.R
import com.hjq.demo.ui.dialog.InputDialog
import com.hjq.demo.ui.dialog.MessageDialog

/**
 * @author r
 * @date 2024/8/29
 * @description
 */
class JoinGroupActivity : ChatBaseActivity() {
    private lateinit var etGroupName: EditText
    private lateinit var btnQueryGroup: View
    override fun getLayoutId(): Int {
        return R.layout.activity_join_group
    }

    override fun initView() {
        findViewById<TextView>(R.id.tv_title).text = getString(R.string.join_group)
        etGroupName = findViewById(R.id.et_group_name)
        btnQueryGroup = findViewById(R.id.btn_query_group)
        btnQueryGroup.setOnClickListener {
            val groupName = etGroupName.text.toString()
            if (groupName.isEmpty()) {
                return@setOnClickListener
            }
            if (!groupName.contains("@")) {
                toast("群名应为group@example.com格式")
                return@setOnClickListener
            }
            SmartIMClient.getInstance().smartCommChatRoomManager.getRoomInfo(
                groupName,
                object : IChatRoomCallback {
                    override fun getGroupInfo(roomInfo: SmartGroupInfo?) {
                        roomInfo?.let {
                            MessageDialog.Builder(this@JoinGroupActivity)
                                .setTitle(getString(R.string.tips))
                                .setMessage(getString(R.string.join_group_tips, roomInfo.groupName))
                                // 确定按钮文本
                                .setConfirm(getString(R.string.join))
                                // 设置 null 表示不显示取消按钮
                                .setCancel(getString(R.string.common_cancel))
                                .setListener(object : MessageDialog.OnListener {
                                    override fun onConfirm(dialog: BaseDialog?) {
                                        if (roomInfo.isPasswordProtected) {
                                            InputDialog.Builder(this@JoinGroupActivity)
                                                .setTitle(getString(R.string.require_password))
                                                .setConfirm(getString(R.string.common_confirm))
                                                .setCancel(getString(R.string.common_cancel))
                                                .setListener(object : InputDialog.OnListener {
                                                    override fun onConfirm(
                                                        dialog: BaseDialog?,
                                                        content: String?
                                                    ) {
                                                        content?.let { it1 ->
                                                            joinGroup(
                                                                roomInfo.groupID,
                                                                it1
                                                            )
                                                        }
                                                    }
                                                })
                                                .show();
                                        } else {
                                            showDialog()
                                            joinGroup(roomInfo.groupID)
                                        }
                                    }

                                    override fun onCancel(dialog: BaseDialog?) {
                                    }
                                }).show()
                        }
                    }

                    override fun getGroupInfoFailed() {
                        toast(getString(R.string.not_be_found_group))
                    }
                })
        }
    }

    fun joinGroup(groupId: String, pwd: String = "") {
        SmartIMClient.getInstance().smartCommChatRoomManager.realJoinRoomWithPWD(
            groupId, pwd,
            object : IChatRoomCallback {
                override fun joinRoomSuccess(groupId: String) {
                    hideDialog()
                    finish()
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

    override fun initData() {
    }

    override fun initListener() {
    }
}