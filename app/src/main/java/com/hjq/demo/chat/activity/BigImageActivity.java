package com.hjq.demo.chat.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.hjq.demo.R;
import com.hjq.demo.aop.Log;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.utils.AvatarGenerator;
import com.hjq.demo.http.glide.GlideApp;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class BigImageActivity extends Activity {
    private PhotoView pv;
    private String imgUrl;

    @Log
    public static void start(Context context, String userId, String userNickName) {
        Intent intent = new Intent(context, BigImageActivity.class);
        intent.putExtra(Constant.CONTACT_ID, userId);
        intent.putExtra(Constant.CONTACT_NICK_NAME, userNickName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_big_image);

        initView();
        initData();
        initEvent();
    }

    private void initView() {
        pv = findViewById(R.id.pv);
    }

    private void initData() {
        imgUrl = getIntent().getStringExtra("imgUrl");
        if (!TextUtils.isEmpty(imgUrl)) {
            // 设置图片url
            GlideApp.with(this)
                    .load(imgUrl)
                    .placeholder(R.mipmap.default_user_avatar)
                    .into(pv);
        } else {
            String userId = getIntent().getStringExtra(Constant.CONTACT_ID);
            String userNickname = getIntent().getStringExtra(Constant.CONTACT_NICK_NAME);
            if (!TextUtils.isEmpty(userId)) {
                AvatarGenerator.loadAvatar(this, userId, userNickname, pv, true);
            }
        }

    }

    private void initEvent() {
        //添加点击事件
        pv.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float v, float v1) {
                finish();
            }
        });
    }
}
