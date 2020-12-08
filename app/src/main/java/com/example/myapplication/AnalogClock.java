package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.renderscript.Sampler;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;
import java.util.TimeZone;

import static android.text.format.DateUtils.SECOND_IN_MILLIS;

public class AnalogClock extends View {

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mTimeZone == null && Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
                final String tz = intent.getStringExtra("time-zone");
                mTime = Calendar.getInstance(TimeZone.getTimeZone(tz));
            }
            onTimeChanged();
        }
    };

    private final Runnable mClockTick = new Runnable() {
        @Override
        public void run() {
            onTimeChanged();

            if (mEnableSeconds) {
                final long now = System.currentTimeMillis();
                final long delay = SECOND_IN_MILLIS - now % SECOND_IN_MILLIS;
                postDelayed(this, delay);
            }
        }
    };

    /** height, width of the clock's view */
    private int mHeight, mWidth = 0;

    /** numeric numbers to denote the hours */
    private int[] mClockHours = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};

    /** spacing and padding of the clock-hands around the clock round */
    private int mPadding = 0;
    private int mNumeralSpacing = 0;

    /** truncation of the heights of the clock-hands,
     hour clock-hand will be smaller comparetively to others */
    private int mHandTruncation, mHourHandTruncation = 0;

    /** others attributes to calculate the locations of hour-points */
    private int mRadius = 0;
    private Paint mPaint;
    private Rect mRect = new Rect();
    private boolean isInit;  // it will be true once the clock will be initialized.
    private Drawable mDial;
    private Drawable mHourHand;
    private Drawable mMinuteHand;
    private Drawable mSecondHand;


    private Calendar mTime;
    private String mDescFormat;
    private TimeZone mTimeZone;
    private boolean mEnableSeconds = true;

    public AnalogClock(Context context) {
        this(context, null);
    }

    public AnalogClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnalogClock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final Resources r = context.getResources();
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AnalogClock);
        mTime = Calendar.getInstance();
        mDescFormat = ((SimpleDateFormat) DateFormat.getTimeFormat(context)).toLocalizedPattern();
        mEnableSeconds = a.getBoolean(R.styleable.AnalogClock_showSecondHand, true);
        mDial = a.getDrawable(R.styleable.AnalogClock_dial_watch);
        if (mDial == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mDial = context.getDrawable(R.drawable.dial_watch);
            } else {
                mDial = r.getDrawable(R.drawable.dial_watch);
            }
        }
        mHourHand = a.getDrawable(R.styleable.AnalogClock_hour);
        if (mHourHand == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mHourHand = context.getDrawable(R.drawable.hour);
            } else {
                mHourHand = r.getDrawable(R.drawable.hour);
            }
        }
        mMinuteHand = a.getDrawable(R.styleable.AnalogClock_minute);
        if (mMinuteHand == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mMinuteHand = context.getDrawable(R.drawable.minute);
            } else {
                mMinuteHand = r.getDrawable(R.drawable.minute);
            }
        }
        mSecondHand = a.getDrawable(R.styleable.AnalogClock_second);
        if (mSecondHand == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mSecondHand = context.getDrawable(R.drawable.second);
            } else {
                mSecondHand = r.getDrawable(R.drawable.second);
            }
        }
        initDrawable(context, mDial);
        initDrawable(context, mHourHand);
        initDrawable(context, mMinuteHand);
        initDrawable(context, mSecondHand);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        getContext().registerReceiver(mIntentReceiver, filter);
        mTime = Calendar.getInstance(mTimeZone != null ? mTimeZone : TimeZone.getDefault());
        onTimeChanged();
        if (mEnableSeconds) {
            mClockTick.run();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        getContext().unregisterReceiver(mIntentReceiver);
        removeCallbacks(mClockTick);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int minWidth = Math.max(mDial.getIntrinsicWidth(), getSuggestedMinimumWidth());
        final int minHeight = Math.max(mDial.getIntrinsicHeight(), getSuggestedMinimumHeight());
        setMeasuredDimension(getDefaultSize(minWidth, widthMeasureSpec),
                getDefaultSize(minHeight, heightMeasureSpec));
    }

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int w = getWidth();
        final int h = getHeight();

        final int saveCount = canvas.save();
        canvas.translate(w / 2, h / 2);
        final float scale = Math.min((float) w / mDial.getIntrinsicWidth(),
                (float) h / mDial.getIntrinsicHeight());
        if (scale < 1f) {
            canvas.scale(scale, scale, 0f, 0f);
        }
        mDial.draw(canvas);

        final float hourAngle = mTime.get(Calendar.HOUR) * 30f;
        canvas.rotate(hourAngle, 0f, 0f);
        mHourHand.draw(canvas);

        final float minuteAngle = mTime.get(Calendar.MINUTE) * 6f;
        canvas.rotate(minuteAngle - hourAngle, 0f, 0f);
        mMinuteHand.draw(canvas);

        if (mEnableSeconds) {
            final float secondAngle = mTime.get(Calendar.SECOND) * 6f;
            canvas.rotate(secondAngle - minuteAngle, 0f, 0f);
            mSecondHand.draw(canvas);
        }
        canvas.restoreToCount(saveCount);
        /** circle border */
        mPaint.reset();
        mPaint.setColor(android.R.color.white);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(4);
        mPaint.setAntiAlias(true);
        canvas.drawCircle(mWidth / 2, mHeight / 2, mRadius + mPadding - 10, mPaint);

        /** clock-center */
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(mWidth / 2, mHeight / 2, 12, mPaint);  // the 03 clock hands will be rotated from this center point.

        /** border of hours */

        int fontSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics());
        mPaint.setTextSize(fontSize);  // set font size (optional)

        for (int hour : mClockHours) {
            String tmp = String.valueOf(hour);
            mPaint.getTextBounds(tmp, 0, tmp.length(), mRect);  // for circle-wise bounding

            // find the circle-wise (x, y) position as mathematical rule
            double angle = Math.PI / 6 * (hour - 3);
            int x = (int) (mWidth / 2 + Math.cos(angle) * mRadius - mRect.width() / 2);
            int y = (int) (mHeight / 2 + Math.sin(angle) * mRadius + mRect.height() / 2);

            canvas.drawText(String.valueOf(hour), x, y, mPaint);  // you can draw dots to denote hours as alternative

        }

    }
    @Override
    protected boolean verifyDrawable(Drawable who) {
        return mDial == who
                || mHourHand == who
                || mMinuteHand == who
                || mSecondHand == who
                || super.verifyDrawable(who);
    }

    private void initDrawable(Context context, Drawable drawable) {
        final int midX = drawable.getIntrinsicWidth() / 2;
        final int midY = drawable.getIntrinsicHeight() / 2;
        drawable.setBounds(-midX, -midY, midX, midY);
    }

    private void onTimeChanged() {
        mTime.setTimeInMillis(System.currentTimeMillis());
        setContentDescription(DateFormat.format(mDescFormat, mTime));
        invalidate();
    }

    public void setTimeZone(String id) {
        mTimeZone = TimeZone.getTimeZone(id);
        mTime.setTimeZone(mTimeZone);
        onTimeChanged();
    }
}
