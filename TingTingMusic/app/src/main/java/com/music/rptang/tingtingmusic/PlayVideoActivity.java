package com.music.rptang.tingtingmusic;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

/**
 * 播放视频
 */
public class PlayVideoActivity extends Activity{

    private VideoView videoView;
    private View mVolumeBrightnessLayout;
    private ImageView mOperationBg;
    private ImageView mOperationPercent;
    private AudioManager mAudioManager;
    /** 最大声音 */
    private int mMaxVolume;
    /** 当前声音 */
    private int mVolume = -1;
    /** 当前亮度 */
    private float mBrightness = -1f;
    /** 当前缩放模式 */
    private int mLayout = VideoView.VIDEO_LAYOUT_SCALE;
    private GestureDetector mGestureDetector;
    private MediaController mMediaController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Vitamio.isInitialized(getApplicationContext());
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去掉Activity上面的状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_play_video);
        videoView = (VideoView) findViewById(R.id.surface_view);



        mVolumeBrightnessLayout = findViewById(R.id.operation_volume_brightness);
        mOperationBg = (ImageView) findViewById(R.id.operation_bg);
        mOperationPercent = (ImageView) findViewById(R.id.operation_percent);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);


        mGestureDetector = new GestureDetector(this, new MyGestureListener());
        MediaController mc = new MediaController(this);
        videoView.setMediaController(mc);

        String path = "http://360fast-edu.com/video_courses/mv1.mp4";
        videoView.setVideoPath(path);
        videoView.requestFocus();
        videoView.start();




    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event))
            return true;

        // 处理手势结束
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                endGesture();
                break;
        }

        return super.onTouchEvent(event);
    }

    /** 手势结束 */
    private void endGesture() {
        mVolume = -1;
        mBrightness = -1f;

        // 隐藏
        mDismissHandler.removeMessages(0);
        mDismissHandler.sendEmptyMessageDelayed(0, 500);
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        /** 双击 */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mLayout == VideoView.VIDEO_LAYOUT_ZOOM)
                mLayout = VideoView.VIDEO_LAYOUT_ORIGIN;
            else
                mLayout++;
            if (videoView != null)
                videoView.setVideoLayout(mLayout, 0);
            return true;
        }

        /** 滑动 */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            float mOldX = e1.getX(), mOldY = e1.getY();
            int y = (int) e2.getRawY();
            Display disp = getWindowManager().getDefaultDisplay();
            int windowWidth = disp.getWidth();
            int windowHeight = disp.getHeight();

            if (mOldX > windowWidth * 4.0 / 5)// 右边滑动
                onVolumeSlide((mOldY - y) / windowHeight);
            else if (mOldX < windowWidth / 5.0)// 左边滑动
                onBrightnessSlide((mOldY - y) / windowHeight);

//            long currentPosition = videoView.getCurrentPosition();
//
//  /*向左滑*/
//            if (e1.getX() - e2.getX() > 120) {
//
//                if (currentPosition < 100) {
//                    currentPosition = 0;
//                    videoView.seekTo(currentPosition);
//                } else {
//                    videoView.seekTo(currentPosition - 100);
//                }
//
//            } else if (e2.getX() - e1.getX() > 120) {
//  /*向右滑*/
//                if (currentPosition + 100 > videoView.getDuration()) {
//                    currentPosition = videoView.getDuration();
//                    videoView.seekTo(currentPosition);
//                } else {
//                    videoView.seekTo(currentPosition + 100);
//                }
//
//            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    /** 定时隐藏 */
    private Handler mDismissHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mVolumeBrightnessLayout.setVisibility(View.GONE);
        }
    };

    /**
     * 滑动改变声音大小
     *
     * @param percent
     */
    private void onVolumeSlide(float percent) {
        if (mVolume == -1) {
            mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mVolume < 0)
                mVolume = 0;

            // 显示
            mOperationBg.setImageResource(R.drawable.video_volumn_bg);
            mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
        }

        int index = (int) (percent * mMaxVolume) + mVolume;
        if (index > mMaxVolume)
            index = mMaxVolume;
        else if (index < 0)
            index = 0;

        // 变更声音
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);

        // 变更进度条
        ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
        lp.width = findViewById(R.id.operation_full).getLayoutParams().width
                * index / mMaxVolume;
        mOperationPercent.setLayoutParams(lp);
    }

    /**
     * 滑动改变亮度
     *
     * @param percent
     */
    private void onBrightnessSlide(float percent) {
        if (mBrightness < 0) {
            mBrightness = getWindow().getAttributes().screenBrightness;
            if (mBrightness <= 0.00f)
                mBrightness = 0.50f;
            if (mBrightness < 0.01f)
                mBrightness = 0.01f;

            // 显示
            mOperationBg.setImageResource(R.drawable.video_brightness_bg);
            mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
        }
        WindowManager.LayoutParams lpa = getWindow().getAttributes();
        lpa.screenBrightness = mBrightness + percent;
        if (lpa.screenBrightness > 1.0f)
            lpa.screenBrightness = 1.0f;
        else if (lpa.screenBrightness < 0.01f)
            lpa.screenBrightness = 0.01f;
        getWindow().setAttributes(lpa);

        ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
        lp.width = (int) (findViewById(R.id.operation_full).getLayoutParams().width * lpa.screenBrightness);
        mOperationPercent.setLayoutParams(lp);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (videoView != null)
            videoView.setVideoLayout(mLayout, 0);
        super.onConfigurationChanged(newConfig);
    }


}
