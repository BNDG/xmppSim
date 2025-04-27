/*
 * Copyright (C) 2015 pengjianbo(pengjianbosoft@gmail.com), Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.hjq.demo.http.floatlog;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SPUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemChildLongClickListener;
import com.hjq.demo.R;
import com.hjq.demo.utils.CheckUtil;
import com.hjq.demo.utils.Trace;
import com.hjq.toast.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Desction:悬浮窗
 * Author:pengjianbo
 * Date:15/10/26 下午8:39
 */
public class FloatView extends FrameLayout implements OnTouchListener {

    private static final String POSITION_X = "POSITION_X";
    private static final String POSITION_Y = "POSITION_Y";
    private static final String X_KEY = "X_KEY";
    private static final String LEFT_KEY = "LEFT_KEY";
    private static final String UP_KEY = "UP_KEY";
    private final int HANDLER_TYPE_HIDE_LOGO = 100;//隐藏LOGO

    private WindowManager.LayoutParams mWmParams;
    private WindowManager mWindowManager;
    private Context mContext;
    private boolean mCanHide;//是否允许隐藏
    private float mTouchStartX;
    private float mTouchStartY;
    private float mScreenWidth;
    private float mScreenHeight;
    private boolean mDraging;

    private Timer mTimer;
    private TimerTask mTimerTask;

