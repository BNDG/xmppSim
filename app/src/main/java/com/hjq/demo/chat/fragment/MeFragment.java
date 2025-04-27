package com.hjq.demo.chat.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.FileUtils;
import com.hjq.demo.R;
import com.hjq.demo.chat.activity.BigImageActivity;
import com.hjq.demo.chat.activity.ChatBaseActivity;
import com.hjq.demo.chat.activity.FileManagementActivity;
import com.hjq.demo.chat.activity.MyProfileActivity;
import com.hjq.demo.chat.activity.SettingActivity;
import com.hjq.demo.chat.dao.DBManager;
import com.hjq.demo.chat.entity.AvatarEntity;
import com.hjq.demo.chat.entity.ChatEvent;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.utils.AvatarGenerator;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.utils.Trace;
import com.hjq.widget.layout.SettingBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bndg.smack.SmartCommHelper;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * tab - "我"
 */
public class MeFragment extends BaseChatFragment<ChatBaseActivity> implements View.OnClickListener {

    @BindView(R.id.sdv_avatar)
    ImageView mAvatarSdv;

    @BindView(R.id.tv_name)
    TextView mNickNameTv;

    @BindView(R.id.tv_wx_id)
    TextView mWxIdTv;
    @BindView(R.id.rl_file_management)
    SettingBar mFileManagementRl;
    @BindView(R.id.rl_my_collection)
    SettingBar mMyCollectionRl;
    @BindView(R.id.rl_settings)
    SettingBar mSettingsRl;

    public static MeFragment newInstance() {

        Bundle args = new Bundle();

        MeFragment fragment = new MeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_me;
    }

    public void initView() {
        ButterKnife.bind(this, getView());
        EventBus.getDefault().register(this);
    }

    @Override
    protected void initData() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ChatEvent chatEvent) {
        if (ChatEvent.REFRESH_USER_INFO.equals(chatEvent.getWhat())) {
            onFragmentResume(false);
        }
    }

    @OnClick({R.id.rl_me, R.id.rl_settings, R.id.sdv_avatar,
            R.id.rl_my_collection, R.id.rl_file_management})
    public void onClick(View view) {
        switch (view.getId()) {
            // 个人页面
            case R.id.rl_me:
                startActivity(new Intent(getActivity(), MyProfileActivity.class));
                break;
            // 设置页面
            case R.id.rl_settings:
                startActivity(new Intent(getActivity(), SettingActivity.class));
                break;
            case R.id.sdv_avatar:
                DBManager.Companion.getInstance(getContext())
                        .getAvatarByUserId(PreferencesUtil.getInstance().getUserId())
                        .subscribe(new Consumer<List<AvatarEntity>>() {
                            @Override
                            public void accept(List<AvatarEntity> avatarEntities) throws Throwable {
                                if (!avatarEntities.isEmpty()) {
                                    AvatarEntity avatarEntity = avatarEntities.get(0);
                                    File file = new File(avatarEntity.getAvatarLocalPath());
                                    if (FileUtils.isFileExists(file)) {
                                        Intent intent = new Intent(getActivity(), BigImageActivity.class);
                                        intent.putExtra("imgUrl", avatarEntity.getAvatarLocalPath());
                                        startActivity(intent);
                                    }
                                }
                            }
                        });
                break;
            case R.id.rl_file_management:
                startActivity(new Intent(getActivity(), FileManagementActivity.class));
                break;
            case R.id.rl_my_collection:
                toast(R.string.not_open_yet);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onFragmentResume(boolean first) {
        super.onFragmentResume(first);
        Trace.w("onFragmentResume: ");
        User mUser = PreferencesUtil.getInstance().getUser();
        String userPhone = mUser.getUserPhone();
        if (SmartCommHelper.getInstance().isDeveloperMode()) {
            mWxIdTv.setText(String.format(getString(R.string.wx_id), mUser.getUserId()));
        } else {
            if (!TextUtils.isEmpty(userPhone))
                mWxIdTv.setText(String.format(getString(R.string.wx_id), userPhone));
            else {
                mWxIdTv.setText(String.format(getString(R.string.wx_id), mUser.getUserAccount()));
            }
        }

        mNickNameTv.setText(mUser.getUserNickName());
        AvatarGenerator.loadAvatar(getActivity(), mUser.getUserId(), mUser.getUserNickName(), mAvatarSdv, true);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void refreshMyStatus(String status) {
        ChatBaseActivity attachActivity = getAttachActivity();
        User mUser = PreferencesUtil.getInstance().getUser();
        mNickNameTv.setText(String.format("%s(%s)", mUser.getUserNickName(), status));
    }

    public void updateMyAvatar() {
        User mUser = PreferencesUtil.getInstance().getUser();
        AvatarGenerator.loadAvatar(getActivity(), mUser.getUserId(), mUser.getUserNickName(), mAvatarSdv, true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void refreshMyName() {
        User mUser = PreferencesUtil.getInstance().getUser();
        mNickNameTv.setText(mUser.getUserNickName());
    }

    public void changeUI() {
        mFileManagementRl.setLeftText(R.string.file_management);
        mMyCollectionRl.setLeftText(R.string.my_collection);
        mSettingsRl.setLeftText(R.string.settings);
    }
}
