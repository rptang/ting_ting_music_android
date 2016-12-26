package com.music.rptang.tingtingmusic;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.music.rptang.tingtingmusic.utils.MediaUtils;
import com.music.rptang.tingtingmusic.vo.Mp3Info;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by rptang on 2016/4/12.
 * 实现功能：
 * 1、点击列表上的某首歌播放
 * 2、点击播放按钮，从暂停转为播放状态
 * 3、点击暂停按钮，从播放状态转为暂停状态
 * 4、上一首
 * 5、下一首
 * 6、播放进度显示
 * 7、播放模式
 */
public class  MusicPlayService extends Service implements MediaPlayer.OnCompletionListener,MediaPlayer.OnErrorListener{

    private MediaPlayer mediaPlayer;
    private ArrayList<Mp3Info> mp3Infos;
    private int currentPosition;//列表当前位置
    private MusicUpdateListener musicUpdateListener;
    private boolean isPause = false;

    //顺序播放、单曲循环、随机播放
    public static final int ORDER_PLAY = 1;
    public static final int RANDOM_PLAY = 2;
    public static final int SINGLE_PLAY = 3;
    public int play_mode = ORDER_PLAY;

    //切换播放列表 默认MY_MUSIC_LIST
    public static final int MY_MUSIC_LIST = 1;//本地音乐
    public static final int LOVE_MUSIC_LIST = 2;//收藏音乐
    public static final int RECORD_MUSIC_LIST = 3;//最近播放

    private int changePlayList = MY_MUSIC_LIST;

    public int getChangePlayList(){
        return changePlayList;
    }
    public void setChangePlayList(int changePlayList){
        this.changePlayList = changePlayList;
    }

    //用于设置或者获得播放模式
    public int getPlay_mode() {
        return play_mode;
    }

    public void setPlay_mode(int play_mode) {
        this.play_mode = play_mode;
    }


    //在fragment或者activity中轻松获得状态
    public boolean isPause(){
        return isPause;
    }

    //开启线程
    private ExecutorService es = Executors.newSingleThreadExecutor();

    Runnable updateStatusRunnable = new Runnable() {
        @Override
        public void run() {
            while (true){
                if(musicUpdateListener!=null){
                    musicUpdateListener.onPublish(getCurrentProgress());
                }
            }
        }
    };

    public MusicPlayService() {

    }

    public void setMp3Infos(ArrayList<Mp3Info> mp3Infos) {
        this.mp3Infos = mp3Infos;
    }

    public ArrayList<Mp3Info> getMp3Infos() {
        return mp3Infos;
    }

    Random random = new Random();
    //用于监听当前歌曲播放完后，下一首该如何播放
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        switch (play_mode){
            case ORDER_PLAY:
                next();
                break;
            case RANDOM_PLAY:
                play(random.nextInt(mp3Infos.size()));
                break;
            case SINGLE_PLAY:
                play(currentPosition);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        mediaPlayer.reset();
        return false;
    }

    class PlayBinder extends Binder{
        public MusicPlayService getMusicPlayService(){
            return MusicPlayService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //从引导页启动MusicPlayService的时候，就取sharedPreferences中的状态值
        MyApplication myApplication = (MyApplication) getApplication();
        //如果为空，则为默认为0
        currentPosition = myApplication.sharedPreferences.getInt("currentPosition",0);
        play_mode = myApplication.sharedPreferences.getInt("play_mode",MusicPlayService.ORDER_PLAY);
        Gson gson = new Gson();
        String strJson = myApplication.sharedPreferences.getString("mp3Infos",null);
        mp3Infos = gson.fromJson(strJson, new TypeToken<ArrayList<Mp3Info>>() {
        }.getType());

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);

        //在进入每一个绑定service时，就监听进度改变事件，而状态改变监听则是在启动播放的时候
        es.execute(updateStatusRunnable);
    }

    //启动线程就得销毁
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(es!=null && es.isTerminated()){
            es.shutdown();
            es = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new PlayBinder();//这里为什么要返回这个
    }

    //点击列表上的某首歌播放
    public void play(int position){
        Mp3Info mp3Info = null;
        if (position<0 || position>mp3Infos.size()){
            position = 0;
        }
        mp3Info = mp3Infos.get(position);
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(this, Uri.parse(mp3Info.getUrl()));
                mediaPlayer.prepare();
                mediaPlayer.start();
                currentPosition = position;
            }catch (IOException e){
                e.printStackTrace();
            }
            if(musicUpdateListener!=null){
                musicUpdateListener.onChange(currentPosition);
            }

    }

    //点击播放按钮，从暂停转为播放状态
    public void start(){
        if(mediaPlayer!=null && !mediaPlayer.isPlaying()){
            mediaPlayer.start();
        }
    }

    //点击暂停按钮，从播放状态转为暂停状态
    public void pause(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.getDuration();
            mediaPlayer.pause();
            isPause = true;
        }
    }

    //下一首
    public void next(){
        if(currentPosition >= mp3Infos.size()-1){
            currentPosition = 0;
        }else{
            currentPosition++;
        }
        play(currentPosition);
    }

    //上一首
    public void previous(){
        if(currentPosition<=0){
            currentPosition = mp3Infos.size()-1;
        }else{
            currentPosition--;
        }
        play(currentPosition);
    }

    //更新状态的接口
    public interface MusicUpdateListener{
        public void onPublish(int progress);
        public void onChange(int position);
    }

    public void setMusicUpdateListener(MusicUpdateListener musicUpdateListener) {
        this.musicUpdateListener = musicUpdateListener;
    }

    //在音乐播放中，获得播放的位置信息
    public int getDuration(){
        return mediaPlayer.getDuration();
    }

    //到目前为止，我都不知道这是干什么用的
    public void seekTo(int msec){
        mediaPlayer.seekTo(msec);
    }

    //返回当前的位置
    public int getCurrentPosition(){
        return currentPosition;
    }

    //获得当前位置
    public int getCurrentProgress(){
        if(mediaPlayer!=null && mediaPlayer.isPlaying()){
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    //反馈状态
    public boolean isPlaying(){
        if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
            return mediaPlayer.isPlaying();
        }
        return false;
    }


}
