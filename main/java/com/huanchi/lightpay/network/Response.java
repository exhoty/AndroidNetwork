package com.huanchi.lightpay.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import okhttp3.*;

/**
 * Created by odin on 5/19/16.
 */
public class Response {

    private okhttp3.Response response;

    private Response(okhttp3.Response response) {
        this.response = response;
    }

    static public Response build(okhttp3.Response response){
        return  new Response(response);
    }


    public String url() {
        return this.response.request().url().toString();
    }

    public String method() {
        return this.response.request().method();
    }

    public int getStatus() {
        return this.response.code();
    }

    public String getStatusMessage() {
        return this.response.message();
    }

    private void setConcretReponse(okhttp3.Response reponse) {
        this.response = reponse;
    }

    public <T> PayloadWrap<T> getJson(Class<T> clazz) {
        String str = getString();
        if (str == null || str.equals("")) {
            return PayloadWrap.ERROR;
        }
        JSONObject result;
        try {
            result = new JSONObject(str);
            PayloadWrap wrapper = new PayloadWrap<>();
            int code = result.getInt("code");

            wrapper.setCode(code);
            if(code == 0){
                Gson gson = new Gson();
                T payload = gson.fromJson(result.getString("payload"), clazz);
                wrapper.setPayload(payload);
            }else{
                String message = result.getString("message");
                wrapper.setMessage(message);
            }

            return wrapper;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return PayloadWrap.ERROR;
    }

    private ResponseBody getBody(){
        return this.response.body();
    }

    public String getString() {
        try {
            String body = getBody().string();
            return body;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getHeader(String key) {
        String header = this.response.header(key);
        if (header == null) {
            return "";
        }
        return header;
    }

    public Bitmap getImage() {
        InputStream in = getBody().byteStream();
        Bitmap bitmap = BitmapFactory.decodeStream(in);
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
        }

        return bitmap;
    }

    public InputStream getInputStream() {
        return this.response.body().byteStream();
    }

    public long getContentLength() {
        return response.body().contentLength();
    }

    public String getContentType() {
        MediaType type = response.body().contentType();
        return type.type() + "/" + type.subtype();
    }


    public Map<String, List<String>> getHeaders() {
        Headers headers = this.response.headers();
        return headers.toMultimap();
    }


    public static class PayloadWrap<T>{
        Integer code;
        String message;
        T data;

        public PayloadWrap(){

        }
        public PayloadWrap(int code, String message){
            this.code = code;
            this.message = message;
        }

        public static final PayloadWrap ERROR;
        static {
            ERROR = new PayloadWrap<>(Integer.MAX_VALUE, "PARSE_JSON_FAIL");
        }

        public boolean ok(){
            return this.getCode() == 0;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public T getPayload() {
            return data;
        }

        public void setPayload(T payload) {
            this.data = payload;
        }
    }
}

