package com.hjq.demo.chat.activity;

import android.content.Intent;
import android.media.metrics.Event;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hjq.demo.R;
import com.hjq.demo.chat.adapter.SmartConversationAdapter;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.ConversationDao;
import com.hjq.demo.chat.dao.DBManager;
import com.hjq.demo.chat.dao.UserDao;
import com.hjq.demo.chat.entity.ChatEvent;
import com.hjq.demo.chat.entity.ConversationInfo;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.listener.ContactCallback;
import com.hjq.demo.chat.listener.SimpleResultCallback;
import com.hjq.demo.chat.utils.DensityUtil;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.chat.widget.LoadingDialog;
import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.utils.JsonParser;
import com.rxjava.rxlife.RxLife;
import com.tencent.mmkv.MMKV;
import com.zhy.view.flowlayout.FlowLayout;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * 编辑联系人(设置备注和标签)
 *
 * @author zhou
 */
public class EditContactActivity extends ChatBaseActivity {

    @BindView(R.id.tv_title)
    TextView mTitleTv;

    @BindView(R.id.et_alias)
    EditText mAliasEt;

    @BindView(R.id.et_desc)
    EditText mDescEt;

    // 添加电话
    @BindView(R.id.rl_mobiles)
    RelativeLayout mMobilesRl;

    @BindView(R.id.rl_mobiles_container)
    RelativeLayout mMobilesContainerRl;

    @BindView(R.id.ll_mobile)
    LinearLayout mMobileLl;

    @BindView(R.id.et_mobile)
    EditText mMobileEt;

    @BindView(R.id.iv_clear_mobile)
    ImageView mClearMobileIv;

    // 保存
    @BindView(R.id.tv_right)
    TextView mSaveTv;


    LinearLayout.LayoutParams mParams;

    String mContactId;
    User mContact;
    LoadingDialog mDialog;

    @Override
    public int getLayoutId() {
        return R.layout.activity_edit_contact;
    }

