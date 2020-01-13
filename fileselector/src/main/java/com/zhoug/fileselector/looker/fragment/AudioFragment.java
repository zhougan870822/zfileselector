package com.zhoug.fileselector.looker.fragment;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;


import com.zhoug.common.utils.LogUtils;
import com.zhoug.common.utils.StringUtils;
import com.zhoug.fileselector.R;

import java.io.IOException;

/**
 * 音频播放页
 */
public class AudioFragment extends BaseFileFragment {
    private static final String TAG = "AudioFragment";
    private TextView tvPosition;
    private SeekBar seekBar;
    private TextView tvDuration;
    private ImageView ibPlay;
    private ProgressBar progressBar;

    private MediaPlayer mMediaPlayer;


    private static final int STATUS_START = 1;
    private static final int STATUS_STOP = 2;
    private static final int STATUS_PAUSE = 3;
    private static final int STATUS_NONE = 4;
    private int status = STATUS_NONE;
    private boolean isSeekBarChange = false;//

    @Override
    protected int getLayoutResId() {
        return R.layout.fileselector_fragment_audio;
    }

    @Override
    protected void init(View root) {
        findViews(root);
        init();
    }

    private void findViews(View view) {
        tvPosition = view.findViewById(R.id.tv_position);
        seekBar = view.findViewById(R.id.seekBar);
        tvDuration = view.findViewById(R.id.tv_duration);
        ibPlay = view.findViewById(R.id.ib_play);
        progressBar = view.findViewById(R.id.progressBar);

        ibPlay.setOnClickListener(v -> {
            if (status != STATUS_START) {
                startPlay();
            } else {
                pausePlay();
            }
        });
    }


    private void init() {
        if (zFile == null) {
            return;
        }
        mMediaPlayer = new MediaPlayer();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                updatePlayTime();
                if (initSeekBar && tvPosition!=null) {
                    tvPosition.setText(StringUtils.getStringTime(progress / 1000));
                  /* if(status!=STATUS_NONE && status!=STATUS_STOP){
                       tvDuration.setText(getTimeString(mMediaPlayer.getDuration()/1000));
                   }*/
                }

//                LogUtils.d(TAG, "onProgressChanged: ");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekBarChange = true;
                LogUtils.d(TAG, "onStartTrackingTouch: ");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekBarChange = false;
                if (initSeekBar && mMediaPlayer!=null) {
                    int progress = seekBar.getProgress();
                    mMediaPlayer.seekTo(progress);
                }
                LogUtils.d(TAG, "onStopTrackingTouch: ");
            }
        });
    }

    /**
     * 开始播放
     */
    public void startPlay() {
        if (zFile == null || mMediaPlayer == null) {
            return;
        }
        if (status == STATUS_START) {
            return;
        }

        if (status == STATUS_NONE || status == STATUS_STOP) {
            try {
                mMediaPlayer.setDataSource(zFile.getUrl());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            ibPlay.setImageResource(R.drawable.fileselector_icon_stop);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.prepareAsync();
            status = STATUS_START;
            addProgressListener();

            mMediaPlayer.setOnPreparedListener(mp -> {
                mMediaPlayer.start();

            });

            mMediaPlayer.setOnErrorListener((mp, what, extra) -> {
                LogUtils.d(TAG, "onError: what=" + what + ",extra=" + extra);
                stopPlay();

                return true;
            });
            mMediaPlayer.setOnCompletionListener(mp -> {
//                stopPlay();
                pausePlay();

                seekBar.setProgress(seekBar.getMax());
            });
        } else if (status == STATUS_PAUSE) {
            mMediaPlayer.start();
            ibPlay.setImageResource(R.drawable.fileselector_icon_stop);
            addProgressListener();
            status = STATUS_START;

        }


    }

    /**
     * 停止播放
     */
    public void stopPlay() {
        if (zFile == null || mMediaPlayer == null) {
            return;
        }
        if (status != STATUS_STOP) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            status = STATUS_STOP;
            ibPlay.setImageResource(R.drawable.fileselector_icon_play);
            removeProgressListener();
            progressBar.setVisibility(View.GONE);

        }


    }

    private void pausePlay() {
        if (zFile == null || mMediaPlayer == null) {
            return;
        }

        if (status == STATUS_START) {
            mMediaPlayer.pause();
            status = STATUS_PAUSE;
            ibPlay.setImageResource(R.drawable.fileselector_icon_play);
            removeProgressListener();
            progressBar.setVisibility(View.GONE);

        }
    }

    public void release() {
        if (mMediaPlayer != null) {
            removeProgressListener();
            stopPlay();
            mMediaPlayer.release();
            mMediaPlayer = null;
            initSeekBar = false;
        }
    }

    private boolean initSeekBar = false;//seekBar初始化完成
    private int oldPosition = 0;//上次播放进度

    private Handler mProgressHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
//            LogUtils.d(TAG, "handleMessage: initSeekBar=" + initSeekBar);
            if (mMediaPlayer.isPlaying()) {
                if (!initSeekBar) {
                    int duration = mMediaPlayer.getDuration();
                    LogUtils.d(TAG, "handleMessage: duration=" + duration);
                    if (duration > 0) {
                        seekBar.setMax(duration);
                        tvDuration.setText(StringUtils.getStringTime(duration / 1000));
                        initSeekBar = true;
                    }

                } else {
                    if (!isSeekBarChange) {
                        int currentPosition = mMediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                    }

                }
            }

            int currentPosition = mMediaPlayer.getCurrentPosition();
            LogUtils.d(TAG, "handleMessage: oldPosition=" + oldPosition);
            LogUtils.d(TAG, "handleMessage: currentPosition=" + currentPosition);
            if (currentPosition == oldPosition) {
                if (progressBar.getVisibility() != View.VISIBLE) {
                    progressBar.setVisibility(View.VISIBLE);
                }
            } else {
                if (progressBar.getVisibility() != View.GONE) {
                    progressBar.setVisibility(View.GONE);
                }
            }
            oldPosition = currentPosition;
            return true;
        }
    });

    private Runnable mProgressRunnable = new Runnable() {
        @Override
        public void run() {
            mProgressHandler.sendEmptyMessage(0);
            mProgressHandler.postDelayed(mProgressRunnable, 1000);
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
     * 视图是否已经对用户可见，系统的方法
     *
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        LogUtils.d(TAG, "setUserVisibleHint: isVisibleToUser=" + isVisibleToUser);
        if (isResumed()) {
            if (isVisibleToUser) {
                startPlay();
            } else {
                pausePlay();
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
            startPlay();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        LogUtils.d(TAG, "onPause: ");
        pausePlay();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogUtils.d(TAG, "onDestroyView: ");
        release();
    }


}
