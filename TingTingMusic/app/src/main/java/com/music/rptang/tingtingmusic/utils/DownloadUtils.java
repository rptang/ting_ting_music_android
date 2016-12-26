package com.music.rptang.tingtingmusic.utils;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import com.music.rptang.tingtingmusic.vo.NetMp3Info;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by rptang on 2016/5/13.
 */
public class DownloadUtils {

    private static final String DOWNLOAD_URL = "/download?title=&pst=naga&fr=";
    public static final int SUCCESS_LRC = 1;
    private static final int FAILED_LRC = 2;
    private static final int SUCCESS_MP3 = 3;
    private static final int FAILED_MP3 = 4;
    private static final int GET_MP3_URL = 5;
    private static final int GET_FAILED_MP3_URL = 6;
    private static final int MUSIC_EXISTS = 7;

    private static DownloadUtils sInstance;
    private OnDownloadListener mListener;

    private ExecutorService mThreadPool;

    /**
     * 设置回调的监听器对象
     * @return
     */
    public DownloadUtils setListener(OnDownloadListener mListener){
        this.mListener = mListener;
        return this;//这个return this 很有妙处
    }

    //获取下载工具的实例
    public synchronized static DownloadUtils getsInstance(){

        if (sInstance == null){
            try {
                sInstance = new DownloadUtils();
            }catch (ParserConfigurationException e){
                e.printStackTrace();
            }
        }
        return sInstance;
    }

    private DownloadUtils() throws ParserConfigurationException{
        mThreadPool = Executors.newSingleThreadExecutor();//开启一个单线程
    }

