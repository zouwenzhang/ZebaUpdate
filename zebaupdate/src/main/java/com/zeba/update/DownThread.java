package com.zeba.update;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownThread extends Thread {

    private boolean isRun=true;
    private String mUrl;
    private File saveDir;
    private String fileMD5;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public DownThread url(String url){
        mUrl=url;
        return this;
    }

    public DownThread md5(String md5){
        fileMD5=md5;
        return this;
    }

    public DownThread dir(File dir){
        saveDir=dir;
        return this;
    }

    public boolean download(DownListener listener){
        if(mUrl==null||saveDir==null){
            return false;
        }
        downListener=listener;
        start();
        return true;
    }

    public void cancel(){
        isRun=false;
        downListener=null;
    }

    @Override
    public void run() {
        boolean isSuccess=false;
        try {
            String fileName= Base64.encodeToString(mUrl.getBytes(),Base64.DEFAULT);
            File saveFile=new File(saveDir.getAbsolutePath()+"/"+fileName+".apk");
            if(saveFile.exists()&&saveFile.isFile()){
                saveFile.delete();
            }
            URL url = new URL(mUrl);
            HttpURLConnection conn = (HttpURLConnection) url
                    .openConnection();
            conn.setConnectTimeout(5 * 1000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty(
                    "Accept",
                    "image/gif, image/jpeg, image/pjpeg, image/pjpeg," +
                            " application/x-shockwave-flash, application/xaml+xml," +
                            " application/vnd.ms-xpsdocument, application/x-ms-xbap," +
                            " application/x-ms-application, application/vnd.ms-excel," +
                            " application/vnd.ms-powerpoint, application/msword, */*");
            conn.setRequestProperty("Accept-Language", "zh-CN");
            conn.setRequestProperty("Charset", "UTF-8");
            // int startPos =0;
            // int endPos = conn.getContentLength();
            // conn.setRequestProperty("Range", "bytes=" + startPos +
            // "-"+ endPos);
            conn.setRequestProperty(
                    "User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2;" +
                            " Trident/4.0; .NET CLR 1.1.4322;" +
                            " .NET CLR 2.0.50727; .NET CLR 3.0.04506.30;" +
                            " .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.connect();
            int length = conn.getContentLength();
            InputStream is = conn.getInputStream();
            saveFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(saveFile);
            int count = 0;
            byte buf[] = new byte[1024*1024];
            int lastp=0;
            do {
                int numread = is.read(buf, 0, 1024*1024);
                count += numread;
                final int progress = (int) (((float) count / length) * 100);
                if(lastp!=progress){
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(downListener!=null){
                                downListener.progress(progress);
                            }
                        }
                    });
                    lastp=progress;
                }
                if (numread <= 0) {
                    final String downMD5=FileMD5Util.getFileMD5(saveFile);
                    final File nf=new File(saveFile.getParentFile(),downMD5+".apk");
                    saveFile.renameTo(nf);
                    isSuccess=true;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(downListener!=null){
                                downListener.progress(100);
                            }
                            if(fileMD5!=null&&!"".equals(fileMD5)){
                                if(!fileMD5.equals(downMD5)){
                                    if(downListener!=null){
                                        downListener.error("下载失败，文件签名错误");
                                    }
                                    return;
                                }
                            }
                            if(downListener!=null){
                                downListener.success(nf);
                            }
                        }
                    });
                    break;
                }
                fos.write(buf, 0, numread);
            } while (isRun);
            fos.flush();
            fos.close();
            is.close();
            conn.disconnect();
            if(!isSuccess){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(downListener!=null){
                            if(isRun){
                                downListener.error("下载失败，连接错误");
                            }else{
                                downListener.error("下载已取消");
                            }
                        }
                    }
                });

            }
        } catch (final Exception e) {
            e.printStackTrace();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(downListener!=null){
                        downListener.error("下载失败，"+e.getMessage());
                    }
                }
            });
        }
    }

    private DownListener downListener;

    public interface DownListener{
        void progress(int p);
        void success(File file);
        void error(String msg);
    }
}
