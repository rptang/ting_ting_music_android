/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.music.rptang.tingtingmusic;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.google.gson.Gson;
import com.music.rptang.tingtingmusic.utils.MediaUtils;
import com.music.rptang.tingtingmusic.vo.Mp3Info;

import java.util.ArrayList;
//import com.astuetz.viewpager.extensions.sample.QuickContactFragment;

public class MainActivity extends BaseActivity implements View.OnClickListener{

	private final Handler handler = new Handler();
	private MyMusicFragment myMusicFragment;
	private NetMusicFragment netMusicFragment;
	private MVMusicFragment mvMusicFragment;

	private ArrayList<Mp3Info> mp3Infos;
	private ImageView iv_album,iv_previous,iv_play,iv_next;
	private TextView tv_song_name1,tv_song_artist1;
	private RelativeLayout rl_music_play_control;

	private PagerSlidingTabStrip tabs;
	private ViewPager pager;
	private MyPagerAdapter adapter;

	private Drawable oldBackground = null;
	private int currentColor = 0xFF666666;

	MyApplication myApplication;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		iv_album = (ImageView) findViewById(R.id.iv_album);
		iv_play = (ImageView)findViewById(R.id.iv_play);
		iv_next = (ImageView)findViewById(R.id.iv_next);
		iv_previous = (ImageView)findViewById(R.id.iv_previous);
		tv_song_name1 = (TextView)findViewById(R.id.tv_song_name1);
		tv_song_artist1 = (TextView)findViewById(R.id.tv_song_artist1);
		rl_music_play_control = (RelativeLayout)findViewById(R.id.rl_music_play_control);
		rl_music_play_control.setOnClickListener(this);
		iv_play.setOnClickListener(this);
		iv_next.setOnClickListener(this);
		iv_previous.setOnClickListener(this);

		myApplication = (MyApplication)getApplication();
		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		pager = (ViewPager) findViewById(R.id.pager);
		adapter = new MyPagerAdapter(getSupportFragmentManager());

		pager.setAdapter(adapter);

		final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
				.getDisplayMetrics());
		pager.setPageMargin(pageMargin);

		tabs.setViewPager(pager);

		changeColor(currentColor);
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()){

			case R.id.iv_play:
				if(musicPlayService.isPlaying()){
					musicPlayService.pause();
					iv_play.setImageResource(R.drawable.play);
				}else{
					if(musicPlayService.isPause()){
						musicPlayService.start();
						iv_play.setImageResource(R.drawable.pause);
					}else {
						musicPlayService.play(musicPlayService.getCurrentPosition());
						iv_play.setImageResource(R.drawable.pause);
					}
				}

				break;
			case R.id.iv_next:
				this.musicPlayService.next();
				iv_play.setImageResource(R.drawable.pause);
				break;
			case R.id.iv_previous:
				this.musicPlayService.previous();
				iv_play.setImageResource(R.drawable.pause);
				break;
			case R.id.rl_music_play_control:
				Intent intent = new Intent(this,PlayUIActivity.class);
				mp3Infos = musicPlayService.getMp3Infos();
				if(mp3Infos!=null){
					startActivity(intent);
				}
				overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
				break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		//绑定播放服务
		bindMusicPlayService();
	}

	@Override
	public void onPause() {
		super.onPause();
		unbindMusicPlayService();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//在结束activity时保存一些状态值在sharedPreferences中，以便于下次再进入app的时候取值
		//为什么在这里也拿不到mp3Infos,不设了全局变量了吗
		MyApplication myApplication = (MyApplication) getApplication();
		SharedPreferences.Editor editor = myApplication.sharedPreferences.edit();
		mp3Infos = MediaUtils.getMp3Infos(this);
		Gson gson = new Gson();
		String strJson = gson.toJson(mp3Infos);
		System.out.println(mp3Infos);
		editor.putString("mp3Infos",strJson);
		editor.putInt("currentPosition",musicPlayService.getCurrentPosition());
		editor.putInt("play_mode",musicPlayService.getPlay_mode());
		editor.commit();
	}

	@Override
	public void publish(int progress) {

	}

	@Override
	public void change(int position) {



		//从绑定的服务中获取从shareperference中的音乐集合
		myMusicFragment = MyMusicFragment.newInstance();
		mp3Infos = musicPlayService.getMp3Infos();
		if(mp3Infos==null){

		}else{
			if(position>=0 && position<mp3Infos.size()){
				System.out.println(mp3Infos);
				Mp3Info mp3Info = mp3Infos.get(position);
				System.out.println(mp3Info+"============");
				tv_song_name1.setText(mp3Info.getTitle());
				System.out.println(mp3Info.getTitle()+"==============");
				tv_song_artist1.setText(mp3Info.getArtist());

				Bitmap albumBitmap =  MediaUtils.getArtwork(this, mp3Info.getId(), mp3Info.getAlbumId(), true, false);
				if(albumBitmap==null){
					iv_album.setImageResource(R.drawable.music_play);
				}else{
					iv_album.setImageBitmap(albumBitmap);
				}

			}
		}
		if(musicPlayService.isPlaying()){
			iv_play.setImageResource(R.drawable.pause);
		}else {
			iv_play.setImageResource(R.drawable.play);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

//		case R.id.action_contact:
//			QuickContactFragment dialog = new QuickContactFragment();
//			dialog.show(getSupportFragmentManager(), "QuickContactFragment");
//			return true;
//
		}

		return super.onOptionsItemSelected(item);
	}

	private void changeColor(int newColor) {

		tabs.setIndicatorColor(newColor);

		// change ActionBar color just if an ActionBar is available
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

			Drawable colorDrawable = new ColorDrawable(newColor);
			Drawable bottomDrawable = getResources().getDrawable(R.drawable.actionbar_bottom);
			LayerDrawable ld = new LayerDrawable(new Drawable[] { colorDrawable, bottomDrawable });

			if (oldBackground == null) {

				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
					ld.setCallback(drawableCallback);
				} else {
					//getActionBar().setBackgroundDrawable(ld);
				}

			} else {

				TransitionDrawable td = new TransitionDrawable(new Drawable[] { oldBackground, ld });

				// workaround for broken ActionBarContainer drawable handling on
				// pre-API 17 builds
				// https://github.com/android/platform_frameworks_base/commit/a7cc06d82e45918c37429a59b14545c6a57db4e4
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
					td.setCallback(drawableCallback);
				} else {
					//getActionBar().setBackgroundDrawable(td);
				}

				td.startTransition(200);

			}

			oldBackground = ld;

			// http://stackoverflow.com/questions/11002691/actionbar-setbackgrounddrawable-nulling-background-from-thread-handler
