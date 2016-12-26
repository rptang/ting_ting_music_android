package com.music.rptang.tingtingmusic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import io.vov.vitamio.Vitamio;

/**
 * Created by rptang on 2016/4/21.
 */
public class MVMusicFragment extends Fragment implements View.OnClickListener{

    private ImageView iv_mv1;
    private Intent intent;

    public static MVMusicFragment newInstance(){
        MVMusicFragment mvMusicFragment = new MVMusicFragment();
        return mvMusicFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mv_music_fragment,null);
        iv_mv1 = (ImageView) view.findViewById(R.id.iv_mv1);


        iv_mv1.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_mv1:
                intent = new Intent(getActivity(),PlayVideoActivity.class);
                getActivity().startActivity(intent);
                MainActivity mainActivity = (MainActivity)getActivity();
                if(mainActivity.musicPlayService.isPlaying()){
                    mainActivity.musicPlayService.pause();
                }
                break;
        }
    }
}
