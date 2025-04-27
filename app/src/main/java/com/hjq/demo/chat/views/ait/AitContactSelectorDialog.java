// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.hjq.demo.chat.views.ait;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ScreenUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hjq.demo.R;
import com.hjq.demo.chat.model.ait.AitUserInfo;

import java.util.List;

/**
 * Team member @ Dialog
 */
public class AitContactSelectorDialog extends BottomSheetDialog {
    private AitContactAdapter adapter;
    private ItemListener listener;

    private LinearLayoutManager layoutManager;

    private View contactArrowIcon;
    private RecyclerView contactList;

    public AitContactSelectorDialog(@NonNull Context context) {
        this(context, R.style.TransBottomSheetTheme);
    }

    public AitContactSelectorDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        setContentView(
                LayoutInflater.from(context).inflate(R.layout.ait_select_dialog, null, false),
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.getScreenHeight() * 2 / 3));

        setCanceledOnTouchOutside(true);
        initViews();
    }

    private void initViews() {
        contactArrowIcon = findViewById(R.id.contact_arrow_icon);
        contactList = findViewById(R.id.contact_list);
        contactArrowIcon.setOnClickListener(v -> dismiss());
        layoutManager = new LinearLayoutManager(getContext());
        contactList.setLayoutManager(layoutManager);
        adapter = new AitContactAdapter(getContext());
        adapter.setOnItemListener(
                item -> {
                    if (listener != null) {
                        listener.onSelect(item);
                    }
                    dismiss();
                });
        contactList.setAdapter(adapter);
        contactList.addOnScrollListener(
                new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            int position = layoutManager.findLastVisibleItemPosition();
                            if (listener != null && adapter.getItemCount() < position + 5) {
                                listener.onLoadMore();
                            }
                        }
                    }

                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                    }
                });
    }

    public void setData(List<AitUserInfo> data, boolean refresh) {
        adapter.setMembers(data);
        if (refresh) {
            adapter.notifyDataSetChanged();
        }
    }

    public void addData(List<AitUserInfo> data) {
        adapter.addMembers(data);
        adapter.notifyItemRangeInserted(adapter.getItemCount() - data.size(), data.size());
    }

    public void setData(List<AitUserInfo> data, boolean refresh, boolean showAll) {
        adapter.setShowAll(showAll);
        adapter.setMembers(data);
        if (refresh) {
            adapter.notifyDataSetChanged();
        }
    }

    public void setOnItemListener(ItemListener listener) {
        this.listener = listener;
    }

    public static class ItemListener {
        public void onSelect(AitUserInfo item) {
        }

        public void onLoadMore() {
        }
    }
}
