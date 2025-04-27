package com.hjq.demo.chat.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.entity.ChatEvent;
import com.hjq.demo.chat.manager.MessageNotifycation;
import com.hjq.demo.utils.Trace;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.util.ArrayList;

import com.bndg.smack.entity.SmartMessage;
import com.bndg.smack.enums.SmartConversationType;

/**
 * @author r
 * @date 2024/9/20
 * @description 拒绝通话
 */

public class DeclineCallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {}
}