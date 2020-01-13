package com.zhoug.fileselector.loader;

import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;


import com.zhoug.common.annotation.Filetype;
import com.zhoug.common.content.FileType;
import com.zhoug.common.interfaces.DefaultLifecycleObserver;
import com.zhoug.common.utils.LogUtils;
import com.zhoug.common.utils.ThreadUtils;
import com.zhoug.fileselector.beans.LocalMedia;
import com.zhoug.fileselector.beans.LocalMediaFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 本地媒体加载器,从数据库加载指定类型的文件
 * @Author HK-LJJ
 * @Date 2020/1/6
 * @Description
 */
public class LocalMediaLoader implements DefaultLifecycleObserver, Loader.OnLoadCompleteListener<Cursor> {
    private static final String TAG = ">>>LocalMediaLoader";
    private Context context;
    private long maxDuration;//最大时长,单位秒
    private long minDuration;//最小时长
    @Filetype
    private int fileType = FileType.IMAGE;//文件类型,默认图片

    /**
     * 数据库游标加载器
     */
    private CursorLoader mLoader;

    /**
     * 加载监听器
     */
    private LocalMediaLoadListener mLoadListener;

    //排序
    private static final String ORDER_BY = MediaStore.Files.FileColumns._ID + " DESC";
    //查询uri
    private static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");
    // 查询条件
    private static final String SELECTION = MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
            + " AND " + MediaStore.MediaColumns.SIZE + ">0";

