package com.zhoug.fileselector.selector.impl;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;


import com.zhoug.fileselector.R;
import com.zhoug.fileselector.beans.LocalMedia;
import com.zhoug.fileselector.selector.ui.FileSelectorActivity;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 文件选择器,使用自定义的FileChooserActivity选择文件
 * @Author HK-LJJ
 * @Date 2020/1/8
 * @Description
 */
class FileSelectorImpl extends AbsFileSelector {
    private static final String TAG = ">>>FileChooserImpl";

    FileSelectorImpl(Activity activity) {
        super(activity);
    }

    FileSelectorImpl(Fragment fragment) {
        super(fragment);
    }


    @Override
    public void start() {
        Intent navigationIntent = FileSelectorActivity.getNavigationIntent(mActivity,config);
        if(mFragment!=null){
            mFragment.startActivityForResult(navigationIntent, REQUEST_CODE);
        }else {
            mActivity.startActivityForResult(navigationIntent,REQUEST_CODE );
        }
        //动画
        mActivity.overridePendingTransition(R.anim.fileselector_alpha_activity_int, R.anim.fileselector_alpha_activity_out);

    }

    @SuppressWarnings("unchecked")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_CODE && resultCode==Activity.RESULT_OK && data!=null){
            Serializable result = data.getSerializableExtra(FileSelectorActivity.EXTRA_RESULT_MEDIA);
           if(mOnFileChooseListener!=null){
               if(result==null){
                   mOnFileChooseListener.onResult(new ArrayList<>());
               }else{
                   ArrayList<LocalMedia> localMedias= (ArrayList<LocalMedia>) result;
                   mOnFileChooseListener.onResult(localMedias);
               }
           }
        }
    }



}
