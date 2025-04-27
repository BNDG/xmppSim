package com.hjq.demo.chat.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.hjq.demo.R;
import com.hjq.demo.chat.adapter.NewFriendsMsgAdapter;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.DBManager;
import com.hjq.demo.chat.entity.FriendApply;
import com.rxjava.rxlife.RxLife;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.callback.ISmartCallback;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * 好友申请列表
 *
 * @author zhou
 */
public class NewFriendsActivity extends ChatBaseActivity {

    @BindView(R.id.ll_root)
    LinearLayout mRootLl;

    @BindView(R.id.tv_title)
    TextView mTitleTv;

    @BindView(R.id.lv_new_friends_msg)
    ListView mNewFriendsMsgLv;

    NewFriendsMsgAdapter mNewFriendsMsgAdapter;
    MessageReceiver mMessageReceiver;


    @Override
    public int getLayoutId() {
        return R.layout.activity_new_friends;
    }

    @Override
    public void initView() {
        mTitleTv.setText(R.string.new_friends);
        setTitleStrokeWidth(mTitleTv);
        mNewFriendsMsgAdapter = new NewFriendsMsgAdapter(NewFriendsActivity.this, new ArrayList<>());
        mNewFriendsMsgLv.setAdapter(mNewFriendsMsgAdapter);
    }

    @Override
    public void initListener() {
        mNewFriendsMsgLv.setOnItemClickListener((parent, view, position, id) -> {
            FriendApply friendApply = mNewFriendsMsgAdapter.getItem(position);
            if (Constant.FRIEND_APPLY_STATUS_ACCEPT.equals(friendApply.getStatus())) {
                // 如果已通过申请
                // 进入用户详情页
                UserInfoActivity.start(NewFriendsActivity.this, friendApply.getFriendUserId());
            } else {
                friendApply.setStatus(Constant.FRIEND_APPLY_STATUS_CHECKED);
                DBManager.Companion.getInstance(NewFriendsActivity.this)
                        .saveFriendApply(friendApply)
                        .to(RxLife.to(NewFriendsActivity.this))
                        .subscribe(new CompletableObserver() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {

                            }

                            @Override
                            public void onComplete() {
                                // 未通过申请
                                // 进入好友申请处理页面
                                startActivity(new Intent(NewFriendsActivity.this, NewFriendsAcceptActivity.class).
                                        putExtra("applyId", friendApply.getApplyId())
                                );
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {

                            }
                        });
            }
        });

        mNewFriendsMsgLv.setOnItemLongClickListener((adapterView, view, position, id) -> {
            FriendApply friendApply = mNewFriendsMsgAdapter.getItem(position);
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.popup_window_new_friends_msg, null);
            // 给popwindow加上动画效果
            LinearLayout mPopRootLl = view.findViewById(R.id.ll_pop_root);
            view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));
            mPopRootLl.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_bottom_in));
            // 设置popwindow的宽高
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            PopupWindow mPopupWindow = new PopupWindow(view, dm.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT);

            // 使其聚集
            mPopupWindow.setFocusable(true);
            // 设置允许在外点击消失
            mPopupWindow.setOutsideTouchable(true);

            // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
            mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
            backgroundAlpha(0.5f);  //透明度

            mPopupWindow.setOnDismissListener(() -> backgroundAlpha(1f));
            // 弹出的位置
            mPopupWindow.showAtLocation(mRootLl, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

            // 删除
            RelativeLayout mDeleteRl = view.findViewById(R.id.rl_delete);
            mDeleteRl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SmartIMClient.getInstance()
                            .getFriendshipManager()
                            .rejectPresence(friendApply.getFriendUserId(), new ISmartCallback() {
                                @Override
                                public void onSuccess() {
                                    DBManager.Companion.getInstance(NewFriendsActivity.this)
                                            .deleteFriendApply(friendApply)
                                            .to(RxLife.to(NewFriendsActivity.this))
                                            .subscribe(new CompletableObserver() {
                                                @Override
                                                public void onSubscribe(@NonNull Disposable d) {

                                                }

                                                @Override
                                                public void onComplete() {
                                                    updateFriendApplyList();
                                                    mPopupWindow.dismiss();
                                                }

                                                @Override
                                                public void onError(@NonNull Throwable e) {

                                                }
                                            });

                                }

                                @Override
                                public void onFailed(int code, String desc) {
                                    toast(desc);
                                }
                            });

                }
            });

            // 取消
            RelativeLayout mCancelRl = view.findViewById(R.id.rl_cancel);
            mCancelRl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPopupWindow.dismiss();
                }
            });

            return true;
        });
    }

    @Override
    public void initData() {
        registerMessageReceiver();
    }

    private void updateFriendApplyList() {
        DBManager.Companion.getInstance(this)
                .getFriendApplies()
                .to(RxLife.to(this))
                .subscribe(new Consumer<List<FriendApply>>() {
                    @Override
                    public void accept(List<FriendApply> friendApplies) throws Throwable {
                        mNewFriendsMsgAdapter.setData(friendApplies);
                        mNewFriendsMsgAdapter.notifyDataSetChanged();
                    }
                });
    }

    @OnClick({R.id.tv_right, R.id.rl_search})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_right:
                startActivity(new Intent(NewFriendsActivity.this, AddContactsActivity.class));
                break;
            case R.id.rl_search:
                startActivity(new Intent(NewFriendsActivity.this, AddFriendsBySearchActivity.class));
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFriendApplyList();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void registerMessageReceiver() {
        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(Constant.RECEIVED_FRIEND_APPLY);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constant.RECEIVED_FRIEND_APPLY.equals(intent.getAction())) {
                updateFriendApplyList();
            }
        }
    }

    /**
     * 设置添加屏幕的背景透明度
     * 1.0完全不透明，0.0f完全透明
     *
     * @param bgAlpha 透明度值
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        // 0.0-1.0
        lp.alpha = bgAlpha;
        getWindow().setAttributes(lp);
    }

}