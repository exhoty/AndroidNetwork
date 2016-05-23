package com.huanchi.lightpay.network;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okio.BufferedSink;

/**
 * Created by odin on 5/19/16.
 */
public class RequestBody {

    public enum ENCODING {
        WWW_URL_ENCODED,
        MULTIPART,
        RAW,
    }

    private ENCODING encoding = ENCODING.WWW_URL_ENCODED;
    private byte[] rawData;

    public void setEncoding(ENCODING encoding){
        this.encoding = encoding;
    }

    private Map<String, Object> fields = new HashMap<String, Object>();

    public byte[] getRawData() {
        if (rawData == null){
            return new byte[0];
        }
        return rawData;
    }

    public void setRawData(byte[] rawData) {
        this.rawData = rawData;
    }


    public void addField(String field, Object fieldData){
        this.fields.put(field, fieldData);
    }

    public okhttp3.RequestBody build(){
        if (this.encoding == ENCODING.WWW_URL_ENCODED){
            FormBody.Builder builder = new FormBody.Builder();
            for(String key : fields.keySet()){
                Object field = fields.get(key);
                if (field instanceof String) {
                    builder.add(key, (String)field);
                }
            }

            return builder.build();
        }else if (this.encoding == ENCODING.MULTIPART){
            MultipartBody.Builder builder = new MultipartBody.Builder("lightpayformboundray").setType(MultipartBody.FORM);

            for(String key : fields.keySet()){
                Object field = fields.get(key);

                if (field instanceof String){
                    builder.addFormDataPart(key, (String)field);
                }else if (field instanceof File){
                    File file = (File)field;
                    if (!file.exists()) continue;
                    try {
                        String type = file.toURI().toURL().openConnection().getContentType();
                        okhttp3.RequestBody body = okhttp3.RequestBody.create(MediaType.parse(type), file);
                        builder.addFormDataPart(key, file.getName(), body);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return builder.build();
        }else if (this.encoding == ENCODING.RAW){
            return new okhttp3.RequestBody(){

                @Override
                public MediaType contentType() {
                    return MediaType.parse("application/octet-stream");
                }

                @Override
                public void writeTo(BufferedSink sink) throws IOException {
                    sink.write(getRawData());
                }
            };
        }

        return null;
    }



}
