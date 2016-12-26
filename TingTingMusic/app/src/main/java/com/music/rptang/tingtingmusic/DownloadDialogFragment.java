package com.music.rptang.tingtingmusic;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import com.music.rptang.tingtingmusic.utils.Content;
import com.music.rptang.tingtingmusic.utils.DownloadUtils;
import com.music.rptang.tingtingmusic.vo.NetMp3Info;

import java.io.File;

/**
 * Created by rptang on 2016/5/13.
 */
public class DownloadDialogFragment extends DialogFragment {

    private NetMp3Info netMp3Info; //传入当前下载歌曲对象
    private MainActivity mainActivity;

    public static DownloadDialogFragment newInstance(NetMp3Info netMp3Info){

        DownloadDialogFragment downloadDialogFragment = new DownloadDialogFragment();
        downloadDialogFragment.netMp3Info = netMp3Info;
        return downloadDialogFragment;
    }

    private String[] items;

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
        mainActivity = (MainActivity) getActivity();
        items = new String[]{getString(R.string.download),getString(R.string.cancel)};
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setCancelable(true);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                switch (which) {
                    case 0:
                        downloadMusic();//执行下载音乐
                        break;
                    case 1:
                        dialogInterface.dismiss();//取消下载
                        break;
                }
            }
        });
        return builder.show();
    }
//    //监听下载成功
//    public interface DownloadSuccessListener{
//        void downloadSuccessListener(String isDownloadSuccess);//回传一个字符串
//    }


    private void downloadMusic(){

        Toast.makeText(mainActivity,"正在下载"+netMp3Info.getMusicName(),Toast.LENGTH_SHORT).show();
        DownloadUtils.getsInstance().setListener(new DownloadUtils.OnDownloadListener(){
            @Override
            public void onDownload(String mp3Url) {
                Toast.makeText(mainActivity,"歌曲下载成功",Toast.LENGTH_SHORT).show();
                //扫描新下载的歌曲
                Uri contentUri = Uri.fromFile(new File(mp3Url));
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,contentUri);
                mainActivity.sendBroadcast(mediaScanIntent);
                netMp3Info.setArtist(netMp3Info.getArtist());

//                scanFile(mainActivity, Environment.getExternalStorageDirectory()+ Content.DIR_MUSIC+"/"+mp3Url);
//                DownloadSuccessListener listener =  mainActivity;
//                listener.downloadSuccessListener(mp3Url);

            }

            @Override
            public void onFailed(String error) {
                Toast.makeText(mainActivity,error,Toast.LENGTH_LONG).show();
            }
        }).download(netMp3Info);
    }

    /**
     * 通知媒体库更新
     */
    public void scanFile(Context context,String filePath){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(new File(filePath)));
        context.sendBroadcast(mediaScanIntent);
    }
}
