
package com.hjq.demo.chat.fragment;

import android.graphics.Paint;
import android.text.TextPaint;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.gyf.immersionbar.ImmersionBar;
import com.hjq.demo.R;
import com.hjq.demo.app.AppFragment;
import com.hjq.demo.chat.activity.ChatBaseActivity;

abstract class BaseChatFragment<A extends ChatBaseActivity> extends AppFragment<ChatBaseActivity>  {

    /**
     * 获取状态栏字体颜色
     */
    protected boolean isStatusBarDarkFont() {
        // 返回真表示黑色字体
//        return getAttachActivity().isStatusBarDarkFont();
        return true;
    }
    /**
     * 初始化沉浸式
     */
    @NonNull
    protected ImmersionBar createStatusBarConfig() {
        return ImmersionBar.with(this)
                // 默认状态栏字体颜色为黑色
                .statusBarDarkFont(isStatusBarDarkFont())
                // 指定导航栏背景颜色
                .navigationBarColor(R.color.titlebar_bg)
                // 状态栏字体和导航栏内容自动变色，必须指定状态栏颜色和导航栏颜色才可以自动变色
                .autoDarkModeEnable(true, 0.2f);
    }
/**
     * 渲染标题粗细程度
     *
     * @param textView 标题textView
     */
    protected void setTitleStrokeWidth(TextView textView) {
        TextPaint paint = textView.getPaint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        // 控制字体加粗的程度
        paint.setStrokeWidth(0.8f);
    }

}
