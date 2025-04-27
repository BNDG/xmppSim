package com.hjq.demo.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bndg.smack.enums.SmartContentType;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hjq.base.BaseAdapter;
import com.hjq.base.RecyclerPagerAdapter;
import com.hjq.demo.R;
import com.hjq.demo.aop.Log;
import com.hjq.demo.app.AppActivity;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.MessageDao;
import com.hjq.demo.chat.entity.ChatEvent;
import com.hjq.demo.chat.entity.ChatImageBean;
import com.hjq.demo.chat.entity.ChatMessage;
import com.hjq.demo.chat.receiver.SmartMessageReceiver;
import com.hjq.demo.chat.service.DownloadService;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.ui.adapter.ImagePreviewMsgAdapter;
import com.hjq.demo.utils.OpenFileUtils;
import com.hjq.demo.utils.Trace;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.minetsh.imaging.IMGEditActivity;

/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2019/03/05
 * desc   : 查看大图 viewpager
 */
public final class ImagePreviewMsgActivity extends AppActivity
        implements ViewPager.OnPageChangeListener,
        BaseAdapter.OnItemClickListener {

    public static final int IMG_PAGE_SIZE = 100;
    private View iv_download;
    private View iv_share;
    private String currentFileLocalPath;
    private String currentOriginId;
    private String currentImageUrl;
    private int currentPosition;
    private RecyclerPagerAdapter recyclerPagerAdapter;
    private View tvEdit;

    @Log
    public static void start(Context context, Bundle bundle) {
        Intent intent = new Intent(context, ImagePreviewMsgActivity.class);
        intent.putExtras(bundle);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent, bundle);
    }


    private ViewPager mViewPager;
    private ImagePreviewMsgAdapter mAdapter;
    @Override
    protected int getLayoutId() {
        return R.layout.image_preview_activity;
    }

    @Override
    protected void initView() {
        mViewPager = findViewById(R.id.vp_image_preview_pager);
        iv_download = findViewById(R.id.iv_download);
        iv_share = findViewById(R.id.iv_share);
        tvEdit = findViewById(R.id.tv_edit);
        setOnClickListener(R.id.tv_edit);
        EventBus.getDefault().register(this);
        IntentFilter msgFilter = new IntentFilter();
        msgFilter.addAction(Constant.ACTION_DOWNLOAD_COMPLETE);
        SmartMessageReceiver smartMessageReceiver = new SmartMessageReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (PreferencesUtil.getInstance().getUser() == null) {
                    return;
                }
                // 处理接收到的广播，从意图中获取额外数据
                String action = intent.getAction();
                if (Constant.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    String originId = intent.getStringExtra(Constant.MESSAGE_ORIGIN_ID);
                    String localFilePath = intent.getStringExtra(Constant.MESSAGE_FILE_LOCAL);
                    List<ChatImageBean> data = mAdapter.getData();
                    for (ChatImageBean chatImageBean : data) {
                        if (chatImageBean.getOriginId().equals(originId)) {
                            chatImageBean.fileLocalPath = (localFilePath);
                            break;
                        }
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(smartMessageReceiver, msgFilter);
    }

    @Override
    protected void initData() {
        // 通过originId 查询conversationId的前后5条数据 如果查不到数据了 就添加一个标识 不再查询了
        String conversationId = getString(Constant.CONVERSATION_ID);
        String fileLocalPath = getString(Constant.MESSAGE_FILE_LOCAL);
        String originId = getString(Constant.MESSAGE_ORIGIN_ID);
        String messageContent = getString(Constant.MESSAGE_CONTENT);
        ChatImageBean chatImageBean = new ChatImageBean();
        chatImageBean.conversationId = conversationId;
        chatImageBean.fileUrl = messageContent;
        chatImageBean.setOriginId(originId);
        chatImageBean.fileLocalPath = fileLocalPath;
        List<ChatImageBean> images = new ArrayList<>();
        images.add(chatImageBean);
        mAdapter = new ImagePreviewMsgAdapter(this);
        mAdapter.setData(images);
        mAdapter.setOnItemClickListener(this);
        recyclerPagerAdapter = new RecyclerPagerAdapter(mAdapter);
        mViewPager.setAdapter(recyclerPagerAdapter);
        mViewPager.addOnPageChangeListener(this);
        // 初始只有一张图片
        int index = 0;
        if (index < images.size()) {
            mViewPager.setCurrentItem(index);
            onPageSelected(index);
        }
        iv_share.setOnClickListener(v -> {
            Intent imageIntent = Intent.createChooser(OpenFileUtils.getImageFileIntent(ImagePreviewMsgActivity.this,
                    new File(currentFileLocalPath)), "分享到");
            startActivity(imageIntent);
        });
        iv_download.setOnClickListener(v -> {
            toast(getString(R.string.downloading));
            // 一定是url
            DownloadService.Companion.startService(this, currentImageUrl, currentOriginId, SmartContentType.IMAGE);
        });
    }

    /**
     * 查询数据库
     *
     * @param position       当前位置
     * @param conversationId
     * @param originId
     */
    private void loadDbData(int position, String conversationId, String originId) {
        List<ChatImageBean> data = mAdapter.getData();
        int totalSize = data.size();
        // 如果当前位置小于总长度的一半，那么就从当前位置开始查询，如果大于总长度的一半，那么就从总长度减去当前位置开始查询
        // total = 11 position = 3 11 - 3
        // total = 22 position = 5
        // toaal = 2 position = 1
        boolean canGetPrevious = false;
        boolean canGetNext = false;
        if ((position == 0) && mAdapter.hasPrePage) {
            canGetPrevious = true;
        }
        if ((position == totalSize - 1) && mAdapter.hasNextPage) {
            canGetNext = true;
        }
        Trace.d(">>>>", "loadDbData: posi " + position + " total/2 " + totalSize / 2 + " canGetPre " + canGetPrevious + " canGetNext " + canGetNext);
        if (canGetPrevious || canGetNext) {
            MessageDao.getInstance().queryImgMsgPreNext(this, conversationId, originId, canGetPrevious, canGetNext, new MessageDao.MessageDaoCallback() {
                @Override
                public void getImageMsgs(Pair<List<ChatMessage>, List<ChatMessage>> listPair) {
                    List<ChatMessage> previousImages = listPair.first;
                    List<ChatMessage> nextImages = listPair.second;
                    List<ChatImageBean> newBeans = new ArrayList<>();
                    List<ChatImageBean> preImageBeans = new ArrayList<>();
                    if (previousImages != null && !previousImages.isEmpty()) {
                        // 在data之前追加查询到的数据
                        for (ChatMessage chatMessage : previousImages) {
                            ChatImageBean chatImageBean = new ChatImageBean();
                            chatImageBean.conversationId = chatMessage.getConversationId();
                            chatImageBean.fileUrl = chatMessage.getMessageContent();
                            chatImageBean.setOriginId(chatMessage.getOriginId());
                            chatImageBean.fileLocalPath = chatMessage.getFileLocalPath();
                            preImageBeans.add(chatImageBean);
                        }
                        Trace.d(">>>>", "getPreviousImages: 查询到" + preImageBeans.size());
                        newBeans.addAll(preImageBeans);
                        if (previousImages.size() < IMG_PAGE_SIZE) {
                            mAdapter.hasPrePage = false;
                        }
                    } else {
                        mAdapter.hasPrePage = false;
                    }
                    newBeans.addAll(data);
                    List<ChatImageBean> nextImageBeans = new ArrayList<>();
                    if (nextImages != null && !nextImages.isEmpty()) {
                        // 在data之前追加查询到的数据
                        for (ChatMessage chatMessage : nextImages) {
                            ChatImageBean chatImageBean = new ChatImageBean();
                            chatImageBean.conversationId = chatMessage.getConversationId();
                            chatImageBean.fileUrl = chatMessage.getMessageContent();
                            chatImageBean.setOriginId(chatMessage.getOriginId());
                            chatImageBean.fileLocalPath = chatMessage.getFileLocalPath();
                            nextImageBeans.add(chatImageBean);
                        }
                        newBeans.addAll(nextImageBeans);
                        Trace.d(">>>>", "getNextImages: 查询到" + nextImageBeans.size());
                        if (nextImages.size() < IMG_PAGE_SIZE) {
                            mAdapter.hasNextPage = false;
                        }
                    } else {
                        mAdapter.hasNextPage = false;
                    }
                    int currentPosition = position + preImageBeans.size();
                    mAdapter.setData(newBeans);
                    mViewPager.setAdapter(new RecyclerPagerAdapter(mAdapter));
                    Trace.d(">>>>", "getImageMsgs: 当前总数 " + newBeans.size() + " 设置位置position " + currentPosition);
                    mViewPager.setCurrentItem(currentPosition, false);
                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ChatEvent chatEvent) {
        if (ChatEvent.FILE_DOWNLOAD_COMPLETE.equals(chatEvent.getWhat())) {
            Bundle bundle = chatEvent.bundle;
            String eventOriginId = bundle.getString(Constant.MESSAGE_ORIGIN_ID);
            if (currentOriginId.equals(eventOriginId)) {
                iv_download.setVisibility(View.GONE);
                iv_share.setVisibility(View.VISIBLE);
                currentFileLocalPath = bundle.getString(Constant.MESSAGE_FILE_LOCAL);
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
        ChatImageBean chatImageBean = mAdapter.getData().get(position);
        currentFileLocalPath = chatImageBean.fileLocalPath;
        currentImageUrl = chatImageBean.fileUrl;
        currentOriginId = chatImageBean.originId;
        if (!TextUtils.isEmpty(currentFileLocalPath)) {
            iv_share.setVisibility(View.VISIBLE);
            iv_download.setVisibility(View.GONE);
        } else {
            iv_download.setVisibility(View.VISIBLE);
            iv_share.setVisibility(View.GONE);
        }
        loadDbData(currentPosition, chatImageBean.conversationId, chatImageBean.originId);
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
            ChatImageBean item = mAdapter.getItem(currentPosition);
            Uri uri = Uri.fromFile(new File(item.fileLocalPath));
            Trace.d("onClick: " + uri + " =item " + item);
            intent.putExtra(IMGEditActivity.EXTRA_IMAGE_URI, uri);
            intent.putExtra(IMGEditActivity.EXTRA_IMAGE_SAVE_PATH, tempFile.getAbsolutePath());
            startActivityForResult(intent, new OnActivityCallback() {
                @Override
                public void onActivityResult(int resultCode, @Nullable Intent data) {
                    /*if (resultCode == RESULT_OK) {
                        // 数据源改成编辑后的图片
                        mAdapter.getData().set(currentPosition, tempFile.getAbsolutePath());
                        // viewpage item只有3个mViewPager
                        recyclerPagerAdapter.setNeedRefresh(true);
                        recyclerPagerAdapter.notifyDataSetChanged();
                        // 还需要更改 宫格选择图片activity的数据源
                    }*/
                }
            });
        }
    }
}
