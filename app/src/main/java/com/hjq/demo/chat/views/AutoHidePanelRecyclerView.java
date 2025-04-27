package com.hjq.demo.chat.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.effective.android.panel.PanelSwitchHelper;
import com.hjq.demo.utils.Trace;

/**
 *
 */
public class AutoHidePanelRecyclerView extends RecyclerView {

    PanelSwitchHelper panelSwitchHelper;
    private boolean isIntercepted;

    public void setPanelSwitchHelper(PanelSwitchHelper panelSwitchHelper) {
        this.panelSwitchHelper = panelSwitchHelper;
    }

    public AutoHidePanelRecyclerView(Context context) {
        this(context, null);
    }

    public AutoHidePanelRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public AutoHidePanelRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutManager(new LinearLayoutManager(context));
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (e != null && e.getAction() != MotionEvent.ACTION_CANCEL && !isIntercepted) {
            if (panelSwitchHelper != null) {
//                Trace.d("setIntercepted: 隐藏" + e.getAction() );
                panelSwitchHelper.hookSystemBackByPanelSwitcher();
            }
        }
        if(e != null && (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_CANCEL)) {
//            Trace.d("setIntercepted: 不拦截" +  e.getAction() );
            setIntercepted(false);
        }
        return super.onTouchEvent(e);
    }

    public void setIntercepted(boolean isIntercepted) {
//        Trace.d("setIntercepted: 是否拦截？ " + isIntercepted);
        this.isIntercepted = isIntercepted;
    }
}
