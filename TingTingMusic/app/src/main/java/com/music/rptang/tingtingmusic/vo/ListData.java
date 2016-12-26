package com.music.rptang.tingtingmusic.vo;

public class ListData {

	public static final int SEND = 1;
	public static final int RECEIVE = 2;
	private int flag;
	private String content;
	private String time;

	public ListData(String content, int flag, String time) {
		setContent(content);
		setFlag(flag);
		setTime(time);
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

}
