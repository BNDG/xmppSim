package com.hjq.demo.chat.listener;

import android.text.style.ClickableSpan;
import android.view.View;

import com.hjq.demo.aop.SingleClick;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.utils.AutoStartPermissionManager;
import com.hjq.demo.utils.Trace;

// 或者使用不同的 ClickableSpan 子类
public class CustomClickableSpan extends ClickableSpan {
    private String tag;
    private OnLinkClickListener mListener;

    public CustomClickableSpan(String tag) {
        this.tag = tag;
    }
    public CustomClickableSpan(String tag, OnLinkClickListener listener) {
        this.tag = tag;
        this.mListener = listener;
    }

    @SingleClick
    @Override
    public void onClick(View widget) {
        if(mListener != null) {
            mListener.onLinkClick(widget);
        }
        // 处理点击事件，区分不同的 ClickableSpan
        if(Constant.GO_AUTO_START.equals(tag)) {
            AutoStartPermissionManager.startToAutoStartSetting(ActivityManager.getInstance().getTopActivity());
        } else if(Constant.GO_POWER_MANAGER.equals(tag)) {
            AutoStartPermissionManager.startBatteryStrategyActivity(ActivityManager.getInstance().getTopActivity());
        }
    }

    public interface OnLinkClickListener {
        void onLinkClick(View view);
    }
}

