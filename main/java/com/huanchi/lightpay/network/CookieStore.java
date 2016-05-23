package com.huanchi.lightpay.network;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by odin on 5/19/16.
 */
public class CookieStore implements java.net.CookieStore{

    private Context context;
    private String cookieFileName = "cookie";
    private Map<String, List<HttpCookie>> cookies = new HashMap<>();

    public CookieStore(Context context){
        this.context = context;
        load();
    }

    public void persistent(){
        SharedPreferences setting = context.getSharedPreferences(cookieFileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = setting.edit();
        editor.clear();
        for(String host : cookies.keySet()){
            List<HttpCookie> cookie = cookies.get(host);
            Set<String> set = new HashSet<>();
            for (HttpCookie c: cookie) {
                set.add(c.toString());
            }

            if (!set.isEmpty()){
                editor.putStringSet(host, set);
            }
        }
        editor.commit();
    }


    private void load(){
        SharedPreferences setting = context.getSharedPreferences(cookieFileName, Context.MODE_PRIVATE);
        Map<String, Set<String>> all = (Map<String, Set<String>>) setting.getAll();
        for (String host : all.keySet()){
            URI uri = URI.create("http://" + host);

            Set<String> v = all.get(host);
            Iterator<String> it = v.iterator();
            while(it.hasNext()){
                String cookieStr = it.next();
                String[] kv = cookieStr.split("=");
                if (kv.length == 2){
                    addInCache(uri, new HttpCookie(kv[0], kv[1].trim()));
                }
            }
        }
    }

    private void addInCache(URI uri, HttpCookie cookie){
        List<HttpCookie> hostCookie = cookies.get(uri.getHost());
        if (hostCookie == null){
            hostCookie = new LinkedList<>();
            cookies.put(uri.getHost(), hostCookie);
        }

        boolean find = false;
        for (HttpCookie c : hostCookie){
            if (c.equals(cookie)){
                find = true;
            }
        }
        if (!find){
            hostCookie.add(cookie);
        }
    }

    @Override
    public void add(URI uri, HttpCookie cookie) {
        List<HttpCookie> hostCookie = cookies.get(uri.getHost());
        if (hostCookie == null){
            hostCookie = new LinkedList<>();
            cookies.put(uri.getHost(), hostCookie);
        }

        boolean exist = false;
        String name = cookie.getName();
        String value = cookie.getValue();
        HttpCookie remove = null;
        for (HttpCookie c : hostCookie){
            if (c.getName().equals(name)){
                if (c.getValue().replaceAll("\"", "").equals(value)){
                    exist = true;
                }else{
                    remove = c;
                }

                break;
            }
        }
        if (!exist){
            if (remove != null){
                hostCookie.remove(remove);
            }
            hostCookie.add(cookie);
            persistent();
        }
    }

    @Override
    public List<HttpCookie> get(URI uri) {
        List<HttpCookie> ret = cookies.get(uri.getHost());
        return  ret == null ? Collections.EMPTY_LIST : ret;
    }

    @Override
    public List<HttpCookie> getCookies() {
        return null;
    }

    @Override
    public List<URI> getURIs() {
        Iterator<String> it = cookies.keySet().iterator();
        List<URI> list = new ArrayList<>();
        while(it.hasNext()){
            list.add(URI.create("http://" + it.next()));
        }
        return list;
    }

    @Override
    public boolean remove(URI uri, HttpCookie cookie) {
        cookies.remove(cookie.getName());
        return false;
    }

    @Override
    public boolean removeAll() {
        cookies.clear();
        return true;
    }

    //剔除cookie出现的引号
    private String cookieToStr(HttpCookie cookie){
        return cookie.toString().replaceAll("\"", "");
    }
}
