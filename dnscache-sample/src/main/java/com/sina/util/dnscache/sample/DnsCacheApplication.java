package com.sina.util.dnscache.sample;


import android.app.Application;
import android.content.Context;

import com.sina.util.dnscache.DNSCache;
import com.sina.util.dnscache.sample.tasksetting.SpfConfig;

public class DnsCacheApplication extends Application{
    public static Context mGlobalInstance;
    @Override
    public void onCreate() {
        super.onCreate();
        mGlobalInstance = this;
        SpfConfig.init(this.getApplicationContext());
        DNSCache.Init(this);
        DNSCache.getInstance().preLoadDomains(new String[]{"api.weibo.cn","api.camera.weibo.com"});
    }
}
