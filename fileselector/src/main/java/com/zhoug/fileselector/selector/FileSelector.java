package com.zhoug.fileselector.selector;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.zhoug.common.annotation.Filetype;
import com.zhoug.common.content.FileType;
import com.zhoug.fileselector.beans.LocalMedia;

import java.util.List;


/**
 * 文件选择器接口
 * @Author HK-LJJ
 * @Date 2020/1/6
 * @Description
 */
public interface FileSelector {


    /**
     * 打开选择页面
     */
    void start();

    /**
     * 需要在activity或者在Fragment的onActivityResult方法中调用
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    void onActivityResult(int requestCode, int resultCode, Intent data);


    /**
     * 设置选择文件后的回掉
     *
     * @param onFileChooseListener
     * @return
     */
    FileSelector setOnFileChooseListener(onSelectListener onFileChooseListener);

    /**
     * 配置
     *
     * @param config
     * @return
     */
    FileSelector setConfig(@NonNull Config config);

    @NonNull
    Config getConfig();

    final class Config implements Parcelable {
        /**
         * 选择文件的类型
         */
        @Filetype
        public int fileType = FileType.IMAGE;

        /**
         * 音视频文件的最大播放时长
         */
        public long maxDuration = Long.MAX_VALUE;
        /**
         * 音视频文件的最小播放时长
         */
        public long minDuration = 0;
        /**
         * 设置最多选择的文件数
         */
        public int maxNum = Integer.MAX_VALUE;
        /**
         * 设置单选模式
         */
        public boolean singleModel = false;

        /**
         * 是否允许拍摄
         */
        public boolean isCamera=false;

        public Config() {

        }

        protected Config(Parcel in) {
            fileType = in.readInt();
            maxDuration = in.readLong();
            minDuration = in.readLong();
            maxNum = in.readInt();
            singleModel = in.readByte() != 0;
            isCamera = in.readByte() != 0;

        }


        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(fileType);
            dest.writeLong(maxDuration);
            dest.writeLong(minDuration);
            dest.writeInt(maxNum);
            dest.writeByte((byte) (singleModel ? 1 : 0));
            dest.writeByte((byte) (isCamera ? 1 : 0));

        }

        public static final Creator<Config> CREATOR = new Creator<Config>() {
            @Override
            public Config createFromParcel(Parcel in) {
                return new Config(in);
            }

            @Override
            public Config[] newArray(int size) {
                return new Config[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

    }

    /**
     * 选择结果回掉监听
     */
    interface onSelectListener{
        void onResult(List<LocalMedia> medias);
    }

}
