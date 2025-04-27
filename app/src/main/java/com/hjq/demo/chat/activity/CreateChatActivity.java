package com.hjq.demo.chat.activity;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.demo.R;
import com.hjq.demo.aop.Log;
import com.hjq.demo.app.AppActivity;
import com.hjq.demo.chat.adapter.PickContactAdapter;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.UserDao;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.listener.ContactCallback;
import com.hjq.demo.chat.utils.AvatarGenerator;
import com.hjq.demo.chat.utils.PinyinComparator;
import com.hjq.demo.chat.widget.LoadingDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author r
 * @date 2024/9/20
 * @description 转发消息-创建新的聊天
 */

public class CreateChatActivity extends ChatBaseActivity {

    public static final String USER_IDS = "USER_IDS";
    public static final String USER_NICKNAMES = "USER_NICKNAMES";
    public static final String CONVERSATIONS = "CONVERSATIONS";
    private PickContactAdapter contactAdapter;
    private ListView listView;
    // 可滑动的显示选中用户的View
    private LinearLayout mAvatarListLl;
    private ImageView mSearchIv;
    private TextView mSaveTv;
    private HashMap<String, String> conversations = new HashMap<>();
    private ArrayList<String> checkedUserIdList = new ArrayList<>();
    private List<User> checkedUserList = new ArrayList<>();
    private List<String> initUserIdList = new ArrayList<>();
    private StringBuilder pickedUserNickNameBuffer;
    private int totalCount = 0;
    LoadingDialog loadingDialog;
    private EditText mSearchEt;

    @Log
    public static void start(AppActivity context, CreateChatsListener listener) {
        Intent intent = new Intent(context, CreateChatActivity.class);
        context.startActivityForResult(intent, new OnActivityCallback() {
            @Override
            public void onActivityResult(int resultCode, @Nullable Intent data) {
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        listener.getCheckedContacts(data.getStringArrayListExtra(USER_IDS),
                                data.getStringExtra(USER_NICKNAMES));
                        listener.getCheckedConversations((HashMap<String, String>) data.getSerializableExtra(CONVERSATIONS));
                    }
                }
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.create_chat_activity;
    }

    @Override
    protected void initView() {
        TextView tv_left = findViewById(R.id.tv_left);
        tv_left.setText(getString(R.string.create_group));
        tv_left.setVisibility(View.VISIBLE);
        loadingDialog = new LoadingDialog(CreateChatActivity.this);
        UserDao.getInstance().getAllFriendList(CreateChatActivity.this, new ContactCallback() {
            @Override
            public void getContactList(@NonNull List<User> users) {
                if (users == null) {
                    return;
                }
                // 对list进行排序
                Collections.sort(users, new PinyinComparator() {
                });
                contactAdapter = new PickContactAdapter(CreateChatActivity.this,
                        R.layout.item_pick_contact_list, users, checkedUserIdList, initUserIdList);
                listView.setAdapter(contactAdapter);
            }
        });

        mAvatarListLl = findViewById(R.id.ll_avatar_list);
        mSearchIv = findViewById(R.id.iv_search);
        mSaveTv = findViewById(R.id.tv_right);
        mSearchEt = findViewById(R.id.et_search);

        listView = findViewById(R.id.lv_friends);
        mSaveTv.setText(getString(R.string.confirm));
        mSaveTv.setVisibility(View.VISIBLE);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final User friend = contactAdapter.getItem(position);
                CheckBox mPickFriendCb = view.findViewById(R.id.cb_pick_friend);
                boolean isEnabled = mPickFriendCb.isEnabled();
                boolean isChecked = mPickFriendCb.isChecked();
                if (isEnabled) {
                    if (isChecked) {
                        mPickFriendCb.setChecked(false);
                        removeCheckedImage(friend.getUserId(), friend);
                    } else {
                        mPickFriendCb.setChecked(true);
                        addCheckedImage(friend);
                    }
                }
            }
        });

        mSaveTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createXmppChat();
            }
        });
    }

    /**
     * 创建聊天室
     * 至少3个人
     */
    private void createXmppChat() {
        List<String> pickedUserIdList = new ArrayList<>();
        pickedUserIdList.addAll(checkedUserIdList);
        StringBuilder pickedUserIdBuffer = new StringBuilder();
        if (!pickedUserIdList.isEmpty()) {
            for (String pickedUserId : pickedUserIdList) {
                pickedUserIdBuffer.append(pickedUserId);
                pickedUserIdBuffer.append(",");
            }
            pickedUserIdBuffer.deleteCharAt(pickedUserIdBuffer.length() - 1);
        }

        pickedUserNickNameBuffer = new StringBuilder();
        if (null != checkedUserList && !checkedUserList.isEmpty()) {
            for (User checkedUser : checkedUserList) {
                pickedUserNickNameBuffer.append(checkedUser.getUserNickName());
                pickedUserNickNameBuffer.append(Constant.SEPARATOR_NICKNAME);
            }
        }
        setResult(RESULT_OK, new Intent()
                .putStringArrayListExtra(CreateChatActivity.USER_IDS, checkedUserIdList)
                .putExtra(CreateChatActivity.USER_NICKNAMES, pickedUserNickNameBuffer.toString())
                .putExtra(CreateChatActivity.CONVERSATIONS, conversations));

        finish();
    }

    @Override
    protected void initData() {

    }

    private void addCheckedImage(final User friend) {
        // 是否已包含
        if (checkedUserIdList.contains(friend.getUserId())) {
            return;
        }
        totalCount++;
        checkedUserIdList.add(friend.getUserId());
        checkedUserList.add(friend);
        conversations.put(friend.getUserId(), friend.getUserNickName());
        // 包含TextView的LinearLayout
        // 参数设置
        LinearLayout.LayoutParams menuLinerLayoutParames = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        View view = LayoutInflater.from(this).inflate(
                R.layout.item_create_group_header, null);
        ImageView mAvatarSdv = view.findViewById(R.id.sdv_avatar);
        AvatarGenerator.loadAvatar(this, friend.getUserId(), friend.getUserNickName(), mAvatarSdv, false);
        menuLinerLayoutParames.setMargins(6, 0, 6, 0);
        // 设置id，方便后面删除
        view.setTag(friend.getUserId());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeCheckedImage(friend.getUserId(), friend);
                contactAdapter.notifyDataSetChanged();
            }
        });
        mAvatarListLl.addView(view, menuLinerLayoutParames);
        if (totalCount > 0) {
            if (mSearchIv.getVisibility() == View.VISIBLE) {
                mSearchIv.setVisibility(View.GONE);
            }
            mSaveTv.setText(String.format(getString(R.string.done_contacts), totalCount));
            mSaveTv.setEnabled(true);
        }
    }

    private void removeCheckedImage(String userId, User friend) {
        View view = mAvatarListLl.findViewWithTag(userId);
        mAvatarListLl.removeView(view);
        totalCount--;
        checkedUserIdList.remove(userId);
        checkedUserList.remove(friend);
        conversations.remove(userId);
        mSaveTv.setText(String.format(getString(R.string.done_contacts), totalCount));
        if (totalCount <= 0) {
            if (mSearchIv.getVisibility() == View.GONE) {
                mSearchIv.setVisibility(View.VISIBLE);
            }
            mSaveTv.setText("确定");
            mSaveTv.setEnabled(false);
        }
    }

    @Override
    public void initListener() {

    }

    public interface CreateChatsListener {

        default void getCheckedContacts(ArrayList<String> stringArrayListExtra, String stringExtra) {
        }

        default void getCheckedConversations(HashMap<String, String> convresations) {
        }

    }
}
