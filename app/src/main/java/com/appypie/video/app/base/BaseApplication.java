package com.appypie.video.app.base;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;


import com.appypie.video.app.di.modules.ApplicationContextModule;
import com.appypie.video.app.di.modules.DaggerMyApplicationComponent;
import com.appypie.video.app.di.modules.ApplicationContextModule;
import com.appypie.video.app.di.modules.DaggerMyApplicationComponent;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

public class BaseApplication extends Application implements HasAndroidInjector {

    @Inject
    DispatchingAndroidInjector<Object> dispatchingAndroidInjector;

    public static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();

        appContext=this;

        DaggerMyApplicationComponent
                .builder()
                .applicationContextModule(new ApplicationContextModule(this))
                .build()
                .inject(this);

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    @Override
    public AndroidInjector<Object> androidInjector() {
        return dispatchingAndroidInjector;
    }
}