    final Handler mTimerHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == HANDLER_TYPE_HIDE_LOGO) {
                // 比如隐藏悬浮框
                if (mCanHide) {
                    mCanHide = false;
                    if (TRANSLATE_X) {
                        if (TRANSLATE_TOLEFT) {
                            translateX((float) (-FloatView_width / 1.2), 500);
                        } else {
                            translateX((float) (FloatView_width / 1.2), 500);
                        }
                    } else {
                        if (TRANSLATE_TOBOTTOM) {
                            translateY((float) (FloatView_width / 1.2), 500);
                        } else {
                            translateY((float) (-FloatView_width / 1.2), 500);
                        }
                    }
                }
            }
            super.handleMessage(msg);
        }
    };
    private View rootFloatView;
    private int FloatView_width;
    private int FloatView_height;
    private boolean TRANSLATE_TOLEFT;
    private boolean TRANSLATE_TOBOTTOM;
    private double LIMIT;

    private boolean TRANSLATE_X;
    private LinearLayout mContainView;
    private ListView mListView;
    private View contentView;
    private InputMethodManager im;
    private ObjectAnimator translateX;
    private ObjectAnimator translateY;
    private boolean mCanClose;
    private RectF rectF;
    private RecyclerView rv_content;
    private HttpLogAdapter mAdapter;
    private View tv_clear;
    private View v_outside;
    private FrameLayout fl_contain;
    private View nsv_scroll;
    private TextView tv_results;
    private View bt_back;

    public FloatView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context mContext) {
        this.mContext = mContext;
        im = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        // 更新浮动窗口位置参数 靠边
        DisplayMetrics dm = new DisplayMetrics();
        // 获取屏幕信息
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        this.mWmParams = new WindowManager.LayoutParams();
        // 设置窗体显示类型
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mWmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mWmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        // 设置图片格式，效果为背景透明
        mWmParams.format = PixelFormat.RGBA_8888;
//        this.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        int flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_FULLSCREEN;
//                PixelFormat.TRANSLUCENT;
//        WindowManager.LayoutParams.TYPE_SYSTEM_ERROR |
//
//        WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
//                | WindowManager.LayoutParams.FLAG_FULLSCREEN;
//
        // 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        // 如果设置了WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE，弹出的View收不到Back键的事件
        mWmParams.flags = flags;
//        mWmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // 调整悬浮窗显示的停靠位置为左侧置
        mWmParams.gravity = Gravity.LEFT | Gravity.TOP;

        mScreenHeight = mWindowManager.getDefaultDisplay().getHeight();

        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        mWmParams.x = (int) SPUtils.getInstance().getInt(POSITION_X, 0);
        mWmParams.y = (int) SPUtils.getInstance().getInt(POSITION_Y, (int) (mScreenHeight / 2));
        // 设置悬浮窗口长宽数据
        mWmParams.width = LayoutParams.WRAP_CONTENT;
        mWmParams.height = LayoutParams.WRAP_CONTENT;
        addView(createView(mContext));
        mWindowManager.addView(this, mWmParams);
        createContainView(mContext);
        mWindowManager.addView(mContainView, createParams());
        mContainView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    closeContentView();
                }
                return true;
            }
        });
        mTimer = new Timer();
    }

    private void checkPositon(int x, int y) {
        if (mScreenHeight - y < LIMIT) {
            // 如果y坐标为靠下 则在下边 往下隐藏
            TRANSLATE_X = false;
            TRANSLATE_TOBOTTOM = true;
            mWmParams.y = (int) (mScreenHeight - FloatView_height);
        } else if (y < LIMIT) {
            // 如果y坐标为靠上 则在上边 往上隐藏
            TRANSLATE_X = false;
            TRANSLATE_TOBOTTOM = false;
            mWmParams.y = 0;
        } else if (x >= mScreenWidth / 2) {
            // 如果x坐标为mScreenWidth 则在右边 往右隐藏
            TRANSLATE_X = true;
            TRANSLATE_TOLEFT = false;
            mWmParams.x = (int) (mScreenWidth - FloatView_width);
        } else if (x < mScreenWidth / 2) {
            // 如果x坐标为0 则在左边 往左隐藏
            TRANSLATE_X = true;
            TRANSLATE_TOLEFT = true;
            mWmParams.x = 0;
        }
        SPUtils.getInstance().put(X_KEY, TRANSLATE_X);
        SPUtils.getInstance().put(LEFT_KEY, TRANSLATE_TOBOTTOM);
        SPUtils.getInstance().put(UP_KEY, TRANSLATE_TOLEFT);
        mWindowManager.updateViewLayout(this, mWmParams);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // 更新浮动窗口位置参数 靠边
        DisplayMetrics dm = new DisplayMetrics();
        // 获取屏幕信息
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        int oldX = mWmParams.x;
        int oldY = mWmParams.y;
        getPosition();
        switch (newConfig.orientation) {

            case Configuration.ORIENTATION_LANDSCAPE://横屏
                Trace.d("横屏" + oldX + "<>" + oldY);
                mWmParams.x = (int) (oldX * (mScreenWidth / mScreenHeight));
                if (TRANSLATE_X) {
                    mWmParams.x = mWmParams.x >= mScreenWidth / 2 ? (int) (mScreenWidth - FloatView_width) : 0;
                }
                mWmParams.y = (int) (oldY * (mScreenWidth / mScreenHeight));
                if (mWmParams.y > mScreenHeight - FloatView_width) {
                    mWmParams.y = (int) (mScreenHeight - FloatView_width);
                }

                if (mWmParams.y < 0) {
                    mWmParams.y = 0;
                }
                break;
            case Configuration.ORIENTATION_PORTRAIT://竖屏
                Trace.d("竖屏" + oldX + "<>" + oldY);
                mWmParams.x = (int) (oldX * (mScreenHeight / mScreenWidth));
                if (mWmParams.x > mScreenWidth - FloatView_width) {
                    mWmParams.x = (int) (mScreenWidth - FloatView_width);
                }

                if (mWmParams.x < 0) {
                    mWmParams.x = 0;
                }

                mWmParams.y = (int) (oldY * (mScreenHeight / mScreenWidth));
                if (!TRANSLATE_X) {
                    mWmParams.y = mWmParams.y >= mScreenHeight / 2 ? (int) (mScreenHeight - FloatView_height) : 0;
                }
                break;
        }
        mWindowManager.updateViewLayout(this, mWmParams);
    }

    private void getPosition() {
        TRANSLATE_X = (boolean) SPUtils.getInstance().getBoolean(X_KEY, false);
        TRANSLATE_TOBOTTOM = (boolean) SPUtils.getInstance().getBoolean(LEFT_KEY, false);
        TRANSLATE_TOLEFT = (boolean) SPUtils.getInstance().getBoolean(UP_KEY, false);
    }

    /**
     * 创建Float view
     *
     * @param context
     * @return
     */
    private View createView(final Context context) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        // 从布局文件获取浮动窗口视图
        rootFloatView = inflater.inflate(R.layout.float_view_widget, null);
        rootFloatView.setOnTouchListener(this);
        rootFloatView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mCanHide && !mDraging) {
                    if (mContainView.getVisibility() == INVISIBLE) {
                        mContainView.setVisibility(VISIBLE);
                        loadAnimIn(contentView);
                    }
                }
            }
        });
        rootFloatView.measure(MeasureSpec.makeMeasureSpec(0,
                MeasureSpec.UNSPECIFIED), MeasureSpec
                .makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

        FloatView_width = rootFloatView.getMeasuredWidth();
        FloatView_height = rootFloatView.getMeasuredHeight();
        LIMIT = FloatView_width * 1.5;


        return rootFloatView;
    }

    private void createContainView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        mContainView = (LinearLayout) inflater.inflate(R.layout.float_view_contain, null);
        fl_contain = mContainView.findViewById(R.id.fl_contain);
        v_outside = mContainView.findViewById(R.id.fl_outside);
        v_outside.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeContentView();
            }
        });
        contentView = inflater.inflate(R.layout.float_view_home, null);
        mContainView.setVisibility(INVISIBLE);
        rv_content = contentView.findViewById(R.id.rv_content);
        nsv_scroll = contentView.findViewById(R.id.nsv_scroll);
        tv_results = contentView.findViewById(R.id.tv_results);
        bt_back = contentView.findViewById(R.id.bt_back);
        tv_clear = contentView.findViewById(R.id.tv_clear);
        tv_results.setOnClickListener(v -> {
            CheckUtil.copyTextToBoard(v.getContext(), tv_results.getText().toString());
            ToastUtils.show(R.string.copied);
        });
        bt_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAnimOutRight(nsv_scroll);
                loadAnimInLeft(rv_content);
            }
        });

        tv_clear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.setList(new ArrayList<HttpLogEvent>());
            }
        });
        rv_content.setLayoutManager(new LinearLayoutManager(context));
        mAdapter = new HttpLogAdapter(new ArrayList<HttpLogEvent>());
        mAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                HttpLogEvent item = (HttpLogEvent) adapter.getItem(position);
                if (null != item && !TextUtils.isEmpty(item.results)) {
                    if (item.isTrace()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            tv_results.setText(Html.fromHtml(item.results, Html.FROM_HTML_MODE_LEGACY));
                        } else {
                            tv_results.setText(Html.fromHtml(item.results));
                        }
                    } else {
                        String format = CheckUtil.formatJson(item.results);
                        tv_results.setText(format);
                    }
                    loadAnimOutLeft(rv_content);
                    loadAnimInRight(nsv_scroll);
                }
            }
        });
        mAdapter.setOnItemChildLongClickListener(new OnItemChildLongClickListener() {
            @Override
            public boolean onItemChildLongClick(BaseQuickAdapter adapter, View view, int position) {
                HttpLogEvent item = (HttpLogEvent) adapter.getItem(position);
                int id = view.getId();
                if (id == R.id.tv_url) {
                    CheckUtil.copyTextToBoard(view.getContext(), item.url);
                    ToastUtils.show(R.string.copied);
                } else if (id == R.id.tv_params) {
                    CheckUtil.copyTextToBoard(view.getContext(), item.params);
                    ToastUtils.show(R.string.copied);
                }
                return true;
            }

        });
        rv_content.setAdapter(mAdapter);
        fl_contain.addView(contentView);
    }


    private void closeContentView() {
        if (!mCanClose || mContainView.getVisibility() == INVISIBLE) {
            return;
        }
        mCanClose = false;
        loadAnimOut(contentView, new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mContainView.setVisibility(INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 右边往左
     *
     * @param view
     */
    private void loadAnimInRight(View view) {
        ObjectAnimator oa = ObjectAnimator.ofFloat(view, "translationX", view.getWidth(), 0);
        oa.setDuration(300);
        oa.start();
        oa.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                bt_back.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void loadAnimOutRight(View view) {
        ObjectAnimator oa = ObjectAnimator.ofFloat(view, "translationX", 0, view.getWidth());
        oa.setDuration(300);
        oa.start();
        oa.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                bt_back.setVisibility(INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void loadAnimOutLeft(View view) {
        ObjectAnimator oa = ObjectAnimator.ofFloat(view, "translationX", 0, -view.getWidth());
        oa.setDuration(300);
        oa.start();

    }

    private void loadAnimInLeft(View view) {
        ObjectAnimator oa = ObjectAnimator.ofFloat(view, "translationX", -view.getWidth(), 0);
        oa.setDuration(300);
        oa.start();
    }


    private void loadAnimIn(View contentView) {
        mCanClose = true;
        Animation animation;
        animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, -1, Animation.RELATIVE_TO_SELF, 0, Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0);
        animation.setDuration(250);
        contentView.startAnimation(animation);
    }


    private void loadAnimOut(View contentView, Animation.AnimationListener listener) {
        Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -1, Animation.ABSOLUTE, 0, Animation.ABSOLUTE, 0);
        animation.setDuration(250);
        animation.setAnimationListener(listener);
        contentView.startAnimation(animation);
    }


    public WindowManager.LayoutParams createParams() {
        WindowManager.LayoutParams newParams = new WindowManager.LayoutParams();
        // 设置window type
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            newParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            newParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        }
        // 设置图片格式，效果为背景透明
        newParams.format = PixelFormat.RGBA_8888;
        int flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_FULLSCREEN;
        newParams.flags = flags;
//      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
//      WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM |
//      WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
        // 调整悬浮窗显示的停靠位置为左侧置
        newParams.gravity = Gravity.LEFT | Gravity.TOP;
        newParams.x = 0;
        newParams.y = 0;
        newParams.width = LayoutParams.MATCH_PARENT;
        newParams.height = LayoutParams.MATCH_PARENT;
        return newParams;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        removeTimerTask();
        // 获取相对屏幕的坐标，即以屏幕左上角为原点
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        /*if (rectF != null && !rectF.contains(x, y)) {
            ToastUtils.MakeToast("不包含");
            return false;
        }*/

        if (!mCanHide) {
            restoreView();
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchStartX = event.getX();
                mTouchStartY = event.getY();
                mDraging = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float mMoveStartX = event.getX();
                float mMoveStartY = event.getY();
                // 如果移动量大于3才移动
                if (Math.abs(mTouchStartX - mMoveStartX) > 3
                        && Math.abs(mTouchStartY - mMoveStartY) > 3) {
                    mDraging = true;
                }
                // 更新浮动窗口位置参数
                mWmParams.x = (int) (x - mTouchStartX);
                mWmParams.y = (int) (y - mTouchStartY);
                mWindowManager.updateViewLayout(this, mWmParams);
//                    return false;

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                SPUtils.getInstance().put(POSITION_X, mWmParams.x);
                SPUtils.getInstance().put(POSITION_Y, mWmParams.y);
                checkPositon(mWmParams.x, mWmParams.y);
                timerForHide();
                // 初始化
                mTouchStartX = mTouchStartY = 0;
                break;
        }
        return false;
    }

    private void restoreView() {
        getPosition();
        if (TRANSLATE_X) {
            translateX(0, 100);
        } else {
            translateY(0, 100);
        }
        mCanHide = true;
    }


    /**
     * 在X轴进行平移 左右
     */
    private void translateX(final float di, long duration) {
        if (translateX == null) {
            translateX = ObjectAnimator.ofFloat(this, "translationX", 0, di);
        }
        translateX.setFloatValues(0, di);
        translateX.setDuration(duration);
        translateX.setInterpolator(new LinearInterpolator());
        translateX.start();
        translateX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                /*if (di > 0) {
                    rectF = new RectF(mWmParams.x + di, mWmParams.y, mWmParams.x + FloatView_width + di, mWmParams.y + FloatView_height);
                    Trace.d(rectF.left + "<>><" + rectF.right);
                } else {
                    rectF = new RectF(mWmParams.x - di, mWmParams.y, mWmParams.x + FloatView_width - di, mWmParams.y + FloatView_height);
                }*/
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        TRANSLATE_TOLEFT = !TRANSLATE_TOLEFT;
    }

    /**
     * 在Y轴进行平移 上下
     */
    private void translateY(final float di, long duration) {
        if (translateY == null) {
            translateY = ObjectAnimator.ofFloat(this, "translationY", 0, di);
        }
        translateY.setFloatValues(0, di);
        translateY.setDuration(duration);
        translateY.setInterpolator(new LinearInterpolator());
        translateY.start();
        translateY.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                /*if (di > 0) {
                    rectF = new RectF(mWmParams.x, mWmParams.y + di, mWmParams.x + FloatView_width + di, mWmParams.y + di + FloatView_height);
                } else {
                    rectF = new RectF(mWmParams.x, mWmParams.y - di, mWmParams.x + FloatView_width + di, mWmParams.y - di + FloatView_height);
                }*/
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        TRANSLATE_TOBOTTOM = !TRANSLATE_TOBOTTOM;
    }


    private void removeTimerTask() {
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    private void removeFloatView() {
        try {
            if (mContainView != null) {
                mContainView.removeAllViews();
                mWindowManager.removeViewImmediate(mContainView);
                mContainView = null;
            }
            mWindowManager.removeView(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * 显示悬浮窗
     */
    public void show() {
        mWmParams.x = (int) SPUtils.getInstance().getInt(POSITION_X, 0);
        mWmParams.y = (int) SPUtils.getInstance().getInt(POSITION_Y, (int) (mScreenHeight / 2));
        checkPositon(mWmParams.x, mWmParams.y);
        timerForHide();
    }


    /**
     * 定时隐藏float view
     */
    private void timerForHide() {
        mCanHide = true;
        //结束任务
        if (mTimerTask != null) {
            try {
                mTimerTask.cancel();
                mTimerTask = null;
            } catch (Exception e) {
            }

        }
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                Message message = mTimerHandler.obtainMessage();
                message.what = HANDLER_TYPE_HIDE_LOGO;
                mTimerHandler.sendMessage(message);
            }
        };
        if (mCanHide) {
            mTimer.schedule(mTimerTask, 1000);
        }
    }

    /**
     * 是否Float view
     */
    public void destroy() {
        Trace.d("销毁了.......");
        removeFloatView();
        removeTimerTask();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        try {
            mTimerHandler.removeCallbacksAndMessages(null);
        } catch (Exception e) {
        }
    }

    public void setContent(final HttpLogEvent event) {
        contentView.post(new Runnable() {
            @Override
            public void run() {
                if (mAdapter != null) {
                    List<HttpLogEvent> data = mAdapter.getData();
                    if (data.contains(event)) {
                        for (HttpLogEvent logEvent : data) {
                            if (event.equals(logEvent)) {
                                if (event.isTrace()) {
                                    logEvent.results += "<br>" + "+--------------<br>" + "<font color=\"#FFA500\">" + event.header + "</font>" + "<br>" + event.results;
                                } else {
                                    logEvent.results = event.results;
                                }
                            }
                        }
                    } else {
                        if (event.isTrace()) {
                            event.results += "<br>" + "+--------------<br>" + "<font color=\"#FFA500\">" + event.header + "</font>" + "<br>" + event.results;
                        }
                        mAdapter.addData(event);
                    }
                }
            }
        });

    }
}

