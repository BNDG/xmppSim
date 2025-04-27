package com.hjq.demo.chat.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.hjq.demo.R;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.MessageDao;
import com.hjq.demo.chat.dao.UserDao;
import com.hjq.demo.chat.entity.ChatEvent;
import com.hjq.demo.chat.entity.ChatMessage;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.manager.MessageNotifycation;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.utils.Trace;
import com.hjq.http.lifecycle.ApplicationLifecycle;

import org.greenrobot.eventbus.EventBus;

import com.bndg.smack.constant.SmartConstants;

public class SmartMessageReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (PreferencesUtil.getInstance().getUser() == null) {
            return;
        }
        // 处理接收到的广播，从意图中获取额外数据
        String action = intent.getAction();
        if (SmartConstants.ACCOUNT_STATUS.equals(action)) {
            ChatEvent event = new ChatEvent(SmartConstants.ACCOUNT_STATUS);
            event.obj = intent.getStringExtra(SmartConstants.CURRENT_STATUS);
            EventBus.getDefault().post(event);
        } else if (Constant.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            String originId = intent.getStringExtra(Constant.MESSAGE_ORIGIN_ID);
            String localFilePath = intent.getStringExtra(Constant.MESSAGE_FILE_LOCAL);
            MessageDao.getInstance().getMessageByOriginId(ApplicationLifecycle.getInstance(),
                    originId, new MessageDao.MessageDaoCallback() {
                        @Override
                        public void getMessageByOriginId(ChatMessage chatMessage) {
                            if (chatMessage != null) {
                                chatMessage.setFileLocalPath(localFilePath);
                                MessageDao.getInstance().save(chatMessage);
                                ChatEvent event = new ChatEvent(ChatEvent.FILE_DOWNLOAD_COMPLETE);
                                Bundle bundle = new Bundle();
                                bundle.putString(Constant.MESSAGE_ORIGIN_ID, chatMessage.getOriginId());
                                bundle.putString(Constant.MESSAGE_FILE_LOCAL, localFilePath);
                                event.bundle = bundle;
                                EventBus.getDefault().post(event);
                            }
                        }
                    });
        } else if (Constant.ACTION_DOWNLOAD_PROGRESS.equals(action)) {
            String originId = intent.getStringExtra(Constant.MESSAGE_ORIGIN_ID);
            int progress = intent.getIntExtra(Constant.DOWNLOAD_PROGRESS, 0);
            //  通知界面进行刷新进度
            ChatEvent event = new ChatEvent(ChatEvent.FILE_DOWNLOAD_PROGRESS);
            Bundle bundle = new Bundle();
            bundle.putString(Constant.MESSAGE_ORIGIN_ID, originId);
            bundle.putInt(Constant.DOWNLOAD_PROGRESS, progress);
            event.bundle = bundle;
            EventBus.getDefault().post(event);
        }
    }

}
