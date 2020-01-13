package com.zhoug.fileselector;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * @Author HK-LJJ
 * @Date 2020/1/13
 * @Description
 */
public class FileProvider extends android.support.v4.content.FileProvider {
    public static  String AUTHORITY=".fileprovider";


    public static String getAuthority(@NonNull Context context){
        return context.getPackageName()+AUTHORITY;
    }

}
