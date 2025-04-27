package com.hjq.demo.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hjq.base.BaseActivity;
import com.hjq.base.BaseAdapter;
import com.hjq.base.RecyclerPagerAdapter;
import com.hjq.demo.R;
import com.hjq.demo.aop.Log;
import com.hjq.demo.app.AppActivity;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.entity.ChatEvent;
import com.hjq.demo.chat.service.DownloadService;
import com.hjq.demo.ui.adapter.ImagePreviewAdapter;
import com.hjq.demo.utils.OpenFileUtils;
import com.hjq.demo.utils.Trace;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.bndg.smack.enums.SmartContentType;
import me.minetsh.imaging.IMGEditActivity;
import me.relex.circleindicator.CircleIndicator;

/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2019/03/05
 * desc   : 查看大图 viewpager
 */
public final class ImagePreviewActivity extends AppActivity
        implements ViewPager.OnPageChangeListener,
        BaseAdapter.OnItemClickListener {

    private static final String INTENT_KEY_IN_IMAGE_LIST = "imageList";
    private static final String INTENT_KEY_IN_IMAGE_INDEX = "imageIndex";
    private View iv_download;
    private View iv_share;
    private String fileLocalPath;
    private String originId;
    private int currentPosition;
    private RecyclerPagerAdapter recyclerPagerAdapter;

    public static void start(Context context, String url) {
        ArrayList<String> images = new ArrayList<>(1);
        images.add(url);
        start(context, images);
    }

    public static void start(Context context, String url, Bundle bundle) {
        ArrayList<String> images = new ArrayList<>(1);
        images.add(url);
        start(context, images, 0, bundle);
    }

    public static void start(Context context, List<String> urls) {
        start(context, urls, 0, new Bundle());
    }

    @Log
    public static void start(Context context, List<String> urls, int index, Bundle bundle, ImageSelectActivity.OnPhotoSelectListener listener) {
        if (urls == null || urls.isEmpty()) {
            return;
        }
        Intent intent = new Intent(context, ImagePreviewActivity.class);
        if (urls.size() > 2000) {
            // 请注意：如果传输的数据量过大，会抛出此异常，并且这种异常是不能被捕获的
            // 所以当图片数量过多的时候，我们应当只显示一张，这种一般是手机图片过多导致的
            // 经过测试，传入 3121 张图片集合的时候会抛出此异常，所以保险值应当是 2000
            // android.os.TransactionTooLargeException: data parcel size 521984 bytes
            urls = Collections.singletonList(urls.get(index));
        }

        if (urls instanceof ArrayList) {
            intent.putExtra(INTENT_KEY_IN_IMAGE_LIST, (ArrayList<String>) urls);
        } else {
            intent.putExtra(INTENT_KEY_IN_IMAGE_LIST, new ArrayList<>(urls));
        }
        intent.putExtras(bundle);
        intent.putExtra(INTENT_KEY_IN_IMAGE_INDEX, index);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (listener != null && context instanceof BaseActivity) {
            ((BaseActivity) context).startActivityForResult(intent, (resultCode, data) -> {
                if (listener == null) {
                    return;
                }

                if (data == null) {
                    listener.onCancel();
                    return;
                }
                ArrayList<String> list = data.getStringArrayListExtra(ImageSelectActivity.INTENT_KEY_OUT_IMAGE_LIST);
                boolean isOriginalData = !TextUtils.isEmpty(data.getStringExtra(ImageSelectActivity.IS_ORIGINAL_DATA));
                if (list == null || list.isEmpty()) {
                    listener.onCancel();
                    return;
                }

                Iterator<String> iterator = list.iterator();
                while (iterator.hasNext()) {
                    if (!new File(iterator.next()).isFile()) {
                        iterator.remove();
                    }
                }
                if (resultCode == RESULT_OK && !list.isEmpty()) {
                    if (isOriginalData) {
                        boolean isOriginal = data.getBooleanExtra(ImageSelectActivity.IS_ORIGINAL, false);
                        listener.onSelected(list, isOriginal);
                    } else {
                        listener.onSelected(list);
                    }
                    return;
                }
                listener.onCancel();

            });
        } else {
            context.startActivity(intent, bundle);
        }
    }

    @Log
    public static void start(Context context, List<String> urls, int index, Bundle bundle) {
        start(context, urls, index, bundle, null);
    }

    private boolean canEdit;
    private ViewPager mViewPager;
    private ImagePreviewAdapter mAdapter;

    /**
     * 圆圈指示器
     */
    private CircleIndicator mCircleIndicatorView;
    /**
     * 文本指示器
     */
    private TextView mTextIndicatorView;

    @Override
    protected int getLayoutId() {
        return R.layout.image_preview_activity;
    }

    @Override
    protected void initView() {
        mViewPager = findViewById(R.id.vp_image_preview_pager);
        mCircleIndicatorView = findViewById(R.id.ci_image_preview_indicator);
        mTextIndicatorView = findViewById(R.id.tv_image_preview_indicator);
        iv_download = findViewById(R.id.iv_download);
        iv_share = findViewById(R.id.iv_share);
        setOnClickListener(R.id.tv_edit, R.id.tv_send);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void initData() {
        canEdit = getBoolean(Constant.EDIT_PHOTO);
        if (canEdit) {
            findViewById(R.id.tv_edit).setVisibility(View.VISIBLE);
            findViewById(R.id.tv_send).setVisibility(View.VISIBLE);
        }
        ArrayList<String> images = getStringArrayList(INTENT_KEY_IN_IMAGE_LIST);
        if (images == null || images.isEmpty()) {
            finish();
            return;
        }
        mAdapter = new ImagePreviewAdapter(this);
        mAdapter.setData(images);
        mAdapter.setOnItemClickListener(this);
        recyclerPagerAdapter = new RecyclerPagerAdapter(mAdapter);
        mViewPager.setAdapter(recyclerPagerAdapter);
        if (images.size() != 1) {
            if (images.size() < 10) {
                // 如果是 10 张以内的图片，那么就显示圆圈指示器
                mCircleIndicatorView.setVisibility(View.VISIBLE);
                mCircleIndicatorView.setViewPager(mViewPager);
            } else {
                // 如果超过 10 张图片，那么就显示文字指示器
                mTextIndicatorView.setVisibility(View.VISIBLE);
                mViewPager.addOnPageChangeListener(this);
            }

            int index = getInt(INTENT_KEY_IN_IMAGE_INDEX);
            if (index < images.size()) {
                mViewPager.setCurrentItem(index);
                onPageSelected(index);
            }
        } else {
            // 单张图片 图片是存放在应用目录 所以能够file
            fileLocalPath = getString(Constant.MESSAGE_FILE_LOCAL);
            originId = getString(Constant.MESSAGE_ORIGIN_ID);
            if (!TextUtils.isEmpty(originId)) {
                if (!TextUtils.isEmpty(fileLocalPath)) {
                    iv_share.setVisibility(View.VISIBLE);
                    iv_share.setOnClickListener(v -> {
                        Intent imageIntent = Intent.createChooser(OpenFileUtils.getImageFileIntent(ImagePreviewActivity.this,
                                new File(fileLocalPath)), "分享到");
                        startActivity(imageIntent);
                    });
                } else {
                    iv_download.setVisibility(View.VISIBLE);
                    iv_download.setOnClickListener(v -> {
                        toast(getString(R.string.downloading));
                        // 一定是url
                        String url = images.get(0);
                        DownloadService.Companion.startService(this, url, originId, SmartContentType.IMAGE);
                    });
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ChatEvent chatEvent) {
        if (ChatEvent.FILE_DOWNLOAD_COMPLETE.equals(chatEvent.getWhat())) {
            Bundle bundle = chatEvent.bundle;
            String eventOriginId = bundle.getString(Constant.MESSAGE_ORIGIN_ID);
            if (originId.equals(eventOriginId)) {
                iv_share.setVisibility(View.VISIBLE);
                fileLocalPath = bundle.getString(Constant.MESSAGE_FILE_LOCAL);
            }
        }
    }

    @NonNull
    @Override
    protected ImmersionBar createStatusBarConfig() {
        return super.createStatusBarConfig()
                // 隐藏状态栏和导航栏
                .hideBar(BarHide.FLAG_HIDE_BAR);
    }

    @Override
    public boolean isStatusBarDarkFont() {
        return false;
    }

    /**
     * {@link ViewPager.OnPageChangeListener}
     */

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onPageSelected(int position) {
        currentPosition = position;
        mTextIndicatorView.setText((position + 1) + "/" + mAdapter.getCount());
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewPager.removeOnPageChangeListener(this);
        EventBus.getDefault().unregister(this);
    }

    /**
     * {@link BaseAdapter.OnItemClickListener}
     *
     * @param recyclerView RecyclerView 对象
     * @param itemView     被点击的条目对象
     * @param position     被点击的条目位置
     */
    @Override
    public void onItemClick(RecyclerView recyclerView, View itemView, int position) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        // 单击图片退出当前的 Activity
        onBackPressed();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tv_edit) {
//            /storage/emulated/0/DCIM/CropImage/CROP_20241120_191647.jpeg
            // 微信是存储到了公共目录下
            File tempFile = new File(getCacheDir(), System.currentTimeMillis() + "_temp.jpg");
            Intent intent = new Intent(this, IMGEditActivity.class);
            String item = mAdapter.getItem(currentPosition);
            Uri uri = Uri.fromFile(new File(item));
            Trace.d("onClick: " + uri + " =item " + item);
            intent.putExtra(IMGEditActivity.EXTRA_IMAGE_URI, uri);
            intent.putExtra(IMGEditActivity.EXTRA_IMAGE_SAVE_PATH, tempFile.getAbsolutePath());
            startActivityForResult(intent, new OnActivityCallback() {
                @Override
                public void onActivityResult(int resultCode, @Nullable Intent data) {
                    if (resultCode == RESULT_OK) {
                        // 数据源改成编辑后的图片
                        mAdapter.getData().set(currentPosition, tempFile.getAbsolutePath());
                        // viewpage item只有3个mViewPager
                        recyclerPagerAdapter.setNeedRefresh(true);
                        recyclerPagerAdapter.notifyDataSetChanged();
                        // 还需要更改 宫格选择图片activity的数据源
                    }
                }
            });
        } else if (view.getId() == R.id.tv_send) {
            // 用什么方式通知 eventbus还是setResult
            // 完成选择
            String item = mAdapter.getItem(currentPosition);
            ArrayList<String> mSelectImage = new ArrayList<>();
            mSelectImage.add(item);
            Intent data = new Intent()
                    .putStringArrayListExtra(ImageSelectActivity.INTENT_KEY_OUT_IMAGE_LIST, mSelectImage);
            setResult(RESULT_OK, data);
            finish();
        }
    }
}
