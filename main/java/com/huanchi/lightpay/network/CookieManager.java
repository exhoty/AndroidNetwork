package com.huanchi.lightpay.network;

import android.content.Context;

import java.net.HttpCookie;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by odin on 5/19/16.
 */
public class CookieManager extends java.net.CookieManager {

    private static CookieManager instance;

    //call init when application init
    public static void init(Context context) {
        synchronized (CookieManager.class) {
            if (instance == null) {
                CookieStore store = new CookieStore(context);
                instance = new CookieManager(store);
            }
        }
    }

    public static CookieManager instance() {
        return instance;
    }


    public CookieManager(CookieStore store) {
        super(store, null);
    }

    public Map<String, List<String>> get(URI uri){
        List<HttpCookie> cookies = getCookieStore().get(uri);

        if (cookies.isEmpty()) {
            return Collections.emptyMap();
        }

        StringBuilder result = new StringBuilder();

        result.append(cookieToStr(cookies.get(0)));
        for (int i = 1; i < cookies.size(); i++) {
            result.append("; ").append(cookieToStr(cookies.get(i)));
        }

        return Collections.singletonMap("Cookie", Collections.singletonList(result.toString()));
    }

    //剔除cookie出现的引号
    private String cookieToStr(HttpCookie cookie){
        return cookie.toString().replaceAll("\"", "");
    }
}
