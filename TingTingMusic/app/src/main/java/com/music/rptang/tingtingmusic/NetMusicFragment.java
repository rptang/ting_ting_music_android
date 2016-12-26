package com.music.rptang.tingtingmusic;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.music.rptang.tingtingmusic.adapter.NetMusicAdapter;
import com.music.rptang.tingtingmusic.utils.AppUtils;
import com.music.rptang.tingtingmusic.utils.Content;
import com.music.rptang.tingtingmusic.utils.SearchMusicUtils;
import com.music.rptang.tingtingmusic.vo.NetMp3Info;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by rptang on 2016/4/12.
 */
public class NetMusicFragment extends Fragment implements View.OnClickListener,AdapterView.OnItemClickListener{

    private LinearLayout ll_loading_progress_bar;
    private ArrayList<NetMp3Info> netMp3Infos;
    private NetMusicAdapter netMusicAdapter;
    private ListView lv_net_songs_list;
    private EditText et_music_search;
    private ImageView ivDeleteText;
    private Button bt_music_search;
    private TextView tv_check_net;
    private int page = 1; //搜索音乐的页码

    public static NetMusicFragment newInstance(){
        NetMusicFragment netMusicFragment = new NetMusicFragment();
        return netMusicFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.net_music_fragment,null);
        netMp3Infos = new ArrayList<NetMp3Info>();
        //找控件
        ll_loading_progress_bar = (LinearLayout)view.findViewById(R.id.ll_loading_progress_bar);
        et_music_search = (EditText)view.findViewById(R.id.et_music_search);
        lv_net_songs_list = (ListView)view.findViewById(R.id.lv_net_songs_list);
        ivDeleteText = (ImageView)view.findViewById(R.id.ivDeleteText);
        bt_music_search = (Button)view.findViewById(R.id.bt_music_search);
        tv_check_net = (TextView)view.findViewById(R.id.tv_check_net);
        //添加监听事件
        ivDeleteText.setOnClickListener(this);
        bt_music_search.setOnClickListener(this);
        lv_net_songs_list.setOnItemClickListener(this);
        //监听编辑框事件
        et_music_search.addTextChangedListener(textWatcher);
        loadNetData();//加载网络音乐
        return view;
    }

    private void loadNetData(){
        ll_loading_progress_bar.setVisibility(View.VISIBLE);
        //执行异步加载网络音乐任务
        //new LoadNetDataTask().execute(Content.BAIDU_URL+Content.MUSIC_DAYHOT);
        new LoadNetDataTask().execute(Content.MIGU_URL);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivDeleteText:
                et_music_search.setText("");
                break;
            case R.id.bt_music_search:
                searchMusic();
                break;
        }
    }

    private void searchMusic() {
        //点击搜索按钮，先把输入框隐藏,不知道为什么没有隐藏掉
        AppUtils.hideInputMethod(et_music_search);
        String key = et_music_search.getText().toString();
        if(TextUtils.isEmpty(key)){
            Toast.makeText(getActivity(),"请输入歌名/歌手/专辑 ",Toast.LENGTH_SHORT).show();
            return;
        }
        //加载动画再次显示
        ll_loading_progress_bar.setVisibility(View.VISIBLE);
        SearchMusicUtils.getInstance().setListener(new SearchMusicUtils.OnNetMp3InfoListener() {
            @Override
            public void onNetMp3Info(ArrayList<NetMp3Info> netMp3Infos) {
                //首先是获得原有的网络音乐列表集合
                //遇到一个bug，在网络音乐还没加载出来之前
                if (netMusicAdapter!=null) {
                    ArrayList<NetMp3Info> np = netMusicAdapter.getNetMp3Infos();
                    np.clear();//然后清空
                    np.addAll(netMp3Infos);//再把搜索到的音乐集合加进去
                    netMusicAdapter.notifyDataSetChanged();//刷新界面
                    ll_loading_progress_bar.setVisibility(View.GONE);//隐藏加载动画
                }
            }
        }).search(key, page);
    }

    //编辑框监听事件,输入字后，删除键出现，无字删除键消失
    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if(editable.length()==0){
                ivDeleteText.setVisibility(View.GONE);
            }else{
                ivDeleteText.setVisibility(View.VISIBLE);
            }
        }
    };

    //listview的item点击事件
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        //精准保证所点击的position都有对应的对象，这就是细节
        if(position>=netMusicAdapter.getNetMp3Infos().size()||position<0) return;
        showDownloadDialog(position);
    }

    //点击item出现下载弹窗
    private void showDownloadDialog(int position) {

        DownloadDialogFragment downloadDialogFragment = DownloadDialogFragment.newInstance(netMp3Infos.get(position));
        downloadDialogFragment.show(getActivity().getFragmentManager(), "download");
    }


    class LoadNetDataTask extends AsyncTask<String,Integer,Integer>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ll_loading_progress_bar.setVisibility(View.VISIBLE);
            lv_net_songs_list.setVisibility(View.GONE);
        }

        @Override
        protected Integer doInBackground(String... params) {
            String url = params[0];
            try {
                //使用Jsoup组件请求网络，并解析音乐数据
                //没网的时候，异步加载走到这里就停了,怎么办呢
//                Document document = Jsoup.connect(url).userAgent(Content.USER_AGENT).timeout(6*1000).get();
//                Elements songTitles = document.select("span.song-title");
//                Elements artists = document.select("span.author_list");
//                for(int i=0;i<songTitles.size()-1;i++){
//                    NetMp3Info netMp3Info = new NetMp3Info();
//                    //class="song-title " style='width: 305px;'><a href="/song/247911654" target="_blank"
//                    // title="金志文夏洛特烦恼" data-film="null">夏洛特烦恼</a>
//                    Elements urls = songTitles.get(i).getElementsByTag("a");//取到 <a 后面的所有东西
//                    //取到/song/247911654这是音乐地址
//                    netMp3Info.setUrl(urls.get(0).attr("href"));
//                    //取到夏洛特烦恼
//                    netMp3Info.setMusicName(urls.get(0).text());
//                    //<span class="author_list" title="金志文"><a hidefocus="true" href="/artist/5913">金志文</a>
//                    //取到金志文
//                    Elements artistElements = artists.get(i).getElementsByTag("a");
//                    netMp3Info.setArtist(artistElements.get(0).text());
//                    netMp3Info.setAlbum("热歌榜");
//                    netMp3Infos.add(netMp3Info);

                Document document = Jsoup.connect(url).userAgent(Content.USER_AGENT).timeout(6*1000).get();
                Elements songTitles = document.select("span.fl.song_name");
                Elements artists = document.select("span.fl.singer_name.mr5");
                for(int i=0;i<songTitles.size();i++){
                    NetMp3Info netMp3Info = new NetMp3Info();
                    //class="song-title " style='width: 305px;'><a href="/song/247911654" target="_blank"
                    // title="金志文夏洛特烦恼" data-film="null">夏洛特烦恼</a>
                    Elements urls = songTitles.get(i).getElementsByTag("a");//取到 <a 后面的所有东西
                    //取到/song/247911654这是音乐地址
                    netMp3Info.setUrl(urls.get(0).attr("href"));
                    //取到夏洛特烦恼
                    netMp3Info.setMusicName(urls.get(0).text());
                    //<span class="author_list" title="金志文"><a hidefocus="true" href="/artist/5913">金志文</a>
                    //取到金志文
                    Elements artistElements = artists.get(i).getElementsByTag("a");
                    netMp3Info.setArtist(artistElements.get(0).text());
                    netMp3Info.setAlbum("热歌榜");
                    netMp3Infos.add(netMp3Info);
                }
            }catch (IOException e){
                e.printStackTrace();
                return -1;
            }
//            try {
//                //使用Jsoup组件请求网络，并解析音乐数据
//                //没网的时候，异步加载走到这里就停了,怎么办呢
//                Document document = Jsoup.connect(url).userAgent(Content.USER_AGENT).timeout(6*1000).get();
//                Elements songTitles = document.select("span.song-title");
//                System.out.println("songTitles:"+songTitles);
//                Elements artists = document.select("span.author_list");
//                System.out.println("artists:"+artists);
//                for(int i=0;i<songTitles.size();i++){
//                    NetMp3Info netMp3Info = new NetMp3Info();
//                    //class="song-title " style='width: 305px;'><a href="/song/247911654" target="_blank"
//                    // title="金志文夏洛特烦恼" data-film="null">夏洛特烦恼</a>
//                    Elements urls = songTitles.get(i).getElementsByTag("a");//取到 <a 后面的所有东西
//                    //取到/song/247911654这是音乐地址
//                    netMp3Info.setUrl(urls.get(0).attr("href"));
//                    //取到夏洛特烦恼
//                    netMp3Info.setMusicName(urls.get(0).text());
//                    //<span class="author_list" title="金志文"><a hidefocus="true" href="/artist/5913">金志文</a>
//                    //取到金志文
//                    System.out.println(songTitles.size()+"========");
//                    System.out.println(i+"========");
//                    Elements artistElements = artists.get(i).getElementsByTag("a");
//                    netMp3Info.setArtist(artistElements.get(0).text());
//                    netMp3Info.setAlbum("热歌榜");
//                    netMp3Infos.add(netMp3Info);
//                }
//            }catch (IOException e){
//                e.printStackTrace();
//                return -1;
//            }
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if(result==1){
                if(netMp3Infos==null){
                    tv_check_net.setVisibility(View.VISIBLE);
                }
                netMusicAdapter = new NetMusicAdapter(getActivity(),netMp3Infos);
                lv_net_songs_list.setAdapter(netMusicAdapter);
                lv_net_songs_list.addFooterView(LayoutInflater.from(getActivity()).inflate(R.layout.footview_layout,null));
            }
            ll_loading_progress_bar.setVisibility(View.GONE);
            lv_net_songs_list.setVisibility(View.VISIBLE);
        }
    }
}
