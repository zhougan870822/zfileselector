package com.zhoug.fileselector.beans;



import com.zhoug.common.annotation.Filetype;
import com.zhoug.common.content.FileType;
import com.zhoug.common.utils.JsonUtils;

import java.io.Serializable;

/**
 * 本地媒体文件
 * @Author HK-LJJ
 * @Date 2020/1/6
 * @Description
 */
public class LocalMedia implements Serializable {
    /**
     * 文件路径
     */
    private String path;

    /**
     * 文件名(主要用于音视频)
     */
    private String name;
    /**
     * 文件大小
     */
    private long size;
    /**
     * 可播放文件(视频/音频)的总时长.单位毫秒
     */
    private long duration;

    /**
     * 宽
     */
    private int width;

    /**
     * 高
     */
    private int height;




    /**
     * 文件类型
     */
    @Filetype
    private int fileType;

    private String mimeType;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getFileType() {
        if (fileType == 0) {
            fileType = FileType.getType(path);
        }

        return fileType;
    }

    public void setFileType(@Filetype int fileType) {
        this.fileType = fileType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this);
    }


}
