package com.ext.draggablerotationalcubelibrary;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Interpolator;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.PageTransformer;

import java.lang.reflect.Field;

public class ViewPagerCustomDuration extends ViewPager {

    public ViewPagerCustomDuration(Context context) {
        super(context);
        postInitViewPager();
    }

    public ViewPagerCustomDuration(Context context, AttributeSet attrs) {
        super(context, attrs);
        postInitViewPager();
    }

    private ScrollerCustomDuration mScroller = null;
    public double scrollDurationFactor = 1.0;
    private PageTransformer pageTransformer = null;

    /**
     * Override the Scroller instance with our own class so we can change the
     * duration
     */
    private void postInitViewPager() {
        try {
            Field scroller = ViewPager.class.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            Field interpolator = ViewPager.class.getDeclaredField("sInterpolator");
            interpolator.setAccessible(true);

            mScroller = new ScrollerCustomDuration(getContext(),
                    (Interpolator) interpolator.get(null));
            scroller.set(this, mScroller);
        } catch (Exception e) {
        }
    }

    /**
     * Set the factor by which the duration will change
     */
    /**
     * Set the scroll duration factor
     */
    public void setScrollDurationFactor(double scrollFactor) {
        this.scrollDurationFactor = scrollFactor;
        if (mScroller != null) {
            mScroller.setScrollDurationFactor(scrollFactor);
        }
    }

    /**
     * Get the current scroll duration factor
     */
    public double getScrollDurationFactor() {
        return scrollDurationFactor;
    }

    /**
     * Set the page transformer
     */
    public void setPageTransformer(boolean reverseDrawingOrder, PageTransformer transformer) {
        this.pageTransformer = transformer;
        super.setPageTransformer(reverseDrawingOrder, transformer);
    }

    /**
     * Set the page transformer with duration
     */
    public void setPageTransformer(boolean reverseDrawingOrder, PageTransformer transformer, int duration) {
        this.pageTransformer = transformer;
        super.setPageTransformer(reverseDrawingOrder, transformer, duration);
    }

    /**
     * Get the current page transformer
     */
    public PageTransformer getPageTransformer() {
        return pageTransformer;
    }

}