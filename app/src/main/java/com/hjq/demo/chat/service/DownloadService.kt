package com.hjq.demo.chat.service

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bndg.smack.enums.SmartContentType
import com.blankj.utilcode.util.FileUtils
import com.hjq.demo.R
import com.hjq.demo.chat.cons.Constant
import com.hjq.demo.other.ContentResolverUriStore
import com.hjq.demo.utils.Trace
import com.hjq.http.EasyHttp
import com.hjq.http.lifecycle.LifecycleService
import com.hjq.http.listener.OnDownloadListener
import com.hjq.http.model.FileContentResolver
import com.hjq.http.model.HttpMethod
import com.hjq.toast.ToastUtils
import java.io.File


/**
 * @author r
 * @date 2024/8/23
 * @description  下载服务
 */
class DownloadService : LifecycleService() {

    private var msgType: String? = SmartContentType.FILE
    var originId: String? = ""
    val downloadStatusMap = mutableMapOf<String, String>()

    companion object {
        private const val CHANNEL_ID = "DownloadChannel"
        private const val URL_KEY = "url"
        private const val NOTIFICATION_ID = 1

        fun startService(context: Context, url: String, msgOriginId: String, msgType: String) {
            val intent = Intent(context, DownloadService::class.java).apply {
                putExtra(URL_KEY, url)
                putExtra(Constant.MESSAGE_ORIGIN_ID, msgOriginId)
                putExtra(Constant.MESSAGE_TYPE, msgType)
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra(URL_KEY)
        originId = intent?.getStringExtra(Constant.MESSAGE_ORIGIN_ID)
        msgType = intent?.getStringExtra(Constant.MESSAGE_TYPE)
        originId?.let {
            if (downloadStatusMap.containsKey(it)) {
                val status = downloadStatusMap[it]
                if (status == Constant.STATUS_COMPLETED || status == Constant.STATUS_DOWNLOADING) {
                    ToastUtils.show(getString(R.string.downloading))
                    return START_NOT_STICKY
                }
            }
            // Update status to downloading
            downloadStatusMap[it] = Constant.STATUS_DOWNLOADING
            url?.let {
                startDownload(it)
            }
        }
        return START_NOT_STICKY
    }

    private fun startDownload(fileUrl: String) {
        val fileName: String = fileUrl.substring(fileUrl.lastIndexOf("/") + 1)
        // 如果是放到外部存储目录下则需要适配分区存储
        val localFile: File
        var outputUri: Uri? = null
        if (SmartContentType.FILE == msgType) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // 适配 Android 10 分区存储特性
                val values = ContentValues();
                // 设置显示的文件名
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
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
        } else {
            val dirFile = File(getExternalFilesDir(null), "download")
            FileUtils.createOrExistsDir(dirFile)
            localFile = File(dirFile, fileName)
        }
        EasyHttp.download(this)
            .method(HttpMethod.GET)
            .file(localFile)
            .url(fileUrl)
            .listener(object : OnDownloadListener {
                override fun onDownloadStart(file: File?) {
                }

                override fun onDownloadProgressChange(file: File?, progress: Int) {
                    if (SmartContentType.FILE == msgType) {
                        LocalBroadcastManager.getInstance(this@DownloadService)
                            .sendBroadcast(
                                Intent(Constant.ACTION_DOWNLOAD_PROGRESS).putExtra(
                                    Constant.MESSAGE_ORIGIN_ID, originId
                                )
                                    .putExtra(Constant.DOWNLOAD_PROGRESS, progress)
                            )
                    }
                }

                override fun onDownloadSuccess(file: File?) {
                    Trace.d(
                        ">>>>", "onComplete: uri >> ${file?.toString()}",
                        "uri ${outputUri?.toString()}"
                    )
                    // 应该只处理下载--消息状态由监听器处理
                    var localString = outputUri?.toString()
                    // 只有文件类型 才需要存到公共目录
                    if (SmartContentType.FILE == msgType) {
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                            localString = outputUri?.toString()
                        } else {
                            // 适配 Android 10 以下 用真实路径
                            localString = file?.absolutePath
                        }
                    } else {
                        localString = file?.absolutePath
                    }
                    file?.let {
                        LocalBroadcastManager.getInstance(this@DownloadService)
                            .sendBroadcast(
                                Intent(Constant.ACTION_DOWNLOAD_COMPLETE).putExtra(
                                    Constant.MESSAGE_ORIGIN_ID,
                                    originId
                                )
                                    .putExtra(Constant.MESSAGE_FILE_LOCAL, localString)
                            )
                        downloadStatusMap[originId!!] = Constant.STATUS_COMPLETED
                    }
                }

                override fun onDownloadFail(file: File?, throwable: Throwable?) {
                    downloadStatusMap[originId!!] = Constant.STATUS_FAILED
                }

                override fun onDownloadEnd(file: File?) {
                }
            })
            .start();
    }
}