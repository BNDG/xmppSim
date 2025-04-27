package com.hjq.demo.chat.adapter;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.hjq.demo.R;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.utils.AvatarGenerator;
import com.hjq.demo.http.glide.GlideApp;

import java.util.List;

public class PickContactAdapter extends BaseAdapter {
    private final Context mContext;
    private LayoutInflater layoutInflater;
    private int resource;

    private List<User> friendList;
    private List<String> checkedUserIdList;
    List<String> initUserIdList;

    public PickContactAdapter(Context context, int resource, List<User> friendList, List<String> checkedUserIdList, List<String> initUserIdList) {
        this.mContext = context;
        layoutInflater = LayoutInflater.from(context);
        this.resource = resource;
        this.friendList = friendList;
        this.checkedUserIdList = checkedUserIdList;
        this.initUserIdList = initUserIdList;
    }

    @Override
    public int getCount() {
        return friendList.size();
    }

    @Override
    public User getItem(int position) {
        return friendList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        User friend = friendList.get(position);
        ViewHolder viewHolder;
        if (null == convertView) {
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(resource, null);
            viewHolder.mHeaderTv = convertView.findViewById(R.id.tv_header);
            viewHolder.mTempView = convertView.findViewById(R.id.view_temp);
            viewHolder.mNickNameTv = convertView.findViewById(R.id.tv_nick_name);
            viewHolder.mAvatarSdv = convertView.findViewById(R.id.sdv_avatar);
            viewHolder.mPickFriendCb = convertView.findViewById(R.id.cb_pick_friend);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String header = friend.getUserHeader();
        if (0 == position || null != header && !header.equals(getItem(position - 1).getUserHeader())) {
            if ("".equals(header)) {
                viewHolder.mHeaderTv.setVisibility(View.GONE);
                viewHolder.mTempView.setVisibility(View.VISIBLE);
            } else {
                viewHolder.mHeaderTv.setVisibility(View.VISIBLE);
                viewHolder.mHeaderTv.setText(header);
                viewHolder.mTempView.setVisibility(View.GONE);
            }
        } else {
            viewHolder.mHeaderTv.setVisibility(View.GONE);
            viewHolder.mTempView.setVisibility(View.VISIBLE);
        }
        AvatarGenerator.loadAvatar(mContext, friend.getUserId(), friend.getUserNickName(), viewHolder.mAvatarSdv, false);
        viewHolder.mNickNameTv.setText(friend.getUserNickName());

        if (initUserIdList.contains(friend.getUserId())) {
            viewHolder.mPickFriendCb.setEnabled(false);
        } else {
            if (checkedUserIdList.contains(friend.getUserId())) {
                viewHolder.mPickFriendCb.setChecked(true);
            } else {
                viewHolder.mPickFriendCb.setChecked(false);
            }
        }

        return convertView;
    }

    class ViewHolder {
        TextView mHeaderTv;
        View mTempView;
        TextView mNickNameTv;
        ImageView mAvatarSdv;
        CheckBox mPickFriendCb;
    }
}
