package com.hjq.demo.chat.activity;

import android.content.Context;
import android.graphics.Paint;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.SPUtils;
import com.gyf.immersionbar.ImmersionBar;
import com.hjq.demo.R;
import com.hjq.demo.app.AppActivity;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.MessageDao;
import com.hjq.demo.chat.dao.UserDao;
import com.hjq.demo.chat.entity.CallCreatorInfo;
import com.hjq.demo.chat.entity.ChatMessage;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.chat.widget.AlertDialog;
import com.hjq.demo.chat.widget.NoTitleAlertDialog;
import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.utils.JsonParser;

import java.util.ArrayList;

import butterknife.ButterKnife;

/**
 * activity基类
 *
 * @author zhou
 */
public abstract class ChatBaseActivity extends AppActivity {

    protected UserDao userDao;
    protected User myUserInfo;


    @Override
    protected void initLayout() {
        super.initLayout();
        userDao = UserDao.getInstance();
        myUserInfo = PreferencesUtil.getInstance().getUser();
        ButterKnife.bind(this);
        initListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void back(View view) {
        onBackPressed();
    }

    public abstract void initListener();

    @NonNull
    @Override
    protected ImmersionBar createStatusBarConfig() {
        return super.createStatusBarConfig()
                // 指定导航栏背景颜色
                .statusBarColor(R.color.titlebar_bg)
                .navigationBarColor(R.color.titlebar_bg);
    }

    @Override
    protected void onResume() {
        super.onResume();
        postDelayed(this::checkHasCalling, 250);
    }

    private void checkHasCalling() {
        String callId = SPUtils.getInstance().getString(Constant.CURRENT_CALL_ID);
        if (!TextUtils.isEmpty(callId)) {
            MessageDao.getInstance().getMessageByCallId(callId, new MessageDao.MessageDaoCallback() {
                @Override
                public void getMessageByCallId(ChatMessage messageByCallId) {
                    if (messageByCallId != null) {
                        // 如果是来电
                        if (ChatMessage.isStartCallType(messageByCallId.getCallType())) {
                            //如果在一分钟内
                            if (System.currentTimeMillis() - messageByCallId.getTimestamp() < 60 * 1000) {
                                String callCreatorInfo = messageByCallId.getCallCreatorInfo();
                                CallCreatorInfo infoBean = JsonParser.deserializeByJson(callCreatorInfo, CallCreatorInfo.class);
                                if (null != infoBean) {
                                    if (messageByCallId.isGroupMsg()) {
                                        ArrayList<String> conversationIds = new ArrayList<>();
                                        conversationIds.add(infoBean.creatorJid);
                                    } else {
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * 显示警告弹窗
     *
     * @param context    context
     * @param title      标题
     * @param content    内容
     * @param confirm    确认键
     * @param cancelable 点击空白处是否消失
     */
    protected void showAlertDialog(Context context, String title, String content, String confirm, boolean cancelable) {
        final AlertDialog mAlertDialog = new AlertDialog(context, title, content, confirm);
        mAlertDialog.setOnDialogClickListener(new AlertDialog.OnDialogClickListener() {
            @Override
            public void onOkClick() {
                mAlertDialog.dismiss();
            }

        });
        // 点击空白处消失
        mAlertDialog.setCancelable(cancelable);
        mAlertDialog.show();
    }

    /**
     * 显示警告弹窗(无标题)
     *
     * @param context context
     * @param content 内容
     * @param confirm 确认键
     */
    protected void showNoTitleAlertDialog(Context context, String content, String confirm) {
        final NoTitleAlertDialog mNoTitleAlertDialog = new NoTitleAlertDialog(context, content, confirm);
        mNoTitleAlertDialog.setOnDialogClickListener(new NoTitleAlertDialog.OnDialogClickListener() {
            @Override
            public void onOkClick() {
                mNoTitleAlertDialog.dismiss();
            }

        });
        // 点击空白处消失
        mNoTitleAlertDialog.setCancelable(true);
        mNoTitleAlertDialog.show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 在Activity不可见时解绑Service，避免泄露
    }
}