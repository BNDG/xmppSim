package com.hjq.demo.chat.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.hjq.demo.R;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.utils.AvatarGenerator;

import java.util.List;

/**
 * @author r
 * @date 2024/7/3
 * @description Brief description of the file content.
 */
public class MultiTalkMemberAdapter extends BaseQuickAdapter<User, BaseViewHolder> {


    public MultiTalkMemberAdapter(@Nullable List<User> data) {
        super(R.layout.member_multi_talk, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, User groupMember) {
        AvatarGenerator.loadRectAvatar(getContext(), groupMember.getUserId(), groupMember.getUserNickName(), baseViewHolder.getView(R.id.sdv_avatar), true);

    }
}
