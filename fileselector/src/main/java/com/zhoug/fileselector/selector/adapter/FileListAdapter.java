package com.zhoug.fileselector.selector.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.zhoug.common.adapters.recycler.BaseRecyclerViewAdapter;
import com.zhoug.common.adapters.recycler.BaseViewHolder;
import com.zhoug.common.content.FileType;
import com.zhoug.common.imageloader.ImageLoader;
import com.zhoug.common.utils.IntentUtils;
import com.zhoug.common.utils.StringUtils;
import com.zhoug.fileselector.R;
import com.zhoug.fileselector.beans.LocalMedia;
import com.zhoug.fileselector.selector.FileSelector;

import java.util.ArrayList;

/**
 * @Author HK-LJJ
 * @Date 2020/1/7
 * @Description
 */
public class FileListAdapter extends BaseRecyclerViewAdapter<LocalMedia> {
    private static final String TAG = ">>>FileListAdapter";

    private Context context;
    /**
     * 选中回掉
     */
    private OnCheckChangeCallback onCheckChangeCallback;

    /**
     * 选择的文件
     */
    private ArrayList<LocalMedia> selectedMedias = new ArrayList<>();

    private final FileSelector.Config config;

    public FileListAdapter(Context context, FileSelector.Config config) {
        this.context = context;
        this.config = config;

    }

    @Override
    public int getLayoutId(int viewType) {
        return R.layout.fileselector_file_list_item;
    }


    @Override
    public void onBindData(BaseViewHolder holder, LocalMedia data, int position, int viewType) {
        if (data != null) {
//            LogUtils.d(TAG, "onBindData:"+data);

            ImageView imageView = holder.getView(R.id.iv_image);
            ImageView ivCheck = holder.getView(R.id.iv_check);
            ImageView ivType = holder.getView(R.id.iv_type);
            TextView tvDuration = holder.getView(R.id.tv_duration);
            View iconGroup = holder.getView(R.id.icon_group);

            setCheckImage(ivCheck, isSelected(data));
            ivCheck.setTag(position);
            ivCheck.setOnClickListener(checkOnClickListener);
            switch (data.getFileType()) {
                case FileType.IMAGE:
                    iconGroup.setVisibility(View.GONE);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    ImageLoader.load(context, imageView, data.getPath(), null,null,null );
                    break;
                case FileType.VIDEO:
                    iconGroup.setVisibility(View.VISIBLE);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    ImageLoader.load(context, imageView, data.getPath(), null, null, null);
                    ivType.setImageResource(R.drawable.fileselector_picture_video);
                    tvDuration.setText(StringUtils.getStringTime(data.getDuration() / 1000));
                    break;
                case FileType.AUDIO:
                    iconGroup.setVisibility(View.VISIBLE);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    ImageLoader.load(context, imageView, R.drawable.fileselector_icon_audio, null, null, null);
                    ivType.setImageResource(R.drawable.fileselector_picture_audio);
                    tvDuration.setText(StringUtils.getStringTime(data.getDuration() / 1000));
                    break;
                default:
                    iconGroup.setVisibility(View.GONE);

            }

            holder.itemView.setClickable(true);
            holder.itemView.setTag(position);
            holder.itemView.setOnClickListener(onItemClickListener);
        }
    }

    /**
     * 选择按钮点击监听
     */
    private View.OnClickListener checkOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag();
            LocalMedia itemData = getItemData(position);
//            LogUtils.d(TAG, "onClick:position=" + position);
            changeSelectedStatus((ImageView) v, itemData);

        }
    };

    private View.OnClickListener onItemClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position= (int) v.getTag();
            LocalMedia itemData = getItemData(position);
            Intent readFileIntent = IntentUtils.getReadFileIntent(context, itemData.getPath(), context.getPackageName() + ".fileprovider", itemData.getMimeType());
            context.startActivity(readFileIntent);

        }
    };

    /**
     * 根据选择状态设置图片
     *
     * @param ivCheck
     * @param check
     */
    private void setCheckImage(ImageView ivCheck, boolean check) {
        if (check) {
            ivCheck.setImageResource(R.drawable.fileselector_picture_checked);
        } else {
            ivCheck.setImageResource(R.drawable.fileselector_picture_uncheck);
        }
    }

    /**
     * 改变LocalMedia的选择状态
     *
     * @param localMedia
     */
    private void changeSelectedStatus(ImageView view, LocalMedia localMedia) {
        //文件路径一样表示同一个文件
        LocalMedia selected = null;
        if (selectedMedias.size() > 0) {
            for (LocalMedia media : selectedMedias) {
                String path = localMedia.getPath();
                if (path.equals(media.getPath())) {
                    selected = media;
                    break;
                }
            }
        }
        int size = selectedMedias.size();
        //未被选中->选中
        if (selected == null) {
            //单选模式
            if (config.singleModel) {
                if(size==0){
                    selectedMedias.add(localMedia);
                    setCheckImage(view, true);
                }
                if (onCheckChangeCallback != null) {
                    onCheckChangeCallback.onCompletion();
                }
            } else {
                //多选模式
                if(size<config.maxNum){
                    selectedMedias.add(localMedia);
                    setCheckImage(view, true);
                    if (onCheckChangeCallback != null) {
                        onCheckChangeCallback.onCheckedChange(localMedia,true );
                       /* if(selectedMedias.size()==config.maxNum){
                            onCheckChangeCallback.onCompletion();
                        }else{
                            onCheckChangeCallback.onCheckedChange(localMedia,true );
                        }*/
                    }
                }else{
                    if (onCheckChangeCallback != null) {
                        onCheckChangeCallback.onCompletion();
                    }
                }
            }
        } else {
            //已经被选中->取消选中
            selectedMedias.remove(selected);
            setCheckImage(view, false);
            if (onCheckChangeCallback != null) {
                onCheckChangeCallback.onCheckedChange(localMedia, false);
            }

        }

    }


    /**
     * 判断指定的LocalMedia是否被选中
     *
     * @param localMedia
     * @return
     */
    private boolean isSelected(LocalMedia localMedia) {
        if (selectedMedias.size() > 0) {
            for (LocalMedia media : selectedMedias) {
                String path = localMedia.getPath();
                if (path.equals(media.getPath())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 选择监听器(只有点击选择才会回掉)
     *
     * @param onCheckChangeCallback
     */
    public void setOnCheckChangeCallback(OnCheckChangeCallback onCheckChangeCallback) {
        this.onCheckChangeCallback = onCheckChangeCallback;
    }

    /**
     * 获取选择的文件集合
     *
     * @return
     */
    public ArrayList<LocalMedia> getSelectedMedias() {
        return selectedMedias;
    }

    public void setSelectedMedias(ArrayList<LocalMedia> selectedMedias) {
        if (selectedMedias == null) {
            selectedMedias = new ArrayList<>();
        }
        this.selectedMedias = selectedMedias;

    }

    /**
     * 添加选中文件
     *
     * @param media
     */
    public void addSelectedMedias(LocalMedia media) {
        this.selectedMedias.add(media);
    }

    /**
     * 点击选择文件监听
     */
    public interface OnCheckChangeCallback {
        //选择状态改变
        void onCheckedChange(LocalMedia localMedia, boolean isChecked);

        //选择完成(单选/选择的数目达到最大数)
        void onCompletion();

    }
}
