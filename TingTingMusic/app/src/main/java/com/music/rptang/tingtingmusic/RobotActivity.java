package com.music.rptang.tingtingmusic;

/**
 * Created by rptang on 2016/5/14.
 */
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.music.rptang.tingtingmusic.adapter.TextAdapter;
import com.music.rptang.tingtingmusic.utils.HttpData;
import com.music.rptang.tingtingmusic.utils.HttpGetDataListener;
import com.music.rptang.tingtingmusic.vo.ListData;

public class RobotActivity extends Activity implements HttpGetDataListener,
        OnClickListener {
    public static RobotActivity ma;

    private ListView mListView;
    private EditText mEditText;
    private TextView mTextView;
    private ImageView mHelp_id;

    private HttpData httpData;
    private List<ListData> lists; // 用于封装聊天数据
    private TextAdapter adapter;

    private double currentTime, oldTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.robot_main);

        initView();
        // 返回按钮
        mHelp_id.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                RobotActivity.this.finish();
            }
        });
    }

    // 初始化组件
    private void initView() {
        ma = RobotActivity.this;
        mListView = (ListView) findViewById(R.id.listView);
        mEditText = (EditText) findViewById(R.id.et_send);
        mTextView = (TextView) findViewById(R.id.tv_send);
        mHelp_id = (ImageView) findViewById(R.id.iv_backtrack);
        lists = new ArrayList<ListData>();

        mTextView.setOnClickListener(this); // 发送按钮监听
        // 为mListView设置适配器
        adapter = new TextAdapter(this, lists);
        mListView.setAdapter(adapter);
        // 欢迎语
        ListData listData = new ListData(getWelcomeWord(), ListData.RECEIVE,
                getTime());
        lists.add(listData);
    }

    // 获取欢迎语
    private String getWelcomeWord() {
        String welcome = null;
        String[] welcome_array = this.getResources().getStringArray(
                R.array.welcome);
        int index = (int) (Math.random() * (welcome_array.length - 1));
        welcome = welcome_array[index];
        return welcome;
    }

    @Override
    public void getDataUrl(String data) {
        parseData(data);
    }

    // 解析获取到的数据
    public void parseData(String data) {
        try {
            JSONObject obj = new JSONObject(data);
            String code = obj.getString("code");
            String text = obj.getString("text");
            // 封装获取到的聊天内容 text到ListData对象
            ListData listData = new ListData(text, ListData.RECEIVE, getTime());
            lists.add(listData);
            adapter.notifyDataSetChanged(); // 重新适配
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        String content_str = mEditText.getText().toString();
        mEditText.setText(""); // 清空编辑框
        // 去掉空格
        String str1 = content_str.replace(" ", "");// 去空格
        String str2 = str1.replace("\n", ""); // 去回车
        ListData listData = new ListData(content_str, ListData.SEND, getTime());
        lists.add(listData);
        // 移除信息
        // if(lists.size()>10){
        // for(int i=0;i<lists.size();i++){
        // lists.remove(i);
        // }
        // }
        adapter.notifyDataSetChanged();
        String url = "http://www.tuling123.com/openapi/api?key=d2fe26a0a8343289fe50a8120f43c461&info="
                + str2;
        httpData = (HttpData) new HttpData(url, this).execute();
    }

    // 获取时间
    private String getTime() {
        currentTime = System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        Date curDate = new Date();
        String str = format.format(curDate);
        if (currentTime - oldTime >= 5 * 60 * 1000) {
            oldTime = currentTime;
            return str;
        } else {
            return "";
        }

    }
}

