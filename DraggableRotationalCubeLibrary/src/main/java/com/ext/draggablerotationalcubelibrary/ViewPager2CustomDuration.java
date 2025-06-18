package com.ext.draggablerotationalcubelibrary;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.lang.reflect.Field;

public class ViewPager2CustomDuration extends FrameLayout {

    private ViewPager2 viewPager2;
    private double scrollDurationFactor = 1.0;

    public ViewPager2CustomDuration(@NonNull Context context) {
        super(context);
        init();
    }

    public ViewPager2CustomDuration(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewPager2CustomDuration(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        viewPager2 = new ViewPager2(getContext());
        addView(viewPager2, new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        postInitViewPager2();
    }

    private void postInitViewPager2() {
        try {
            // Get the RecyclerView from ViewPager2
            Field recyclerViewField = ViewPager2.class.getDeclaredField("mRecyclerView");
            recyclerViewField.setAccessible(true);
            RecyclerView recyclerView = (RecyclerView) recyclerViewField.get(viewPager2);

            // Get the ViewFlinger from RecyclerView
            Field viewFlingerField = RecyclerView.class.getDeclaredField("mViewFlinger");
            viewFlingerField.setAccessible(true);
            Object viewFlinger = viewFlingerField.get(recyclerView);

            // Get the scroller from ViewFlinger
            Field scrollerField = viewFlinger.getClass().getDeclaredField("mScroller");
            scrollerField.setAccessible(true);
            Object scroller = scrollerField.get(viewFlinger);

            // Replace with our custom scroller
            ScrollerCustomDuration customScroller = new ScrollerCustomDuration(getContext());
            scrollerField.set(viewFlinger, customScroller);
        } catch (Exception e) {
            // If reflection fails, we'll use the default behavior
            // This is expected on some devices or Android versions
        }
    }

    /**
     * Set the factor by which the duration will change
     */
    public void setScrollDurationFactor(double scrollFactor) {
        this.scrollDurationFactor = scrollFactor;
        // Update the scroller if we have access to it
        try {
            Field recyclerViewField = ViewPager2.class.getDeclaredField("mRecyclerView");
            recyclerViewField.setAccessible(true);
            RecyclerView recyclerView = (RecyclerView) recyclerViewField.get(viewPager2);

            Field viewFlingerField = RecyclerView.class.getDeclaredField("mViewFlinger");
            viewFlingerField.setAccessible(true);
            Object viewFlinger = viewFlingerField.get(recyclerView);

            Field scrollerField = viewFlinger.getClass().getDeclaredField("mScroller");
            scrollerField.setAccessible(true);
            Object scroller = scrollerField.get(viewFlinger);

            if (scroller instanceof ScrollerCustomDuration) {
                ((ScrollerCustomDuration) scroller).setScrollDurationFactor(scrollFactor);
            }
        } catch (Exception e) {
            // Ignore if reflection fails
        }
    }

    /**
     * Get the current scroll duration factor
     */
    public double getScrollDurationFactor() {
        return scrollDurationFactor;
    }

    // Delegate all ViewPager2 methods to the internal viewPager2 instance
    public void setAdapter(RecyclerView.Adapter<?> adapter) {
        viewPager2.setAdapter(adapter);
    }

    public RecyclerView.Adapter<?> getAdapter() {
        return viewPager2.getAdapter();
    }

    public void setCurrentItem(int item) {
        viewPager2.setCurrentItem(item);
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        viewPager2.setCurrentItem(item, smoothScroll);
    }

    public int getCurrentItem() {
        return viewPager2.getCurrentItem();
    }

    public void setPageTransformer(ViewPager2.PageTransformer transformer) {
        viewPager2.setPageTransformer(transformer);
    }

    public void setOrientation(int orientation) {
        viewPager2.setOrientation(orientation);
    }

    public int getOrientation() {
        return viewPager2.getOrientation();
    }

    public void setUserInputEnabled(boolean enabled) {
        viewPager2.setUserInputEnabled(enabled);
    }

    public boolean isUserInputEnabled() {
        return viewPager2.isUserInputEnabled();
    }

    public void setOffscreenPageLimit(int limit) {
        viewPager2.setOffscreenPageLimit(limit);
    }

    public int getOffscreenPageLimit() {
        return viewPager2.getOffscreenPageLimit();
    }

    public ViewPager2 getViewPager2() {
        return viewPager2;
    }
}