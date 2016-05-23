package com.huanchi.lightpay;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.baidu.mapapi.SDKInitializer;
import com.huanchi.lightpay.config.CacheFileConfig;
import com.huanchi.lightpay.engine.AppEngine;
import com.huanchi.lightpay.network.CookieManager;
import com.huanchi.lightpay.service.LocationService;
import com.huanchi.lightpay.util.ApplicationUtil;
import com.huanchi.lightpay.util.ImageLoaderUtils;
import com.huanchi.lightpay.util.Utils;
import com.lidroid.xutils.http.RequestParams;

import java.io.File;


public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ApplicationUtil.init(this);
        CookieManager.init(this);
    }
}
