package com.hjq.demo.chat.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;

import com.blankj.utilcode.util.StringUtils;
import com.bndg.smack.SmartIMClient;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.demo.R;
import com.hjq.demo.aop.SingleClick;
import com.hjq.demo.chat.activity.AddContactsActivity;
import com.hjq.demo.chat.activity.ConversationListActivity;
import com.hjq.demo.chat.activity.NewFriendsActivity;
import com.hjq.demo.chat.activity.UserInfoActivity;
import com.hjq.demo.chat.activity.UserInfoFileHelperActivity;
import com.hjq.demo.chat.activity.UserInfoMyActivity;
import com.hjq.demo.chat.adapter.SmartFriendsAdapter;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.DBManager;
import com.hjq.demo.chat.dao.UserDao;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.listener.ContactCallback;
import com.hjq.demo.chat.listener.OnFragmentListener;
import com.hjq.demo.chat.utils.CommonUtil;
import com.hjq.demo.chat.utils.PinyinComparator;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.chat.utils.SimpleWeakObjectPool;
import com.hjq.demo.chat.widget.LQRRecyclerView;
import com.hjq.demo.chat.widget.QuickIndexBar;
import com.hjq.demo.other.PermissionCallback;
import com.hjq.demo.utils.Trace;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;
import com.rxjava.rxlife.RxLife;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bndg.smack.SmartCommHelper;

/**
 * 通讯录界面
 *
 * @author zhou
 */
public class ContactsFragment extends BaseChatFragment {

    SmartFriendsAdapter mAdapter;

    @BindView(R.id.tv_title)
    TextView mTitleTv;

    @BindView(R.id.rvContacts)
    LQRRecyclerView mRvContacts;
    @BindView(R.id.qib)
    QuickIndexBar mQib;
    @BindView(R.id.tvLetter)
    TextView mTvLetter;

    LayoutInflater mInflater;

    // 好友总数
    TextView mFriendsCountTv;

    TextView mNewFriendsUnreadNumTv;

    private OnFragmentListener mListener;
    @BindView(R.id.iv_search)
    View ivSearch;
    @BindView(R.id.iv_add)
    View ivAdd;
    private PopupMenu popupMenu;
    private TextView tvNewFriendMsg;
    private TextView tvMyGroup;

    /**
     * 更新用户头像
     *
     * @param jid
     */
    public void updateUserAvatar(String jid) {
        List<User> data = mAdapter.getData();
        for (int i = 0; i < data.size(); i++) {
            User user = data.get(i);
            if (user.getUserId().equals(jid)) {
                mAdapter.notifyItemChanged(i + mAdapter.getHeaderLayoutCount());
            }
        }
    }

    public static ContactsFragment newInstance() {

        Bundle args = new Bundle();

        ContactsFragment fragment = new ContactsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_contacts;
    }

    @Override
    protected void initView() {
        ButterKnife.bind(this, getView());
    }

