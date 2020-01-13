package com.zhoug.fileselector.selector.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;

import com.zhoug.common.adapters.recycler.BaseRecyclerViewAdapter;
import com.zhoug.common.adapters.recycler.BaseViewHolder;
import com.zhoug.common.content.FileType;
import com.zhoug.common.imageloader.ImageLoader;
import com.zhoug.common.utils.ResourceUtils;
import com.zhoug.fileselector.R;
import com.zhoug.fileselector.beans.LocalMediaFolder;

import java.util.List;


/**
 * @Author HK-LJJ
 * @Date 2020/1/7
 * @Description
 */
public class FolderListAdapter extends BaseRecyclerViewAdapter<LocalMediaFolder> {
    private static final String TAG = ">>>FolderListAdapter";
    private Context context;
    //选择的position
    private String selectedPath;//选择的文件夹
    private int colorNormal;//未选中时item的背景
    private int colorSelected;//选中时item的背景

    public FolderListAdapter(Context context) {
        this.context = context;
        colorSelected = ResourceUtils.getColor(context, R.color.fileselector_folder_bg_selected);
        colorNormal = ResourceUtils.getColor(context, R.color.fileselector_folder_bg_normal);
        addHeader(LayoutInflater.from(context).inflate(R.layout.fileselector_folder_list_header, null));

    }

    @Override
    public int getLayoutId(int viewType) {
        return R.layout.fileselector_folder_list_item;
    }

    @Override
    public void onBindData(BaseViewHolder holder, LocalMediaFolder data, int position, int viewType) {
//        LogUtils.d(TAG, "onBindData:position=" + position);
        if (data != null) {
            ImageView imageView = holder.getView(R.id.iv_image);
            holder.setText(R.id.tv_name, data.getName());
            holder.setText(R.id.tv_size, "(" + data.getNum() + ")");

            String firstFilePath = data.getFirstFilePath();
            int type = FileType.getType(firstFilePath);
            if (type == FileType.IMAGE || type == FileType.VIDEO) {
                ImageLoader.load(context, imageView, firstFilePath, null, null, null);
            } else {
                ImageLoader.load(context, imageView, R.drawable.fileselector_icon_audio, null, null, null);
            }
            if (isSelected(data)) {
                holder.itemView.setBackgroundColor(colorSelected);
            } else {
                holder.itemView.setBackgroundColor(colorNormal);
            }
        }
    }




    /**
     * 是否选中(通过文件夹路径判断)
     *
     * @param localMediaFolder
     * @return
     */
    private boolean isSelected(LocalMediaFolder localMediaFolder) {
        if (localMediaFolder != null) {
            String path = localMediaFolder.getPath();
            if (selectedPath == null) {
                return path == null;
            } else return selectedPath.equals(path);
        }
        return false;
    }


    /**
     * 设置选中
     * @param selectedPath
     */
    public void setSelectedPath(String selectedPath) {
        this.selectedPath = selectedPath;
    }

    /**
     * 选中
     *
     * @return
     */
    public String getSelectedPath() {
        return selectedPath;
    }

    /**
     * 获取选中的文件夹
     * @return
     */
    public LocalMediaFolder getSelected() {
        List<LocalMediaFolder> data = getData();
        if(data!=null){
            for(LocalMediaFolder folder:data){
                if(isSelected(folder)){
                    return folder;
                }
            }
        }
        return null;
    }


}
