package com.music.rptang.tingtingmusic.utils;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.music.rptang.tingtingmusic.vo.NetMp3Info;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by rptang on 2016/4/22.
 * 搜索音乐工具类
 */
public class SearchMusicUtils {

    private static final int SIZE = 20;//查询前20条数据
    //private static final String URL = Content.BAIDU_URL + Content.BAIDU_SEARCH;

    private static SearchMusicUtils searchMusicUtils;
    private OnNetMp3InfoListener onNetMp3InfoListener;

    private ExecutorService executorService;

    //初始化工具类
    public synchronized static SearchMusicUtils getInstance(){
        if (searchMusicUtils==null){
            try{
                searchMusicUtils = new SearchMusicUtils();
            }catch (ParserConfigurationException e){
                e.printStackTrace();
            }
        }
        return searchMusicUtils;
    }

    private SearchMusicUtils() throws ParserConfigurationException{
        executorService = Executors.newSingleThreadExecutor();
    }

    public SearchMusicUtils setListener(OnNetMp3InfoListener listener){
        onNetMp3InfoListener = listener;
        return this;
    }

    public void search(final String key,final int page){
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case Content.SUCCESS:
                        if(onNetMp3InfoListener!=null){
                            onNetMp3InfoListener.onNetMp3Info((ArrayList<NetMp3Info>) msg.obj);
                        }
                        break;
                    case Content.FAILD:
                        if (onNetMp3InfoListener!=null){
                            onNetMp3InfoListener.onNetMp3Info(null);
                        }
                        break;
                }
            }
        };

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<NetMp3Info> netMp3Infos = getMusicList(key,page);
                if (netMp3Infos==null){
                    handler.sendEmptyMessage(Content.FAILD);
                    return;
                }

                handler.obtainMessage(Content.SUCCESS,netMp3Infos).sendToTarget();
            }
        });
    }

    //使用Jsoup请求网络，解析数据
    private ArrayList<NetMp3Info> getMusicList(final String key,final int page){
        final String start = String.valueOf((page - 1) * SIZE);


        try {
            String keyUrlEnCode = URLEncoder.encode(key,"utf8");
            String URL = Content.MIGU_SEARCH_HEAD + keyUrlEnCode +Content.MIGU_SEARCH_FOOT;
            Document document = Jsoup.connect(URL).data("query","java")
                    .userAgent(Content.USER_AGENT).timeout(6 * 1000).get();

            Elements songTitles = document.select("span.fl.song_name");
            Elements artists = document.select("span.fl.singer_name.mr5");
            ArrayList<NetMp3Info> netMp3Infos = new ArrayList<NetMp3Info>();

            for(int i=0;i<songTitles.size();i++){
                NetMp3Info netMp3Info = new NetMp3Info();
                Elements urls = songTitles.get(i).getElementsByTag("a");
                netMp3Info.setUrl(urls.get(0).attr("href"));
                netMp3Info.setMusicName(urls.get(0).text());

                Elements artistElements = artists.get(i).getElementsByTag("a");
                netMp3Info.setArtist(artistElements.get(0).text());

                netMp3Infos.add(netMp3Info);
                System.out.println(netMp3Infos);
            }

            return netMp3Infos;
        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }
//    private ArrayList<NetMp3Info> getMusicList(final String key,final int page){
//        final String start = String.valueOf((page - 1) * SIZE);
//
//        try {
//            Document document = Jsoup.connect(URL).data("key",key,"start",start,"size",String.valueOf(SIZE))
//                    .userAgent(Content.USER_AGENT).timeout(6 * 1000).get();
//
//            Elements songTitles = document.select("div.song-item.clearfix");
//            Elements songInfos;
//            ArrayList<NetMp3Info> netMp3Infos = new ArrayList<NetMp3Info>();
//
//            TAG:
//            for (org.jsoup.nodes.Element song : songTitles){
//                //得到每首歌曲的信息
//                songInfos = song.getElementsByTag("a");
//                NetMp3Info netMp3Info = new NetMp3Info();
//                for (org.jsoup.nodes.Element info : songInfos){
//
//                    //收费的歌曲
//                    if (info.attr("href").startsWith("http://y.baidu.com/song/")){
//                        continue TAG;
//                    }
//
//                    //跳转到百度音乐盒的歌曲
//                    if(info.attr("href").equals("#") && !TextUtils.isEmpty(info.attr("data-songdata"))){
//                        continue TAG;
//                    }
//
//                    //歌曲链接
//                    if(info.attr("href").startsWith("/song")){
//                        netMp3Info.setMusicName(info.text());
//                        netMp3Info.setUrl(info.attr("href"));
//                    }
//
//                    //歌手链接
//                    if (info.attr("href").startsWith("/data")){
//                        netMp3Info.setArtist(info.text());
//                    }
//
//                    //专辑链接
//                    if (info.attr("href").startsWith("/album")){
//                        netMp3Info.setAlbum(info.text().replaceAll("《|》",""));
//                    }
//                }
//                netMp3Infos.add(netMp3Info);
//
//            }
//            System.out.println(netMp3Infos);
//            return netMp3Infos;
//        }catch (IOException e){
//            e.printStackTrace();
//        }
//
//        return null;
//    }


    public interface OnNetMp3InfoListener{
        public void onNetMp3Info(ArrayList<NetMp3Info> netMp3Infos);
    }
}
