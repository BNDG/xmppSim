package com.hjq.demo.chat.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.FileUtils;
import com.hjq.demo.R;
import com.hjq.demo.chat.dao.DBManager;
import com.hjq.demo.chat.entity.AvatarEntity;
import com.hjq.demo.chat.entity.ChatEvent;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.utils.AvatarGenerator;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.chat.widget.LoadingDialog;
import com.hjq.demo.ui.activity.ImageCropActivity;
import com.hjq.demo.ui.activity.ImageSelectActivity;
import com.hjq.demo.utils.Trace;
import com.hjq.http.model.FileContentResolver;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.FileCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.callback.IUserInfoCallback2;
import com.bndg.smack.model.SmartUserInfo;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * 个人信息
 *
 * @author zhou
 */
public class MyProfileActivity extends ChatBaseActivity {

    // 昵称
    @BindView(R.id.rl_nick_name)
    RelativeLayout mNickNameRl;

    // kptk号
    @BindView(R.id.rl_wx_id)
    RelativeLayout mWxIdRl;

    // 二维码
    @BindView(R.id.rl_qr_code)
    RelativeLayout mQrCodeRl;

    // 更多
    @BindView(R.id.rl_more)
    RelativeLayout mMoreRl;

    @BindView(R.id.tv_title)
    TextView mTitleTv;

    @BindView(R.id.tv_nick_name)
    TextView mNickNameTv;

    @BindView(R.id.tv_wx_id)
    TextView mWxIdTv;

    @BindView(R.id.sdv_avatar)
    ImageView mAvatarSdv;

    @BindView(R.id.iv_wx_id)
    ImageView mWxIdIv;

    LoadingDialog mDialog;

    private static final int UPDATE_AVATAR_BY_TAKE_CAMERA = 1;
    private static final int UPDATE_AVATAR_BY_ALBUM = 2;
    private static final int UPDATE_USER_NICK_NAME = 3;
    private static final int UPDATE_USER_WX_ID = 4;

    String mImageName;

    @Override
    public int getLayoutId() {
        return R.layout.activity_my_profile;
    }

    @Override
    public void initView() {
        mTitleTv.setText(R.string.personal_info);
        setTitleStrokeWidth(mTitleTv);
    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {
        mDialog = new LoadingDialog(MyProfileActivity.this);
        AvatarGenerator.loadAvatar(getActivity(), myUserInfo.getUserId(), myUserInfo.getUserNickName(), mAvatarSdv, true);
        renderWxId(myUserInfo);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNickNameTv.setText(myUserInfo.getUserNickName());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ChatEvent event) {
        if (ChatEvent.REFRESH_USER_AVATAR.equals(event.getWhat())) {
            if (PreferencesUtil.getInstance().getUserId().equals(event.obj)) {
                AvatarGenerator.loadAvatar(getActivity(), myUserInfo.getUserId(), myUserInfo.getUserNickName(), mAvatarSdv, true);
            }
        }
    }

    @OnClick({R.id.rl_avatar, R.id.sdv_avatar, R.id.rl_nick_name, R.id.rl_wx_id,
            R.id.rl_qr_code, R.id.rl_more})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_avatar:
                ImageSelectActivity.start(this, data -> {
                    // 裁剪头像
                    cropImageFile(new File(data.get(0)));
                });
                break;
            case R.id.sdv_avatar:
                DBManager.Companion.getInstance(getContext())
                        .getAvatarByUserId(PreferencesUtil.getInstance().getUserId())
                        .subscribe(new Consumer<List<AvatarEntity>>() {
                            @Override
                            public void accept(List<AvatarEntity> avatarEntities) throws Throwable {
                                if (!avatarEntities.isEmpty()) {
                                    AvatarEntity avatarEntity = avatarEntities.get(0);
                                    File file = new File(avatarEntity.getAvatarLocalPath());
                                    if (FileUtils.isFileExists(file)) {
                                        Intent intent = new Intent(MyProfileActivity.this, BigImageActivity.class);
                                        intent.putExtra("imgUrl", avatarEntity.getAvatarLocalPath());
                                        startActivity(intent);
                                    }
                                }
                            }
                        });
                break;
            case R.id.rl_nick_name:
                // 昵称
                startActivityForResult(new Intent(this, EditNameActivity.class), UPDATE_USER_NICK_NAME);
                break;
            case R.id.rl_wx_id:
                break;
            case R.id.rl_qr_code:
                startActivity(new Intent(this, MyQrCodeActivity.class));
                break;
            case R.id.rl_more:
                startActivity(new Intent(this, MyMoreProfileActivity.class));
                break;
        }
    }

    /**
     * 渲染ID
     */
    private void renderWxId(User user) {
        mWxIdTv.setText(user.getUserId());
        mWxIdRl.setClickable(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case UPDATE_USER_NICK_NAME:
                    // 昵称
                    mNickNameTv.setText(myUserInfo.getUserNickName());
                    break;
                case UPDATE_USER_WX_ID:
                    renderWxId(myUserInfo);
                    break;
            }
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

    /**
     * 更新裁剪后的图片
     *
     * @param file
     * @param deleteFile
     */
    private void updateCropImage(File file, boolean deleteFile) {
        mDialog.setMessage(getString(R.string.uploading));
        mDialog.show();
        mDialog.setCanceledOnTouchOutside(false);
        Uri contentUri = null;
        if (file instanceof FileContentResolver) {
            contentUri = ((FileContentResolver) file).getContentUri();
        } else {
            contentUri = Uri.fromFile(file);
        }
        Trace.d("updateCropImage: " + file);
        Tiny.FileCompressOptions options = new Tiny.FileCompressOptions();
        Tiny.getInstance()
                .source(contentUri)
                .asFile()
                .withOptions(options)
                .compress(new FileCallback() {
                    @Override
                    public void callback(boolean isSuccess, String outfile, Throwable t) {
                        Trace.d("callback: 输出文件路径" + outfile);
                        SmartIMClient.getInstance().getSmartCommUserManager().changeImage(new File(outfile), new IUserInfoCallback2() {
                            @Override
                            public void onSuccess(SmartUserInfo userInfo) {
                                mDialog.dismiss();
                                AvatarGenerator.saveAvatarFileByUserInfo(userInfo, true);
                            }

                            @Override
                            public void onFailed(int code, String desc) {
                                mDialog.dismiss();
                                toast(desc);
                            }
                        });
                    }
                });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}