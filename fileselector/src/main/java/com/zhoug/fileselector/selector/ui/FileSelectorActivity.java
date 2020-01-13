package com.zhoug.fileselector.selector.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.zhoug.common.adapters.recycler.dividers.GridItemDecoration;
import com.zhoug.common.adapters.recycler.dividers.ListItemDecoration;
import com.zhoug.common.annotation.Filetype;
import com.zhoug.common.base.AbsActivity;
import com.zhoug.common.content.FileType;
import com.zhoug.common.permission.PermissionManager;
import com.zhoug.common.utils.AppUtils;
import com.zhoug.common.utils.FileUtils;
import com.zhoug.common.utils.IntentUtils;
import com.zhoug.common.utils.LogUtils;
import com.zhoug.common.utils.UriUtils;
import com.zhoug.fileselector.R;
import com.zhoug.fileselector.beans.LocalMedia;
import com.zhoug.fileselector.beans.LocalMediaFolder;
import com.zhoug.fileselector.loader.LocalMediaLoader;
import com.zhoug.fileselector.selector.FileSelector;
import com.zhoug.fileselector.selector.adapter.FileListAdapter;
import com.zhoug.fileselector.selector.adapter.FolderListAdapter;
import com.zhoug.widgets.dialog.ProgressDialog;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件选择页面
 */
public class FileSelectorActivity extends AbsActivity {
    private static final String TAG = ">>>FileSelectorActivity";
    private RecyclerView mRecyclerViewContent;
    private RecyclerView mRecyclerViewNavigation;
    private DrawerLayout mDrawerLayout;
    private ImageView mIvMenu;
    private TextView mTvTitle;
    private TextView mTvOk;
    private TextView mTvChooseNum;
    private FolderListAdapter mFolderListAdapter;
    private FileListAdapter mFileListAdapter;

    private FileSelector.Config mConfig;
    protected ProgressDialog loadDialog;

    public static final String EXTRA_RESULT_MEDIA = "extra_result_local_media";

    private static final String EXTRA_CONFIG = "extra_media_config";

    public static Intent getNavigationIntent(Context context, FileSelector.Config config) {
        Intent intent = new Intent(context, FileSelectorActivity.class);
        intent.putExtra(EXTRA_CONFIG, config);
        return intent;
    }

    private void initDataFromIntent() {
        Intent intent = getIntent();
        Parcelable parcelableExtra = intent.getParcelableExtra(EXTRA_CONFIG);
        if (parcelableExtra != null) {
            mConfig = (FileSelector.Config) parcelableExtra;
        } else {
            mConfig = new FileSelector.Config();
        }
    }

    @Override
    protected void setStatusBar() {
//        super.setStatusBar();

    }

    @Override
    protected void setOrientation(){
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fileselector_activity_file_selector;
    }

    @Override
    protected void findViews() {
        mRecyclerViewContent = findViewById(R.id.recyclerView_content);
        mRecyclerViewNavigation = findViewById(R.id.recyclerView_navigation);
        mDrawerLayout = findViewById(R.id.drawerLayout);
        mIvMenu = findViewById(R.id.iv_menu);
        mTvTitle = findViewById(R.id.tv_title);
        mTvOk = findViewById(R.id.tv_ok);
        mTvChooseNum = findViewById(R.id.tv_choose_num);

        //设置侧边栏的宽度
        ViewGroup.LayoutParams layoutParams = mRecyclerViewNavigation.getLayoutParams();
        layoutParams.width = AppUtils.getScreenSize(this).x * 2 / 3;
        mRecyclerViewNavigation.setLayoutParams(layoutParams);
    }


    @Override
    protected void addListener() {
        //菜单按钮单击显示/关闭左侧菜单
        mIvMenu.setOnClickListener(v -> {
            if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
                mDrawerLayout.closeDrawer(Gravity.START);
            } else {
                mDrawerLayout.openDrawer(Gravity.START);
            }
        });

