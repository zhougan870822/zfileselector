package com.zhoug.fileselector.selector.impl;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.zhoug.fileselector.selector.FileSelector;

/**
 * 文件选择器创建工厂
 * @Author HK-LJJ
 * @Date 2020/1/6
 * @Description
 */
public class FileSelectorFactory {

    public static FileSelector createFileChooser(Activity activity){
        return new FileSelectorImpl(activity);
    }

    public static FileSelector createFileChooser(Fragment fragment){
        return new FileSelectorImpl(fragment);
    }



}