    @Override
    protected void initData() {
        initPopupMenu();
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

    private void initPopupMenu() {
        popupMenu = new PopupMenu(requireContext(), ivAdd);
        popupMenu.getMenuInflater().inflate(R.menu.main_menu, popupMenu.getMenu());

        // 设置菜单项点击事件
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.rl_scan_qr_code:
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
                    case R.id.rl_add_friends:
                        startActivity(new Intent(getActivity(), AddContactsActivity.class));
                        return true;
                    // 添加更多菜单项的处理逻辑
                    default:
                        return false;
                }
            }
        });
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitleStrokeWidth(mTitleTv);
        mAdapter = new SmartFriendsAdapter(new ArrayList<>());
        mAdapter.setCanShowStatus(true);
        mRvContacts.setAdapter(mAdapter);
        mInflater = LayoutInflater.from(getActivity());
        View headerView = mInflater.inflate(R.layout.item_contacts_header, null);
        tvNewFriendMsg = headerView.findViewById(R.id.tv_new_friends);
        tvMyGroup = headerView.findViewById(R.id.tv_my_group);
        mAdapter.addHeaderView(headerView);
        View footerView = mInflater.inflate(R.layout.item_contacts_footer, null);
        mAdapter.addFooterView(footerView);
        mFriendsCountTv = footerView.findViewById(R.id.tv_total);
        RelativeLayout mNewFriendsRl = headerView.findViewById(R.id.rl_new_friends);
        mNewFriendsRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), NewFriendsActivity.class));
            }
        });
        RelativeLayout mGroupChatsRl = headerView.findViewById(R.id.rl_group_chats);
        mGroupChatsRl.setOnClickListener(view -> {
            ConversationListActivity.start(getContext());
        });
        mNewFriendsUnreadNumTv = headerView.findViewById(R.id.tv_new_friends_unread);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int position) {
                User friend = (User) baseQuickAdapter.getItem(position);
                Trace.d("onItemClick: " + friend.getUserNickName());
                String userType = friend.getUserType();
                if (Constant.USER_TYPE_WEIXIN.equals(userType)) {
                    UserInfoActivity.start(getActivity(), friend.getUserId());
                } else if (Constant.USER_TYPE_FILEHELPER.equals(userType)) {
                    startActivity(new Intent(getActivity(), UserInfoFileHelperActivity.class).
                            putExtra("userId", friend.getUserId()));
                } else {
                    if (friend.getUserId().equals(PreferencesUtil.getInstance().getUserId())) {
                        startActivity(new Intent(getActivity(), UserInfoMyActivity.class));
                    } else {
                        UserInfoActivity.start(getActivity(), friend.getUserId());
                    }
                }
            }
        });

        mQib.setOnLetterUpdateListener(new QuickIndexBar.OnLetterUpdateListener() {
            @Override
            public void onLetterUpdate(String letter) {
                //显示对话框
                showLetter(letter);
                //滑动到第一个对应字母开头的联系人
                if ("↑".equalsIgnoreCase(letter)) {
                    mRvContacts.moveToPosition(0);
                } else if ("☆".equalsIgnoreCase(letter)) {
                    mRvContacts.moveToPosition(0);
                } else {
                    List<User> data = mAdapter.getData();
                    for (int i = 0; i < data.size(); i++) {
                        User friend = data.get(i);
                        String c = friend.getUserHeader().charAt(0) + "";
                        if (c.equalsIgnoreCase(letter)) {
                            mRvContacts.moveToPosition(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onLetterCancel() {
                //隐藏对话框
                hideLetter();
            }
        });

    }

    private void showLetter(String letter) {
        mTvLetter.setVisibility(View.VISIBLE);
        mTvLetter.setText(letter);
    }


    private void hideLetter() {
        mTvLetter.setVisibility(View.GONE);
    }

    /**
     * 是否显示快速导航条
     *
     * @param show
     */
    public void showQuickIndexBar(boolean show) {
        if (mQib != null) {
            mQib.setVisibility(show ? View.VISIBLE : View.GONE);
            mQib.invalidate();
        }
    }

    public void refreshNewFriendsUnreadNum() {
        post(new Runnable() {
            @Override
            public void run() {
                DBManager.Companion.getInstance(getContext())
                        .getUnReadFriendApplyCount()
                        .to(RxLife.to(ContactsFragment.this))
                        .subscribe(list -> {
                            if (!list.isEmpty()) {
                                mNewFriendsUnreadNumTv.setVisibility(View.VISIBLE);
                                mNewFriendsUnreadNumTv.setText(String.valueOf(list.size()));
                            } else {
                                mNewFriendsUnreadNumTv.setVisibility(View.GONE);
                            }
                        });
            }
        });
    }

    /**
     * 刷新好友列表
     */
    public void refreshFriendsList() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                UserDao.getInstance().getAllFriendList(getActivity(), new ContactCallback() {
                    @Override
                    public void getContactList(@NonNull List<User> mFriendList) {
                        for(User user : mFriendList) {
                            user.setOnline(SmartIMClient.getInstance().getFriendshipManager().isOnline(user.getUserId()));
                        }
                        Collections.sort(mFriendList, new PinyinComparator() {
                        });
                        //mFriendList.addAll(generateMockData());
                        // 这里会丢失好友状态
                        mAdapter.setList(mFriendList);
                        mFriendsCountTv.setText(String.format(getString(R.string.contacts_count_str), mFriendList.size()));
                        Trace.w("refreshFriendsList: 刷新好友列表");
                    }
                });
            }
        }, 250);

    }

    private List<User> generateMockData() {
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            User u = new User();
            u.setUserId(PreferencesUtil.getInstance().getUserId());
            if (i % 2 == 0) {
                u.setUserNickName(generateRandomChineseString(2));
            } else {
                u.setUserNickName(generateRandomEnglishString(3));
            }
            u.setUserHeader(CommonUtil.generateUserHeader(u.getUserNickName()));
            userList.add(u);
        }
        return userList;
    }

    // 生成指定长度的英文字符串
    public String generateRandomEnglishString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    // 生成指定长度的中文字符串
    public String generateRandomChineseString(int length) {
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            // 生成GB2312编码的汉字
            byte[] bytes = new byte[2];
            bytes[0] = (byte) (0xA1 + random.nextInt(39));
            bytes[1] = (byte) (0xA1 + random.nextInt(94));
            try {
                sb.append(new String(bytes, "GB2312"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return sb.toString();
    }

    public void notifyContactRemoved(String contactId) {
        if (!TextUtils.isEmpty(contactId)) {
            for (int i = 0; i < mAdapter.getData().size(); i++) {
                User user = mAdapter.getData().get(i);
                if (contactId.equals(user.getUserId())) {
                    mAdapter.remove(i);
                    break;
                }
            }
            mFriendsCountTv.setText(String.format(StringUtils.getString(R.string.contacts_count_str), mAdapter.getData().size()));
        }
    }

    public void refreshFriend(String contactId) {
        UserDao.getInstance().getUserById(ContactsFragment.this, contactId, new ContactCallback() {
            @Override
            public void getUser(@Nullable User userById) {
                if (userById != null) {
                    if (!TextUtils.isEmpty(contactId)) {
                        boolean needAdd = true;
                        for (int i = 0; i < mAdapter.getData().size(); i++) {
                            User user = mAdapter.getData().get(i);
                            if (contactId.equals(user.getUserId())) {
                                needAdd = false;
                                userById.setOnline(user.isOnline());
                                mAdapter.getData().set(i, userById);
                                int position = i + mAdapter.getHeaderLayoutCount();
                                mAdapter.notifyItemChanged(position);
                                Trace.d("refreshFriend: pos = " + position);
                                break;
                            }
                        }
                        if(needAdd) {
                            List<User> data = mAdapter.getData();
                            data.add(userById);
                            Collections.sort(data, new PinyinComparator() {
                            });
                            mAdapter.setList(data);
                            mFriendsCountTv.setText(String.format(getString(R.string.contacts_count_str), data.size()));
                        }
                    }
                }
            }
        });
    }

    public void refreshFriendStatus(String contactId, boolean isOnLine) {
        if (mAdapter == null || mAdapter.getData().isEmpty()) {
        } else {
            for (int i = 0; i < mAdapter.getData().size(); i++) {
                User user = mAdapter.getData().get(i);
                if (contactId.equals(user.getUserId())) {
                    user.setOnline(isOnLine);
                    int position = i + mAdapter.getHeaderLayoutCount();
                    mAdapter.notifyItemChanged(position);
                    break;
                }
            }
        }
    }

    public void changeUI() {
        mTitleTv.setText(R.string.tab_contacts);
        tvNewFriendMsg.setText(R.string.verification_msg);
        tvMyGroup.setText(R.string.my_group_chats);
        mFriendsCountTv.setText(String.format(StringUtils.getString(R.string.contacts_count_str), mAdapter.getData().size()));
        initPopupMenu();
    }
}