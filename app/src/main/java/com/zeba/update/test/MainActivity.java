package com.zeba.update.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.zeba.update.ZebaUpdate;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.bt_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZebaUpdate.createDialog(MainActivity.this)
                        .url("your apk download url")
                        .content("update content")
                        .isForce(false)
                        .themeColor(getResources()
                                .getColor(R.color.colorPrimary))
                        .show();
            }
        });
    }
}
