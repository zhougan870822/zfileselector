package com.zhoug.fileselector.looker.fragment;

import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.zhoug.common.imageloader.ImageLoader;
import com.zhoug.common.utils.LogUtils;
import com.zhoug.common.utils.StringUtils;
import com.zhoug.fileselector.R;


/**
 * 使用exoplayer框架播放视频
 */
public class VideoFragment extends BaseFileFragment {
    private static final String TAG = ">>>VideoFragment";
    private PlayerView mPlayerView;
    private ImageView ivPlay;
    private ImageView ivSlt;
    private RelativeLayout rlController;
    private SeekBar seekBar;
    private TextView tvPosition;
    private TextView tvDuration;
    private ProgressBar progressBar;
    private RelativeLayout clickView;


    private boolean uiIsShow = true;
    private Handler uiHandler = new Handler();//显示隐藏ui
    private boolean isSeekBarChange = false;//
    private boolean initSeekBar = false;//seekBar初始化完成

    private ExoPlayer player;

    @Override
    protected int getLayoutResId() {
        return R.layout.fileselector_fragment_video;
    }

    @Override
    protected void init(View root) {
        findViews(root);
        init();
    }


    private void findViews(View view) {
        mPlayerView = view.findViewById(R.id.playerView);
        ivPlay = view.findViewById(R.id.iv_play);
        ivSlt = view.findViewById(R.id.iv_slt);
        rlController = view.findViewById(R.id.rl_controller);
        seekBar = view.findViewById(R.id.seekBar);
        tvPosition = view.findViewById(R.id.tv_position);
        tvDuration = view.findViewById(R.id.tv_duration);
        progressBar = view.findViewById(R.id.progressBar);
        clickView = view.findViewById(R.id.clickView);

        ivPlay.setOnClickListener(v->{
            if (player == null) return;
            if (player.getPlayWhenReady()) {
                pause();
            } else {
                play();

            }
        });

        clickView.setOnClickListener(v->{
            if (uiIsShow) {
                showUi(false);
            } else {
                showUi(true);
            }
        });
    }


