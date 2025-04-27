package com.hjq.demo.chat.activity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.SPUtils;
import com.bndg.smack.SmartCommHelper;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.callback.ISmartCallback;
import com.bndg.smack.constant.SmartConstants;
import com.hjq.demo.R;
import com.hjq.demo.aop.SingleClick;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.DBManager;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.utils.CryptoUtil;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.utils.CheckUtil;
import com.hjq.demo.utils.Trace;
import com.hjq.toast.ToastUtils;
import com.rxjava.rxlife.RxLife;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * 登录
 *
 * @author zhou
 */
public class LoginActivity extends ChatBaseActivity implements View.OnClickListener {

    @BindView(R.id.tv_title)
    TextView mTitleTv;

    @BindView(R.id.rl_account)
    View rl_account;

    @BindView(R.id.et_account)
    EditText mAccountEt;
    @BindView(R.id.et_server)
    EditText mServerEt;

    @BindView(R.id.et_password)
    EditText mPasswordEt;

    @BindView(R.id.btn_next)
    Button mNextBtn;

    @BindView(R.id.ll_login_via_wechat_id_email_qq_id)
    LinearLayout mLoginViaWechatIdOrEmailOrQqIdLl;
    private String mUsernameData = "";

    @Override
    public int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    public void initView() {

    }

    @Override
    public void initListener() {
        mAccountEt.addTextChangedListener(new TextChange());
        mPasswordEt.addTextChangedListener(new TextChange());
        mServerEt.addTextChangedListener(new TextChange());
    }

    @Override
    public void initData() {
        Trace.d("initData: --------------------------------");
        mTitleTv.setText(getString(R.string.login_via_wechat_id_email_qq_id));
        mNextBtn.setText(getString(R.string.login));
        mServerEt.setText(SPUtils.getInstance(SmartConstants.SP_NAME).getString(SmartConstants.CONSTANT_DOMAIN));
        mTitleTv.setOnLongClickListener(view -> {
            startActivity(ConfigActivity.class);
            return true;
        });
    }

    @SingleClick
    @OnClick({R.id.btn_next})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_next:
                mUsernameData = mAccountEt.getText().toString();
                String password = mPasswordEt.getText().toString();
                String serverAddress = mServerEt.getText().toString();
                if (!RegexUtils.isMatch(CheckUtil.REGEX_URL2, serverAddress)) {
                    toast("服务器地址格式不正确!");
                    return;
                } else {
                    SPUtils.getInstance(SmartConstants.SP_NAME).put(SmartConstants.CONSTANT_DOMAIN, serverAddress);
                    SPUtils.getInstance(SmartConstants.SP_NAME).put(SmartConstants.CONSTANT_HOST, serverAddress);
                }
                loginAccount(mUsernameData, password);
                break;
        }
    }

    private void loginAccount(String username, String password) {
        mNextBtn.setEnabled(false);
        showDialog();
        xmppLogin(username, password, "");
    }

    private void xmppLogin(String tusername, String tpassword, String unionid) {
        PreferencesUtil.getInstance().setUserId(SmartCommHelper.getInstance().getUserIdByAccount(tusername));
        SmartIMClient.getInstance().connectAndLogin(tusername, tpassword, new ISmartCallback() {
            @Override
            public void onSuccess() {
                // 登录成功，可以在这里处理后续操作，如发送消息、监听聊天室等
                CryptoUtil.encryptAndSaveCredentials2(tusername, tpassword);
                String imUserId = SmartCommHelper.getInstance().getUserIdByAccount(tusername);
                PreferencesUtil.getInstance().setLogin(true);
                Trace.d("登录成功 Connected and logged in successfully. imUserId = " + imUserId);
                DBManager.Companion.getInstance(LoginActivity.this)
                        .getUserById(imUserId)
                        .to(RxLife.to(LoginActivity.this))
                        .subscribe(new Consumer<List<User>>() {
                            @Override
                            public void accept(List<User> users) throws Throwable {
                                User user;
                                if (!users.isEmpty()) {
                                    user = users.get(0);
                                    user.setUnionid(unionid);
                                    user.setIsFriend(Constant.IS_MYSELF);
                                    user.setUserAccount(mUsernameData);
                                } else {
                                    user = new User();
                                    user.setBelongAccount(imUserId);
                                    user.setUserId(imUserId);
                                    user.setUnionid(unionid);
                                    user.setIsFriend(Constant.IS_MYSELF);
                                    user.setUserAccount(mUsernameData);
                                }
                                Trace.d("accept: " + user,
                                        user.getUserId(),
                                        user.getBelongAccount());
                                DBManager.Companion.getInstance(LoginActivity.this)
                                        .saveContact(user)
                                        .to(RxLife.to(LoginActivity.this))
                                        .subscribe(new CompletableObserver() {
                                            @Override
                                            public void onSubscribe(@NonNull Disposable d) {

                                            }

                                            @Override
                                            public void onComplete() {
                                                PreferencesUtil.getInstance().setUser(user);
                                                hideDialog();
                                                // 登录完后再添加监听器
                                                MainActivity.start(LoginActivity.this, Constant.FROM_LOGIN);
                                                ActivityManager.getInstance().finishAllActivities(MainActivity.class);
                                            }


                                            @Override
                                            public void onError(@NonNull Throwable e) {
                                                Trace.d("onComplete: 保存完了--" + e);
                                            }
                                        });
                            }
                        });
            }

            @Override
            public void onFailed(int code, String desc) {
                mNextBtn.setEnabled(true);
                hideDialog();
                ToastUtils.show(desc);
            }
        });
    }

    class TextChange implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            boolean passwordEtHasText = mPasswordEt.getText().length() > 0;
            boolean accountEtHasText = mAccountEt.getText().length() > 0;
            boolean serverEtHasText = mServerEt.getText().length() > 0;
            if (accountEtHasText && passwordEtHasText && serverEtHasText) {
                // "登录"按钮可用
                mNextBtn.setBackgroundResource(R.drawable.btn_login_next_enable);
                mNextBtn.setTextColor(getColor(R.color.register_btn_text_enable));
                mNextBtn.setEnabled(true);
            } else {
                // "登录"按钮不可用
                mNextBtn.setBackgroundResource(R.drawable.btn_login_next_disable);
                mNextBtn.setTextColor(getColor(R.color.register_btn_text_disable));
                mNextBtn.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

}