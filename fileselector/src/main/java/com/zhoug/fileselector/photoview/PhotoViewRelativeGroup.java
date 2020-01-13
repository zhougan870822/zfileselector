package com.zhoug.fileselector.photoview;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class PhotoViewRelativeGroup extends RelativeLayout {
    private static final String TAG = "PhotoViewRelativeGroup";

    public PhotoViewRelativeGroup(Context context) {
        super(context);
    }

    public PhotoViewRelativeGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoViewRelativeGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PhotoViewRelativeGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
//            LogUtils.d(TAG, "onInterceptTouchEvent: "+e.getMessage());
            return false;
        }
    }
}
