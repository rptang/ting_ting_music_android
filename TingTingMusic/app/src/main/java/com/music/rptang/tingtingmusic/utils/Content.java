package com.music.rptang.tingtingmusic.utils;

/**
 * Created by rptang on 2016/4/15.
 */
public class Content {

    //存放一些公共值
    public static final String SP_NAME = "TingTingMusic";
    public static final String DB_NAME = "mycollection.db";

    public static final String BAIDU_URL = "http://music.baidu.com/";//百度音乐地址
    public static final String MUSIC_DAYHOT = "top/dayhot/?pst=shouyeTop";//热歌榜地址
    public static final String BAIDU_SEARCH = "/search/song";
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0";

    public static final int SUCCESS = 1;  //搜索成功
    public static final int FAILD = 0;  //搜索失败

    public static final String DIR_MUSIC = "/tingting_music/music";
    public static final String DIR_LRC = "/tingting_music/lrc";

    public static final int PLAY_RECORD_NUM = 20;//最近播放显示最大条数

    public static final String MIGU_URL = "http://music.migu.cn/rank/184_98.html?loc=P4Z1Y4L1N1&locno=0";

    public static final String MIGU_SEARCH_HEAD = "http://music.migu.cn/webfront/searchNew/searchAll.do?keyword=";
    public static final String MIGU_SEARCH_FOOT = "&keytype=all&pagesize=20&pagenum=1";

    //http://music.migu.cn/order/1000012024/down/self/P7Z1Y1L1N1/1/001002C
    public static final String MIGU_DOWN_HEAD = "http://music.migu.cn/order/";
    public static final String MIGU_DOWN_FOOT = "/down/self/P7Z1Y1L1N1/1/001002C";


}
