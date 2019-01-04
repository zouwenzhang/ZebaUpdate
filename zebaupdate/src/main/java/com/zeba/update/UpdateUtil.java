package com.zeba.update;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;

import java.io.File;

public class UpdateUtil {

    public static File getSaveDir(Context context){
        File saveDir=context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        saveDir=new File(saveDir,"zebaUpdate");
        if(!saveDir.exists()||!saveDir.isDirectory()){
            saveDir.mkdirs();
        }
        return saveDir;
    }

    public static void installAPP(Context context,File file) {
        if(file==null){
            return;
        }
        if(!file.getAbsolutePath().endsWith(".apk")
                &&!file.getAbsolutePath().endsWith(".APK")){
            return ;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(
                    context
                    , context.getPackageName()+".fileprovider"
                    , file);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
    }
}
