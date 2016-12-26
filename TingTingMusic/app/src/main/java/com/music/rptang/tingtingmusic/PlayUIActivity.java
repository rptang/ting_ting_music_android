package com.music.rptang.tingtingmusic;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.music.rptang.tingtingmusic.adapter.MyPagerAdapter;
import com.music.rptang.tingtingmusic.utils.MediaUtils;
import com.music.rptang.tingtingmusic.vo.Mp3Info;

import java.util.ArrayList;
import java.util.List;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import lrcview.LrcView;

/**
 * Created by rptang on 2016/4/13.
 */
public class PlayUIActivity extends BaseActivity implements View.OnClickListener,SeekBar.OnSeekBarChangeListener {

    private ImageView iv_pull_down,iv_play_ui_play,iv_play_ui_next,iv_play_ui_previous,
            iv_play_ui_play_mode,iv_share,iv_song_image,iv_play_ui_like;
    private TextView tv_play_ui_song,tv_play_ui_artist,tv_play_ui_end_time,
            tv_play_ui_play_time,tv_song_lrc;
    private ArrayList<Mp3Info> mp3Infos;
    private SeekBar sb_play_ui_seekbar;
    private static final int UPDATE_TIME = 0x1;
    private static MyHandler myHandler;
    Mp3Info mp3Info;
    private LrcView mLrcView;
    private ViewPager viewPager;
    private List<View> views;
    MyApplication myApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_play_ui);
        myApplication = (MyApplication)getApplication();
        iv_pull_down = (ImageView)findViewById(R.id.iv_pull_down);
        tv_play_ui_song = (TextView)findViewById(R.id.tv_play_ui_song);
        tv_play_ui_artist = (TextView)findViewById(R.id.tv_play_ui_artist);
        tv_play_ui_end_time = (TextView)findViewById(R.id.tv_play_ui_end_time);
        tv_play_ui_play_time = (TextView)findViewById(R.id.tv_play_ui_play_time);
        iv_play_ui_play = (ImageView)findViewById(R.id.iv_play_ui_play);
        iv_play_ui_next = (ImageView)findViewById(R.id.iv_play_ui_next);
        iv_play_ui_previous = (ImageView)findViewById(R.id.iv_play_ui_previous);
        iv_play_ui_play_mode = (ImageView)findViewById(R.id.iv_play_ui_play_mode);
        iv_share = (ImageView)findViewById(R.id.iv_share);
        sb_play_ui_seekbar = (SeekBar)findViewById(R.id.sb_play_ui_seekbar);
        viewPager = (ViewPager)findViewById(R.id.vp_play_ui_pager);

        iv_play_ui_like = (ImageView)findViewById(R.id.iv_play_ui_like);
        iv_pull_down.setOnClickListener(this);
        iv_play_ui_play.setOnClickListener(this);
        iv_play_ui_next.setOnClickListener(this);
        iv_play_ui_previous.setOnClickListener(this);
        iv_play_ui_play_mode.setOnClickListener(this);
        iv_share.setOnClickListener(this);
        sb_play_ui_seekbar.setOnSeekBarChangeListener(this);
        //mp3Infos = MediaUtils.getMp3Infos(this);
        iv_play_ui_like.setOnClickListener(this);

        initViewPager();

        myHandler = new MyHandler(this);
    }

    private void initViewPager() {
        View son_pager_song_image = getLayoutInflater().inflate(R.layout.son_pager_song_image,null);
        iv_song_image = (ImageView) son_pager_song_image.findViewById(R.id.iv_song_image);



        View son_pager_song_lrc = getLayoutInflater().inflate(R.layout.son_pager_song_lrc,null);
        tv_song_lrc = (TextView)son_pager_song_lrc.findViewById(R.id.tv_song_lrc);
        //mLrcView = (LrcView)findViewById(R.id.lrc);

        views = new ArrayList<View>();
        views.add(son_pager_song_image);
        views.add(son_pager_song_lrc);
        viewPager.setAdapter(new MyPagerAdapter(this, views));


        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在结束activity时保存一些状态值在sharedPreferences中，以便于下次再进入app的时候取值
        SharedPreferences.Editor editor = myApplication.sharedPreferences.edit();
        editor.putInt("currentPosition",musicPlayService.getCurrentPosition());
        editor.putInt("play_mode", musicPlayService.getPlay_mode());
        editor.commit();
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
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser){
            musicPlayService.pause();
            musicPlayService.seekTo(progress);
            musicPlayService.start();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    /**
     * 进度条控件已经内部处理过了，开始时间的改变是在子线程中改变主线程的UI，这当然是不可以的
     * 怎么办呢，用你最熟悉的Handler处理吧
     */
    static class MyHandler extends android.os.Handler{
        //内部类去要想使用外部类的权限，就得把外部类拿进来
        private PlayUIActivity playUIActivity;
        public MyHandler(PlayUIActivity playUIActivity){
            this.playUIActivity = playUIActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(playUIActivity!=null){
                switch (msg.what){
                    case UPDATE_TIME:
                        playUIActivity.tv_play_ui_play_time.setText(MediaUtils.formatTime(msg.arg1));
                        break;
                }
            }
        }
    }

    //这里是子线程，不断的发送msg给主线程，通知其更改UI
    @Override
    public void publish(int progress) {
        Message msg = myHandler.obtainMessage(UPDATE_TIME);
        msg.arg1 = progress;
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        myHandler.sendMessage(msg);
        sb_play_ui_seekbar.setProgress(progress);
    }

    @Override
    public void change(int position) {

        mp3Infos = musicPlayService.getMp3Infos();

        if(mp3Infos.size()>0) {

            mp3Info = mp3Infos.get(position);
            tv_play_ui_song.setText(mp3Info.getTitle());
            tv_play_ui_artist.setText(mp3Info.getArtist());
            tv_play_ui_end_time.setText(MediaUtils.formatTime(mp3Info.getDuration()));

            Bitmap albumBitmap = MediaUtils.getArtwork(this, mp3Info.getId(), mp3Info.getAlbumId(), true, false);
            if (albumBitmap == null) {
                iv_song_image.setImageResource(R.drawable.default_image);
            } else {
                iv_song_image.setImageBitmap(albumBitmap);
            }

            iv_play_ui_play.setImageResource(R.drawable.pause);
            sb_play_ui_seekbar.setProgress(0);
            sb_play_ui_seekbar.setMax((int) mp3Info.getDuration());
            if (musicPlayService.isPlaying()) {
                iv_play_ui_play.setImageResource(R.drawable.pause);
            } else {
                iv_play_ui_play.setImageResource(R.drawable.play);
            }

            //来判断该歌曲是否已经被收藏，来决定界面收藏图片的状态
            try {
                Mp3Info collectMp3Info = myApplication.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId", "=", mp3Info.getId()));
                System.out.println(mp3Info);
                System.out.println(collectMp3Info);
                if (collectMp3Info != null) {
                    if (collectMp3Info.getIsLike() == 0) {
                        iv_play_ui_like.setImageResource(R.drawable.like);
                    } else {
                        iv_play_ui_like.setImageResource(R.drawable.liked);
                    }
                } else {
                    iv_play_ui_like.setImageResource(R.drawable.like);
                }
            } catch (DbException e) {
                e.printStackTrace();
            }

            switch (musicPlayService.getPlay_mode()) {
                case MusicPlayService.ORDER_PLAY:
                    iv_play_ui_play_mode.setImageResource(R.drawable.list_cycle);
                    break;
                case MusicPlayService.RANDOM_PLAY:
                    iv_play_ui_play_mode.setImageResource(R.drawable.random);
                    break;
                case MusicPlayService.SINGLE_PLAY:
                    iv_play_ui_play_mode.setImageResource(R.drawable.single_cycle);
                    break;
                default:
                    break;
            }
        }
    }

    private void showShare() {
        ShareSDK.initSDK(this);
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

// 分享时Notification的图标和文字  2.5.9以后的版本不调用此方法
        //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle(getString(R.string.share));
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        oks.setTitleUrl("http://sharesdk.cn");
        // text是分享文本，所有平台都需要这个字段
        oks.setText("分享歌曲:" + "《" + mp3Info.getTitle()+"+"+mp3Info.getArtist()+"》");
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        //oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl("http://sharesdk.cn");
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment("好听到爆炸");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl("http://sharesdk.cn");

// 启动分享GUI
        oks.show(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_pull_down:
                finish();
                overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
                break;
            case R.id.iv_play_ui_play:
                if(musicPlayService.isPlaying()){
                    musicPlayService.pause();
                    iv_play_ui_play.setImageResource(R.drawable.play);
                }else{
                    if(musicPlayService.isPause()){
                        musicPlayService.start();
                        iv_play_ui_play.setImageResource(R.drawable.pause);
                    }else{
                        musicPlayService.play(musicPlayService.getCurrentPosition());
                    }
                }
                break;
            case R.id.iv_play_ui_previous:
                musicPlayService.previous();
                break;
            case R.id.iv_play_ui_next:
                musicPlayService.next();
                break;
            case R.id.iv_play_ui_play_mode:
                switch (musicPlayService.getPlay_mode()){
                    case MusicPlayService.ORDER_PLAY:
                        iv_play_ui_play_mode.setImageResource(R.drawable.random);
                        musicPlayService.setPlay_mode(MusicPlayService.RANDOM_PLAY);
                        break;
                    case MusicPlayService.RANDOM_PLAY:
                        iv_play_ui_play_mode.setImageResource(R.drawable.single_cycle);
                        musicPlayService.setPlay_mode(MusicPlayService.SINGLE_PLAY);
                        break;
                    case MusicPlayService.SINGLE_PLAY:
                        iv_play_ui_play_mode.setImageResource(R.drawable.list_cycle);
                        musicPlayService.setPlay_mode(MusicPlayService.ORDER_PLAY);
                }
                break;
            case R.id.iv_share:
                showShare();
                break;
            case R.id.iv_play_ui_like:{
                Mp3Info mp3Info = mp3Infos.get(musicPlayService.getCurrentPosition());
                try {
                    //利用一个新的collectMp3Info来储存歌曲id的值
                    Mp3Info collectMp3Info = myApplication.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId","=",getId(mp3Info)));
                    if (collectMp3Info == null){
                        mp3Info.setMp3InfoId(mp3Info.getId());
                        mp3Info.setIsLike(1);
                        myApplication.dbUtils.save(mp3Info);
                        iv_play_ui_like.setImageResource(R.drawable.liked);
                    }else {

                        int isLike = collectMp3Info.getIsLike();
                        if(isLike==1){
                            collectMp3Info.setIsLike(0);
                            iv_play_ui_like.setImageResource(R.drawable.like);
                        }else {
                            collectMp3Info.setIsLike(1);
                            iv_play_ui_like.setImageResource(R.drawable.liked);
                        }

                        myApplication.dbUtils.update(collectMp3Info,"isLike");

//                        myApplication.dbUtils.deleteById(Mp3Info.class, collectMp3Info.getId());
//                        iv_play_ui_like.setImageResource(R.drawable.like);
                    }
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
            default:
                break;
        }
    }

    private long getId(Mp3Info mp3Info){
        //初始收藏状态
        long id = 0;
        switch (musicPlayService.getChangePlayList()){

            case MusicPlayService.MY_MUSIC_LIST:
                id = mp3Info.getId();
                break;
            case MusicPlayService.LOVE_MUSIC_LIST:
                id = mp3Info.getMp3InfoId();
                break;
            default:
                break;
        }
        return id;
    }
}

