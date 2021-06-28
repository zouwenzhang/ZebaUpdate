package com.zeba.update;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
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
        setLastAppCode(context);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
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

    public static void installAPK(Context context,File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
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

    public static void requestInstallPermission(Activity activity){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            activity.requestPermissions(
                    new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES},1001);
        }
    }

    public static boolean checkInstallPermission(Context context){
        if(!isNeedCheckPermission(context)){
            return true;
        }
        boolean haveInstallPermission;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //先判断是否有安装未知来源应用的权限
            haveInstallPermission = context.getPackageManager().canRequestPackageInstalls();
            if(!haveInstallPermission){
                return false;
            }
        }
        return true;
    }

    public static void openInstallPermission(Activity context){
        Uri packageURI = Uri.parse("package:"+context.getPackageName());
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,packageURI);
        context.startActivityForResult(intent, 1000);
    }

    public static boolean isNeedCheckPermission(Context context){
        SharedPreferences sf=context.getSharedPreferences("zeba_update",Context.MODE_PRIVATE);
        int last= sf.getInt("lastCode",-1);
        if(last==getAppCode(context)){
            return true;
        }
        return false;
    }

    public static boolean isNoHint(Context context){
        SharedPreferences sf=context.getSharedPreferences("zeba_update",Context.MODE_PRIVATE);
        int last= sf.getInt("noHint",-1);
        if(last==1){
            return true;
        }
        return false;
    }

    public static void setLastAppCode(Context context){
        SharedPreferences sf=context.getSharedPreferences("zeba_update",Context.MODE_PRIVATE);
        sf.edit().putInt("lastCode",getAppCode(context)).commit();
    }

    public static void setNoHint(Context context){
        SharedPreferences sf=context.getSharedPreferences("zeba_update",Context.MODE_PRIVATE);
        sf.edit().putInt("noHint",1).commit();
    }

    public static int getAppCode(Context context){
        try{
            // 获取packagemanager的实例
            PackageManager packageManager = context.getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(),0);
            return packInfo.versionCode;
        }catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }
}
