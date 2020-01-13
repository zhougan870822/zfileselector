package com.zhoug.fileselector.selector;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.WindowManager;


import com.zhoug.common.beans.ValuePair;
import com.zhoug.common.content.FileType;
import com.zhoug.fileselector.R;
import com.zhoug.fileselector.selector.impl.FileSelectorFactory;
import com.zhoug.widgets.dialog.list.ListDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件选择窗口
 *
 * @Author HK-LJJ
 * @Date 2020/1/10
 * @Description
 */
public class FileSelectorDialog {
    private static final int VALUE_IMAGE = 1;
    private static final int VALUE_VIDEO = 2;
    private static final int VALUE_AUDIO = 3;
    public static final int FLAG_IMAGE = 1;//0001  
    public static final int FLAG_VIDEO = 2;//0010  
    public static final int FLAG_AUDIO = 4;//0100  
    public static final int FLAG_ALL = 7;//0111

    private ChooseDialog mDialog;
    private List<ValuePair<Integer>> mItems;
    private FileSelector mFileSelector;

    private Activity mActivity;
    private Fragment mFragment;
    private FileSelector.onSelectListener onSelectListener;
    private int flag = FLAG_IMAGE;

    private @NonNull
    ChooseConfig chooseConfig = new ChooseConfig();


    private @NonNull
    FileSelector.Config config = new FileSelector.Config();

    public FileSelectorDialog(@NonNull Activity activity) {
        this.mActivity = activity;
    }

    public FileSelectorDialog(@NonNull Fragment fragment) {
        this.mFragment = fragment;
        this.mActivity = fragment.getActivity();
    }

    /**
     * 显示选择Dialog
     */
    public void show() {
        if (mDialog == null) {
            init();
        }

        mDialog.show();
    }

    /**
     * 初始化
     */
    private void init() {
        mItems = initItem();
        mDialog = new ChooseDialog(mActivity);
        mDialog.setData(mItems);
        mDialog.addQuxiaoBtn(true);
        mDialog.setAttributes(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
        mDialog.setDefWindowAnimations();
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setOnItemClickListener((parent, view, position, id) -> {
            mDialog.cancel();
            mFileSelector = createFileChooser();
            //设置配置
            config.maxNum=chooseConfig.maxNum;
            config.singleModel=chooseConfig.singleModel;
            config.maxDuration=chooseConfig.maxDuration;
            config.minDuration=chooseConfig.minDuration;
            ValuePair<Integer> valuePair = mDialog.getData().get(position);
            switch (valuePair.getValue()) {
                case VALUE_IMAGE:
                    config.fileType = FileType.IMAGE;
                    config.isCamera=chooseConfig.captureImage;
                    break;
                case VALUE_VIDEO:
                    config.fileType = FileType.VIDEO;
                    config.isCamera=chooseConfig.captureVideo;

                    break;
                case VALUE_AUDIO:
                    config.fileType = FileType.AUDIO;
                    config.isCamera=chooseConfig.captureAudio;

                    break;
            }
            mFileSelector.setConfig(config);
            mFileSelector.start();

        });

    }

    /**
     * 创建FileChooser
     *
     * @return
     */
    private FileSelector createFileChooser() {
        if (mFileSelector == null) {
            if (mFragment != null) {
                mFileSelector = FileSelectorFactory.createFileChooser(mFragment);
            } else {
                mFileSelector = FileSelectorFactory.createFileChooser(mActivity);
            }
            mFileSelector.setOnFileChooseListener(onSelectListener);
        }
        return mFileSelector;
    }

    /**
     * 初始化选项
     *
     * @return
     */
    private List<ValuePair<Integer>> initItem() {
        if (mItems == null) {
            mItems = new ArrayList<>();
        } else {
            mItems.clear();
        }
        if ((flag & FLAG_IMAGE) == FLAG_IMAGE) {
            mItems.add(new ValuePair<>("图片", VALUE_IMAGE));
        }
        if ((flag & FLAG_VIDEO) == FLAG_VIDEO) {
            mItems.add(new ValuePair<>("视频", VALUE_VIDEO));
        }
        if ((flag & FLAG_AUDIO) == FLAG_AUDIO) {
            mItems.add(new ValuePair<>("音频", VALUE_AUDIO));
        }
        return mItems;
    }

    /**
     * 需要在activity或者在Fragment的onActivityResult方法中调用
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mFileSelector != null) {
            mFileSelector.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * 支持选择文件的类型
     *
     * @param flag {@link #FLAG_IMAGE#FLAG_VIDEO#FLAG_AUDIO#FLAG_ALL}
     */
    public void addFlag(int flag) {
        this.flag |= flag;
    }

    public FileSelector.onSelectListener getOnSelectListener() {
        return onSelectListener;
    }

    /**
     * 文件选择回掉
     * @param onSelectListener
     */
    public void setOnSelectListener(FileSelector.onSelectListener onSelectListener) {
        this.onSelectListener = onSelectListener;
    }

    /**
     * 文件选择配置
     * @return
     */
    @NonNull
    public ChooseConfig getChooseConfig() {
        return chooseConfig;
    }

    /**
     * 文件选择配置
     * @param chooseConfig
     */
    public void setChooseConfig(@NonNull ChooseConfig chooseConfig) {
        this.chooseConfig = chooseConfig;
    }

    /**
     * 文件选择窗口Dialog
     */
    private class ChooseDialog extends ListDialog<ValuePair<Integer>> {

        public ChooseDialog(@NonNull Context context) {
            super(context, R.style.widget_dialog_full);
        }

        @Override
        public String getText(ValuePair<Integer> itemData) {
            return itemData.getLabel();
        }

    }

    public static class ChooseConfig {
        /**
         * 最多选择数目
         */
        public int maxNum=Integer.MAX_VALUE;
        /**
         * 是否允许拍摄
         */
        public boolean captureImage=false;
        public boolean captureVideo=false;
        public boolean captureAudio=false;

        /**
         * 音视频的最大时长
         */
        public long maxDuration=Long.MAX_VALUE;
        /**
         * 音视频的最段时长
         */
        public long minDuration=0;

        /**
         * 是否是单选模式
         */
        public boolean singleModel=false;

    }
}
