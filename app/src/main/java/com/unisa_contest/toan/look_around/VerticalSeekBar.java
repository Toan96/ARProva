package com.unisa_contest.toan.look_around;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Antonio on 31/01/2018.
 * .
 */

public class VerticalSeekBar extends android.support.v7.widget.AppCompatSeekBar /*prima era seekBar*/ {

    private OnSeekBarChangeListener onChangeListener;
    private int lastProgress = 0;

    public VerticalSeekBar(Context context) {
        super(context);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(h, w, oldH, oldW);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //noinspection SuspiciousNameCombination
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    protected void onDraw(Canvas c) {
        c.rotate(-90);
        c.translate(-getHeight(), 0);
        super.onDraw(c);
    }

    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener onChangeListener) {
        this.onChangeListener = onChangeListener;
    }

    //touch perform non dovrebbe essere necessario per la seekBar
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onChangeListener.onStartTrackingTouch(this);
                setPressed(true);
                setSelected(true);
                break;
            case MotionEvent.ACTION_MOVE:
                super.onTouchEvent(event);
                int progress = getMax() - (int) (getMax() * event.getY() / getHeight());
                // Ensure progress stays within boundaries
                if (progress < 0) {
                    progress = 0;
                }
                if (progress > getMax()) {
                    progress = getMax();
                }
                setProgress(progress);  // Draw progress
                if (progress != lastProgress) {
                    // Only enact listener if the progress has actually changed
                    lastProgress = progress;
                    onChangeListener.onProgressChanged(this, progress, true);
                }
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                setPressed(true);
                setSelected(true);
                break;
            case MotionEvent.ACTION_UP:
                onChangeListener.onStopTrackingTouch(this);
                setPressed(false);
                setSelected(false);
                break;
            case MotionEvent.ACTION_CANCEL:
                super.onTouchEvent(event);
                setPressed(false);
                setSelected(false);
                break;
        }
        return true;
    }

    public synchronized void setProgressAndThumb(int progress) {
        setProgress(progress);
        onSizeChanged(getWidth(), getHeight(), 0, 0);
        if (progress != lastProgress) {
            // Only enact listener if the progress has actually changed
            lastProgress = progress;
            onChangeListener.onProgressChanged(this, progress, true);
        }
    }
}
