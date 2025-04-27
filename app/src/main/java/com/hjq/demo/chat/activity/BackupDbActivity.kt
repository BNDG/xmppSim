package com.hjq.demo.chat.activity

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.TimeUtils
import com.hjq.base.BaseDialog
import com.hjq.demo.R
import com.hjq.demo.aop.Permissions
import com.hjq.demo.aop.SingleClick
import com.hjq.demo.chat.utils.FileUtil
import com.hjq.demo.other.ContentResolverUriStore
import com.hjq.demo.ui.dialog.MessageDialog
import com.hjq.http.model.FileContentResolver
import com.hjq.permissions.Permission
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat


/**
 * @author r
 * @date 2024/11/21
 * @description Brief description of the file content.
 */
class BackupDbActivity : ChatBaseActivity() {
    private val REQUEST_CODE_OPEN_DOCUMENT: Int = 1111

    override fun getLayoutId(): Int {
        return R.layout.activity_backup_db
    }

    override fun initView() {
        findViewById<TextView>(R.id.tv_title).text = getString(R.string.backup_chat_history)
    }

    override fun initData() {
        setOnClickListener(R.id.acb_backup_chat_db, R.id.acb_restore_chat_db)
    }

    override fun initListener() {
    }

    @SingleClick
    @Permissions(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)
    override fun onClick(view: View?) {
        view?.let {
            when (it.id) {
                R.id.acb_backup_chat_db -> {
                    //备份数据库
                    // 获取应用数据库文件路径
                    val databaseFile = getDatabasePath("ChatMsg.db")
                    if (!databaseFile.exists()) {
                        return
                    }
                    showDialog()
                    var outputUri: Uri? = null
                    val localFile: File
                    val nowString = TimeUtils.getNowString(SimpleDateFormat("MMddHHmm"))
                    val fileName = nowString + "ChatMsg.db"
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // 适配 Android 10 分区存储特性
                        val values = ContentValues().apply {
                            put(MediaStore.Downloads.DISPLAY_NAME, fileName)  // 设置文件名
                        }
                        // 生成一个新的 uri 路径
                        // 注意这里使用 ContentResolver 插入的时候都会生成新的 Uri
                        // 解决方式将 ContentValues 和 Uri 作为 key 和 value 进行持久化关联
                        outputUri = ContentResolverUriStore.insert(
                            this,
                            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                            values
                        )
                        localFile = FileContentResolver(getContentResolver(), outputUri, fileName)
                    } else {
                        localFile = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            fileName
                        )
                    }
                    if (localFile is FileContentResolver) {
                        val inputStream: InputStream = FileInputStream(databaseFile)
                        val openOutputStream = localFile.openOutputStream(false)
                        try {
                            inputStream.copyTo(openOutputStream)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        FileUtils.copy(databaseFile, localFile)
                    }
                    toast(R.string.success)
                    hideDialog()
                }

                R.id.acb_restore_chat_db -> {
                    showDialog()
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.setType("application/octet-stream")  // 允许选择二进制文件
                    startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT)
                    hideDialog()
                }

                else -> {}
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_OPEN_DOCUMENT && resultCode == RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                // 获取文件路径
                val fileName = FileUtil.getUrlFileName(this, uri)
                if (fileName.endsWith(".db")) {
                    // 是 .db 文件
                    // 可以执行后续操作
                    MessageDialog.Builder(this)
                        .setTitle(getString(R.string.tips))
                        .setMessage("从文件恢复记录将覆盖本地")
                        // 确定按钮文本
                        .setConfirm(getString(R.string.confirm))
                        // 设置 null 表示不显示取消按钮
                        .setCancel(getString(R.string.common_cancel))
                        .setListener(object : MessageDialog.OnListener {
                            override fun onConfirm(dialog: BaseDialog?) {
                                val databaseFile = getDatabasePath("ChatMsg.db")
                                // 检查数据库文件是否存在，如果存在则删除
                                if (databaseFile.exists()) {
                                    try {
                                        // 删除旧的数据库文件
                                        if (databaseFile.delete()) {
                                            toast("旧数据库文件已删除")
                                        } else {
                                            toast("删除旧数据库文件失败")
                                            return
                                        }
                                    } catch (e: Exception) {
                                        // 捕获删除文件时的异常
                                        e.printStackTrace()
                                        toast("删除旧数据库文件失败: ${e.message}")
                                        return
                                    }
                                }
                                FileUtils.delete(getDatabasePath("ChatMsg.db-shm"))
                                FileUtils.delete(getDatabasePath("ChatMsg.db-wal"))
                                // 检查输入流是否为 null
                                val inputStream = contentResolver.openInputStream(uri)
                                if (inputStream == null) {
                                    toast("无法打开数据库文件")
                                    return
                                }
                                try {
                                    // 复制新的数据库文件
                                    val outputStream = FileOutputStream(databaseFile)
                                    inputStream.use { input ->
                                        outputStream.use { output ->
                                            input.copyTo(output)
                                        }
                                    }
                                    // 成功后显示提示
                                    toast("恢复成功 杀进程重启app")
                                } catch (e: Exception) {
                                    // 捕获复制文件时的异常
                                    e.printStackTrace()
                                    toast("恢复数据库失败: ${e.message}")
                                } finally {
                                    // 确保流的资源被正确释放
                                    inputStream?.close()
                                }
                            }

                            override fun onCancel(dialog: BaseDialog?) {
                            }
                        }).show()
                } else {
                    // 不是 .db 文件，提示用户
                    toast("请选择一个 .db 文件")
                }
            }
        }
    }
}