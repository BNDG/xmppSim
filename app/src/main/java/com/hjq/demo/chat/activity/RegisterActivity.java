package com.hjq.demo.chat.activity;

import static com.hjq.demo.chat.utils.ValidateUtil.validatePassword;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.SPUtils;
import com.bndg.smack.SmartCommHelper;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.callback.ILoginCallback;
import com.bndg.smack.callback.ISmartCallback;
import com.bndg.smack.callback.IUserInfoCallback2;
import com.bndg.smack.constant.SmartConstants;
import com.bndg.smack.model.SmartUserInfo;
import com.bndg.smack.utils.BitmapUtils;
import com.bumptech.glide.Glide;
import com.hjq.demo.R;
import com.hjq.demo.aop.SingleClick;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.UserDao;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.utils.AvatarGenerator;
import com.hjq.demo.chat.utils.CommonUtil;
import com.hjq.demo.chat.utils.CryptoUtil;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.chat.widget.LoadingDialog;
import com.hjq.demo.ui.activity.ImageCropActivity;
import com.hjq.demo.ui.activity.ImageSelectActivity;
import com.hjq.demo.utils.CheckUtil;
import com.hjq.demo.utils.Trace;
import com.hjq.http.model.FileContentResolver;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.FileCallback;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 注册
 *
 * @author zhou
 */
public class RegisterActivity extends ChatBaseActivity {

    @BindView(R.id.sdv_avatar)
    ImageView mAvatarSdv;

    @BindView(R.id.et_nick_name)
    EditText mNickNameEt;

    @BindView(R.id.et_account)
    EditText mAccountEt;

    @BindView(R.id.et_password)
    EditText mPasswordEt;
    @BindView(R.id.et_server)
    EditText mServerEt;

    @BindView(R.id.btn_register)
    Button mRegisterBtn;


    LoadingDialog mDialog;

