package com.hjq.demo.chat.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.hjq.demo.R;
import com.hjq.demo.chat.activity.MainActivity;
import com.hjq.demo.chat.activity.SearchActivity;
import com.hjq.demo.chat.activity.UserInfoActivity;
import com.hjq.demo.chat.activity.WebViewActivity;
import com.hjq.demo.chat.entity.QrCodeContent;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.chat.widget.ConfirmDialog;
import com.hjq.demo.utils.JsonParser;
import com.hjq.toast.ToastUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 发现
 *
 * @author zhou
 */
public class DiscoverFragment extends BaseChatFragment {

    @BindView(R.id.tv_title)
    TextView mTitleTv;

    // 开启"附近的人"标记
    @BindView(R.id.iv_open_people_nearby)
    ImageView mOpenPeopleNearbyIv;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitleStrokeWidth(mTitleTv);

        if (PreferencesUtil.getInstance().isOpenPeopleNearby()) {
            mOpenPeopleNearbyIv.setVisibility(View.VISIBLE);
        } else {
            mOpenPeopleNearbyIv.setVisibility(View.GONE);
        }

    }

    public static DiscoverFragment newInstance() {

        Bundle args = new Bundle();

        DiscoverFragment fragment = new DiscoverFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_discover;
    }

    @Override
    protected void initView() {
        ButterKnife.bind(this, getView());
    }

    @Override
    protected void initData() {

    }

    @Override
    public void onResume() {
        super.onResume();
        if (PreferencesUtil.getInstance().isOpenPeopleNearby()) {
            mOpenPeopleNearbyIv.setVisibility(View.VISIBLE);
        } else {
            mOpenPeopleNearbyIv.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == MainActivity.REQUEST_CODE_SCAN) {
                String isbn = data.getStringExtra("CaptureIsbn");
                if (!TextUtils.isEmpty(isbn)) {
                    if (isbn.contains("http")) {
                        Intent intent = new Intent(getActivity(), WebViewActivity.class);
                        intent.putExtra(WebViewActivity.RESULT, isbn);
                        startActivity(intent);
                    } else {
                        try {
                            QrCodeContent qrCodeContent = JsonParser.deserializeByJson(isbn, QrCodeContent.class);
                            if (qrCodeContent != null && QrCodeContent.QR_CODE_TYPE_USER.equals(qrCodeContent.getType())) {
                                UserInfoActivity.start(getActivity(), qrCodeContent.getUserid());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @OnClick({R.id.rl_moments, R.id.rl_scan, R.id.rl_search,
            R.id.rl_people_nearby, R.id.rl_game, R.id.rl_mini_programs})
    public void onClick(View view) {
        String[] permissions;
        switch (view.getId()) {
            case R.id.rl_moments:
                break;
            case R.id.rl_scan:
                break;
            case R.id.rl_search:
                startActivity(new Intent(getActivity(), SearchActivity.class));
                break;
            case R.id.rl_people_nearby:
                // 动态申请定位权限
                break;
            case R.id.rl_game:
                break;
            case R.id.rl_mini_programs:
                break;
        }
    }

}