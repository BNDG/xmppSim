package com.hjq.demo.ui.dialog;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.hjq.base.BaseDialog;
import com.hjq.demo.R;
import com.hjq.demo.aop.SingleClick;
import com.hjq.demo.chat.utils.AvatarGenerator;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject
 *    time   : 2018/10/18
 *    desc   : 可进行拷贝的副本
 */
public final class ForwardMsgDialog {

    public static final class Builder
            extends BaseDialog.Builder<ForwardMsgDialog.Builder> {

        private final ImageView iv_img;
        private final TextView tv_title;
        @Nullable
        private ForwardMsgDialog.OnListener mListener;

        private final TextView mMessageView;

        public Builder(Context context) {
            super(context);
            setContentView(R.layout.forward_msg_dialog);
            setAnimStyle(BaseDialog.ANIM_IOS);
            setGravity(Gravity.CENTER);
            mMessageView = findViewById(R.id.tv_content);
            iv_img = findViewById(R.id.iv_conversation_img);
            tv_title = findViewById(R.id.tv_conversation_title);
            setOnClickListener(R.id.btn_cancel, R.id.btn_send);
        }

        public ForwardMsgDialog.Builder setMessage(CharSequence text) {
            mMessageView.setText(text);
            return this;
        }

        public Builder setConversationTitle(String conversationTitle) {
            tv_title.setText(conversationTitle);
            return this;
        }

        public Builder setConversationId(String conversationId) {
            AvatarGenerator.loadAvatar(getContext(), conversationId, conversationId, iv_img, false);
            return this;
        }

        public ForwardMsgDialog.Builder setListener(ForwardMsgDialog.OnListener listener) {
            mListener = listener;
            return this;
        }

        @Override
        public BaseDialog create() {
            return super.create();
        }

        @SingleClick
        @Override
        public void onClick(View view) {
            int viewId = view.getId();
            if (viewId == R.id.btn_send) {
                dismiss();
                if (mListener == null) {
                    return;
                }
                mListener.onConfirm(getDialog());
            } else if (viewId == R.id.btn_cancel) {
                dismiss();
                if (mListener == null) {
                    return;
                }
                mListener.onCancel(getDialog());
            }
        }

    }

    public interface OnListener {

        /**
         * 点击确定时回调
         */
        void onConfirm(BaseDialog dialog);

        /**
         * 点击取消时回调
         */
        default void onCancel(BaseDialog dialog) {}
    }
}