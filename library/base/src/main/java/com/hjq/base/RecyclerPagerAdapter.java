package com.hjq.base;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2020/08/28
 * desc   : PagerAdapter 封装
 */
@SuppressWarnings("rawtypes")
public final class RecyclerPagerAdapter extends PagerAdapter {
    private final RecyclerView.Adapter mAdapter;
    private boolean needRefresh;
    private Object currentObject;

    public RecyclerPagerAdapter(RecyclerView.Adapter adapter) {
        if (adapter == null) {
            throw new IllegalArgumentException("are you ok?");
        }
        mAdapter = adapter;
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onChanged() {
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getCount() {
        return mAdapter.getItemCount();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        RecyclerView.ViewHolder holder = mAdapter.createViewHolder(container, 0);
        container.addView(holder.itemView);
        mAdapter.onBindViewHolder(holder, position);
        // 将新的页面项添加到 itemList 中
        return holder.itemView;
    }

    @Override
    public int getItemPosition(Object object) {
        // 需要刷新并且是当前页
        if (object.equals(currentObject) && needRefresh) {
            needRefresh = false;
            return POSITION_NONE;
        } else {
            // 如果位置发生了变化，可以返回 POSITION_CHANGED
            // 这里假设你的数据没有变化，所以返回 POSITION_UNCHANGED
            return super.getItemPosition(object);
        }
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    public void setNeedRefresh(boolean b) {
        this.needRefresh = b;
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.setPrimaryItem(container, position, object);
        currentObject = object;
//        Log.d(">>>>", "setPrimaryItem: pos " + position);
    }
}