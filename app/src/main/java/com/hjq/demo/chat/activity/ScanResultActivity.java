package com.hjq.demo.chat.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.tv.TvView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.SizeUtils;
import com.hjq.demo.R;
import com.hjq.demo.aop.Log;
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
public class ScanResultActivity extends ChatBaseActivity {

    private static final String SCAN_RESULT = "SCAN_RESULT";
    @BindView(R.id.tv_title)
    TextView mTitleTv;
    @BindView(R.id.textViewScanResult)
    TextView textViewScanResult;

    @Override
    public int getLayoutId() {
        return R.layout.scan_result_activity;
    }

    @Override
    public void initView() {
        mTitleTv.setText(getString(R.string.scan_result));
        setTitleStrokeWidth(mTitleTv);
    }

    @Override
    public void initListener() {

    }

    @Log
    public static void start(Context context, String param) {
        Intent intent = new Intent(context, ScanResultActivity.class);
        intent.putExtra(SCAN_RESULT, param);
        context.startActivity(intent);
    }

    @Override
    public void initData() {
        String stringExtra = getIntent().getStringExtra(SCAN_RESULT);
        textViewScanResult.setText(stringExtra);
    }

}