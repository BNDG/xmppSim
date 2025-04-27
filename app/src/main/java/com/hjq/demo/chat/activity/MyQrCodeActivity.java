package com.hjq.demo.chat.activity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.SizeUtils;
import com.hjq.demo.R;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.entity.QrCodeContent;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.utils.AvatarGenerator;
import com.hjq.demo.http.glide.GlideApp;
import com.hjq.demo.utils.JsonParser;
import com.hjq.demo.utils.Trace;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.hmsscankit.WriterException;
import com.huawei.hms.ml.scan.HmsBuildBitmapOption;
import com.huawei.hms.ml.scan.HmsScan;

import butterknife.BindView;

/**
 * 二维码名片
 *
 * @author zhou
 */
public class MyQrCodeActivity extends ChatBaseActivity {

    @BindView(R.id.tv_title)
    TextView mTitleTv;

    @BindView(R.id.sdv_avatar)
    ImageView mAvatarSdv;

    @BindView(R.id.tv_nick_name)
    TextView mNickNameTv;

    @BindView(R.id.iv_sex)
    ImageView mSexIv;

    @BindView(R.id.tv_region)
    TextView mRegionTv;

    @BindView(R.id.sdv_qr_code)
    ImageView mQrCodeSdv;


    @Override
    public int getLayoutId() {
        return R.layout.activity_my_qr_code;
    }

    @Override
    public void initView() {
        mTitleTv.setText(R.string.qr_code);
        setTitleStrokeWidth(mTitleTv);
    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {
        AvatarGenerator.loadAvatar(this, myUserInfo.getUserId(), myUserInfo.getUserNickName(), mAvatarSdv, true);
        mNickNameTv.setText(myUserInfo.getUserNickName());
        if (Constant.USER_SEX_MALE.equals(myUserInfo.getUserSex())) {
            mSexIv.setImageResource(R.drawable.icon_sex_male);
        } else if (Constant.USER_SEX_FEMALE.equals(myUserInfo.getUserSex())) {
            mSexIv.setImageResource(R.drawable.icon_sex_female);
        } else {
            mSexIv.setVisibility(View.GONE);
        }

        mRegionTv.setText(myUserInfo.getUserRegion());
        HmsBuildBitmapOption options = new HmsBuildBitmapOption.Creator().
                setBitmapMargin(1).setBitmapColor(getColor(R.color.primary_chat_user))
                .setBitmapBackgroundColor(Color.WHITE).create();
        Bitmap resultImage = null;
        try {
            resultImage = ScanUtil.buildBitmap(generateQRCode(myUserInfo), HmsScan.QRCODE_SCAN_TYPE, SizeUtils.dp2px(256), SizeUtils.dp2px(256), options);
            GlideApp.with(this)
                    .load(resultImage)
                    .into(mQrCodeSdv);
        } catch (WriterException e) {
            Trace.d("initData: " + e);
        }
    }

    /**
     * 生成二维码名片
     *
     * @param user
     * @return
     */
    private String generateQRCode(User user) {
        QrCodeContent qrCodeContent = new QrCodeContent();
        qrCodeContent.setAliasusername(user.getUserAccount());
        qrCodeContent.setPhone(user.getUserPhone());
        qrCodeContent.setUserid(user.getUserId());
        qrCodeContent.setNickname(user.getUserNickName());
        qrCodeContent.setType(QrCodeContent.QR_CODE_TYPE_USER);
        return QrCodeContent.START + JsonParser.serializeToJson(qrCodeContent);
    }

}