        mTvOk.setOnClickListener(v -> completeSelector());
    }



    @Override
    protected void onCreateFinish(@Nullable Bundle savedInstanceState) {
        LogUtils.d(TAG, "onCreateFinish:");
        initDataFromIntent();
        initRecyclerView(savedInstanceState);

        new PermissionManager(this)
                .addPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .setCallback((success, denied) -> {
                    if (success) {
                        loadData();
                    } else {
                        toastShort("需要存储权限");
                    }
                })
                .request();
    }

    private void initRecyclerView(Bundle savedInstanceState) {
        //左侧菜单:文件夹列表
        mFolderListAdapter = new FolderListAdapter(this);
        if (savedInstanceState != null) {
            mFolderListAdapter.setSelectedPath(OutStateUtils.getSelectedFolder(savedInstanceState));
        }

        //菜单点击事件
        mFolderListAdapter.setOnItemClickListener((adapter, view, position) -> {
            LocalMediaFolder itemData = mFolderListAdapter.getItemData(position);
            if (itemData != null) {
                mFolderListAdapter.setSelectedPath(itemData.getPath());
                List<LocalMedia> medias = itemData.getMedias();
                mFileListAdapter.setData(medias);
                updateTitle(itemData);
                if (mFolderListAdapter.getSelectedPath() == null) {
                    addHeader();
                } else {
                    removeHeader();
                }

            }
            mFolderListAdapter.notifyDataSetChanged();
            mFileListAdapter.notifyDataSetChanged();
            mDrawerLayout.closeDrawer(Gravity.START);
        });



        mRecyclerViewNavigation.setLayoutManager(new LinearLayoutManager(this));
        ListItemDecoration dividerItem = new ListItemDecoration(this, OrientationHelper.VERTICAL);
        mRecyclerViewNavigation.addItemDecoration(dividerItem);
        mRecyclerViewNavigation.setAdapter(mFolderListAdapter);

        //文件列表
        mFileListAdapter = new FileListAdapter(this,mConfig);

        if (savedInstanceState != null) {
            ArrayList<LocalMedia> selectedMedias = OutStateUtils.getSelectedMedias(savedInstanceState);
            if (selectedMedias != null) {
                mFileListAdapter.setSelectedMedias(selectedMedias);
                updataSelectedNum();

            }
        }
        mFileListAdapter.setOnCheckChangeCallback(new FileListAdapter.OnCheckChangeCallback() {
            @Override
            public void onCheckedChange(LocalMedia localMedia, boolean isChecked) {
                updataSelectedNum();
            }

            @Override
            public void onCompletion() {
                updataSelectedNum();
                if(mConfig.singleModel){
                    completeSelector();
                }else{
                    toastShort("已经达到最多选择数目:"+mFileListAdapter.getSelectedMedias().size());
                }
            }
        });
        mRecyclerViewContent.setLayoutManager(new GridLayoutManager(this, 4));
        int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.fileselector_file_gride_divider);
        GridItemDecoration gridItemDecoration = new GridItemDecoration.Builder(this)
                .setColor(Color.WHITE)
                .setHorizontalSpan(dimensionPixelSize)
                .setVerticalSpan(dimensionPixelSize)
                .setShowLastLine(true)
                .build();
        mRecyclerViewContent.addItemDecoration(gridItemDecoration);
        mRecyclerViewContent.setAdapter(mFileListAdapter);

    }

    /**
     * 添加header用于拍照
     */
    private void addHeader() {
        if (!mConfig.isCamera || mFileListAdapter == null || mFileListAdapter.getHeaderCount() != 0) {
            return;
        }

        View header = getLayoutInflater().inflate(R.layout.fileselector_file_list_item_camera, null);
        TextView tvLabel=header.findViewById(R.id.tv_camera);
        mFileListAdapter.addHeader(header);
        mFileListAdapter.setHeaderFullLine(false);

        switch (mConfig.fileType) {
            case FileType.IMAGE:
                tvLabel.setText("拍照");
                break;
            case FileType.AUDIO:
                tvLabel.setText("录音");
                break;
            case FileType.VIDEO:
                tvLabel.setText("录像");
                break;
            default:
                tvLabel.setText("拍摄");
                break;
        }

        header.setOnClickListener(v -> {
            new PermissionManager(this)
                    .addPermissions(Manifest.permission.CAMERA)
                    .setCallback((success, denied) -> {
                        if (success) {
                            int selectedNum = mFileListAdapter.getSelectedMedias().size();
                            //非单选
                            if(!mConfig.singleModel){
                                if(selectedNum==mConfig.maxNum){
                                    toastShort("已经达到最多选择数目:"+selectedNum);
                                }else{
                                    capture();
                                }
                            }else{
                                //单选
                                if(selectedNum==0){
                                    capture();
                                }else{
                                    toastShort("只能单选");
                                }
                            }
                        } else {
                            toastShort("需要相机权限");
                        }
                    })
                    .request();
        });
    }

    private void removeHeader() {
        if (!mConfig.isCamera || mFileListAdapter != null && mFileListAdapter.getHeaderCount() != 0) {
            mFileListAdapter.removeAllHeader();
        }
    }

    private final int CAPTURE_CODE = 183;
    private String capturePath;
    //拍摄
    private void capture(){
        int fileType = mConfig.fileType;
        capturePath = getCapturePath(fileType);
        Intent captureIntent = null;
        if (fileType == FileType.IMAGE) {
            captureIntent = IntentUtils.getCaptureImageIntent(this, capturePath, getPackageName() + ".fileprovider");
        } else if (fileType == FileType.VIDEO) {
            captureIntent = IntentUtils.getCaptureVideoIntent(this, capturePath, getPackageName() + ".fileprovider");
        } else if (fileType == FileType.AUDIO) {
            captureIntent = IntentUtils.getCaptureAudioIntent(this, capturePath, getPackageName() + ".fileprovider");
        }
        if (captureIntent != null) {
            startActivityForResult(captureIntent, CAPTURE_CODE);
        }
    }

    private String getCapturePath(@Filetype int fileType) {
        String path = null;
        if (fileType == FileType.IMAGE) {
            File folder = FileUtils.getExternalPublicFolder(Environment.DIRECTORY_PICTURES);
            if (folder != null) {
                return new File(folder, System.currentTimeMillis() + ".jpg").getAbsolutePath();
            } else {
                return null;
            }
        } else if (fileType == FileType.VIDEO) {
            File folder = FileUtils.getExternalPublicFolder(Environment.DIRECTORY_MOVIES);
            if (folder != null) {
                return new File(folder, System.currentTimeMillis() + ".mp4").getAbsolutePath();
            } else {
                return null;
            }
        } else if (fileType == FileType.AUDIO) {
            File folder = FileUtils.getExternalPublicFolder(Environment.DIRECTORY_MUSIC);
            if (folder != null) {
                return new File(folder, System.currentTimeMillis() + ".mp3").getAbsolutePath();
            } else {
                return null;
            }
        } else {
            toastShort("文件类型错误");
            return null;
        }
    }

    /**
     * 根据文件类型加载数据
     */
    private void loadData() {
        showLoading();
        LocalMediaLoader localMediaLoader = new LocalMediaLoader(this, this, mConfig.fileType, mConfig.maxDuration, mConfig.minDuration);
        localMediaLoader.setLoadListener(folders -> {
            cancelLoading();
            LogUtils.d(TAG, "loadAllMedia:folder size=" + folders.size());
            if (folders.size() > 0) {
                mFolderListAdapter.setData(folders);
                //选中的文件夹
                LocalMediaFolder selected = mFolderListAdapter.getSelected();
                if (selected == null) {
                    selected = folders.get(0);
                    mFolderListAdapter.setSelectedPath(selected.getPath());
                }
                String selectedPath = mFolderListAdapter.getSelectedPath();
                if (selectedPath == null) {
                    addHeader();
                } else {
                    removeHeader();
                }
                LogUtils.d(TAG, "loadData:media size=" + selected.getNum());
                mFileListAdapter.setData(selected.getMedias());
                updateTitle(selected);
                mFolderListAdapter.notifyDataSetChanged();
                mFileListAdapter.notifyDataSetChanged();
                updataSelectedNum();
            }

        });
        localMediaLoader.loadAllMedia();
    }

    private void updateTitle(LocalMediaFolder folder) {
        String name = folder.getName();
        mTvTitle.setText(name == null ? "文件" : name);
    }

    private void updataSelectedNum(){
        if(mTvChooseNum==null){
            return;
        }
        String chooseNum = "0";
        ArrayList<LocalMedia> selectedMedias = mFileListAdapter.getSelectedMedias();
        if (selectedMedias != null) {
            chooseNum = selectedMedias.size() + "";
        }
        mTvChooseNum.setText(chooseNum);
    }

    /**
     * 完成选择
     */
    private void completeSelector(){
        Intent result = new Intent();
        result.putExtra(EXTRA_RESULT_MEDIA, mFileListAdapter.getSelectedMedias());
        setResult(RESULT_OK, result);
        finishActivity();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFileListAdapter != null) {
            OutStateUtils.saveSelectedMedias(outState, mFileListAdapter.getSelectedMedias());
        }
        if (mFolderListAdapter != null) {
            OutStateUtils.saveSelectedFolder(outState, mFolderListAdapter.getSelectedPath());
        }
    }


    /**
     * 数据保存
     */
    private static class OutStateUtils {
        private static final String KEY_SELECTED_MEDIAS = "key_selected_medias";
        private static final String KEY_SELECTED_FOLDER = "key_selected_folder";

        private static void saveSelectedMedias(Bundle outState, ArrayList<LocalMedia> selectedMedias) {
            if (selectedMedias != null && selectedMedias.size() > 0) {
                outState.putSerializable(KEY_SELECTED_MEDIAS, selectedMedias);
            }

        }

        @SuppressWarnings("unchecked")
        private static ArrayList<LocalMedia> getSelectedMedias(Bundle outState) {
            if (outState != null) {
                Serializable serializable = outState.getSerializable(KEY_SELECTED_MEDIAS);
                if (serializable == null) {
                    return null;
                } else {
                    return (ArrayList<LocalMedia>) serializable;
                }
            }
            return null;
        }

        private static void saveSelectedFolder(Bundle outState, String selectedPath) {
            if (selectedPath != null) {
                outState.putString(KEY_SELECTED_FOLDER, selectedPath);
            }
        }

        private static String getSelectedFolder(Bundle outState) {
            if (outState != null) {
                return outState.getString(KEY_SELECTED_FOLDER, null);
            }
            return null;
        }
    }

    private void finishActivity() {
        finish();
        overridePendingTransition(R.anim.fileselector_alpha_activity_int, R.anim.fileselector_alpha_activity_out);
    }

    @Override
    public void finishActivity(int requestCode) {
        super.finishActivity(requestCode);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fileselector_alpha_activity_int, R.anim.fileselector_alpha_activity_out);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_CODE && resultCode == RESULT_OK) {
            boolean success = false;
            if (data != null) {
                capturePath = UriUtils.getPathFromUri(this, data.getData());
            }
            if (capturePath != null) {
                File file = new File(capturePath);
                if (file.exists()) {
                    success = true;
                } else {
                    success = false;
                }
            }
            LogUtils.d(TAG, "onActivityResult:" + (success ? "拍摄成功:capturePath=" + capturePath : "拍摄失败:capturePath=" + capturePath));
            if (success) {
                AppUtils.scannerFile(this, capturePath);
                //创建拍摄文件的LocalMedia对象
                LocalMedia newMedia = new LocalMedia();
                newMedia.setPath(capturePath);
                newMedia.setDuration(FileUtils.getVideoDuration(capturePath));

                //单选
                if(mConfig.singleModel){
                    ArrayList<LocalMedia> localMedia=new ArrayList<>();
                    localMedia.add(newMedia);
                    mFileListAdapter.setSelectedMedias(localMedia);
                    completeSelector();
                }else{
                    List<LocalMedia> data1 = mFileListAdapter.getData();
                    if (data1 == null) {
                        data1 = new ArrayList<>();
                        mFileListAdapter.setData(data1);
                    }
                    data1.add(0, newMedia);
                    mFileListAdapter.addSelectedMedias(newMedia);
                    updataSelectedNum();
                    mFileListAdapter.notifyDataSetChanged();
                }


            }
        }
    }

  

    /**
     * 显示正在加载框
     */
    public void showLoading(){
        if (loadDialog == null) {
            loadDialog = new ProgressDialog(this);
            loadDialog.setCancelable(true);
            loadDialog.setCanceledOnTouchOutside(false);
            loadDialog.setZezhao(true);
        }
        if (!loadDialog.isShowing()) {
            loadDialog.show();
        }
    }

    /**
     * 隐藏正在加载框
     */
  
    public void cancelLoading(){
        if (loadDialog != null && loadDialog.isShowing()) {
            loadDialog.cancel();
        }
    }
    
}
