package com.music.rptang.tingtingmusic;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.music.rptang.tingtingmusic.adapter.LocalSongsListAdapter;
import com.music.rptang.tingtingmusic.utils.MediaUtils;
import com.music.rptang.tingtingmusic.vo.Mp3Info;

import java.util.ArrayList;

/**
 * Created by rptang on 2016/4/11.
 */
public class LocalSongsActivity extends BaseActivity implements View.OnClickListener,AdapterView.OnItemClickListener{

    private ListView lv_local_songs_list;
    private ArrayList<Mp3Info> mp3Infos;
    private LocalSongsListAdapter localSongsListAdapter;
    private ImageView iv_backtrack,iv_album,iv_previous,iv_play,iv_next;
    private TextView tv_song_name1,tv_song_artist1,tv_wenxintishi,tv_local_songs_title;
    private RelativeLayout rl_music_play_control;
    private boolean isChange = false;  //表示当前播放列表是否为本地歌曲列表
    private int download;
    MyApplication myApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_local_songs);
        myApplication = (MyApplication)getApplication();
        lv_local_songs_list = (ListView)findViewById(R.id.lv_local_songs_list);
        iv_backtrack = (ImageView)findViewById(R.id.iv_backtrack);
        iv_album = (ImageView) findViewById(R.id.iv_album);
        iv_play = (ImageView)findViewById(R.id.iv_play);
        iv_next = (ImageView)findViewById(R.id.iv_next);
        iv_previous = (ImageView)findViewById(R.id.iv_previous);
        tv_song_name1 = (TextView)findViewById(R.id.tv_song_name1);
        tv_song_artist1 = (TextView)findViewById(R.id.tv_song_artist1);
        tv_wenxintishi = (TextView)findViewById(R.id.tv_wenxintishi);
        tv_local_songs_title = (TextView)findViewById(R.id.tv_local_songs_title);
        rl_music_play_control = (RelativeLayout)findViewById(R.id.rl_music_play_control);
        rl_music_play_control.setOnClickListener(this);
        iv_play.setOnClickListener(this);
        iv_backtrack.setOnClickListener(this);
        iv_next.setOnClickListener(this);
        iv_previous.setOnClickListener(this);
        lv_local_songs_list.setOnItemClickListener(this);

        Intent intent = getIntent();
        download = intent.getIntExtra("download",0);
        if(download==0){
            initDate();
        }else {
            tv_local_songs_title.setText("下载歌曲");
            tv_wenxintishi.setText("您还没有下载歌曲");
            tv_wenxintishi.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在结束activity时保存一些状态值在sharedPreferences中，以便于下次再进入app的时候取值
        //为什么在这里也拿不到mp3Infos,不设了全局变量了吗
        MyApplication myApplication = (MyApplication) getApplication();
        SharedPreferences.Editor editor = myApplication.sharedPreferences.edit();
        mp3Infos = MediaUtils.getMp3Infos(this);
        Gson gson = new Gson();
        String strJson = gson.toJson(musicPlayService.getMp3Infos());
        System.out.println(musicPlayService.getMp3Infos());
        editor.putString("mp3Infos",strJson);
        editor.putInt("currentPosition",musicPlayService.getCurrentPosition());
        editor.putInt("play_mode",musicPlayService.getPlay_mode());
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //绑定播放服务
        bindMusicPlayService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindMusicPlayService();
    }

    /**
     * 初始化本地音乐列表
     */
    private void initDate() {
        mp3Infos = MediaUtils.getMp3Infos(this);
        System.out.println(mp3Infos);
        if(mp3Infos==null){
            tv_wenxintishi.setText("您本地还没有歌曲");
            tv_wenxintishi.setVisibility(View.VISIBLE);
        }else {
            localSongsListAdapter = new LocalSongsListAdapter(this,mp3Infos);
            localSongsListAdapter.notifyDataSetChanged();
            lv_local_songs_list.setAdapter(localSongsListAdapter);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_backtrack:
                finish();
                break;
            case R.id.iv_play:
                if(musicPlayService.isPlaying()){
                    musicPlayService.pause();
                    iv_play.setImageResource(R.drawable.play);
                }else{
                    if(musicPlayService.isPause()){
                        musicPlayService.start();
                        iv_play.setImageResource(R.drawable.pause);
                    }else {
                        musicPlayService.play(musicPlayService.getCurrentPosition());
                        iv_play.setImageResource(R.drawable.pause);
                    }
                }

                break;
            case R.id.iv_next:
                this.musicPlayService.next();
                iv_play.setImageResource(R.drawable.pause);
                break;
            case R.id.iv_previous:
                this.musicPlayService.previous();
                iv_play.setImageResource(R.drawable.pause);
                break;
            case R.id.rl_music_play_control:
                Intent intent = new Intent(this,PlayUIActivity.class);
                mp3Infos = musicPlayService.getMp3Infos();
                if(mp3Infos!=null){
                    startActivity(intent);
                }
                overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        //为什么在这里也拿不到mp3Infos,不设了全局变量了吗
        mp3Infos = MediaUtils.getMp3Infos(this);

        musicPlayService.setMp3Infos(mp3Infos);
        musicPlayService.play(position);

        //保存播放时间
        savePlayRecord();
//        if(!isChange){
//            //将本地列表写到MusicPlayService中
//            musicPlayService.setMp3Infos(mp3Infos);
//            isChange = true;
//        }
//        musicPlayService.play(position);
        iv_play.setImageResource(R.drawable.pause);
    }

    //保存播放记录
    private void savePlayRecord(){

        Mp3Info mp3Info = musicPlayService.getMp3Infos().get(musicPlayService.getCurrentPosition());
        try {
            Mp3Info playRecordMp3Info = myApplication.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId","=",mp3Info.getId()));
            if(playRecordMp3Info==null){
                mp3Info.setMp3InfoId(mp3Info.getId());
                mp3Info.setPlayTime(System.currentTimeMillis());
                myApplication.dbUtils.save(mp3Info);
            }else {
                playRecordMp3Info.setPlayTime(System.currentTimeMillis());
                myApplication.dbUtils.update(playRecordMp3Info,"playtime");
            }
        }catch (DbException e){
            e.printStackTrace();
        }
    }

    @Override
    public void publish(int progress) {

    }

    @Override
    public void change(int position) {
        //切换状态播放位置
        changeUIStatus(position);
    }

    //回调播放状态下的UI设置
    public void changeUIStatus(int position){
        //从绑定的服务中获取从shareperference中的音乐集合
        mp3Infos = musicPlayService.getMp3Infos();
        if(mp3Infos==null){

        }else{
            if(position>=0 && position<mp3Infos.size()){
                Mp3Info mp3Info = mp3Infos.get(position);
                tv_song_name1.setText(mp3Info.getTitle());
                tv_song_artist1.setText(mp3Info.getArtist());

                Bitmap albumBitmap =  MediaUtils.getArtwork(this, mp3Info.getId(), mp3Info.getAlbumId(), true, false);
                if(albumBitmap==null){
                    iv_album.setImageResource(R.drawable.music_play);
                }else{
                    iv_album.setImageBitmap(albumBitmap);
                }

            }
        }
        if(musicPlayService.isPlaying()){
            iv_play.setImageResource(R.drawable.pause);
        }else {
            iv_play.setImageResource(R.drawable.play);
        }
//        if(position>=0 && position<mp3Infos.size()){
//            Mp3Info mp3Info = mp3Infos.get(position);
//            tv_song_name1.setText(mp3Info.getTitle());
//            tv_song_artist1.setText(mp3Info.getArtist());

//            if(musicPlayService.isPlaying()){
//                iv_play.setImageResource(R.drawable.pause);
//            }else {
//                iv_play.setImageResource(R.drawable.play);
//            }
//
//            //Bitmap albumBitmap =  MediaUtils.getArtwork(this, mp3Info.getTitle(),mp3Info.getId(), mp3Info.getAlbumId(),true);
//            Bitmap albumBitmap =  MediaUtils.getArtwork(this,mp3Info.getId(), mp3Info.getAlbumId(),true,false);
//            if(albumBitmap==null){
//                iv_album.setImageResource(R.drawable.music_play);
//            }else{
//                iv_album.setImageBitmap(albumBitmap);
//            }
//        }
    }
}
