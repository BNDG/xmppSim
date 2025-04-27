package com.hjq.demo.chat.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.hjq.demo.R;

import java.util.List;

/**
 * @author r
 * @date 2024/7/26
 * @description Brief description of the file content.
 */
public class EmojiGridAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    public EmojiGridAdapter(@Nullable List<String> data) {
        super(R.layout.item_emoji_text, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, String str) {
        helper.setText(R.id.tv_emoji, str);
    }
}
