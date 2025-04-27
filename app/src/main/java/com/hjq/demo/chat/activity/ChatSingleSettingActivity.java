package com.hjq.demo.chat.activity;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hjq.demo.R;
import com.hjq.demo.aop.Log;
import com.hjq.demo.aop.SingleClick;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.DBManager;
import com.hjq.demo.chat.dao.MessageDao;
import com.hjq.demo.chat.dao.UserDao;
import com.hjq.demo.chat.entity.ChatEvent;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.listener.ContactCallback;
import com.hjq.demo.chat.utils.AvatarGenerator;
import com.hjq.demo.chat.widget.ConfirmDialog;
import com.hjq.demo.utils.Trace;
import com.hjq.widget.view.SwitchButton;
import com.tencent.mmkv.MMKV;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import com.bndg.smack.enums.SmartConversationType;

/**
 * 单聊设置
 *
 * @author zhou
 * @description 查找、清空聊天记录置顶等
 */
public class ChatSingleSettingActivity extends ChatBaseActivity implements View.OnClickListener {

    @BindView(R.id.tv_title)
    TextView mTitleTv;

    @BindView(R.id.tv_nick_name)
    TextView mNickNameTv;

    @BindView(R.id.sdv_avatar)
    ImageView mAvatarSdv;

    @BindView(R.id.rl_add_user_to_group)
    RelativeLayout mAddUserToGroupRl;

    @BindView(R.id.rl_clear)
    View mClearRl;

    String contactId;
    String userNickName;
    SwitchButton sb_mute;

    @Log
    public static void start(Context context, String userId, String userNickName) {
        Intent intent = new Intent(context, ChatSingleSettingActivity.class);
        intent.putExtra(Constant.CONTACT_ID, userId);
        intent.putExtra(Constant.CONTACT_NICK_NAME, userNickName);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_single_chat_setting;
    }

    public void initView() {
        mTitleTv.setText(R.string.chat_info);
        contactId = getIntent().getStringExtra(Constant.CONTACT_ID);
        userNickName = getIntent().getStringExtra(Constant.CONTACT_NICK_NAME);
        setTitleStrokeWidth(mTitleTv);
        sb_mute = findViewById(R.id.sb_mute);
        mNickNameTv.setText(userNickName);
        sb_mute.setChecked(MMKV.defaultMMKV().getBoolean(contactId + Constant.MUTE_KEY, false));
        UserDao.getInstance().getUserById(ChatSingleSettingActivity.this, contactId, new ContactCallback() {
            @Override
            public void getUser(@Nullable User userById) {
                AvatarGenerator.loadAvatar(ChatSingleSettingActivity.this, userById.getUserId(),
                        userById.getUserNickName(), mAvatarSdv, false);
            }
        });
        sb_mute.setOnCheckedChangeListener((button, isChecked) -> {
            MMKV.defaultMMKV().putBoolean(contactId + Constant.MUTE_KEY, isChecked);
        });
        setOnClickListener(R.id.rl_search_chat_msg, R.id.rl_clear, R.id.rl_add_user_to_group);
    }

    @Override
    protected void initData() {

    }

    @SingleClick
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_add_user_to_group:
                Intent intent = new Intent(this, CreateGroupActivity.class);
                intent.putExtra("createType", Constant.CREATE_GROUP_TYPE_FROM_SINGLE);
                intent.putExtra("userId", contactId);
                intent.putExtra("userNickName", userNickName);
                startActivity(intent);
                break;

            case R.id.rl_clear:
                final ConfirmDialog clearConfirmDialog = new ConfirmDialog(this, "",
                        String.format(getString(R.string.deletion_chat_history_with), userNickName), getString(R.string.confirm), "");
                clearConfirmDialog.setOnDialogClickListener(new ConfirmDialog.OnDialogClickListener() {
                    @Override
                    public void onOkClick() {
                        // 清除本地message
                        MessageDao.getInstance().deleteMessageByConversationId(contactId);
                        // todo 暂时删除 以后是清空
                        DBManager.Companion.getInstance(ChatSingleSettingActivity.this)
                                .deleteConversation(myUserInfo.getUserId(), contactId);
                        // 通知界面进行刷新
                        EventBus.getDefault().post(new ChatEvent(ChatEvent.REFRESH_CHAT_UI));
                        clearConfirmDialog.dismiss();
                    }

                    @Override
                    public void onCancelClick() {
                        clearConfirmDialog.dismiss();
                    }
                });
                // 点击空白处消失
                clearConfirmDialog.setCancelable(true);
                clearConfirmDialog.show();
                break;
            case R.id.rl_search_chat_msg:
                SearchRecordActivity.start(this, SmartConversationType.SINGLE.name(), contactId, userNickName);
                break;
        }
    }

    @Override
    public void initListener() {

    }
}