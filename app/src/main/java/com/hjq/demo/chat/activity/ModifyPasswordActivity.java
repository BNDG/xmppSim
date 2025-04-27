package com.hjq.demo.chat.activity;

import static com.hjq.demo.chat.utils.ValidateUtil.validatePassword;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.hjq.demo.R;
import com.hjq.demo.chat.widget.LoadingDialog;
import com.hjq.demo.chat.widget.NoTitleAlertDialog;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 设置密码
 *
 * @author zhou
 */
public class ModifyPasswordActivity extends ChatBaseActivity {

    @BindView(R.id.tv_title)
    TextView mTitleTv;

    @BindView(R.id.tv_right)
    TextView mCompleteTv;

    @BindView(R.id.et_old_password)
    EditText mOldPasswordEt;

    @BindView(R.id.et_new_password)
    EditText mNewPasswordEt;

    @BindView(R.id.et_confirm_password)
    EditText mConfirmPasswordEt;

    LoadingDialog mDialog;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_modify_password;
    }

    public void initView() {
        mTitleTv.setText(R.string.modify_password);
        mDialog = new LoadingDialog(ModifyPasswordActivity.this);
        setTitleStrokeWidth(mTitleTv);
        mCompleteTv.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initData() {

    }

    @OnClick({R.id.tv_right})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_right:
                submitForm();
                break;
        }
    }

    /**
     * 提交表单
     */
    private void submitForm() {

        String oldPassword = mOldPasswordEt.getText().toString();
        String newPassword = mNewPasswordEt.getText().toString();
        String confirmPassword = mConfirmPasswordEt.getText().toString();
        if (TextUtils.isEmpty(oldPassword)) {
            showAlertDialog(ModifyPasswordActivity.this, getString(R.string.tips),
                    getString(R.string.old_password_empty), getString(R.string.confirm), true);
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            showAlertDialog(ModifyPasswordActivity.this, getString(R.string.tips),
                    getString(R.string.new_password_empty), getString(R.string.confirm), true);
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            showAlertDialog(ModifyPasswordActivity.this, getString(R.string.tips),
                    getString(R.string.confirm_password_empty), getString(R.string.confirm), true);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showAlertDialog(ModifyPasswordActivity.this, getString(R.string.tips),
                    getString(R.string.confirm_password_incorrect), getString(R.string.confirm), true);
            return;
        }

        if (!validatePassword(newPassword)) {
            showAlertDialog(ModifyPasswordActivity.this, getString(R.string.tips),
                    getString(R.string.password_rules), getString(R.string.confirm), true);
            return;
        }

        mDialog.setMessage(getString(R.string.sending));
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        modifyPassword(myUserInfo.getUserId(), oldPassword, newPassword);
    }

    /**
     * 修改密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    private void modifyPassword(final String userId, final String oldPassword, final String newPassword) {
        mDialog.dismiss();
        final NoTitleAlertDialog mNoTitleAlertDialog = new NoTitleAlertDialog(ModifyPasswordActivity.this,
                getString(R.string.modify_password_success_tips), getString(R.string.confirm));
        mNoTitleAlertDialog.setOnDialogClickListener(() -> {
            mNoTitleAlertDialog.dismiss();
            ModifyPasswordActivity.this.finish();
        });
        // 点击空白处消失
        mNoTitleAlertDialog.setCancelable(false);
        mNoTitleAlertDialog.show();
    }

    @Override
    public void initListener() {

    }
}
