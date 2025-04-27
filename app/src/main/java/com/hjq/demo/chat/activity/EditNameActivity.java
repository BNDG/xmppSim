package com.hjq.demo.chat.activity;

import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.hjq.demo.R;
import com.hjq.demo.chat.entity.ChatEvent;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.chat.widget.LoadingDialog;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;

import com.bndg.smack.SmartIMClient;
import com.bndg.smack.callback.IUserInfoCallback2;
import com.bndg.smack.model.SmartUserInfo;

/**
 * 更改名字
 *
 * @author zhou
 */
public class EditNameActivity extends ChatBaseActivity {

    @BindView(R.id.tv_title)
    TextView mTitleTv;

    @BindView(R.id.et_nick)
    EditText mNickNameEt;

    @BindView(R.id.v_nick)
    View mNickView;

    @BindView(R.id.tv_right)
    TextView mSaveTv;

    LoadingDialog mDialog;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_edit_name;
    }


    public void initView() {

        mDialog = new LoadingDialog(EditNameActivity.this);
        mSaveTv.setOnClickListener(view -> {
            mDialog.setMessage(getString(R.string.saving));
            mDialog.show();
            String userNickName = mNickNameEt.getText().toString();
            SmartUserInfo userInfo = new SmartUserInfo();
            userInfo.setNickname(userNickName);
            doModifyName(userInfo);
        });
        mSaveTv.setVisibility(View.VISIBLE);
        mTitleTv.setText(R.string.edit_name);
        setTitleStrokeWidth(mTitleTv);

        mNickNameEt.setText(myUserInfo.getUserNickName());
        // 光标移至最后
        CharSequence charSequence = mNickNameEt.getText();
        if (charSequence instanceof Spannable) {
            Spannable spanText = (Spannable) charSequence;
            Selection.setSelection(spanText, charSequence.length());
        }
        mNickNameEt.addTextChangedListener(new TextChange());

        mNickNameEt.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                mNickView.setBackgroundColor(getColor(R.color.divider_green));
            } else {
                mNickView.setBackgroundColor(getColor(R.color.divider_grey));
            }
        });
    }

    @Override
    protected void initData() {

    }

    @Override
    public void initListener() {

    }

    class TextChange implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String newNickName = mNickNameEt.getText().toString();
            String oldNickName = myUserInfo.getUserNickName();
            // 是否填写
            boolean isNickNameHasText = newNickName.length() > 0;
            // 是否修改
            boolean isNickNameChanged = !oldNickName.equals(newNickName);

            if (isNickNameHasText && isNickNameChanged) {
                // 可保存
                mSaveTv.setEnabled(true);
            } else {
                // 不可保存
                mSaveTv.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

    private void doModifyName(SmartUserInfo userInfo) {
        SmartIMClient.getInstance().getSmartCommUserManager().setMyUserInfo(userInfo, new IUserInfoCallback2() {
            @Override
            public void onSuccess(SmartUserInfo userInfo1) {
                mDialog.dismiss();
                setResult(RESULT_OK);
                myUserInfo.setUserNickName(userInfo.getNickname());
                EventBus.getDefault().post(new ChatEvent(ChatEvent.REFRESH_USER_INFO));
                PreferencesUtil.getInstance().setUser(myUserInfo);
                finish();
            }

            @Override
            public void onFailed(int code, String desc) {
                mDialog.dismiss();
                toast(desc);
            }
        });
    }
}
