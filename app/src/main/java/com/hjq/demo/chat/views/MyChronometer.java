//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.hjq.demo.chat.views;


import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.icu.text.MeasureFormat;
import android.icu.text.MeasureFormat.FormatWidth;
import android.icu.util.Measure;
import android.icu.util.MeasureUnit;
import android.net.Uri;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import androidx.appcompat.widget.AppCompatTextView;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.IllegalFormatException;
import java.util.Locale;
import com.hjq.demo.R;

public class MyChronometer extends AppCompatTextView {
    private static final String TAG = "Chronometer";
    private long mBase;
    private long mNow;
    private boolean mVisible;
    private boolean mStarted;
    private boolean mRunning;
    private boolean mLogged;
    private String mFormat;
    private Formatter mFormatter;
    private Locale mFormatterLocale;
    private Object[] mFormatterArgs;
    private StringBuilder mFormatBuilder;
    private OnChronometerTickListener mOnChronometerTickListener;
    private StringBuilder mRecycle;
    private boolean mCountDown;
    private long costSeconds;
    private final Runnable mTickRunnable;
    private static final int MIN_IN_SEC = 60;
    private static final int HOUR_IN_SEC = 3600;

    public MyChronometer(Context context) {
        this(context, (AttributeSet)null, 0);
    }

    public MyChronometer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyChronometer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mFormatterArgs = new Object[1];
        this.mRecycle = new StringBuilder(8);
        this.mTickRunnable = new Runnable() {
            public void run() {
                if (MyChronometer.this.mRunning) {
                    MyChronometer.this.updateText(SystemClock.elapsedRealtime());
                    MyChronometer.this.dispatchChronometerTick();
                    MyChronometer.this.postDelayed(MyChronometer.this.mTickRunnable, 1000L);
                }

            }
        };
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyChronometer, defStyleAttr, 0);
        this.setFormat(a.getString(R.styleable.MyChronometer_format));
        this.setCountDown(a.getBoolean(R.styleable.MyChronometer_countDown, false));
        a.recycle();
        this.init();
    }

    private void init() {
        this.mBase = SystemClock.elapsedRealtime();
        this.updateText(this.mBase);
    }

    public void setCountDown(boolean countDown) {
        this.mCountDown = countDown;
        this.updateText(SystemClock.elapsedRealtime());
    }

    public boolean isCountDown() {
        return this.mCountDown;
    }

    public boolean isTheFinalCountDown() {
        try {
            this.getContext().startActivity((new Intent("android.intent.action.VIEW",
                    Uri.parse("https://youtu.be/9jK-NcRmVcw")))
                    .addCategory("android.intent.category.BROWSABLE")
                    .addFlags(528384));
            return true;
        } catch (Exception var2) {
            return false;
        }
    }

    public void setBase(long base) {
        this.mBase = base;
        this.dispatchChronometerTick();
        this.updateText(SystemClock.elapsedRealtime());
    }

    public long getBase() {
        return this.mBase;
    }

    public void setFormat(String format) {
        this.mFormat = format;
        if (format != null && this.mFormatBuilder == null) {
            this.mFormatBuilder = new StringBuilder(format.length() * 2);
        }

    }

    public String getFormat() {
        return this.mFormat;
    }

    public void setOnChronometerTickListener(OnChronometerTickListener listener) {
        this.mOnChronometerTickListener = listener;
    }

    public OnChronometerTickListener getOnChronometerTickListener() {
        return this.mOnChronometerTickListener;
    }

    public void start() {
        this.mStarted = true;
        this.updateRunning();
    }

    public void stop() {
        this.mStarted = false;
        this.updateRunning();
    }

    public void setStarted(boolean started) {
        this.mStarted = started;
        this.updateRunning();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mVisible = false;
        this.updateRunning();
    }

    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        this.mVisible = visibility == VISIBLE;
        this.updateRunning();
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        this.updateRunning();
    }

    private synchronized void updateText(long now) {
        this.mNow = now;
        Log.e("Chronometer", "now: " + this.mNow + " mBase: " + this.mBase + " cost: " + (this.mNow - this.mBase));
        long seconds = this.mCountDown ? this.mBase - now : now - this.mBase;
        seconds /= 1000L;
        boolean negative = false;
        if (seconds < 0L) {
            seconds = -seconds;
            negative = true;
        }

        this.costSeconds = seconds;
        String text = DateUtils.formatElapsedTime(this.mRecycle, seconds);
        if (negative) {
            text = this.getResources().getString(R.string.negative_duration, new Object[]{text});
        }

        if (this.mFormat != null) {
            Locale loc = Locale.getDefault();
            if (this.mFormatter == null || !loc.equals(this.mFormatterLocale)) {
                this.mFormatterLocale = loc;
                this.mFormatter = new Formatter(this.mFormatBuilder, loc);
            }

            this.mFormatBuilder.setLength(0);
            this.mFormatterArgs[0] = text;

            try {
                this.mFormatter.format(this.mFormat, this.mFormatterArgs);
                text = this.mFormatBuilder.toString();
            } catch (IllegalFormatException var9) {
                if (!this.mLogged) {
                    Log.w("Chronometer", "Illegal format string: " + this.mFormat);
                    this.mLogged = true;
                }
            }
        }

        this.setText(text);
    }

    private void updateRunning() {
        boolean running = this.mVisible && this.mStarted && this.isShown();
        if (running != this.mRunning) {
            if (running) {
                this.updateText(SystemClock.elapsedRealtime());
                this.dispatchChronometerTick();
                this.postDelayed(this.mTickRunnable, 1000L);
            } else {
                this.removeCallbacks(this.mTickRunnable);
            }

            this.mRunning = running;
        }

    }

    void dispatchChronometerTick() {
        if (this.mOnChronometerTickListener != null) {
            this.mOnChronometerTickListener.onChronometerTick(this);
        }

    }

    private static String formatDuration(long ms) {
        int duration = (int)(ms / 1000L);
        if (duration < 0) {
            duration = -duration;
        }

        int h = 0;
        int m = 0;
        if (duration >= 3600) {
            h = duration / 3600;
            duration -= h * 3600;
        }

        if (duration >= 60) {
            m = duration / 60;
            duration -= m * 60;
        }

        ArrayList<Measure> measures = new ArrayList();
        if (h > 0) {
            measures.add(new Measure(h, MeasureUnit.HOUR));
        }

        if (m > 0) {
            measures.add(new Measure(m, MeasureUnit.MINUTE));
        }

        measures.add(new Measure(duration, MeasureUnit.SECOND));
        return MeasureFormat.getInstance(Locale.getDefault(), FormatWidth.WIDE).formatMeasures((Measure[])measures.toArray(new Measure[measures.size()]));
    }

    public CharSequence getContentDescription() {
        return formatDuration(this.mNow - this.mBase);
    }

    public CharSequence getAccessibilityClassName() {
        return Chronometer.class.getName();
    }

    public long getCostSeconds() {
        return this.costSeconds;
    }

    public interface OnChronometerTickListener {
        void onChronometerTick(MyChronometer chronometer);
    }
}
