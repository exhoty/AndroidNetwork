package com.huanchi.lightpay.network;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;


/**
 * Created by odin on 5/19/16.
 */
public class Request extends AsyncTask<Void, Integer, Response>{

    enum HTTP_METHOD {
        GET,
        POST,
        DELETE,
        PUT,
        PATCH,
        OPTION,
        HEAD
    }

    private RequestHandler handler = null;
    private HTTP_METHOD method;
    private String url;
    private Map<String, String> header;
    private RequestBody body;
    private Call syncCall;
    private CookieManager cookieManager;
    private int timeout;
    private boolean canceled = false;
    private OkHttpClient clientTemplate; //client模本，其他build出的client都使用一套pool缓存资源
    private boolean cache;

    public Request(String url){
        this.url = url.toLowerCase();
        this.body = new RequestBody();
        this.header = new HashMap<>();
        this.timeout = 5; //default 5 seconds
        this.canceled = false;
        this.cache = false; //默认关闭网络缓存
    }

    public void setCookieManager(CookieManager cookieManager) {
        this.cookieManager = cookieManager;
    }

    /**
     * 设置HTTP body编码方式
     * @param encoding
     */
    public void setBodyEncoding(RequestBody.ENCODING encoding){
        this.body.setEncoding(encoding);
    }

    /**
     * 添加header
     * @param key
     * @param value
     */
    public void setHeader(String key, String value){
        this.header.put(key, value);
    }

    /**
     * 添加表单域
     * @param field
     * @param value
     */
    public void addForm(String field, String value){
        this.body.addField(field, value);
    }
    public void addForm(String field, File file){
        this.body.addField(field, file);
    }
    public void addForm(byte[] rawData){
        this.body.setRawData(rawData);
    }

    private RequestBody getBody(){
        if (this.body == null){
            this.body = new RequestBody();
        }

        return this.body;
    }


    public void setClient(OkHttpClient client){
        this.clientTemplate = client;
    }

    private OkHttpClient client(){
        //模板生成新的client，新client和模板使用相同的连接池
        OkHttpClient.Builder clientBuilder = this.clientTemplate.newBuilder();
        if (!this.cache){
            clientBuilder.cache(null);
        }
        clientBuilder.readTimeout(this.timeout, TimeUnit.SECONDS);
        OkHttpClient client = clientBuilder.build();
        return client;
    }

    private void buildBody(okhttp3.Request.Builder builder) {
        if (this.method == HTTP_METHOD.POST){
            builder.post(this.body.build());
        }else if (this.method == HTTP_METHOD.PUT){
            builder.put(this.body.build());
        }else if (this.method == HTTP_METHOD.DELETE){
            builder.delete(this.body.build());
        }else if (this.method == HTTP_METHOD.PATCH){
            builder.patch(this.body.build());
        }
    }

    private okhttp3.Request.Builder buildHeader(okhttp3.Request.Builder builder){
        Iterator<Map.Entry<String, String>> it = this.header.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String, String> kv = it.next();
            builder.addHeader(kv.getKey(), kv.getValue());
        }

        Map<String, List<String>> cookies = cookieManager.get(URI.create(this.url));
        List<String> list = cookies.get("Cookie");
        if (list != null && list.size() == 1){
            String cookie = list.get(0);
            builder.addHeader("Cookie", cookie);
        }

        return builder;
    }

    /**
     * 取消请求
     */
    public void cancel(){
        canceled = true;
        if (this.syncCall != null){
            this.syncCall.cancel();
        }
    }

    /**
     * 设置读取超时
     * @param seconds
     */
    public Request timeout(int seconds){
        this.timeout = seconds;
        return this;
    }

    /**
     * 启用缓存
     */
    public Request cache(){
        this.cache = true;
        return this;
    }

    /**
     * 自动转为https
     */
    public Request secure(){
        if (this.url.startsWith("http://")){
            this.url = this.url.replaceFirst("http://", "https://");
        }
        return this;
    }

    @Override
    protected Response doInBackground(Void... params) {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();

        buildHeader(builder);
        if (this.method == HTTP_METHOD.POST
                || this.method == HTTP_METHOD.DELETE
                || this.method == HTTP_METHOD.PUT) {
            buildBody(builder);
        }
        builder.url(this.url);
        okhttp3.Request request = builder.build();

        Response rspWrapper = null;
        try {
            this.syncCall = client().newCall(request);
            if (this.canceled){
                this.syncCall.cancel();
            }
            okhttp3.Response response = this.syncCall.execute();
            rspWrapper = Response.build(response);

            if (rspWrapper.getStatus() == 200){
                this.cookieManager.put(URI.create(this.url), rspWrapper.getHeaders());
                this.handler.onSucc(rspWrapper);
            }else{
                this.handler.onFail(rspWrapper);
            }
        } catch (Exception e) {
            this.handler.setException(e);
        }

        return rspWrapper;
    }


    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        this.handler.onProceed(values);
    }

    @Override
    protected void onPostExecute(Response response) {
        super.onPostExecute(response);
        this.handler.onUpdate(response);
    }


    public void get(RequestHandler handler){
        this.method = HTTP_METHOD.GET;
        this.handler = handler;
        execute();
    }

    public void post(RequestHandler handler){
        this.method = HTTP_METHOD.POST;
        this.handler = handler;
        execute();
    }

    public void delete(RequestHandler handler){
        this.method = HTTP_METHOD.DELETE;
        this.handler = handler;
        execute();
    }

    public void put(RequestHandler handler){
        this.method = HTTP_METHOD.PUT;
        this.handler = handler;
        execute();
    }

    static abstract public class RequestHandler{
        public Exception exception;
        public void setException(Exception exception) {
            this.exception = exception;
        }
        public Exception getException() {
            return exception;
        }

        //处理status==200
        public void onSucc(Response rsp){
            Log.d("request:succ", String.valueOf(rsp.getStatus()) + " " + rsp.method() + " " + rsp.url()) ;
        }
        //处理status！=200
        public void onFail(Response rsp){
            Log.d("request:fail", String.valueOf(rsp.getStatus()) + " " + rsp.method() + " " + rsp.url()) ;
        }
        //处理网络异常
        public void onExcept(Exception e){
        }
        //处理界面更新
        public void onUpdate(Response rsp){
        }
        //处理网络进度
        public void onProceed(Integer... progress){
        }
    }
}
