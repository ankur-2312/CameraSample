package com.camerasample;

import android.app.Application;

public class MyApplication extends Application {

    static private MyApplication instance;

    static MyApplication getContext() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
