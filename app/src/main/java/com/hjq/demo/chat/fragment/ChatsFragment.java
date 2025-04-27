package com.hjq.demo.chat.fragment;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.VibrateUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.demo.R;
import com.hjq.demo.aop.SingleClick;
import com.hjq.demo.chat.activity.AddContactsActivity;
import com.hjq.demo.chat.activity.ChatActivity;
import com.hjq.demo.chat.activity.JoinGroupActivity;
import com.hjq.demo.chat.adapter.ConversationDiffCallback;
import com.hjq.demo.chat.adapter.SmartConversationAdapter;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.DBManager;
import com.hjq.demo.chat.entity.ConversationInfo;
import com.hjq.demo.chat.listener.OnFragmentListener;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.other.PermissionCallback;
import com.hjq.demo.utils.Trace;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;
import com.rxjava.rxlife.RxLife;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bndg.smack.SmartCommHelper;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.enums.SmartConversationType;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * 会话fragment
 */
public class ChatsFragment extends BaseChatFragment {

    @BindView(R.id.tv_title)
    TextView mTitleTv;

    @BindView(R.id.lv_conversation)
    RecyclerView rvList;

    @BindView(R.id.iv_search)
    View ivSearch;
    @BindView(R.id.iv_add)
    View ivAdd;

