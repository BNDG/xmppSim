package com.hjq.demo.chat.service;

import android.content.Intent;
import android.os.IBinder;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bndg.smack.SmartIMClient;
import com.bndg.smack.callback.IFileCallback;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.entity.ChatFileBean;
import com.hjq.demo.chat.entity.ChatVideoBean;
import com.hjq.demo.utils.Trace;
import com.hjq.http.lifecycle.LifecycleService;
import com.hjq.http.listener.OnHttpListener;

public class UploadService extends LifecycleService implements OnHttpListener {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            // 获取要上传的视频文件路径
            String fileType = intent.getStringExtra(Constant.FILE_TYPE);
            Trace.d("onStartCommand: " + fileType);
            if (Constant.FILE_TYPE_VIDEO.equals(fileType)) {
                ChatVideoBean chatVideoBean = intent.getParcelableExtra(Constant.FILE_BEAN);
                uploadFile2(chatVideoBean, chatVideoBean.getThumbnailLocalPath(), true, true);
            } else {
                // 这里传递过来的是包名下的 沙盒文件路径 复制品
                String filePath = intent.getStringExtra(Constant.MESSAGE_FILE_LOCAL);
                ChatFileBean chatFileBean = intent.getParcelableExtra(Constant.FILE_BEAN);
                uploadFile2(chatFileBean, filePath, false, false);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 上传文件
     *
     * @param chatFileBean
     * @param filePath
     * @param isVideoFile  是否是视频缩略图
     */
    private void uploadFile2(ChatFileBean chatFileBean, String filePath, boolean isVideoFile, boolean isVideoThumbnail) {
            SmartIMClient.getInstance().getSmartCommMsgManager().sendFileByOriginalMeans(filePath, new IFileCallback() {
                @Override
                public void onSuccess(String fileUrl) {
                    if (!isVideoFile) {
                        processFileSuccess(chatFileBean, fileUrl);
                    } else {
                        if (isVideoThumbnail) {
                            ChatVideoBean chatVideoBean = (ChatVideoBean) chatFileBean;
                            chatVideoBean.setThumbnailUrl(fileUrl);
                            uploadFile2(chatVideoBean, chatVideoBean.getVideoLocalPath(), true, false);
                        } else {
                            ChatVideoBean chatVideoBean = (ChatVideoBean) chatFileBean;
                            chatVideoBean.setVideoUrl(fileUrl);
                            Intent intent = new Intent();
                            intent.setAction(Constant.UPLOAD_FILE_SUCCESS);
                            intent.putExtra(Constant.FILE_BEAN, chatVideoBean);
                            intent.putExtra(Constant.FILE_TYPE, Constant.FILE_TYPE_VIDEO);
                            LocalBroadcastManager.getInstance(UploadService.this).sendBroadcast(intent);
                        }
                    }
                }

                @Override
                public void onFailed(int code, String desc) {
                    processFiled(chatFileBean);
                }
            });
    }

    private void processFiled(ChatFileBean chatFileBean) {
        Intent intent = new Intent();
        intent.setAction(Constant.UPLOAD_FILE_FAILED);
        intent.putExtra(Constant.MESSAGE_ORIGIN_ID, chatFileBean.getOriginId());
        LocalBroadcastManager.getInstance(UploadService.this).sendBroadcast(intent);
    }

    private void processFileSuccess(ChatFileBean chatFileBean, String fileUrl) {
        chatFileBean.fileUrl = fileUrl;
        Intent intent = new Intent();
        intent.setAction(Constant.UPLOAD_FILE_SUCCESS);
        intent.putExtra(Constant.FILE_BEAN, chatFileBean);
        intent.putExtra(Constant.FILE_TYPE, chatFileBean.msgType);
        LocalBroadcastManager.getInstance(UploadService.this).sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onHttpSuccess(Object o) {

    }

    @Override
    public void onHttpFail(Throwable t) {

    }
}