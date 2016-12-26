package com.music.rptang.tingtingmusic;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;

/**
 * Created by rptang on 2016/4/12.
 * 自定义基础activity，用来让其他activity继承，作为工具activity，用于绑定服务
 */
public abstract class BaseActivity extends FragmentActivity {

    protected MusicPlayService musicPlayService;
    private boolean isBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicPlayService.PlayBinder playBinder = (MusicPlayService.PlayBinder)iBinder;
            musicPlayService = playBinder.getMusicPlayService();
            musicPlayService.setMusicUpdateListener(musicUpdateListener);
            //绑定成功后调用监听onChange方法
            musicUpdateListener.onChange(musicPlayService.getCurrentPosition());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicPlayService = null;
            isBound = false;
        }
    };

    private MusicPlayService.MusicUpdateListener musicUpdateListener = new MusicPlayService.MusicUpdateListener(){
        @Override
        public void onPublish(int progress) {
            publish(progress);
        }

        @Override
        public void onChange(int position) {
            change(position);
        }
    };

    public abstract void publish(int progress);
    public abstract void change(int position);

    //绑定服务
    public void bindMusicPlayService(){
        if(!isBound){
            Intent intent = new Intent(this,MusicPlayService.class);
            bindService(intent,conn,BIND_AUTO_CREATE);
            isBound = true;
        }

    }

    //解除绑定服务
    public void unbindMusicPlayService(){
        if(isBound){
            unbindService(conn);
            isBound = false;
        }
    }
}