    /**
     * 是否同意协议
     */
    private String passWord;
    private String nickName;
    private String mUsernameData;
    private byte[] bitmapData;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_register;
    }

    public void initView() {

        mDialog = new LoadingDialog(RegisterActivity.this);
        mNickNameEt.addTextChangedListener(new TextChange());
        mAccountEt.addTextChangedListener(new TextChange());
        mPasswordEt.addTextChangedListener(new TextChange());
        mServerEt.addTextChangedListener(new TextChange());


        // 定义允许的字符集（字母、数字、连字符、点、下划线）
        String allowedCharacters = "^[a-zA-Z0-9._-]*$";
        // 创建自定义的InputFilter
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source != null && !source.toString().matches(allowedCharacters)) {
                    return "";
                }
                return null;
            }
        };
        // 将自定义的InputFilter应用到EditText
        mAccountEt.setFilters(new InputFilter[]{filter});


        // 定义允许的字符集，包括字母、数字、空格、常见标点符号和中文字符
        String allowedCharacters1 = "^[a-zA-Z0-9\\s._\\-\\u4E00-\\u9FA5]+$";

        // 创建自定义的InputFilter
        InputFilter filter1 = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source != null && !source.toString().matches(allowedCharacters1)) {
                    return "";
                }
                return null;
            }
        };
        // 将自定义的InputFilter应用到EditText，并设置最大长度为30
        mNickNameEt.setFilters(new InputFilter[]{filter1, new InputFilter.LengthFilter(12)});

    }

    @Override
    protected void initData() {
        mServerEt.setText(SPUtils.getInstance(SmartConstants.SP_NAME).getString(SmartConstants.CONSTANT_DOMAIN));
    }


    @SingleClick
    @OnClick({R.id.sdv_avatar, R.id.btn_register,
    })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sdv_avatar:
                ImageSelectActivity.start(this, data -> {
                    // 裁剪头像
                    cropImageFile(new File(data.get(0)));
                });
                break;
            case R.id.btn_register:
                mDialog.setMessage(getString(R.string.registering));
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();

                String nickName = mNickNameEt.getText().toString();
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
                if (!validatePassword(password)) {
                    mDialog.dismiss();
                    toast(R.string.password_rules);
                    return;
                }
                this.passWord = password;
                this.nickName = nickName;
                if (SmartCommHelper.getInstance().isDeveloperMode()) {
                    mRegisterBtn.setEnabled(false);
                    SmartIMClient.getInstance().connect(new ISmartCallback() {
                        @Override
                        public void onSuccess() {
                            registerXmpp();
                        }

                        @Override
                        public void onFailed(int code, String desc) {
                            mDialog.dismiss();
                            mRegisterBtn.setEnabled(true);
                            toast(desc);
                        }
                    });
                    return;
                }
                mRegisterBtn.setEnabled(false);
                break;
        }
    }

    /**
     * 裁剪图片
     */
    private void cropImageFile(File sourceFile) {
        ImageCropActivity.start(this, sourceFile, 1, 1, new ImageCropActivity.OnCropListener() {
            @Override
            public void onSucceed(Uri fileUri, String fileName) {
                File outputFile;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    outputFile = new FileContentResolver(getActivity(), fileUri, fileName);
                } else {
                    try {
                        outputFile = new File(new URI(fileUri.toString()));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                        outputFile = new File(fileUri.toString());
                    }
                }
                updateCropImage(outputFile, true);
            }

            @Override
            public void onError(String details) {
                // 没有的话就不裁剪，直接上传原图片
                // 但是这种情况极其少见，可以忽略不计
                updateCropImage(sourceFile, false);
            }
        });
    }

    private void updateCropImage(File file, boolean deleteFile) {
        Uri contentUri = null;
        if (file instanceof FileContentResolver) {
            contentUri = ((FileContentResolver) file).getContentUri();
        } else {
            contentUri = Uri.fromFile(file);
        }
        Trace.d("updateCropImage: " + file);
        Uri finalContentUri = contentUri;
        Tiny.FileCompressOptions options = new Tiny.FileCompressOptions();
        Tiny.getInstance()
                .source(contentUri)
                .asFile()
                .withOptions(options)
                .compress(new FileCallback() {
                    @Override
                    public void callback(boolean isSuccess, String outfile, Throwable t) {
                        Trace.d("callback: 输出文件路径" + outfile);
                        try {
                            File file1 = new File(outfile);
                            bitmapData = BitmapUtils.getFileBytes(file1);
                            Glide.with(RegisterActivity.this).load(bitmapData).into(mAvatarSdv);
                        } catch (IOException e) {
                        }
                    }
                });
    }

    /**
     * 注册xmpp账号
     */
    private void registerXmpp() {
        SmartIMClient.getInstance()
                .getSmartCommUserManager()
                .createAccount(mUsernameData, passWord, nickName, new ISmartCallback() {
                    @Override
                    public void onSuccess() {
                        SmartIMClient.getInstance().getSmartCommUserManager().login(mUsernameData, passWord, new ILoginCallback() {
                            @Override
                            public void onSuccess() {
                                registerSuccess(mUsernameData, passWord, "");
                            }

                            @Override
                            public void onError(int code, String desc) {
                                mRegisterBtn.setEnabled(true);
                                toast(desc);
                            }
                        });
                    }

                    @Override
                    public void onFailed(int code, String desc) {
                        mDialog.dismiss();
                        mRegisterBtn.setEnabled(true);
                        toast(desc);
                    }
                });
    }

    /**
     * 注册成功后跳转
     */
    private void registerSuccess(String mUsername, String mPassword, String unionid) {
        Trace.d(mUsername + ">>>>>>>onSuccess: 注册成功登录成功了，跳转MainActivity>>>>>>" + mPassword,
                "nickname" + nickName);
        PreferencesUtil.getInstance().setUserId(SmartCommHelper.getInstance().getUserIdByAccount(mUsername));
        CryptoUtil.encryptAndSaveCredentials2(mUsername, mPassword);
        String jid = SmartCommHelper.getInstance().getUserIdByAccount(mUsername);
        SmartUserInfo myInfo = new SmartUserInfo();
        myInfo.setUserAvatar(bitmapData);
        myInfo.setNickname(nickName);
        SmartIMClient.getInstance().getSmartCommUserManager().setMyUserInfo(myInfo, new IUserInfoCallback2() {
            @Override
            public void onSuccess(SmartUserInfo userInfo) {
                mDialog.dismiss();
                Trace.d("registerSuccess: 设置自己的信息 register");
                User user = new User();
                // 注意from可能是null
                user.setBelongAccount(jid);
                user.setUserId(jid);
                user.setUserNickName(nickName);
                user.setIsFriend(Constant.IS_MYSELF);
                user.setUnionid(unionid);
                user.setUserAccount(mUsernameData);
                UserDao.getInstance().saveOrUpdateContact(user);
                PreferencesUtil.getInstance().setUser(user);
                PreferencesUtil.getInstance().setLogin(true);
                userInfo.setUserAvatarHash(CommonUtil.generateId());
                Trace.d("registerSuccess: bitmapData " + bitmapData,
                        jid);
                if (bitmapData != null) {
                    AvatarGenerator.saveAvatarFileByByte(bitmapData, userInfo, false);
                }
                // 获取好友列表
                // 登录完后再添加监听器
                mDialog.dismiss();
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailed(int code, String desc) {
                mDialog.dismiss();
            }
        });
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
            checkSubmit();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

    /**
     * 表单是否填充完成(昵称,手机号,密码,是否同意协议)
     */
    private void checkSubmit() {
        // todo 忽略手机号的注册  passwordHasText trim空格的判断
        boolean nickNameHasText = !mNickNameEt.getText().toString().trim().isEmpty();
        boolean accountText = !mAccountEt.getText().toString().trim().isEmpty();
        boolean passwordHasText = !mPasswordEt.getText().toString().isEmpty();
        boolean serverText = !mServerEt.getText().toString().isEmpty();
        boolean enableRegisterBtn;
        // 验证码登录
        enableRegisterBtn = nickNameHasText && accountText && passwordHasText  && serverText;
        mRegisterBtn.setEnabled(enableRegisterBtn);
        mRegisterBtn.setBackgroundColor(enableRegisterBtn ?
                getColor(R.color.register_btn_bg_enable)
                : getColor(R.color.register_btn_bg_disable));
        mRegisterBtn.setTextColor(enableRegisterBtn ?
                getColor(R.color.register_btn_text_enable)
                : getColor(R.color.register_btn_text_disable));
    }
}
