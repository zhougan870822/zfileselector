package com.zhoug.fileselector.beans;


import com.zhoug.common.utils.JsonUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 本地媒体文夹
 * @Author HK-LJJ
 * @Date 2020/1/6
 * @Description
 */
public class LocalMediaFolder  implements Serializable {
    private String name;//文件名
    private String path;//文件夹路径
    private String firstFilePath;//第一个文件的地址

    private List<LocalMedia> medias = new ArrayList<>();


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFirstFilePath() {
        return firstFilePath;
    }

    public void setFirstFilePath(String firstFilePath) {
        this.firstFilePath = firstFilePath;
    }

    public int getNum() {
        if (medias != null) {
            return medias.size();
        }
        return 0;
    }




    public List<LocalMedia> getMedias() {
        return medias;
    }

    public void setMedias(List<LocalMedia> medias) {
        this.medias = medias;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this);
    }
}
