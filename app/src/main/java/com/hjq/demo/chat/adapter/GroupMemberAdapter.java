package com.hjq.demo.chat.adapter;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.hjq.demo.R;
import com.hjq.demo.chat.entity.GroupMember;
import com.hjq.demo.chat.utils.AvatarGenerator;

import java.util.List;

/**
 * @author r
 * @date 2024/7/3
 * @description Brief description of the file content.
 */
public class GroupMemberAdapter extends BaseMultiItemQuickAdapter<GroupMember, BaseViewHolder> implements LoadMoreModule {
    public static final int DEFAULT_MEMBER = 0;
    public static final int ADD_MEMBER = 1;
    public static final int DEL_MEMBER = 2;

    public GroupMemberAdapter(List<GroupMember> data) {
        super(data);
        addItemType(DEFAULT_MEMBER, R.layout.item_chat_setting_gridview);
        addItemType(ADD_MEMBER, R.layout.item_group_add_member);
        addItemType(DEL_MEMBER, R.layout.item_group_del_member);
        addChildClickViewIds(R.id.iv_add_member, R.id.iv_del_member);
    }


    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, GroupMember groupMember) {
        switch (baseViewHolder.getItemViewType()) {
            case DEFAULT_MEMBER:
                baseViewHolder.setText(R.id.tv_nick_name, groupMember.getMemberName());
                String avatarUserId = groupMember.getMemberOriginId();
                baseViewHolder.setImageResource(R.id.iv_affiliation, groupMember.isOwner() ? R.drawable.icon_muc_owner
                        : groupMember.isAdmin() ? R.drawable.icon_muc_admin
                        : R.drawable.group_default);
                boolean isVisible = groupMember.isAdmin() || groupMember.isOwner();
                baseViewHolder.setVisible(R.id.iv_affiliation, isVisible);
                AvatarGenerator.loadAvatar(getContext(), avatarUserId, groupMember.getMemberName(), baseViewHolder.getView(R.id.sdv_avatar), false);
                break;
            case ADD_MEMBER:
                break;
            case DEL_MEMBER:
                break;
        }

    }
}