    private void init() {
        if (zFile == null) {
            return;
        }
        //设置视频加载中的占位图片
        ImageLoader.load(getContext(), ivSlt, zFile.getUrl(),null,null,null);

        initPlayer();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                updatePlayTime();
                if (player != null && initSeekBar && tvPosition!=null) {
                    tvPosition.setText(StringUtils.getStringTime(progress / 1000));
                }

//                LogUtils.d(TAG, "onProgressChanged: ");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekBarChange = true;
                LogUtils.d(TAG, "onStartTrackingTouch: ");
                uiHandler.removeMessages(0);//默认任务的what为0
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekBarChange = false;
                if (player != null && initSeekBar) {
                    player.seekTo(seekBar.getProgress());

                }

                LogUtils.d(TAG, "onStopTrackingTouch: ");
            }
        });
    }


    /**
     * 初始化Player
     */
    private void initPlayer() {
        if (player == null) {
            //不使用默认控件
            mPlayerView.setUseController(false);
            player = ExoPlayerFactory.newSimpleInstance(getContext(),
                    new DefaultRenderersFactory(getContext()),
                    new DefaultTrackSelector(),
                    new DefaultLoadControl());

            player.addListener(new Player.EventListener() {

                //刷新时间线和/或清单时调用
                @Override
                public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {
                    LogUtils.d(TAG, "onTimelineChanged: ");
                }

                //当可用或选定的曲目更改时调用
                @Override
                public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                    LogUtils.d(TAG, "onTracksChanged: ");
                }

                //当播放机开始或停止加载源时调用
                @Override
                public void onLoadingChanged(boolean isLoading) {
                    LogUtils.d(TAG, "onLoadingChanged: isLoading=" + isLoading);
                    if (isLoading) {
                        showProgressBar(true);
                    } else {
                        showProgressBar(false);
                        ivSlt.setVisibility(View.GONE);
                    }
                }

                //getPlayBackState返回值时调用
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    LogUtils.d(TAG, "onPlayerStateChanged: playWhenReady=" + playWhenReady);
                    LogUtils.d(TAG, "onPlayerStateChanged: playbackState=" + playbackState);
                    if (playbackState == Player.STATE_READY && !initSeekBar) {
                        long duration = player.getDuration();
                        LogUtils.d(TAG, "onPlayerStateChanged: duration=" + duration);
                        if (duration > 0) {
                            tvDuration.setText(StringUtils.getStringTime((int) (duration / 1000)));
                            seekBar.setMax((int) duration);
                            initSeekBar = true;
                        }

                    }
                    if (playbackState == Player.STATE_ENDED) {
                        LogUtils.d(TAG, "onPlayerStateChanged: 播放完成");
                        ivPlay.setImageResource(R.drawable.fileselector_icon_play);

                    }

                }

                //当@link getRepeatmode（）的值更改时调用
                @Override
                public void onRepeatModeChanged(int repeatMode) {
                    LogUtils.d(TAG, "onRepeatModeChanged: repeatMode=" + repeatMode);
                }

                //当@link getShuffleModeEnabled（）的值更改时调用
                @Override
                public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
                    LogUtils.d(TAG, "onShuffleModeEnabledChanged: shuffleModeEnabled=" + shuffleModeEnabled);
                }

                //发生错误时调用.播放状态将转换为STATE_IDLE
                @Override
                public void onPlayerError(ExoPlaybackException error) {
                    LogUtils.d(TAG, "onPlayerError: error=" + error);
                    toastShort("播放发生错误:" + error.getMessage());
                }

                //在不改变时间线的情况下发生位置不连续时调用
                @Override
                public void onPositionDiscontinuity(int reason) {
                    LogUtils.d(TAG, "onPositionDiscontinuity: reason=" + reason);
                }

                //当当前播放参数更改时调用
                @Override
                public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                    LogUtils.d(TAG, "onPlaybackParametersChanged: playbackParameters=" + playbackParameters);
                }

                //当播放机处理完所有挂起的查找请求时调用
                @Override
                public void onSeekProcessed() {
                    LogUtils.d(TAG, "onSeekProcessed: ");
                }
            });


            mPlayerView.setPlayer(player);


            player.setPlayWhenReady(false);//播放/暂停

        }

        MediaSource mediaSource=null;
        String fileUrl = zFile.getUrl();
        Uri uri = Uri.parse(fileUrl);
        LogUtils.d(TAG, "initPlayer:fileUrl="+fileUrl);
        if(fileUrl.startsWith("http")){
            mediaSource = buildMediaSourceRemote(uri);
        }else{
            mediaSource = buildMediaSourceLocal(uri);

        }
        player.prepare(mediaSource, false, false);
    }

    /**
     * 创建网络资源
     * @param uri
     * @return
     */
    private MediaSource buildMediaSourceRemote(Uri uri) {
        return new ExtractorMediaSource.Factory(
                new DefaultHttpDataSourceFactory("exoplayer-codelab-remote")).
                createMediaSource(uri);
    }

    /**
     * 创建本地资源
     * @param uri
     * @return
     */
    private MediaSource buildMediaSourceLocal(Uri uri) {
        return new ExtractorMediaSource.Factory(
                new DefaultDataSourceFactory(getContext(),"exoplayer-codelab-local")).
                createMediaSource(uri);
    }


    /**
     * 释放资源
     */
    private void releasePlayer() {
        if (player != null) {
          /*  curPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();*/
            player.release();
            player = null;
        }
        initSeekBar = false;
        removeProgressListener();
        uiHandler.removeMessages(0);
    }

    private void play() {
        if (player != null) {
            //播放完了重新开始
            if (player.getPlaybackState() == Player.STATE_ENDED) {
                player.seekTo(0);
                seekBar.setProgress(0);
            }

            player.setPlayWhenReady(true);
            ivPlay.setImageResource(R.drawable.fileselector_icon_stop);
            addProgressListener();
        }
    }

    private void pause() {
        if (player != null) {
            player.setPlayWhenReady(false);
            ivPlay.setImageResource(R.drawable.fileselector_icon_play);
        }
        removeProgressListener();
    }




    private void showProgressBar(boolean show) {
        if (show && progressBar.getVisibility() != View.VISIBLE) {
            progressBar.setVisibility(View.VISIBLE);
        } else if (!show && progressBar.getVisibility() != View.GONE) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private long oldProgress;

    //更新进度
    private Handler mProgressHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            if (player != null && initSeekBar) {
                long currentPosition = player.getCurrentPosition();
                seekBar.setProgress((int) currentPosition);
                if (oldProgress != currentPosition) {
                    showProgressBar(false);
                    ivSlt.setVisibility(View.GONE);
                }

                oldProgress = currentPosition;

            }


            return true;
        }
    });


    private Runnable mProgressRunnable = new Runnable() {
        @Override
        public void run() {
            mProgressHandler.sendEmptyMessage(0);
            mProgressHandler.postDelayed(mProgressRunnable, 500);
        }
    };

    private void addProgressListener() {
        LogUtils.d(TAG, "addProgressListener: ");
        mProgressHandler.post(mProgressRunnable);
    }

    private void removeProgressListener() {
        LogUtils.d(TAG, "removeProgressListener: ");
        mProgressHandler.removeMessages(0);
    }

    /**
     * 显示ui
     *
     * @param show
     */
    private void showUi(boolean show) {
        if (uiIsShow == show || rlController == null) return;
        if (show) {
            rlController.setVisibility(View.VISIBLE);
            uiIsShow = true;
            //2秒后自动隐藏ui
            uiHandler.removeMessages(0);//默认任务的what为0
            uiHandler.postDelayed(() -> showUi(false), 3000);
            addProgressListener();

        } else {
            removeProgressListener();
            rlController.setVisibility(View.GONE);
            uiIsShow = false;

        }
    }


    /**
     * 视图是否已经对用户可见，系统的方法
     *
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        LogUtils.d(TAG, "setUserVisibleHint: isVisibleToUser=" + isVisibleToUser);
        if (isResumed()) {
            if (isVisibleToUser) {
                play();
            } else {
                pause();
            }
        }

        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtils.d(TAG, "onResume: " + this.getClass().getName());
        boolean userVisibleHint = getUserVisibleHint();
        if (userVisibleHint) {
            play();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        LogUtils.d(TAG, "onPause: ");
        pause();
    }

    @Override
    public void onDestroyView() {
        LogUtils.d(TAG, "onDestroyView: ");
        super.onDestroyView();
//        release();
        releasePlayer();

    }






}
