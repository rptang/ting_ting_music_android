package com.music.rptang.tingtingmusic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import java.util.List;

/**
 * Created by rptang on 2016/4/15.
 */
public class MyCollectionActivity extends BaseActivity implements View.OnClickListener,AdapterView.OnItemClickListener{

    private TextView tv_local_songs_title,tv_song_name1,tv_song_artist1,tv_wenxintishi;
    private ImageView search,iv_play,iv_album,iv_backtrack,iv_previous,iv_next;
    private ListView lv_local_songs_list;
    private RelativeLayout rl_music_play_control;
    private MyApplication myApplication;
    private ArrayList<Mp3Info> likeMp3Infos,mp3Infos;
    private LocalSongsListAdapter adapter;
    private boolean isChange = false;  //表示当前播放列表是否为收藏列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //这里为什么要本地音乐的布局文件呢，降低代码冗余以及数据错乱
        setContentView(R.layout.activity_local_songs);

        myApplication = (MyApplication)getApplication();

        tv_local_songs_title = (TextView)findViewById(R.id.tv_local_songs_title);
        search = (ImageView)findViewById(R.id.search);
        //对原本的界面进行小改动
        search.setVisibility(View.GONE);
        tv_local_songs_title.setText("我喜欢听");
        //找控件
        lv_local_songs_list = (ListView)findViewById(R.id.lv_local_songs_list);
        iv_play = (ImageView)findViewById(R.id.iv_play);
        tv_song_name1 = (TextView)findViewById(R.id.tv_song_name1);
        tv_song_artist1 = (TextView)findViewById(R.id.tv_song_artist1);
        iv_album = (ImageView) findViewById(R.id.iv_album);
        tv_wenxintishi = (TextView)findViewById(R.id.tv_wenxintishi);
        iv_backtrack = (ImageView)findViewById(R.id.iv_backtrack);
        iv_previous = (ImageView)findViewById(R.id.iv_previous);
        iv_next = (ImageView)findViewById(R.id.iv_next);
        rl_music_play_control = (RelativeLayout)findViewById(R.id.rl_music_play_control);

        //给控件注册监听事件
        lv_local_songs_list.setOnItemClickListener(this);
        iv_play.setOnClickListener(this);
        iv_backtrack.setOnClickListener(this);
        iv_previous.setOnClickListener(this);
        iv_next.setOnClickListener(this);
        rl_music_play_control.setOnClickListener(this);

        initData();
    }

    private void initData() {
//        try{
//            likeMp3Infos = (ArrayList<Mp3Info>)myApplication.dbUtils.findAll(Mp3Info.class);
//            if(likeMp3Infos == null){
//                tv_wenxintishi.setVisibility(View.VISIBLE);
//            }else {
//                adapter = new LocalSongsListAdapter(this,likeMp3Infos);
//                lv_local_songs_list.setAdapter(adapter);
//            }
//        }catch (DbException e){
//            e.printStackTrace();
//        }

        try{
            List<Mp3Info> list = myApplication.dbUtils.findAll(Selector.from(Mp3Info.class).where("isLike","=","1"));
            if(list == null || list.size()==0){
                tv_wenxintishi.setVisibility(View.VISIBLE);
                tv_wenxintishi.setText("快去收藏点喜欢的歌");
            }else {
                likeMp3Infos = (ArrayList<Mp3Info>)list;
                adapter = new LocalSongsListAdapter(this,likeMp3Infos);
                lv_local_songs_list.setAdapter(adapter);
            }
        }catch (DbException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindMusicPlayService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindMusicPlayService();
    }

    @Override
    public void publish(int progress) {

    }

    //点击我的收藏歌单的时候，这个change方法会执行，
    // position从MusicPlayService的sharepreference中取，第一次安装的话取得值是0
    @Override
    public void change(int position) {

        //从绑定的服务中获取从shareperferencez中的音乐集合
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


//        if(position>=0 && position<likeMp3Infos.size()){
//            Mp3Info mp3Info = likeMp3Infos.get(position);
//            tv_song_name1.setText(mp3Info.getTitle());
//            tv_song_artist1.setText(mp3Info.getArtist());
//
//            if(musicPlayService.isPlaying()){
//                iv_play.setImageResource(R.drawable.pause);
//            }else {
//                iv_play.setImageResource(R.drawable.play);
//            }

            //Bitmap albumBitmap =  MediaUtils.getArtwork(this, mp3Info.getTitle(),mp3Info.getId(), mp3Info.getAlbumId(),true);
//            Bitmap albumBitmap =  MediaUtils.getArtwork(this, mp3Info.getId(), mp3Info.getAlbumId(), true, false);
//            if(albumBitmap==null){
//                iv_album.setImageResource(R.drawable.music_play);
//            }else{
//                iv_album.setImageBitmap(albumBitmap);
//            }

        }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

    //为什么在这里也拿不到likeMp3Infos,不设了全局变量了吗
//        try {
//            likeMp3Infos = (ArrayList<Mp3Info>)myApplication.dbUtils.findAll(Mp3Info.class);
//        } catch (DbException e) {
//            e.printStackTrace();
//        }
//        if(!isChange){
//            musicPlayService.setMp3Infos(likeMp3Infos);
//            System.out.println(likeMp3Infos);
//            isChange = true;
//        }
        if(musicPlayService.getChangePlayList()!=MusicPlayService.LOVE_MUSIC_LIST){
            musicPlayService.setMp3Infos(likeMp3Infos);
            musicPlayService.setChangePlayList(MusicPlayService.LOVE_MUSIC_LIST);
        }
        musicPlayService.play(position);
        savePlayRecord();
        iv_play.setImageResource(R.drawable.pause);
    }

    //保存播放记录
    private void savePlayRecord(){

        //获得当前正在播放的音乐对象
        Mp3Info mp3Info = musicPlayService.getMp3Infos().get(musicPlayService.getCurrentPosition());
        try {
            Mp3Info playRecordMp3Info = myApplication.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId","=",mp3Info.getMp3InfoId()));
            if(playRecordMp3Info==null){
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
    protected void onDestroy() {
        super.onDestroy();
        //在结束activity时保存一些状态值在sharedPreferences中，以便于下次再进入app的时候取值
        MyApplication myApplication = (MyApplication) getApplication();
        SharedPreferences.Editor editor = myApplication.sharedPreferences.edit();
//        try {
//            likeMp3Infos = (ArrayList<Mp3Info>)myApplication.dbUtils.findAll(Mp3Info.class);
//        } catch (DbException e) {
//            e.printStackTrace();
//        }
        Gson gson = new Gson();
        String strJson = gson.toJson(musicPlayService.getMp3Infos());
        editor.putString("mp3Infos",strJson);
        editor.putInt("currentPosition",musicPlayService.getCurrentPosition());
        editor.putInt("play_mode",musicPlayService.getPlay_mode());
        editor.commit();
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
}
