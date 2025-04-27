package com.hjq.demo.chat.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hjq.demo.R;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.DBManager;
import com.hjq.demo.chat.dao.UserDao;
import com.hjq.demo.chat.entity.QrCodeContent;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.listener.ContactCallback;
import com.hjq.demo.chat.utils.AvatarGenerator;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.other.PermissionCallback;
import com.hjq.demo.utils.JsonParser;
import com.hjq.demo.utils.Trace;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;
import com.rxjava.rxlife.RxLife;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.callback.IUserInfoCallback2;
import com.bndg.smack.model.SmartUserInfo;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * 添加朋友
 * 主页点击
 * @author zhou
 */
public class AddContactsActivity extends ChatBaseActivity {

    private static final int REQUEST_CODE_SCAN = 0;

    @BindView(R.id.tv_title)
    TextView mTitleTv;

    @BindView(R.id.tv_wx_id)
    TextView mWxIdTv;

    @Override
    public int getLayoutId() {
        return R.layout.activity_add_contacts;
    }

    @Override
    public void initView() {
        setTitleStrokeWidth(mTitleTv);
    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {
        String userPhone = myUserInfo.getUserPhone();
        if (!TextUtils.isEmpty(userPhone)) {
            mWxIdTv.setText(String.format(getString(R.string.account_label), userPhone));
        } else {
            mWxIdTv.setText(String.format(getString(R.string.account_label), myUserInfo.getUserAccount()));
        }
        mTitleTv.setText(getString(R.string.add_contacts));
    }

    @OnClick({R.id.rl_search, R.id.rl_friend_radar, R.id.rl_scan, R.id.ll_my_info})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_search:
                startActivity(new Intent(this, AddFriendsBySearchActivity.class));
                break;

            case R.id.rl_friend_radar:
                break;
            case R.id.rl_scan:
                XXPermissions.with(this)
                        .permission(Permission.CAMERA)
                        .permission(Permission.READ_EXTERNAL_STORAGE)
                        .permission(Permission.WRITE_EXTERNAL_STORAGE)
                        .request(new PermissionCallback() {

                            @Override
                            public void onGranted(List<String> permissions, boolean all) {
                                if (all) {
                                    ScanUtil.startScan(AddContactsActivity.this, Constant.REQUEST_CODE_SCAN_ONE, new HmsScanAnalyzerOptions.Creator().create());
                                }
                            }
                        });
                break;

            case R.id.ll_my_info:
                startActivity(new Intent(this, MyQrCodeActivity.class));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) {
            return;
        }
        //Default View
        if (requestCode == Constant.REQUEST_CODE_SCAN_ONE) {
            HmsScan obj = data.getParcelableExtra(ScanUtil.RESULT);
            if (obj != null) {
                Trace.d("onActivityResult: ===" + obj.getOriginalValue());
                String scanResult = obj.getOriginalValue();
                if (TextUtils.isEmpty(scanResult)) {
                    return;
                }
                if (scanResult.startsWith(QrCodeContent.START)) {
                    String result = scanResult.substring(scanResult.indexOf(",") + 1);
                    QrCodeContent qrCodeContent = JsonParser.deserializeByJson(result, QrCodeContent.class);
                    if (qrCodeContent != null && QrCodeContent.QR_CODE_TYPE_USER.equals(qrCodeContent.getType())) {
                        String imUserId = qrCodeContent.getUserid();
                        if (myUserInfo.getUserId().equals(imUserId)) {
                            // 是自己
                            Intent intent = new Intent(AddContactsActivity.this, UserInfoMyActivity.class);
                            startActivity(intent);
                        } else {
                            UserDao.getInstance().getUserById(AddContactsActivity.this,
                                    imUserId, new ContactCallback() {
                                        @Override
                                        public void getUser(@Nullable User userById) {
                                            if (userById != null && userById.isFriend()) {
                                                // 好友，进入用户详情页
                                                UserInfoActivity.start(AddContactsActivity.this, userById.getUserId());
                                            } else {
                                                // 陌生人，进入陌生人详情页
                                                processStrangerInfo(imUserId, qrCodeContent.getNickname(), qrCodeContent.getPhone(), qrCodeContent.getAliasusername());
                                            }
                                        }
                                    });
                        }
                    }
                } else {
                    ScanResultActivity.start(this, scanResult);
                }
            }
        }
        if (requestCode == REQUEST_CODE_SCAN) {
            String isbn = data.getStringExtra("CaptureIsbn");
            if (!TextUtils.isEmpty(isbn)) {
                if (isbn.contains("http")) {
                    Intent intent = new Intent(this, WebViewActivity.class);
                    intent.putExtra(WebViewActivity.RESULT, isbn);
                    startActivity(intent);
                }
            }
        }
    }

    /**
     * 通过xmpp jid搜索用户 要求对方有名片
     *
     * @param jid
     * @param tNickname
     */
    private void processStrangerInfo(String jid, String tNickname, String phoneData, String accountData) {
        showDialog();
        SmartIMClient.getInstance().getSmartCommUserManager().getUserInfo(jid, new IUserInfoCallback2() {
            @Override
            public void onSuccess(SmartUserInfo userInfo) {
                hideDialog();
                String nickName = userInfo.getNickname();
                Trace.d("onClick: 查找到===" + nickName);
                if (TextUtils.isEmpty(nickName) && TextUtils.isEmpty(tNickname)) {
                    toast(getString(R.string.user_not_found));
                    return;
                }
                AvatarGenerator.saveAvatarFileByUserInfo(userInfo, false);
                User userById = new User();
                userById.setBelongAccount(PreferencesUtil.getInstance().getUserId());
                userById.setUserNickName(nickName);
                userById.setUserId(jid);
                userById.setUserPhone(phoneData);
                userById.setUserAccount(accountData);
                DBManager.Companion.getInstance(AddContactsActivity.this)
                        .saveContact(userById)
                        .to(RxLife.to(AddContactsActivity.this))
                        .subscribe(new CompletableObserver() {
                            @Override
                            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                            }

                            @Override
                            public void onComplete() {
                                UserInfoActivity.start(getContext(), jid, Constant.CONTACTS_FROM_WX_ID);
                            }

                            @Override
                            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

                            }
                        });
            }

            @Override
            public void onFailed(int code, String desc) {
                hideDialog();
                toast(desc);
            }
        });
    }

}
