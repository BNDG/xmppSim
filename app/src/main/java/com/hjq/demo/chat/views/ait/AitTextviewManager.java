package com.hjq.demo.chat.views.ait;

import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.hjq.demo.chat.listener.ChatEdittextAction;
import com.hjq.demo.chat.utils.MessageHelper;

/**
 * @author r
 * @date 2024/8/7
 * @description Brief description of the file content.
 */
public class AitTextviewManager implements AitTextChangeListener {
    private EditText editText;
    private AitManager aitTextWatcher;

    public AitTextviewManager(EditText editText) {
        this.editText = editText;
        initView();
    }

    private void initView() {
        editText.addTextChangedListener(msgInputTextWatcher);
    }

    public void setAitTextWatcher(AitManager aitTextWatcher) {
        this.aitTextWatcher = aitTextWatcher;
    }

    @Override
    public void onTextAdd(String content, int start, int length, boolean hasAt) {
        if (editText.getVisibility() == View.VISIBLE) {
            SpannableString spannableString =
                    MessageHelper.generateAtSpanString(hasAt ? content : "@" + content);
            editText.getEditableText()
                    .replace(hasAt ? start : start - 1, start, spannableString);
            if (!editText.hasFocus()) {
                editText.requestFocus();
                editText.setSelection(editText.getText().length());
            }
        }
    }

    @Override
    public void onTextDelete(int start, int length) {
        if (editText.getVisibility() == View.VISIBLE) {
            int end = start + length - 1;
            editText.getEditableText().replace(start, end, "");
        }
    }

    private boolean canRender = true;

    public void setmProxy(ChatEdittextAction mProxy) {
        this.mProxy = mProxy;
    }

    private ChatEdittextAction mProxy;
    private final TextWatcher msgInputTextWatcher =
            new TextWatcher() {
                private int start;
                private int count;

                //保存输入框的内容，和之后的做对比
                private String editable;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if (!canRender) {
                        return;
                    }
                    if (aitTextWatcher != null) {
                        aitTextWatcher.beforeTextChanged(s, start, count, after);
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!canRender) {
                        return;
                    }
                    this.start = start;
                    this.count = count;
                    if (aitTextWatcher != null) {
                        aitTextWatcher.onTextChanged(s, start, before, count);
                    }
                    if (mProxy != null) {
                        mProxy.onTypeStateChange(!TextUtils.isEmpty(s));
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!canRender) {
                        canRender = true;
                        return;
                    }
                    //隐藏文本选择器选择框
                    if (aitTextWatcher != null && !TextUtils.equals(editable, s)) {
                        aitTextWatcher.afterTextChanged(s);
                    }
                    editable = s.toString();
                }
            };

}
