package com.zeba.update;

import android.app.Activity;
import android.content.Context;

import java.io.File;

public class AppInstaller {

    private String resPath;
    private File saveDir;
    private Context context;
    private AppInstallListener installListener;
    private boolean installPermission=true;

    public AppInstaller(Context context){
        this.context=context;
        saveDir=UpdateUtil.getSaveDir(context);
    }

    public AppInstaller checkPermission(Activity activity){
        if(!UpdateUtil.checkInstallPermission(activity)){
            installPermission=false;
            UpdateUtil.openInstallPermission(activity);
        }
        return this;
    }

    public AppInstaller uri(String resPath){
        this.resPath=resPath;
        return this;
    }

    public AppInstaller listener(AppInstallListener listener){
        this.installListener=listener;
        return this;
    }

    private void download(){
        DownThread downThread=new DownThread().url(resPath).dir(saveDir);
        boolean isStart= downThread.download(new DownThread.DownListener() {
            @Override
            public void progress(int p) {
            }
            @Override
            public void success(File file) {
                install(file);
            }
            @Override
            public void error(String msg) {
                onResult(new RuntimeException(msg));
            }
        });
        if(!isStart){
            onResult(new RuntimeException("安装失败：下载失败"));
        }
    }

    private void install(File file){
        try{
            if(UpdateUtil.checkInstallPermission(context)){
                UpdateUtil.installAPK(context,file);
                onResult(null);
            }else{
                onResult(new RuntimeException(""));
            }
        }catch (Exception e){
            e.printStackTrace();
            onResult(e);
        }
    }

    private void onResult(Exception e){
        if(installListener!=null){
            installListener.onResult(e);
        }
        context=null;
        installListener=null;
    }

    public void start(){
        if(!installPermission){
            return;
        }
        if(resPath.startsWith("http://")||resPath.startsWith("https://")){
            download();
        }else{
            install(new File(resPath));
        }
    }

}
