package com.huanchi.lightpay.network.ssl;

import com.huanchi.lightpay.config.UrlConfig;

import java.net.URI;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * Created by odin on 5/22/16.
 */
public class TrustHostnameVerifier implements HostnameVerifier{

    private static String hostname;

    private static HostnameVerifier hv;
    static {
        hostname = URI.create(UrlConfig.ServerUrl).getHost();
        hv = HttpsURLConnection.getDefaultHostnameVerifier();
    }

    @Override
    public boolean verify(String hostname, SSLSession session) {
        if (hostname.equals(this.hostname)){
            return true;
        }
        return hv.verify(hostname, session);
    }
}
