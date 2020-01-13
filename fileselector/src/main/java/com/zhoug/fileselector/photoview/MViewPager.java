package com.zhoug.fileselector.photoview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * photoView缩小的异常
 */
public class MViewPager extends android.support.v4.view.ViewPager {
    private static final String TAG = "ViewPager";
    public MViewPager(@NonNull Context context) {
        super(context);
    }

    public MViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//            LogUtils.d(TAG, "onInterceptTouchEvent: "+e.getMessage());
            return false;
        }
    }
}
