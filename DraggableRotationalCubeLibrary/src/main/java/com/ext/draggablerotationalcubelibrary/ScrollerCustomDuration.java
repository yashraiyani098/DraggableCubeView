package com.ext.draggablerotationalcubelibrary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class ScrollerCustomDuration extends Scroller {

    private double mScrollFactor = 1;
    private static final int DEFAULT_DURATION = 1000; // 1 second default duration
    private static final Interpolator DEFAULT_INTERPOLATOR = new DecelerateInterpolator();
    private Interpolator mInterpolator;
    private int mCustomDuration = DEFAULT_DURATION;

    public ScrollerCustomDuration(Context context) {
        super(context);
        mInterpolator = DEFAULT_INTERPOLATOR;
    }

    public ScrollerCustomDuration(Context context, Interpolator interpolator) {
        super(context, interpolator);
        mInterpolator = interpolator;
    }

    @SuppressLint("NewApi")
    public ScrollerCustomDuration(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
        mInterpolator = interpolator;
    }

    /**
     * Set the factor by which the duration will change
     */
    public void setScrollDurationFactor(double scrollFactor) {
        mScrollFactor = scrollFactor;
        mCustomDuration = (int) (DEFAULT_DURATION * mScrollFactor);
    }

    /**
     * Get the current scroll duration factor
     */
    public double getScrollDurationFactor() {
        return mScrollFactor;
    }

    /**
     * Set custom duration for smoother cube rotation
     */
    public void setCustomDuration(int duration) {
        mScrollFactor = (double) duration / DEFAULT_DURATION;
        mCustomDuration = duration;
    }

    /**
     * Set the interpolator for smoother animation
     */
    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    /**
     * Get the current custom duration
     */
    public int getCustomDuration() {
        return mCustomDuration;
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        // Use our custom duration
        super.startScroll(startX, startY, dx, dy, mCustomDuration);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        // Override to use our custom duration
        startScroll(startX, startY, dx, dy, mCustomDuration);
    }

    /**
     * Reset the scroller to default settings
     */
    public void reset() {
        mScrollFactor = 1;
        mCustomDuration = DEFAULT_DURATION;
        mInterpolator = DEFAULT_INTERPOLATOR;
    }

}