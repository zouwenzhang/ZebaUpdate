package com.zeba.update;

import android.app.Activity;
import android.content.Context;

import java.io.File;

public class ZebaUpdate {
    private static DownThread downThread;
    public static void autoDownload(Context context,String url,String md5){
        if(downThread!=null){
            return;
        }
        downThread=new DownThread()
                .url(url)
                .md5(md5)
                .dir(UpdateUtil.getSaveDir(context));
        boolean isStart= downThread.download(new DownThread.DownListener() {
            @Override
            public void progress(int p) {

            }
            @Override
            public void success(File file) {
                downThread=null;
            }
            @Override
            public void error(String msg) {
                downThread=null;
            }
        });
        if(!isStart){
            downThread=null;
        }
    }

    public static UpdateDialog createDialog(Activity context){
        return new UpdateDialog(context);
    }
}
