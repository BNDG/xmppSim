package com.hjq.demo.chat.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.hjq.demo.chat.entity.ConversationInfo;
import com.hjq.demo.utils.Trace;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author r
 * @date 2024/9/4
 * @description Brief description of the file content.
 */
public class ConversationDiffCallback extends DiffUtil.ItemCallback<ConversationInfo> {

    /**
     * Determine if it is the same item
     * <p>
     * 判断是否是同一个item
     *
     * @param oldItem New data
     * @param newItem old Data
     * @return
     */
    @Override
    public boolean areItemsTheSame(@NonNull ConversationInfo oldItem, @NonNull ConversationInfo newItem) {
        boolean equals = Objects.equals(oldItem.getConversationId(), newItem.getConversationId());
        return equals;
    }

    /**
     * When it is the same item, judge whether the content has changed.
     * <p>
     * 当是同一个item时，再判断内容是否发生改变
     *
     * @param oldItem New data
     * @param newItem old Data
     * @return
     */
    @Override
    public boolean areContentsTheSame(@NonNull ConversationInfo oldItem, @NonNull ConversationInfo newItem) {
        boolean b = oldItem.getConversationTitle().equals(newItem.getConversationTitle())
                && oldItem.getDigest().equals(newItem.getDigest())
                && oldItem.getLastMsgDate() == newItem.getLastMsgDate()
                && oldItem.isPinned() == newItem.isPinned();
        return b;
    }

    /**
     * Optional implementation
     * Implement this method if you need to precisely modify the content of a view.
     * If this method is not implemented, or if null is returned, the entire item will be refreshed.
     * <p>
     * 可选实现
     * 如果需要精确修改某一个view中的内容，请实现此方法。
     * 如果不实现此方法，或者返回null，将会直接刷新整个item。
     *
     * @param oldItem Old data
     * @param newItem New data
     * @return Payload info. if return null, the entire item will be refreshed.
     */
    @Nullable
    @Override
    public Object getChangePayload(@NonNull ConversationInfo oldItem, @NonNull ConversationInfo newItem) {
        if (!oldItem.getConversationId().equals(newItem.getConversationId())) {
            return null;
        }
        ArrayList<String> refreshList = new ArrayList<>();
        if (oldItem.isPinned() != newItem.isPinned()) {
            refreshList.add(SmartConversationAdapter.PINNED);
        }
        if (!oldItem.getDigest().equals(newItem.getDigest())) {
            refreshList.add(SmartConversationAdapter.REFRESH_CONTENT);
        }
        Trace.d(newItem.getConversationTitle());
        return refreshList;
    }

}