    @Override
    public void initView() {
        mTitleTv.setText(R.string.edit_contact);
        mSaveTv.setVisibility(View.VISIBLE);
        mSaveTv.setEnabled(true);
        setTitleStrokeWidth(mTitleTv);
    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {
        mContactId = getIntent().getStringExtra("contactId");
        mDialog = new LoadingDialog(EditContactActivity.this);
        mParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int marginLeft = DensityUtil.dip2px(EditContactActivity.this, 10);
        int marginTop = DensityUtil.dip2px(EditContactActivity.this, 10);
        mParams.setMargins(marginLeft, marginTop, 0, 0);
        final String isFriend = getIntent().getStringExtra("isFriend");
        UserDao.getInstance().getUserById(EditContactActivity.this, mContactId, new ContactCallback() {
            @Override
            public void getUser(@Nullable User userById) {
                mContact = userById;
                if (TextUtils.isEmpty(mContact.getUserContactAlias())) {
                    // 无备注，展示昵称
                    mAliasEt.setText(mContact.getUserNickName());
                } else {
                    // 有备注，展示备注
                    mAliasEt.setText(mContact.getUserContactAlias());
                }

                mDescEt.setText(mContact.getUserContactDesc());
            }
        });


        if (Constant.IS_NOT_FRIEND.equals(isFriend)) {
            // 非好友不能添加电话
            mMobilesRl.setVisibility(View.GONE);
            mMobilesContainerRl.setVisibility(View.GONE);
        }

        mMobileEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String mobile = s.toString();
                if (!TextUtils.isEmpty(mobile)) {
                    if (mMobileLl.getChildCount() <= 1) {
                        addMobileView("");
                    }
                    mClearMobileIv.setVisibility(View.VISIBLE);
                } else {
                    mClearMobileIv.setVisibility(View.GONE);
                }
            }
        });
        renderMobile();

    }

    /**
     * 设置联系人
     *
     * @param contactId 联系人ID
     * @param alias     联系人备注名
     * @param mobiles   联系人电话(可以多个号码,json格式)
     * @param desc      联系人描述
     */
    private void editContact(final String contactId, final String alias,
                             final String mobiles, final String desc) {
        mDialog.dismiss();
        UserDao.getInstance().getUserById(EditContactActivity.this, contactId, new ContactCallback() {
            @Override
            public void getUser(@Nullable User user) {
                if (null != user) {
                    if (!TextUtils.isEmpty(alias) && !alias.equals(user.getUserContactAlias())) {
                        DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                                .getConversationByConversationId(PreferencesUtil.getInstance().getUserId(),
                                        contactId)
                                .to(RxLife.to(EditContactActivity.this))
                                .subscribe(conversationInfoList -> {
                                    ConversationInfo conversationInfo;
                                    if (!conversationInfoList.isEmpty()) {
                                        conversationInfo = conversationInfoList.get(0);
                                        conversationInfo.setConversationTitle(alias);
                                        DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                                                .saveOrUpdateConversation(conversationInfo)
                                                .to(RxLife.to(EditContactActivity.this)).subscribe(new CompletableObserver() {
                                                    @Override
                                                    public void onSubscribe(@NonNull Disposable d) {

                                                    }

                                                    @Override
                                                    public void onComplete() {
                                                        MMKV.defaultMMKV().putString(mContactId + "_" + Constant.CONVERSATION_TITLE, alias);
                                                        ChatEvent event = new ChatEvent(ChatEvent.CONVERSATION_ITEM_CONTENT_UPDATE);
                                                        Bundle bundle = new Bundle();
                                                        ArrayList<String> refreshList = new ArrayList<>();
                                                        refreshList.add(SmartConversationAdapter.REFRESH_TITLE);
                                                        bundle.putStringArrayList(SmartConversationAdapter.PAYLOADS, refreshList);
                                                        event.bundle = bundle;
                                                        event.obj = mContactId;
                                                        EventBus.getDefault().post(event);
                                                    }

                                                    @Override
                                                    public void onError(@NonNull Throwable e) {

                                                    }
                                                });
                                    }
                                });
                    }
                    user.setUserContactAlias(alias);
                    user.setUserContactMobiles(mobiles);
                    user.setUserContactDesc(desc);
                    UserDao.getInstance().saveOrUpdateContact(user, new SimpleResultCallback() {
                        @Override
                        public void onResult(boolean isSuccess) {
                            ChatEvent event = new ChatEvent(ChatEvent.REFRESH_CONTACT);
                            Bundle bundle = new Bundle();
                            bundle.putString(Constant.CONTACT_ID, user.getUserId());
                            bundle.putBoolean(Constant.FRIEND_ADDED, false);
                            event.bundle = bundle;
                            EventBus.getDefault().post(event);
                            finish();
                        }
                    });
                } else {
                    finish();
                }
            }
        });
    }

    @OnClick({R.id.iv_clear_mobile, R.id.tv_right})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_clear_mobile:
                mMobileEt.setText("");
                break;
            case R.id.tv_right:
                String alias = mAliasEt.getText().toString();
                if (alias.length() > 16) {
                    showAlertDialog(EditContactActivity.this, getString(R.string.tips),
                            "你输入的备注过长", "确定", true);
                    return;
                }
                List<String> mobileList = getMobileList();
                String mobiles = JsonParser.serializeToJson(mobileList);
                String desc = mDescEt.getText().toString();

                mDialog.setMessage(getString(R.string.common_loading));
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();
                editContact(mContactId, alias, mobiles, desc);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 创建一个正常状态的标签
     *
     * @param label
     * @return
     */
    private TextView getTag(String label) {
        TextView textView = new TextView(getApplicationContext());
        textView.setTextSize(14);
        textView.setBackgroundResource(R.drawable.label_normal);
        textView.setTextColor(getColor(R.color.register_btn_bg_enable));
        textView.setText(label);
        textView.setLayoutParams(mParams);
        return textView;
    }

    private void addMobileView(String mobile) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                DensityUtil.dip2px(this, 58));
        View view = LayoutInflater.from(this).inflate(R.layout.item_contact_mobile, null);
        view.setLayoutParams(lp);
        EditText mobileEt = view.findViewById(R.id.et_mobile);
        ImageView clearMobileIv = view.findViewById(R.id.iv_clear_mobile);
        if (!TextUtils.isEmpty(mobile)) {
            mobileEt.setText(mobile);
            clearMobileIv.setVisibility(View.VISIBLE);
        } else {
            clearMobileIv.setVisibility(View.GONE);
        }
        mobileEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String mobile = s.toString();
                if (!TextUtils.isEmpty(mobile)) {
                    if (view == mMobileLl.getChildAt(mMobileLl.getChildCount() - 1)) {
                        addMobileView("");
                    }
                    clearMobileIv.setVisibility(View.VISIBLE);
                } else {
                    mMobileLl.removeView(view);
                    View lastView = mMobileLl.getChildAt(mMobileLl.getChildCount() - 1);
                    EditText lastMobileEt = lastView.findViewById(R.id.et_mobile);
                    lastMobileEt.requestFocus();
                    clearMobileIv.setVisibility(View.GONE);
                }
            }
        });
        clearMobileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mobileEt.setText("");
            }
        });
        mMobileLl.addView(view);
    }

    /**
     * 获取所有的电话号码
     *
     * @return 所有的电话号码
     */
    private List<String> getMobileList() {
        List<String> mobileList = new ArrayList<>();
        int childCount = mMobileLl.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = mMobileLl.getChildAt(i);
            EditText mobileEt = view.findViewById(R.id.et_mobile);
            String mobile = mobileEt.getText().toString();
            if (!TextUtils.isEmpty(mobile)) {
                mobileList.add(mobile);
            }
        }
        return mobileList;
    }

    /**
     * 渲染电话
     */
    private void renderMobile() {
        List<String> mobileList;
        try {
            mobileList = JsonParser.getListFromJson(mContact.getUserContactMobiles(), String.class);
            if (null == mobileList) {
                mobileList = new ArrayList<>();
            }
        } catch (Exception e) {
            mobileList = new ArrayList<>();
        }
        if (mobileList.size() >= 1) {
            mMobileEt.setText(mobileList.get(0));
            // 渲染默认电话输入框会触发默认输入框的textChange事件,多出一个新的电话输入框,需清除
            if (mMobileLl.getChildCount() == 2) {
                mMobileLl.removeViewAt(1);
            }
            for (int i = 1; i < mobileList.size(); i++) {
                addMobileView(mobileList.get(i));
            }
            addMobileView("");
        }
    }
}
