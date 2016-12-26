package com.music.rptang.tingtingmusic;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.lidroid.xutils.DbUtils;
import com.music.rptang.tingtingmusic.utils.Content;

/**
 * Created by rptang on 2016/4/15.
 */
public class MyApplication extends Application{

    public static SharedPreferences sharedPreferences;
    public static DbUtils dbUtils;
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences(Content.SP_NAME, Context.MODE_PRIVATE);
        dbUtils = DbUtils.create(getApplicationContext(),Content.DB_NAME);
        context = getApplicationContext();
    }
}
