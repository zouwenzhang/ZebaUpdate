package com.zeba.update;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Environment;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.File;

public class UpdateDialog  extends Dialog {

    private TextView tvTitle;
    private TextView tvContent;
    private TextView tvCancel;
    private TextView tvOk;
    private NumberProgressBar progressBar;
    private String url;
    private String fileMD5;
    private File saveDir;
    private File apkFile;
    private DownThread downThread;

    public UpdateDialog(Context context) {
        super(context,R.style.UpdateDialog);
        initView(context);
        initListener();
        initAttr();
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        saveDir=UpdateUtil.getSaveDir(context);
    }

    private void initView(Context context){
        View view=View.inflate(context,R.layout.dialog_update_zeba,null);
        setContentView(view);
        tvTitle=view.findViewById(R.id.zeba_dialog_tv_update_title);
        tvContent=view.findViewById(R.id.zeba_dialog_tv_update_content);
        tvCancel=view.findViewById(R.id.zeba_dialog_tv_update_cancel);
        tvOk=view.findViewById(R.id.zeba_dialog_tv_update_ok);
        progressBar=view.findViewById(R.id.zeba_dialog_number_progress_bar);
        progressBar.setProgress(0);
    }

    private void initListener(){
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(downThread!=null){
                    downThread.cancel();
                }
                dismiss();
            }
        });
        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(apkFile!=null){
                    UpdateUtil.installAPP(getContext(),apkFile);
                }else{
                    download();
                }
            }
        });
    }

    private void initAttr(){
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        Point point = new Point();
        display.getSize(point);
        p.width = point.x;
        p.gravity = Gravity.CENTER;
        getWindow().setAttributes(p);
    }

    private void download(){
        if(downThread!=null){
            return;
        }
        tvTitle.setText("正在下载中");
        tvOk.setVisibility(View.GONE);
        downThread=new DownThread().md5(fileMD5).url(url).dir(saveDir);
        boolean isStart= downThread.download(new DownThread.DownListener() {
            @Override
            public void progress(int p) {
                progressBar.setProgress(p);
            }
            @Override
            public void success(File file) {
                UpdateUtil.installAPP(getContext(),file);
                dismiss();
            }
            @Override
            public void error(String msg) {
                tvTitle.setText("更新失败");
                progressBar.setVisibility(View.GONE);
                progressBar.setProgress(0);
                tvContent.setText(msg);
                tvContent.setGravity(Gravity.CENTER_HORIZONTAL);
                tvContent.setVisibility(View.VISIBLE);
                tvOk.setVisibility(View.VISIBLE);
                tvOk.setText("重新下载");
                tvCancel.setVisibility(View.VISIBLE);
                downThread=null;
            }
        });
        if(isStart){
            tvContent.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public UpdateDialog title(String title){
        tvTitle.setText(title);
        return this;
    }

    public UpdateDialog content(String content){
        tvContent.setText(content);
        return this;
    }

    public UpdateDialog cancelText(String text){
        tvCancel.setText(text);
        return this;
    }

    public UpdateDialog okText(String text){
        tvOk.setText(text);
        return this;
    }

    public UpdateDialog themeColor(int color){
        tvOk.setTextColor(color);
        progressBar.setProgressTextColor(color);
        progressBar.setReachedBarColor(color);
        return this;
    }

    public UpdateDialog isForce(boolean force){
        if(force){
            tvCancel.setVisibility(View.GONE);
        }else{
            tvCancel.setVisibility(View.VISIBLE);
        }
        return this;
    }

    public UpdateDialog url(String url){
        this.url=url;
        return this;
    }

    public UpdateDialog md5(String md5){
        fileMD5=md5;
        return this;
    }

    @Override
    public void show() {
        if(fileMD5!=null&&!"".equals(fileMD5)){
            apkFile=new File(saveDir,fileMD5+".apk");
            if(apkFile.exists()&&apkFile.isFile()){
                tvOk.setText("立即安装");
            }else{
                apkFile=null;
            }
        }
        super.show();
    }
}
