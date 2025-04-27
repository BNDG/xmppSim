package com.hjq.demo.chat.activity;

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hbzhou.open.flowcamera.CustomCameraView
import com.hbzhou.open.flowcamera.FlowCameraView
import com.hbzhou.open.flowcamera.listener.ClickListener
import com.hbzhou.open.flowcamera.listener.FlowCameraListener
import com.hbzhou.open.flowcamera.util.LogUtil
import com.hjq.demo.R
import com.hjq.demo.chat.cons.Constant
import java.io.File

/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2018/10/18
 * desc   : 视频录制
 */
class RecordVideoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record_video_activity)
        val flowCamera = findViewById<FlowCameraView>(R.id.flowCamera)
        // 绑定生命周期 您就不用关心Camera的开启和关闭了 不绑定无法预览
        flowCamera.setBindToLifecycle(this)
        // 设置白平衡模式
//        flowCamera.setWhiteBalance(WhiteBalance.AUTO)
        // 设置只支持单独拍照拍视频还是都支持
        // BUTTON_STATE_ONLY_CAPTURE  BUTTON_STATE_ONLY_RECORDER  BUTTON_STATE_BOTH
        flowCamera.setCaptureMode(CustomCameraView.BUTTON_STATE_BOTH)
        // 开启HDR
//        flowCamera.setHdrEnable(Hdr.ON)
        // 设置最大可拍摄小视频时长 S
        flowCamera.setRecordVideoMaxTime(10)
        // 设置拍照或拍视频回调监听
        flowCamera.setFlowCameraListener(object : FlowCameraListener {
            // 录制完成视频文件返回
            override fun recordSuccess(file: File) {
                // 拍照或者视频的文件
                setResult(
                    RESULT_OK,
                    Intent().putExtra(Constant.MESSAGE_FILE_LOCAL, file.absolutePath)
                        .putExtra(Constant.FILE_TYPE, Constant.FILE_TYPE_VIDEO)
                )
                finish()
            }

            // 操作拍照或录视频出错
            override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                LogUtil.e(
                    videoCaptureError.toString().plus("----").plus(message).plus("---").plus(
                        cause.toString()
                    )
                )
            }

            // 拍照返回
            override fun captureSuccess(file: File) {
                setResult(
                    RESULT_OK,
                    Intent().putExtra(Constant.MESSAGE_FILE_LOCAL, file.absolutePath)
                        .putExtra(Constant.FILE_TYPE, Constant.FILE_TYPE_IMAGE)
                )
                finish()
            }
        })
        //左边按钮点击事件
        flowCamera.setLeftClickListener(ClickListener { super.onBackPressed() })
    }
}