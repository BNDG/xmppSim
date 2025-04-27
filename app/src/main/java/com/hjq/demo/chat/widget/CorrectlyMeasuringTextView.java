package com.hjq.demo.chat.widget;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class CorrectlyMeasuringTextView extends AppCompatTextView {

    public CorrectlyMeasuringTextView(Context context) {
        super(context);
    }

    public CorrectlyMeasuringTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CorrectlyMeasuringTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Layout layout = getLayout();
        if (layout == null || layout.getLineCount() <= 1) {
            return;
        }
        int maxWidth = 0;
        int totalHeight = 0;
        // 输出最大宽度
        for (int i = layout.getLineCount() - 1; i >= 0; --i) {
            int lineStart = layout.getLineStart(i);
            int lineEnd = layout.getLineEnd(i);
            // 获取行内缩进
            int paragraphLeft = getLayout().getParagraphLeft(i);
            float lineWidth = layout.getPaint().measureText(getText(), lineStart, lineEnd) + paragraphLeft;
            maxWidth = Math.max(maxWidth, Math.round(lineWidth));
            // 获取每行的高度和行间距
            totalHeight += layout.getLineBottom(i) - layout.getLineTop(i);
        }
        // 加上内边距
        int desiredWidth = Math.min(maxWidth + getPaddingLeft() + getPaddingRight(), MeasureSpec.getSize(widthMeasureSpec));
        int desiredHeight = totalHeight + getPaddingTop() + getPaddingBottom();
        // 设置测量结果 明确告诉系统这个视图的宽度和高度是多少。系统会根据这些尺寸进行布局。
        setMeasuredDimension(desiredWidth, desiredHeight);
    }

}
