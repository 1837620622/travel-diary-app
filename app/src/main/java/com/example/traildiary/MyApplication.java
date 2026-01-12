package com.example.traildiary;

import android.app.Application;
import com.jakewharton.threetenabp.AndroidThreeTen;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化 ThreeTenABP
        AndroidThreeTen.init(this);
    }
}