//			getActionBar().setDisplayShowTitleEnabled(false);
//			getActionBar().setDisplayShowTitleEnabled(true);

		}

		currentColor = newColor;

	}

	public void onColorClicked(View v) {

		int color = Color.parseColor(v.getTag().toString());
		changeColor(color);

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("currentColor", currentColor);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		currentColor = savedInstanceState.getInt("currentColor");
		changeColor(currentColor);
	}

	private Drawable.Callback drawableCallback = new Drawable.Callback() {
		@Override
		public void invalidateDrawable(Drawable who) {
			getActionBar().setBackgroundDrawable(who);
		}

		@Override
		public void scheduleDrawable(Drawable who, Runnable what, long when) {
			handler.postAtTime(what, when);
		}

		@Override
		public void unscheduleDrawable(Drawable who, Runnable what) {
			handler.removeCallbacks(what);
		}
	};

//	@Override
//	public void downloadSuccessListener(String isDownloadSuccess) {
//		if(isDownloadSuccess.length()>0){
//			System.out.println("sdaffffffffffffffffffffffffffffffffffffffff");
//			myMusicFragment.newInstance();
//			//myMusicFragment.onResume();
//
//		}
//	}


	public class MyPagerAdapter extends FragmentPagerAdapter {

		private final String[] TITLES = { "我的专属音乐", "听听音乐馆", "MV潮流在线" };

		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return TITLES[position];
		}

		@Override
		public int getCount() {
			return TITLES.length;
		}

		@Override
		public Fragment getItem(int position) {

			if(position==0){
				if(myMusicFragment==null){
					myMusicFragment = MyMusicFragment.newInstance();
				}
				return myMusicFragment;
			}else if(position==1){
				if(netMusicFragment==null){
					netMusicFragment = NetMusicFragment.newInstance();
				}
				return netMusicFragment;
			}else if (position==2){
				if(mvMusicFragment ==null){
					mvMusicFragment = MVMusicFragment.newInstance();
				}
				return mvMusicFragment;
			}
			return null;
		}

	}

}