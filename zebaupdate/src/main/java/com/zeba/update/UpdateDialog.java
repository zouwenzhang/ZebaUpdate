package com.zeba.update;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Environment;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;

public class UpdateDialog  extends Dialog {

    private TextView tvTitle;
    private TextView tvContent;
    private TextView tvCancel;
    private TextView tvOk;
    private TextView tvNoHint;
    private NumberProgressBar progressBar;
    private String url;
    private String fileMD5;
    private File saveDir;
    private File apkFile;
    private DownThread downThread;
    private WeakReference<Activity> refActivity;
    private Boolean isAllowInstall=null;
    private String sTitle="发现新版本";
    private String sContent="";
    private String sOk="立即更新";
    private boolean isForceUpdate=false;
    private boolean isAutoCheck=true;

    public UpdateDialog(Activity context) {
        super(context,R.style.UpdateDialog);
        initView(context);
        initListener();
        initAttr();
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        saveDir=UpdateUtil.getSaveDir(context);
        refActivity=new WeakReference<>(context);
    }

    private void initView(Context context){
        View view=View.inflate(context,R.layout.dialog_update_zeba,null);
        setContentView(view);
        tvTitle=view.findViewById(R.id.zeba_dialog_tv_update_title);
        tvContent=view.findViewById(R.id.zeba_dialog_tv_update_content);
        tvCancel=view.findViewById(R.id.zeba_dialog_tv_update_cancel);
        tvOk=view.findViewById(R.id.zeba_dialog_tv_update_ok);
        tvNoHint=view.findViewById(R.id.zeba_update_dialog_tv_no_hint);
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
                if(!checkPermission()){
                    return;
                }
                if(apkFile!=null){
                    UpdateUtil.installAPP(getContext(),apkFile);
                }else{
                    download();
                }
            }
        });
        tvNoHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(downThread!=null){
                    downThread.cancel();
                }
                UpdateUtil.setNoHint(refActivity.get());
                dismiss();
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
        sTitle=title;
        tvTitle.setText(title);
        return this;
    }

    public UpdateDialog content(String content){
        sContent=content;
        tvContent.setText(content);
        return this;
    }

    public UpdateDialog cancelText(String text){
        tvCancel.setText(text);
        return this;
    }

    public UpdateDialog autoCheck(boolean isShow){
        isAutoCheck=isShow;
        tvNoHint.setVisibility(isShow?View.VISIBLE:View.GONE);
        return this;
    }

    public UpdateDialog okText(String text){
        sOk=text;
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
        isForceUpdate=force;
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
        if(!isForceUpdate&&isAutoCheck){
            if(UpdateUtil.isNoHint(refActivity.get())){
                return;
            }
        }
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

    private boolean checkPermission(){
        if(isAllowInstall!=null&&!isAllowInstall){
            tvTitle.setText(sTitle);
            tvContent.setText(sContent);
            tvOk.setText(sOk);
            UpdateUtil.openInstallPermission(refActivity.get());
            isAllowInstall=null;
            return false;
        }
        isAllowInstall=UpdateUtil.checkInstallPermission(refActivity.get());
        if(!isAllowInstall){
            tvTitle.setText("提示");
            tvContent.setText("应用自动更新权限已关闭，需要手动开启");
            tvOk.setText("去开启");
        }
        return isAllowInstall;
    }

}
