package com.zhoug.fileselector.selector.impl;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.zhoug.fileselector.selector.FileSelector;

import java.util.Random;

/**
 * 文件选择器基类
 * @Author HK-LJJ
 * @Date 2020/1/8
 * @Description
 */
abstract class AbsFileSelector implements FileSelector {
    protected static final String TAG = ">>>AbsFileSelector";
    Activity mActivity;
    Fragment mFragment;
    final int REQUEST_CODE;
    onSelectListener mOnFileChooseListener;

    protected Config config = new Config();

    AbsFileSelector(Activity activity) {
        this.mActivity = activity;
        REQUEST_CODE = getInt();
    }

    AbsFileSelector(Fragment fragment) {
        this.mFragment = fragment;
        this.mActivity = fragment.getActivity();
        REQUEST_CODE = getInt();//每个实例动态生成一个REQUEST_CODE
    }

    private int getInt() {
        Random random = new Random();
        return random.nextInt(9000) + 999;// 999-9999
    }

    @Override
    public FileSelector setOnFileChooseListener(onSelectListener onFileChooseListener) {
        this.mOnFileChooseListener = onFileChooseListener;
        return this;
    }

    @Override
    public FileSelector setConfig(@NonNull Config config) {
        this.config = config;
        return this;
    }

    @Override
    public @NonNull
    Config getConfig() {
        return config;
    }
}