    /**
     * 下载的具体方法
     */
    public void download(final NetMp3Info netMp3Info){
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case SUCCESS_LRC:
                        if(mListener != null)mListener.onDownload("歌词下载成功");
                        break;
                    case FAILED_LRC:
                        if(mListener != null)mListener.onFailed("歌词下载失败");
                        break;
                    case GET_MP3_URL:
                        System.out.println(msg.obj);
                        downloadMusic(netMp3Info, (String) msg.obj, this);
                        break;
                    case GET_FAILED_MP3_URL:
                        if (mListener != null)mListener.onFailed("下载失败，该歌曲为收费或VIP歌曲");
                        break;
                    case SUCCESS_MP3:
                        if(mListener != null)mListener.onDownload(netMp3Info.getMusicName()+"已下载");
                        String url = Content.BAIDU_URL + netMp3Info.getUrl();
                        System.out.println("download lrc:"+url);
                        downloadLRC(url,netMp3Info.getMusicName(),this);
                        break;
                    case FAILED_MP3:
                        if(mListener != null)mListener.onFailed(netMp3Info.getMusicName()+"下载失败");
                        break;
                    case MUSIC_EXISTS:
                        if (mListener != null)mListener.onFailed("音乐已存在");
                        break;
                }
            }
        };

        getDownloadMusicURL(netMp3Info, handler);
    }

    private void getDownloadMusicURL(final NetMp3Info netMp3Info,final Handler handler){
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {

                String[] aa = netMp3Info.getUrl().split("/");
                String sn = aa[5];
                System.out.println("歌曲编号："+sn);
                String url = Content.MIGU_DOWN_HEAD + sn + Content.MIGU_DOWN_FOOT;
                //拿到的这个页面是正确的
                System.out.println("下载页面："+url+"====================");

                try {
                    Document document = Jsoup.connect(url).userAgent(Content.USER_AGENT).timeout(6 * 1000).get();
                    String[] bb = document.toString().split("song");

                    for (int i=10;i<bb.length;i++){
                        String initMp3Url = bb[i];
                        //System.out.println(initMp3Url+i+i+i+i+i+i+i+i+i+i+i+i);
                        String[] arrayHttp = initMp3Url.split("http");

                        if(arrayHttp.length>1){

                            if(initMp3Url.contains("msisdn")){
                                String[] arrayMp3 = arrayHttp[1].split(".mp3");
                                String result = "http" + arrayMp3[0] + ".mp3";
                                handler.obtainMessage(GET_MP3_URL,result).sendToTarget();
                                return;
                            }
                        }
                    }
                }catch (IOException e){
                    e.printStackTrace();
                    handler.obtainMessage(GET_FAILED_MP3_URL).sendToTarget();
                }


//                try {
//                    //获得下载音乐地址前缀，搜索出来的地址和榜上音乐不太一样，需要自行拼接
//                    String url = Content.BAIDU_URL + "/song/" + netMp3Info.getUrl()
//                            .substring(netMp3Info.getUrl().lastIndexOf("/") + 1) + DOWNLOAD_URL;
//                    //连接网络
//                    Document document = Jsoup.connect(url).userAgent(Content.USER_AGENT).timeout(6000).get();
//                    //找到所有节点信息
//                    Elements targetElements = document.select("a[date-btndata");
//                    if (targetElements.size() <= 0) { //如果该歌曲不提供下载选项，则返回获得歌曲地址失败
//                        handler.obtainMessage(GET_FAILED_MP3_URL).sendToTarget();
//                        return;
//                    }
//                    for (Element e : targetElements) { //得到节点信息后，开始遍历，取出有用的信息
//                        if (e.attr("href").contains(".mp3")) {
//                            String result = e.attr("href");
//                            Message msg = handler.obtainMessage(GET_MP3_URL, result);
//                            msg.sendToTarget();
//                            return;
//                        }
//
//                        if (e.attr("href").startsWith("/vip")) {
//                            targetElements.remove(e);
//                        }
//                    }
//                    if (targetElements.size() <= 0) {
//                        handler.obtainMessage(GET_FAILED_MP3_URL).sendToTarget();
//                        return;
//                    }
//                    String result = targetElements.get(0).attr("href");
//                    Message msg = handler.obtainMessage(GET_MP3_URL, result);
//                    msg.sendToTarget();
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    handler.obtainMessage(GET_FAILED_MP3_URL).sendToTarget();
//                    return;
//                }
            }
        });
    }

    /**
     * 正式访问网络下载音乐
     */
    private void downloadMusic(final NetMp3Info netMp3Info,final String url,final Handler handler){
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                File musicDirFile = new File(Environment.getExternalStorageDirectory()+Content.DIR_MUSIC);
                if(!musicDirFile.exists()){
                    musicDirFile.mkdirs();
                }
                String mp3url = url;
                String target = musicDirFile + "/" + netMp3Info.getMusicName()+".mp3";
                File fileTarget = new File(target);
                if(fileTarget.exists()){
                    handler.obtainMessage(MUSIC_EXISTS).sendToTarget();
                    return;
                }else {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(mp3url).build();
                    try {
                        Response response = client.newCall(request).execute();
                        if(response.isSuccessful()){
                            PrintStream ps = new PrintStream(fileTarget);
                            byte[] bytes = response.body().bytes();
                            ps.write(bytes, 0, bytes.length);
                            ps.close();
                            handler.obtainMessage(SUCCESS_MP3).sendToTarget();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        handler.obtainMessage(FAILED_MP3).sendToTarget();
                    }
                }
            }
        });
    }

    /**
     * 下载歌词
     */
    public void downloadLRC(final String url, final String musicName,final Handler handler){
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Document document = Jsoup.connect(url).userAgent(Content.USER_AGENT).timeout(6000).get();
                    Elements lrcTag = document.select("div.lyric-content");
                    String lrcURL = lrcTag.attr("data-lrclink");
                    File lrcDirFile = new File(Environment.getExternalStorageDirectory() + Content.DIR_LRC);
                    if(!lrcDirFile.exists()){
                        lrcDirFile.mkdirs();
                    }
//百度下载歌词：http://music.baidu.com//data2/lrc/a2bdd18c0e497f1247275fc46c7c8141/264365069/264365069.lrc
                    lrcURL = Content.BAIDU_URL + lrcURL;
                    String target = lrcDirFile + "/" +musicName + ".lrc";
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(lrcURL).build();

                    try {
                        Response response = client.newCall(request).execute();
                        if(response.isSuccessful()){
                            PrintStream ps = new PrintStream(new File(target));
                            byte[] bytes = response.body().bytes();
                            ps.write(bytes,0,bytes.length);
                            ps.close();
                            handler.obtainMessage(SUCCESS_LRC,target).sendToTarget();
                        }
                    }catch (IOException e){
                        e.printStackTrace();
                        handler.obtainMessage(FAILED_LRC).sendToTarget();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 自定义下载事件监听器，回调机制
     */
    public interface OnDownloadListener{
        public void onDownload(String mp3Url);
        public void onFailed(String error);
    }
}
