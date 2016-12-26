package com.music.rptang.tingtingmusic.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.music.rptang.tingtingmusic.MyApplication;

/**
 * Created by rptang on 2016/4/22.
 */
public class AppUtils {

    //隐藏输入键盘
    public static void hideInputMethod(View view){
        InputMethodManager inputMethodManager = (InputMethodManager) MyApplication.context.
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager.isActive()){
            inputMethodManager.hideSoftInputFromInputMethod(view.getWindowToken(),
                    0);
        }
    }
}
