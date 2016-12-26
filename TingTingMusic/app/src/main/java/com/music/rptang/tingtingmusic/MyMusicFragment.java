package com.music.rptang.tingtingmusic;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.music.rptang.tingtingmusic.utils.Content;
import com.music.rptang.tingtingmusic.utils.MediaUtils;
import com.music.rptang.tingtingmusic.vo.Mp3Info;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rptang on 2016/4/11.
 */
public class MyMusicFragment extends Fragment implements View.OnClickListener{

    private LinearLayout layout_local_songs,ll_my_collection,ll_robot,ll_already_download,ll_recent_played;
    private TextView tv_songs_count,tv_song_list_count,tv_download_count;
    private ArrayList<Mp3Info> mp3Infos;

    /**
     * 初始化myMusicFragment,在MainActivity中调用
     * @return
     */
    public static MyMusicFragment newInstance(){
        MyMusicFragment myMusicFragment = new MyMusicFragment();
        return myMusicFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_music_fragment,null);
        layout_local_songs = (LinearLayout)view.findViewById(R.id.ll_local_songs);
        ll_my_collection = (LinearLayout)view.findViewById(R.id.ll_my_collection);
        ll_robot = (LinearLayout)view.findViewById(R.id.ll_robot);
        ll_already_download = (LinearLayout)view.findViewById(R.id.ll_already_download);
        ll_recent_played = (LinearLayout)view.findViewById(R.id.ll_recent_played);
        tv_song_list_count = (TextView)view.findViewById(R.id.tv_song_list_count);
        tv_songs_count = (TextView) view.findViewById(R.id.tv_songs_count);
        tv_download_count = (TextView)view.findViewById(R.id.tv_download_count);

        ll_my_collection.setOnClickListener(this);
        layout_local_songs.setOnClickListener(this);
        ll_robot.setOnClickListener(this);
        ll_already_download.setOnClickListener(this);
        ll_recent_played.setOnClickListener(this);
        return view;
    }



    @Override
    public void onResume() {
        super.onResume();

//        Uri contentUri = Uri.fromFile(new File(".mp3"));
//        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,contentUri);
//        getActivity().sendBroadcast(mediaScanIntent);

        //填充本地音乐数
        mp3Infos = MediaUtils.getMp3Infos(getActivity());
        tv_songs_count.setText(mp3Infos.size() + "");
        //填充我喜欢听音乐数
        try{
            List<Mp3Info>likeMp3Infos = MyApplication.dbUtils.findAll(Selector.from(Mp3Info.class).where("isLike","=",1));
            if(likeMp3Infos==null){
                tv_song_list_count.setText("0首");
            }else{
                tv_song_list_count.setText(likeMp3Infos.size() + "首");
            }
        }catch (DbException e){
            e.printStackTrace();
        }
        //填充最近播放音乐数
        try {
            List<Mp3Info> list = MyApplication.dbUtils.findAll(Selector.from(Mp3Info.class).where("playTime","!=",0).orderBy("playTime",true).limit(Content.PLAY_RECORD_NUM));
            if(list==null){
                tv_download_count.setText(0+"");
            }else{
                tv_download_count.setText(list.size()+"");
            }

        }catch (DbException e){
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ll_local_songs:
                Intent intent = new Intent(getActivity(),LocalSongsActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_my_collection:
                Intent intent1 = new Intent(getActivity(),MyCollectionActivity.class);
                startActivity(intent1);
                break;
            case R.id.ll_robot:
                Intent intent2 = new Intent(getActivity(),RobotActivity.class);
                startActivity(intent2);
                break;
            case R.id.ll_already_download:
                Intent intent3 = new Intent(getActivity(),LocalSongsActivity.class);
                intent3.putExtra("download",1);
                startActivity(intent3);
                break;
            case R.id.ll_recent_played:
                Intent intent4 = new Intent(getActivity(),PlayRecordListActivity.class);
                startActivity(intent4);
                break;
        }
    }

}
