package com.hjq.demo.chat.adapter;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hjq.demo.R;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.http.glide.GlideApp;

import java.util.List;

/**
 * 通讯录黑名单
 *
 * @author zhou
 */
public class BlockedContactAdapter extends BaseAdapter {

    Context mContext;
    List<User> mBlockedContactList;

    public BlockedContactAdapter(Context context, List<User> blockedContactList) {
        this.mContext = context;
        this.mBlockedContactList = blockedContactList;
    }

    public void setData(List<User> blockedContactList) {
        this.mBlockedContactList = blockedContactList;
    }

    @Override
    public int getCount() {
        return mBlockedContactList.size();
    }

    @Override
    public User getItem(int position) {
        return mBlockedContactList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (null == convertView) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_blocked_contact, null);
            viewHolder = new ViewHolder();
            viewHolder.mAvatarSdv = convertView.findViewById(R.id.sdv_avatar);
            viewHolder.mNickNameTv = convertView.findViewById(R.id.tv_nick_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        User blockedContact = getItem(position);
        if (!TextUtils.isEmpty(blockedContact.getUserAvatar())) {
            GlideApp.with(convertView.getContext())
                    .load(Uri.parse(blockedContact.getUserAvatar()))
                    .circleCrop()
                    .placeholder(R.mipmap.default_user_avatar)
                    .into(viewHolder.mAvatarSdv);
        }
        viewHolder.mNickNameTv.setText(blockedContact.getUserNickName());
        return convertView;
    }

    class ViewHolder {
        ImageView mAvatarSdv;
        TextView mNickNameTv;
    }

}