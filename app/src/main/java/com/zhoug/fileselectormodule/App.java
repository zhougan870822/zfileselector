package com.zhoug.fileselectormodule;

import android.app.Application;

import com.zhoug.common.utils.LogUtils;

/**
 * @Author HK-LJJ
 * @Date 2020/1/13
 * @Description
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.setDebug(true);
    }

}
