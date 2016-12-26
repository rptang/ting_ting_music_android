package com.music.rptang.tingtingmusic.vo;

/**
 * Created by rptang on 2016/4/22.
 */
public class NetMp3Info {

    private String musicName;
    private String url;
    private String artist;
    private String album;

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    @Override
    public String toString() {
        return "NetMp3Info{" +
                "musicName='" + musicName + '\'' +
                ", url='" + url + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                '}';
    }
}
