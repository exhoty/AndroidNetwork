package com.huanchi.lightpay.network;

import com.huanchi.lightpay.network.ssl.DefaultSSLSocketFactory;
import com.huanchi.lightpay.network.ssl.TrustHostnameVerifier;
import com.huanchi.lightpay.util.ApplicationUtil;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

/**
 * Created by odin on 5/19/16.
 */
public class Network {

    static private Network holder;
    static public Network instance(){
        synchronized (Network.class){
            if (holder == null){
                holder = new Network();
            }
        }
        return holder;
    }

    private okhttp3.OkHttpClient clientTemplate;

    private Network() {
        //创建原始clinet模型
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.followRedirects(false);
        builder.cache(new Cache(new File(ApplicationUtil.getCachePath()), 20 * 1024 * 1024)); //20M 缓存文件
        builder.readTimeout(5, TimeUnit.SECONDS);
        builder.sslSocketFactory(DefaultSSLSocketFactory.getDefault());
        builder.hostnameVerifier(new TrustHostnameVerifier());

        clientTemplate = builder.build();
    }

    public Request request(String url){
        return configRequest(new Request(url));
    }

    protected Request configRequest(Request request){
        request.setClient(clientTemplate);
        request.setHeader("User-Agent", ApplicationUtil.getUserAgent());
        request.setCookieManager(CookieManager.instance());
        return request;
    }
}
