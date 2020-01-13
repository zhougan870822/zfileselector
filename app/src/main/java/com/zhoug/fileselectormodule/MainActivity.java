package com.zhoug.fileselectormodule;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.zhoug.common.utils.LogUtils;
import com.zhoug.fileselector.beans.LocalMedia;
import com.zhoug.fileselector.beans.ZFile;
import com.zhoug.fileselector.looker.FilePreview;
import com.zhoug.fileselector.selector.FileSelector;
import com.zhoug.fileselector.selector.FileSelectorDialog;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = ">>>MainActivity";
    private Button mBtnSelector;
    private FilePreview mFilepreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
    }

    private FileSelectorDialog fileSelectorDialog;

    private void findViews() {
        mBtnSelector = findViewById(R.id.btn_selector);
        mFilepreview = findViewById(R.id.filepreview);

        mFilepreview.setDeleteEnable(true);


        mBtnSelector.setOnClickListener(v -> {
            if (fileSelectorDialog == null) {
                fileSelectorDialog = new FileSelectorDialog(this);
                fileSelectorDialog.addFlag(FileSelectorDialog.FLAG_ALL);
                FileSelectorDialog.ChooseConfig config = new FileSelectorDialog.ChooseConfig();
                config.captureAudio = true;
                config.captureImage = true;
                config.captureVideo = false;
                config.maxDuration = 1000 * 60;
                config.minDuration = 1000;
                config.maxNum = 5;
                config.singleModel = false;
                fileSelectorDialog.setChooseConfig(config);
                fileSelectorDialog.setOnSelectListener(medias -> {
                    LogUtils.d(TAG, "onResult:medias=" + medias);
                    if(medias!=null && medias.size()>0){
                        List<ZFile> zFiles=new ArrayList<>();
                        for(LocalMedia localMedia :medias){
                            zFiles.add(new ZFile(localMedia.getPath(),localMedia.getFileType() ));
                        }
                        mFilepreview.addAllFile(zFiles);
                    }
                });

            }
            fileSelectorDialog.show();

        });




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (fileSelectorDialog != null) {
            fileSelectorDialog.onActivityResult(requestCode, resultCode, data);
        }
    }
}
