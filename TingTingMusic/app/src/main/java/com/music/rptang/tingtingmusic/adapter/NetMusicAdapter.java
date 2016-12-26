package com.music.rptang.tingtingmusic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.music.rptang.tingtingmusic.R;
import com.music.rptang.tingtingmusic.vo.NetMp3Info;

import java.util.ArrayList;

/**
 * Created by rptang on 2016/4/22.
 */
public class NetMusicAdapter extends BaseAdapter{

    private Context context;
    private ArrayList<NetMp3Info> netMp3Infos;

    public ArrayList<NetMp3Info> getNetMp3Infos() {
        return netMp3Infos;
    }

    public void setNetMp3Infos(ArrayList<NetMp3Info> netMp3Infos) {
        this.netMp3Infos = netMp3Infos;
    }

    public NetMusicAdapter(Context context,ArrayList<NetMp3Info> netMp3Infos){
        this.context = context;
        this.netMp3Infos = netMp3Infos;
    }

    @Override
    public int getCount() {
        return netMp3Infos.size();
    }

    @Override
    public Object getItem(int position) {
        return netMp3Infos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if(convertView==null){
            System.out.println(context);
            convertView = LayoutInflater.from(context).inflate(R.layout.item_net_music_list,null);
            vh = new ViewHolder();
            vh.tv_song_name = (TextView)convertView.findViewById(R.id.tv_song_name);
            vh.tv_song_artist = (TextView)convertView.findViewById(R.id.tv_song_artist);
            convertView.setTag(vh);
        }else{
            vh = (ViewHolder)convertView.getTag();
        }
        //给控件赋值要写在if语句外面，否则第一次加载数据失败
        NetMp3Info netMp3Info = netMp3Infos.get(position);
        vh.tv_song_name.setText(netMp3Info.getMusicName());
        vh.tv_song_artist.setText(netMp3Info.getArtist());

        return convertView;
    }


    static class ViewHolder{
        TextView tv_song_name;
        TextView tv_song_artist;
    }
}
