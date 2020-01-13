package com.zhoug.fileselector.looker;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.zhoug.common.adapters.recycler.BaseRecyclerViewAdapter;
import com.zhoug.common.adapters.recycler.BaseViewHolder;
import com.zhoug.common.content.FileType;
import com.zhoug.common.imageloader.ImageLoader;
import com.zhoug.fileselector.R;
import com.zhoug.fileselector.beans.ZFile;
import com.zhoug.widgets.dialog.list.StringListDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 文件展示控件(默认网格展示4列)
 * @Author HK-LJJ
 * @Date 2020/1/3
 * @Description
 */
public class FilePreview extends FrameLayout {
    private static final String TAG = ">>>FilePreview";
    private RecyclerView recyclerView;
    private Adapter mAdapter;
    private boolean deleteEnable = false;//是否可以删除
    private ArrayList<ZFile> files = new ArrayList<>();
    private OnDeleteListener onDeleteListener;

    public FilePreview(@NonNull Context context) {
        this(context, null);
    }

    public FilePreview(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FilePreview(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fileselector_file_preview, null);
        recyclerView = view.findViewById(R.id.recyclerView_file);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        mAdapter = new Adapter();
        mAdapter.setData(files);
        mAdapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(mAdapter);
        addView(view);
    }

    /**
     * 图片单击事件
     */
    private BaseRecyclerViewAdapter.OnItemClickListener onItemClickListener = new BaseRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(BaseRecyclerViewAdapter adapter, View view, int position) {
            ZFile comFile = mAdapter.getItemData(position);
            if (deleteEnable) {
                StringListDialog dialog = new StringListDialog(getContext(), com.zhoug.widgets.R.style.widget_dialog_full);
                dialog.setCanceledOnTouchOutside(true);
                dialog.addQuxiaoBtn(true);
                dialog.setTitle(null);
                dialog.setDefWindowAnimations();
                dialog.setData(Arrays.asList("查看", "删除"));
                dialog.setOnItemClickListener((parent, v, pos, id) -> {
                    dialog.cancel();
                    if (pos == 0) {
                        //查看
//                        toast("查看");
                        toLook(position);
                    } else if (pos == 1) {
                        //删除
                        files.remove(comFile);
                        mAdapter.notifyDataSetChanged();
                        if (onDeleteListener != null) {
                            onDeleteListener.onDelete(comFile);
                        }
//                        toast("删除");
                    }
                });
                dialog.show();
            } else {
                //查看
//                toast("查看");
                toLook(position);
            }


        }
    };

    /**
     * 适配器
     */
    public class Adapter extends BaseRecyclerViewAdapter<ZFile> {
        @Override
        public int getLayoutId(int viewType) {
            return R.layout.fileselector_file_preview_item;
        }

        @Override
        public void onBindData(BaseViewHolder holder, ZFile data, int position, int viewType) {

            ImageView imageView = holder.getView(R.id.img_file_item);
            ImageView ivVideo = holder.getView(R.id.iv_video);
            ImageView ivAudio = holder.getView(R.id.iv_audio);
            if (data != null) {
//                LogUtils.d(TAG, "onBindData: data=" + data);
                switch (data.getFileType()) {
                    case FileType.IMAGE:
                        ImageLoader.load(getContext(), imageView, data.getUrl());
                        imageView.setVisibility(View.VISIBLE);
                        ivVideo.setVisibility(View.GONE);
                        ivAudio.setVisibility(View.GONE);
                        break;
                    case FileType.VIDEO:
                        ImageLoader.load(getContext(), imageView, data.getUrl());
                        imageView.setVisibility(View.VISIBLE);
                        ivVideo.setVisibility(View.VISIBLE);
                        ivAudio.setVisibility(View.GONE);

                        break;
                    case FileType.AUDIO:
                        imageView.setVisibility(View.GONE);
                        ivVideo.setVisibility(View.GONE);
                        ivAudio.setVisibility(View.VISIBLE);

                        break;
                }

            }
        }

    }


    private void toLook(int position) {
        Intent navigationIntent = FileLookerActivity.getNavigationIntent(getContext(), files, position);
        getContext().startActivity(navigationIntent);

    }

    public boolean isDeleteEnable() {
        return deleteEnable;
    }

    public void setDeleteEnable(boolean deleteEnable) {
        this.deleteEnable = deleteEnable;
    }

    public void addFile(ZFile comFile) {
        if (files == null) {
            files = new ArrayList<>();
            mAdapter.setData(files);
        }
        files.add(comFile);
        mAdapter.notifyDataSetChanged();
    }

    public void addAllFile(List<ZFile> comFiles) {
        if (files == null) {
            files = new ArrayList<>();
            mAdapter.setData(files);
        }
        files.addAll(comFiles);
        mAdapter.notifyDataSetChanged();
    }

    public ArrayList<ZFile> getFiles() {
        return files;
    }

    public void setFiles(ArrayList<ZFile> files) {
        this.files = files;
        mAdapter.setData(this.files);
        mAdapter.notifyDataSetChanged();
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public Adapter getAdapter() {
        return mAdapter;
    }

    public OnDeleteListener getOnDeleteListener() {
        return onDeleteListener;
    }

    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
        this.onDeleteListener = onDeleteListener;
    }

    public interface OnDeleteListener {
        void onDelete(ZFile comFile);
    }

}
