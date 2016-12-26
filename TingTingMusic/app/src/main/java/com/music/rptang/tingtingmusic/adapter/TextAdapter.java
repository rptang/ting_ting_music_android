package com.music.rptang.tingtingmusic.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.music.rptang.tingtingmusic.R;
import com.music.rptang.tingtingmusic.vo.ListData;

public class TextAdapter extends BaseAdapter {

	private List<ListData> lists;
	private Context context;
	private RelativeLayout layout;

	public TextAdapter(Context context, List<ListData> lists) {
		this.context = context;
		this.lists = lists;
	}

	@Override
	public int getCount() {
		return lists.size();
	}

	@Override
	public Object getItem(int position) {
		return lists.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		if (lists.get(position).getFlag() == ListData.SEND) {
			layout = (RelativeLayout) inflater.inflate(R.layout.right_item,
					null);
		}
		if (lists.get(position).getFlag() == ListData.RECEIVE) {
			layout = (RelativeLayout) inflater
					.inflate(R.layout.left_item, null);
		}
		TextView tv = (TextView) layout.findViewById(R.id.tv);
		TextView time = (TextView) layout.findViewById(R.id.time);
		tv.setText(lists.get(position).getContent());
		time.setText(lists.get(position).getTime());

		return layout;
	}

}