    SmartConversationAdapter smartConversationAdapter;
    private static final int REFRESH_CONVERSATION_LIST = 0x3000;
    private MediaPlayer mediaPlayer;
    private View headErrorView;
    private OnFragmentListener mListener;
    private PopupMenu popupMenu;
    private Runnable debounceRunnable;
    private int sortCount = 1;
    private View fetchingView;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_conversation;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (OnFragmentListener) context;
        } catch (ClassCastException e) {
            Trace.d("onAttach: " + e);
        }
    }

    @Override
    protected void initView() {
        ButterKnife.bind(this, getView());
        setTitleStrokeWidth(mTitleTv);

        smartConversationAdapter = new SmartConversationAdapter(new ArrayList<>());
        rvList.setAdapter(smartConversationAdapter);
        smartConversationAdapter.setDiffCallback(new ConversationDiffCallback());
        smartConversationAdapter.setActivity(getAttachActivity());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvList.setLayoutManager(layoutManager);
        rvList.setItemAnimator(null);
        fetchingView = LayoutInflater.from(getContext()).inflate(R.layout.item_chat_fetching, rvList, false);
        smartConversationAdapter.addHeaderView(fetchingView);
        headErrorView = LayoutInflater.from(getContext()).inflate(R.layout.item_chat_net_error, rvList, false);
        headErrorView.setOnClickListener(v -> {
            SmartIMClient.getInstance().checkConnection();
        });
        smartConversationAdapter.addHeaderView(headErrorView);
        smartConversationAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int position) {
                ConversationInfo conversation = (ConversationInfo) baseQuickAdapter.getItem(position);
                if (null != conversation) {
                    // 清除未读
                    conversation.setUnReadNum(0);
                    ArrayList<Object> payLoads = new ArrayList<>();
                    payLoads.add(SmartConversationAdapter.REFRESH_CONTENT);
                    smartConversationAdapter.notifyItemChanged(position, payLoads);
                    if (SmartConversationType.SINGLE.name().equals(conversation.getConversationType())) {
                        ChatActivity.start(getActivity(), SmartConversationType.SINGLE.name(),
                                conversation.getConversationId(), conversation.getConversationTitle());
                    } else {
                        ChatActivity.start(getContext(), SmartConversationType.GROUP.name(),
                                conversation.getConversationId(), conversation.getConversationTitle()
                        );
                    }
                }
            }
        });
    }

    @Override
    protected void initData() {
        initPopupMenu();
    }

    private void initPopupMenu() {
        popupMenu = new PopupMenu(requireContext(), ivAdd);
        popupMenu.getMenuInflater().inflate(R.menu.main_menu, popupMenu.getMenu());
        if (!SmartCommHelper.getInstance().isDeveloperMode()) {
            popupMenu.getMenu().removeItem(R.id.rl_join_group);
        }
        // 设置菜单项点击事件
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.rl_scan_qr_code) {
                    XXPermissions.with(getContext())
                            .permission(Permission.CAMERA)
                            .permission(Permission.WRITE_EXTERNAL_STORAGE)
                            .permission(Permission.READ_EXTERNAL_STORAGE)
                            .request(new PermissionCallback() {

                                @Override
                                public void onGranted(List<String> permissions, boolean all) {
                                    if (all) {
                                        ScanUtil.startScan(getActivity(), Constant.REQUEST_CODE_SCAN_ONE, new HmsScanAnalyzerOptions.Creator().create());
                                    }
                                }
                            });
                    return true;
                } else if (itemId == R.id.rl_add_friends) {
                    startActivity(new Intent(getActivity(), AddContactsActivity.class));
                    return true;
                    // 添加更多菜单项的处理逻辑
                } else if (itemId == R.id.rl_join_group) {
                    startActivity(new Intent(getActivity(), JoinGroupActivity.class));
                }
                return false;
            }
        });
    }


    public static ChatsFragment newInstance() {
        Bundle args = new Bundle();
        ChatsFragment fragment = new ChatsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void refreshConversationList() {
        post(new Runnable() {
            @Override
            public void run() {
                Context context = getContext();
                if (context == null) {
                    return;
                }
                Disposable subscribe = DBManager.Companion.getInstance(context)
                        .getConversationListByUserId(PreferencesUtil.getInstance().getUserId())
                        .to(RxLife.to(ChatsFragment.this))
                        .subscribe(new Consumer<List<ConversationInfo>>() {
                            @Override
                            public void accept(List<ConversationInfo> conversationInfos) throws Throwable {
                                smartConversationAdapter.setList(conversationInfos);
                                // 存储刷新会话的时间
                                SPUtils.getInstance().put(Constant.REFRESH_CONVERSATION_LIST_TIME, System.currentTimeMillis());
                            }
                        });
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @SingleClick
    @OnClick({R.id.iv_search, R.id.iv_add})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_search:
                startActivity(AddContactsActivity.class);
                break;
            case R.id.iv_add:
                popupMenu.show();
                break;
        }
    }

    /**
     * 初始化首页弹出框
     */
    private void playRing() {
        VibrateUtils.vibrate(new long[]{0, 1000, 500, 1000}, 2);
        Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getContext(), RingtoneManager.TYPE_RINGTONE);
        Trace.e("playRing play file" + ringtoneUri);
        mediaPlayer = MediaPlayer.create(getContext(), ringtoneUri);
        mediaPlayer.setLooping(true);
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    private void stopPlayRing() {
        VibrateUtils.cancel();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    public void conversationRemove(String conversationId) {
        List<ConversationInfo> data = smartConversationAdapter.getData();
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getConversationId().equals(conversationId)) {
                smartConversationAdapter.remove(i);
                break;
            }
        }
    }

    /**
     * 会话更新
     *
     * @param conversationId
     */
    public void conversationUpdate(String conversationId, ArrayList<String> payloads) {
        DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .getConversationByConversationId(PreferencesUtil.getInstance().getUserId(),
                        conversationId)
                .to(RxLife.to(ChatsFragment.this))
                .subscribe(conversationInfoList -> {
                    ConversationInfo dbInfo;
                    if (conversationInfoList.isEmpty()) {
                        dbInfo = null;
                    } else {
                        dbInfo = conversationInfoList.get(0);
                    }
                    boolean needAdd = true;
                    if (null != dbInfo) {
                        List<ConversationInfo> data = smartConversationAdapter.getData();
                        int itemPosition = -1;
                        for (int i = 0; i < data.size(); i++) {
                            ConversationInfo adapterConversation = data.get(i);
                            if (conversationId.equals(adapterConversation.getConversationId())) {
                                smartConversationAdapter.getData().set(i, dbInfo);
                                itemPosition = i + smartConversationAdapter.getHeaderLayoutCount();
                                Trace.w( " 未读数= " + dbInfo.getUnReadNum(), "需要notifyItemChanged");
                                smartConversationAdapter.notifyItemChanged(itemPosition,
                                        payloads);
                                needAdd = false;
                                break;
                            }
                        }
                        if (needAdd) {
                            smartConversationAdapter.addData(smartConversationAdapter.calcDataPosition(dbInfo),
                                    dbInfo);
                            Trace.w("conversationUpdate: 新增了 " + conversationId);
                        }
                        // 头像 标题不触发更新
                        if (payloads.contains(SmartConversationAdapter.REFRESH_CONTENT)) {
                            sortConversation();
                        }
                    }
                });
    }

    private void sortConversation() {
        if (debounceRunnable != null) {
            removeCallbacks(debounceRunnable);
        }
        debounceRunnable = () -> smartConversationAdapter.sortConversation();
        Trace.d("conversationUpdate: 排序 " + sortCount++);
        post(debounceRunnable);
    }

    public void updateHeaderErrorView(boolean visible) {
        if (headErrorView != null) {
            headErrorView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void scrollToTop() {
        post(()-> rvList.smoothScrollToPosition(0));
    }

    public void changeUI() {
        mTitleTv.setText(R.string.app_name);
        initPopupMenu();
    }

    public void showChatDataLoading(boolean isLoading) {
        if (fetchingView != null) {
            fetchingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }
}
