package com.zhoug.fileselector.looker.fragment;

import android.view.View;
import android.widget.ProgressBar;

import com.github.chrisbanes.photoview.PhotoView;
import com.zhoug.common.imageloader.ImageLoader;
import com.zhoug.common.utils.LogUtils;
import com.zhoug.fileselector.R;

/**
 * 图片
 */
public class ImageFragment extends BaseFileFragment {
    private static final String TAG = ">>>>>ImageFragment";
    private PhotoView photoView;
    private ProgressBar progressBar;


    @Override
    protected int getLayoutResId() {
        return R.layout.fileselector_fragment_image;
    }

    @Override
    protected void init(View root) {
        photoView=root.findViewById(R.id.photoView);
        progressBar=root.findViewById(R.id.progressBar);
        init();
    }

   private void init(){
        if(zFile==null){
            return;
        }
       ImageLoader.load(getContext(), photoView, zFile.getUrl(),null,R.drawable.fileselector_img_load_error,progressBar);

   }

    @Override
    public void onDestroyView() {
        LogUtils.d(TAG, "onDestroyView: ");
        super.onDestroyView();
    }




}
