package com.ponkan.banana;

import android.app.Application;

public class BananaApplication extends Application {

    private static Application mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static Application getApplication() {
        return mInstance;
    }
}