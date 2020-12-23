package com.xuqiqiang.uikit.requester.demo;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by xuqiqiang on 2020/02/28.
 */
public class CustomApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {//1
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }
}
