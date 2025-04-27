package com.hjq.demo.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hjq.demo.R;
import com.hjq.demo.app.AppAdapter;
import com.hjq.demo.chat.entity.ChatImageBean;
import com.hjq.demo.http.glide.GlideApp;

import java.io.File;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2020/08/28
 * desc   : 图片预览适配器
 */
public final class ImagePreviewMsgAdapter extends AppAdapter<ChatImageBean> {

    // 是否有上一页
    public boolean hasPrePage = true;
    // 是否有下一页
    public boolean hasNextPage = true;

    public ImagePreviewMsgAdapter(Context context) {
        super(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder();
    }

    private final class ViewHolder extends AppAdapter<?>.ViewHolder {

        private final PhotoView mPhotoView;

        private ViewHolder() {
            super(R.layout.image_preview_item);
            mPhotoView = (PhotoView) getItemView();
            mPhotoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float x, float y) {
                    onClick(view);
                }
            });
        }

        @Override
        public void onBindView(int position) {
            ChatImageBean item = getItem(position);
            Uri uri;
            if (!TextUtils.isEmpty(item.fileLocalPath)) {
                // 本地读
                uri = Uri.fromFile(new File(item.fileLocalPath));
            } else {
                // 网络获取
                uri = Uri.parse(item.fileUrl);
            }
            if (uri == null) {
                return;
            }
            GlideApp.with(getContext())
                    .load(uri)
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            if (resource instanceof GifDrawable) {
                                GifDrawable gifDrawable = (GifDrawable) resource;
                                mPhotoView.setImageDrawable(gifDrawable);
                                gifDrawable.start();
                            } else if (resource instanceof BitmapDrawable) {
                                BitmapDrawable bitmapDrawable = (BitmapDrawable) resource;
                                Bitmap bitmap = bitmapDrawable.getBitmap();
                                int width = bitmap.getWidth();
                                int height = bitmap.getHeight();
//                                Trace.d("Original Image Width: " + width);
//                                Trace.d("Original Image Height: " + height);
                                // 将图片设置到ImageView中
                                mPhotoView.setImageBitmap(bitmap);
                                mPhotoView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        float widthScale = width / (float) mPhotoView.getWidth();
                                        float heightScale = height / (float) mPhotoView.getHeight();
                                        float maxScale = Math.max(widthScale, heightScale);
                                        maxScale = maxScale <= 0 ? 1 / maxScale : maxScale * 3f; // 可以适当调整这个值，避免过于放大
//                                        Trace.d("ViewHolder: " + maxScale);
                                        if (maxScale > mPhotoView.getMaximumScale()) {
                                            mPhotoView.setMaximumScale(maxScale);
                                        }
                                    }
                                });
                            } else {
                                mPhotoView.setImageDrawable(resource);
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
        }
    }
}