    // 媒体文件数据库字段
    private static final String[] PROJECTION = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.SIZE,
            "duration"};


    public LocalMediaLoader(LifecycleOwner owner, @NonNull Context context, @Filetype int fileType, long maxDuration, long minDuration) {
        this.context = context;
        this.maxDuration = maxDuration;
        this.minDuration = minDuration;
        this.fileType = fileType;
        owner.getLifecycle().addObserver(this);
    }


    public void setLoadListener(LocalMediaLoadListener loadListener) {
        this.mLoadListener = loadListener;
    }


    /**
     * 创建CursorLoader
     * @return
     */
    private CursorLoader createLoader() {
        CursorLoader cursorLoader = null;
        LogUtils.d(TAG, "onCreateLoader:fileType=" + fileType);
        switch (fileType) {
            case FileType.IMAGE:
                String[] image_selectArgs = new String[]{String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)};
                cursorLoader = new CursorLoader(context,
                        QUERY_URI,
                        PROJECTION,
                        SELECTION,
                        image_selectArgs,
                        ORDER_BY);
                break;
            case FileType.VIDEO:
                String[] video_selectArgs = new String[]{String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)};
                String durationCondition = getDurationCondition(0, 0);
                String video_selection = SELECTION + " AND " + durationCondition;
                cursorLoader = new CursorLoader(context,
                        QUERY_URI,
                        PROJECTION,
                        video_selection,
                        video_selectArgs,
                        ORDER_BY);

                break;
            case FileType.AUDIO:
                String[] audio_selectArgs = new String[]{String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO)};
                String audio_durationCondition = getDurationCondition(0, 500);
                String audio_selection = SELECTION + " AND " + audio_durationCondition;
                cursorLoader = new CursorLoader(context,
                        QUERY_URI,
                        PROJECTION,
                        audio_selection,
                        audio_selectArgs,
                        ORDER_BY);
                break;


        }
        return cursorLoader;
    }

    /**
     * 重Cursor中取得数据
     *
     * @param data 数据Cursor
     */
    private void parseCursor(Cursor data) {
        try {
            if (data != null) {
                //文件夹
                List<LocalMediaFolder> folders = new ArrayList<>();
                //所有文件
                List<LocalMedia> allLocalMedia = new ArrayList<>();
                //所有文件的文件夹
                LocalMediaFolder allMediaFolder = new LocalMediaFolder();
                int count = data.getCount();
                if (count > 0) {
                    data.moveToFirst();
                    do {
                        String path = data.getString(data.getColumnIndexOrThrow(PROJECTION[1]));
                        String mimeType = data.getString(data.getColumnIndexOrThrow(PROJECTION[2]));
                        int w = data.getInt(data.getColumnIndexOrThrow(PROJECTION[3]));
                        int h = data.getInt(data.getColumnIndexOrThrow(PROJECTION[4]));
                        int size = data.getInt(data.getColumnIndexOrThrow(PROJECTION[5]));

                        long duration = data.getLong(data.getColumnIndexOrThrow(PROJECTION[6]));

                        LocalMedia localMedia = new LocalMedia();
                        localMedia.setPath(path);
                        if (w > 0) {
                            localMedia.setWidth(w);
                        }
                        if (h > 0) {
                            localMedia.setHeight(h);
                        }
                        if (size > 0) {
                            localMedia.setSize(size);
                        }
                        if (duration > 0) {
                            localMedia.setDuration(duration);
                        }
                        if (mimeType != null) {
                            localMedia.setMimeType(mimeType);
                        }

                        LocalMediaFolder folder = getImageFolder(path, folders);
                        folder.getMedias().add(localMedia);
                        allLocalMedia.add(localMedia);
                    } while (data.moveToNext());

                    sortFolder(folders);
                    if (allLocalMedia.size() > 0) {
                        String title = fileType == FileType.IMAGE ? "所有图片" : fileType == FileType.VIDEO ? "所有视频" : fileType == FileType.AUDIO ? "所有音频" : "所有文件";
                        allMediaFolder.setName(title);
                        allMediaFolder.setMedias(allLocalMedia);
                        allMediaFolder.setFirstFilePath(allLocalMedia.get(0).getPath());
                        folders.add(0, allMediaFolder);
                    }

                    if (mLoadListener != null) {
                        ThreadUtils.runMainThread(() -> mLoadListener.onLoadComplete(folders));

                    }
                } else {
                    //没有查询到数据
                    if (mLoadListener != null) {
                        ThreadUtils.runMainThread(() -> mLoadListener.onLoadComplete(folders));
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载数据
     */
    public void loadAllMedia() {
        if (mLoader == null) {
            mLoader = createLoader();
            mLoader.registerListener(fileType, this);
        }
        mLoader.startLoading();
    }


    /**
     * 获取文件所在的文件夹, 如果在imageFolders总已经存在则直接返回,否则创建并且加载imageFolders集合
     * @param path 文件路径
     * @param imageFolders 所有创建的文件夹集合
     * @return
     */
    private LocalMediaFolder getImageFolder(String path, List<LocalMediaFolder> imageFolders) {
        File imageFile = new File(path);
        File folderFile = imageFile.getParentFile();
        for (LocalMediaFolder folder : imageFolders) {
            // 同一个文件夹下，返回自己，否则创建新文件夹
            if (folder.getName().equals(folderFile.getName())) {
                return folder;
            }
        }
        LocalMediaFolder newFolder = new LocalMediaFolder();
        newFolder.setName(folderFile.getName());
        newFolder.setPath(folderFile.getAbsolutePath());
        newFolder.setFirstFilePath(path);
        imageFolders.add(newFolder);
        return newFolder;
    }

    /**
     * 文件夹按文件数量进行排序
     * @param imageFolders
     */
    private void sortFolder(List<LocalMediaFolder> imageFolders) {
        // 文件夹按图片数量排序
        Collections.sort(imageFolders, (lhs, rhs) -> {
            int lnum = lhs.getNum();
            int rnum = rhs.getNum();
            if (lnum > rnum) {
                return -1;
            } else if (lnum < rnum) {
                return 1;
            } else {
                return 0;
            }
        });
    }

    /**
     * 获取视频(最长或最小时间)
     * @param exMaxLimit
     * @param exMinLimit
     * @return
     */
    private String getDurationCondition(long exMaxLimit, long exMinLimit) {
        long maxS = maxDuration <= 0 ? Long.MAX_VALUE : maxDuration;
        if (exMaxLimit > 0) {
            maxS = Math.min(maxS, exMaxLimit);
        }
        long minS = Math.max(exMinLimit, minDuration);
        if (minS < 0) {
            minS = 0;
        }

        return String.format(Locale.CHINA, "%d <%s duration and duration <= %d",
                minS,
                minS == 0 ? "" : "=",
                maxS);
    }

    @Override
    public void onLoadComplete(@NonNull Loader loader, @Nullable Cursor data) {
        parseCursor(data);
    }


    /**
     * 文件加载结果监听
     */
    public interface LocalMediaLoadListener {
        void onLoadComplete(@NonNull List<LocalMediaFolder> folders);
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        LogUtils.d(TAG, "onDestroy:");
        if (mLoader != null) {
            try {
                mLoader.cancelLoad();
                mLoader.unregisterListener(this);
                mLoader=null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
