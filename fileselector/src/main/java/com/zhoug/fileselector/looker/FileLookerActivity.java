package com.zhoug.fileselector.looker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhoug.common.base.AbsActivity;
import com.zhoug.common.utils.LogUtils;
import com.zhoug.fileselector.R;
import com.zhoug.fileselector.beans.ZFile;
import com.zhoug.fileselector.looker.fragment.BaseFileFragment;

import java.util.ArrayList;
import java.util.List;


/**
 * 文件查看器
 */
public class FileLookerActivity extends AbsActivity {
    private static final String TAG = "FileLookActivity";
    TextView tvTitle;
    ViewPager viewPager;

    public static final String KEY_POSITION="position";
    public static final String KEY_DATA="data";

    private int position;
    private ArrayList<ZFile> data;
    private FileLookerAdapter fileLookAdapter;


    /**
     * 获取跳转到FileLookerActivity的intent
     * @param context
     * @param comFiles 文件集合
     * @param position 第一个显示的文件在集合中的位置
     * @return intent
     */
    public static Intent getNavigationIntent(Context context, List<ZFile> comFiles, int position){
        Intent intent=new Intent(context,FileLookerActivity.class);
        if(comFiles instanceof ArrayList){
            intent.putExtra(KEY_DATA, (ArrayList) comFiles);
        }else{
            intent.putExtra(KEY_DATA, new ArrayList<>(comFiles));

        }
        intent.putExtra(KEY_POSITION, position);
        return intent;
    }


    @Override
    protected int getLayoutResID() {
        return R.layout.fileselector_activity_file_looker;
    }

    @Override
    protected void findViews() {
        tvTitle=findViewById(R.id.tv_Title);
        viewPager=findViewById(R.id.viewPager);
    }

    @Override
    protected void addListener() {

    }
    @SuppressWarnings("unchecked")
    @Override
    protected void onCreateFinish(@Nullable Bundle savedInstanceState) {
        Intent intent = getIntent();
        position=intent.getIntExtra(KEY_POSITION, 0);
        data= (ArrayList<ZFile>) intent.getSerializableExtra(KEY_DATA);
        LogUtils.d(TAG, "onCreate: datas="+data);
        LogUtils.d(TAG, "onCreate: position="+position);

        init();
    }

    private void init(){
        if(data!=null && position>=data.size()){
            position=0;
        }
        if(fileLookAdapter==null){
            fileLookAdapter=new FileLookerAdapter(getSupportFragmentManager());
        }
        fileLookAdapter.setData(data);
        setTitleNum(fileLookAdapter.getPageTitle(position));

        viewPager.setAdapter(fileLookAdapter);

        viewPager.setCurrentItem(position);

        viewPager.addOnPageChangeListener(new android.support.v4.view.ViewPager.SimpleOnPageChangeListener(){

            @Override
            public void onPageSelected(int position) {
                setTitleNum(fileLookAdapter.getPageTitle(position));
            }

        });

    }

    private void setTitleNum(CharSequence title){
        if(tvTitle!=null){
            tvTitle.setText(title);
        }
    }


    private class FileLookerAdapter extends FragmentPagerAdapter {
        private static final String TAG = ">>>>>FileLookerAdapter";
        private ArrayList<ZFile> data;

        public FileLookerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            ZFile comFile = data.get(position);
            return BaseFileFragment.createLookerFragment(comFile);

        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LogUtils.d(TAG, "instantiateItem: "+position);
            return super.instantiateItem(container, position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            LogUtils.d(TAG, "destroyItem: "+position);
            super.destroyItem(container, position, object);
        }

        @Override
        public int getCount() {
            if(data!=null){
                return data.size();
            }
            return 0;
        }


        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return (position+1)+"/"+getCount();
        }

        public ArrayList<ZFile> getData() {
            return data;
        }

        public void setData(ArrayList<ZFile> data) {
            this.data = data;
        }
    }

}
