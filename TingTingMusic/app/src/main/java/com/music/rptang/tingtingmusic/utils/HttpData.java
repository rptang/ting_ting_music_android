package com.music.rptang.tingtingmusic.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.widget.Toast;

import com.music.rptang.tingtingmusic.RobotActivity;

public class HttpData extends AsyncTask<String, Void, String> {

	private HttpClient mHttpClient;
	private HttpGet mHttpGet;
	private HttpResponse mHttpResponse;
	private HttpEntity mHttpEntity;
	private InputStream in;

	private HttpGetDataListener listener;

	private String url;

	public HttpData(String url, HttpGetDataListener listener) {
		this.url = url;
		this.listener = listener;
	}

	@Override
	protected String doInBackground(String... params) {

		try {
			mHttpClient = new DefaultHttpClient();
			mHttpGet = new HttpGet(url);
			mHttpResponse = mHttpClient.execute(mHttpGet);
			mHttpEntity = mHttpResponse.getEntity();
			in = mHttpEntity.getContent();
			BufferedReader bf = new BufferedReader(new InputStreamReader(in));
			String line = null;
			StringBuffer sb = new StringBuffer();
			while ((line = bf.readLine()) != null) {
				sb.append(line);
			}
			System.out.println("sb:" + sb.toString());
			return sb.toString();

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		if (result != null) {
			listener.getDataUrl(result);

		} else {
			Toast.makeText(RobotActivity.ma, "网络已断开！", Toast.LENGTH_SHORT).show();
		}
		super.onPostExecute(result);
	}

}
