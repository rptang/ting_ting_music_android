package com.music.rptang.tingtingmusic.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by rptang on 2016/4/14.
 */
public class MyPagerAdapter extends PagerAdapter{

    private Context context;
    private List<View> views;

    public MyPagerAdapter(Context context,List<View> views) {
        this.context = context;
        this.views = views;
    }

    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {

        return view == object;
    }

    @Override
    public void destroyItem(View container, int position, Object object) {

        ((ViewPager) container).removeView(views.get(position));
    }

    @Override
    public Object instantiateItem(View container, int position) {
        ((ViewPager)container).addView(views.get(position));
        return views.get(position);
    }
}
