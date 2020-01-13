package com.zhoug.fileselector.beans;

import com.zhoug.common.annotation.Filetype;
import com.zhoug.common.content.FileType;
import com.zhoug.common.utils.JsonUtils;

import java.io.Serializable;

/**
 * 定义文件 用于文件播放
 * @Author HK-LJJ
 * @Date 2020/1/13
 * @Description
 */
public class ZFile implements Serializable {

    /**
     * 文件播放url,可以是本地路径和网络地址
     */
    private final String url;
    /**
     * 文件类型
     */
    private final @Filetype int fileType;

    public ZFile(String url, int fileType) {
        this.url = url;
        this.fileType = fileType;
    }

    public String getUrl() {
        return url;
    }

    public int getFileType() {
        return fileType;
    }

    public boolean isImage(){
        return fileType==FileType.IMAGE;
    }

    public boolean isVideo(){
        return fileType==FileType.VIDEO;
    }

    public boolean isAudio(){
        return fileType==FileType.AUDIO;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this);
    }

}
