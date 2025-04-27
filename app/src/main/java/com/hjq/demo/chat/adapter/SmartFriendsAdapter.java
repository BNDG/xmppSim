package com.hjq.demo.chat.adapter;

import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.hjq.demo.R;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.utils.AvatarGenerator;
import com.hjq.demo.chat.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 通讯录
 *
 * @author zhou
 */
public class SmartFriendsAdapter extends BaseQuickAdapter<User, BaseViewHolder> {

    public static final int FROM_GROUP_SETTING = 1;
    private int fromType;
    List<User> pickedUser = new ArrayList<>();

    private boolean showHeader = true;

    private PickedListener pickedListener;
    private boolean canShowStatus;

    public SmartFriendsAdapter(@Nullable List<User> data) {
        super(R.layout.item_contacts, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, User friend) {
        CheckBox checkBox = helper.getView(R.id.cb_select_user);
        checkBox.setOnCheckedChangeListener(null);
        String userNickName = friend.getUserNickName();
        String userContactAlias = friend.getUserContactAlias();
        String showName = TextUtils.isEmpty(userContactAlias) ? userNickName : userContactAlias;
        helper.setGone(R.id.cb_select_user, fromType != FROM_GROUP_SETTING)
                .setText(R.id.tv_name, showName);
        if(canShowStatus) {
            helper.setVisible(R.id.status_icon, true)
                    .setImageResource(R.id.status_icon, friend.isOnline() ? R.drawable.shape_online_circle : R.drawable.shape_offline_circle);
        }
        checkBox.setChecked(friend.isSelected());
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                friend.setSelected(isChecked);
                if (isChecked) {
                    pickedUser.add(friend);
                } else {
                    pickedUser.remove(friend);
                }
                if (pickedListener != null) {
                    pickedListener.getPickedUsers(pickedUser);
                }
            }
        });
        if (showHeader) {
            String currentLetter;
            if (TextUtils.isEmpty(userContactAlias)) {
                if (TextUtils.isEmpty(friend.getUserHeader())) {
                    String userHeader = CommonUtil.generateUserHeader(userNickName);
                    friend.setUserHeader(userHeader);
                    currentLetter = userHeader;
                } else {
                    currentLetter = friend.getUserHeader();
                }
            } else {
                currentLetter = CommonUtil.generateUserHeader(userContactAlias);
            }

            int position = helper.getAbsoluteAdapterPosition();
            //得到当前字母
            String header = "";
            if (position == getHeaderLayoutCount()) {
                header = currentLetter;
            } else {
                //得到上一个字母
                String preLetter = getData().get(position - getHeaderLayoutCount() - 1).getUserHeader();
                //如果和上一个字母的首字母不同则显示字母栏
                if (!currentLetter.equalsIgnoreCase(preLetter)) {
                    header = currentLetter;
                }
            }
            int nextIndex = position + 1;
            if (nextIndex < getData().size() - 1) {
                //得到下一个字母
                String nextLetter = getData().get(nextIndex).getUserHeader();
                //如果和下一个字母的首字母不同则隐藏下划线
                if (!currentLetter.equalsIgnoreCase(nextLetter)) {
//                    helper.setVisible(R.id.view_header, true);
                } else {
//                    helper.setVisible(R.id.view_header, true);
                }
            } else {
//                helper.setVisible(R.id.view_header, false);
            }
            if (position == getData().size() - 1) {
//                helper.setGone(R.id.view_header, true);
            }

            //根据str是否为空决定字母栏是否显示
            if (TextUtils.isEmpty(header)) {
                helper.setGone(R.id.tv_header, true);
            } else {
                helper.setVisible(R.id.tv_header, true);
                helper.setText(R.id.tv_header, header);
            }
        }
        ImageView imageView = helper.getView(R.id.sdv_avatar);
        AvatarGenerator.loadAvatar(getContext(), friend.getUserId(), showName, imageView, false);
    }

    public void setPickedListener(PickedListener pickedListener) {
        this.pickedListener = pickedListener;
    }

    public void setShowHeader(boolean showHeader) {
        this.showHeader = showHeader;
    }

    public void setFromWhere(int fromType) {
        this.fromType = fromType;
    }

    public void setCanShowStatus(boolean canShowStatus) {
         this.canShowStatus = canShowStatus;
    }

    public interface PickedListener {
        void getPickedUsers(List<User> pickedUser);
    }
}