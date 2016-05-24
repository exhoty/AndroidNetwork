# AndroidNetwork
自家用封装网络库

依赖于
dependencies {
    compile 'com.google.code.gson:gson:2.6.2'
    compile 'com.squareup.okhttp3:okhttp:3.2.0'
}

支持谓词:GET/POST/DELETE/PUT

# 使用方法
Request req = Network.instance().request("http://www.baidu.com");

#GET
req.get(new Callback(){
    public void onSucc(Response rsp){
        //根据需求使用返回对象
        //子线程中禁止UI操作

        rsp.getString();
        rsp.getImage();
        rsp.getPayload(Entity.class);
    }
    
    public void onUpdate(Response rsp){
        //UI操作
    }
});


或者更高级的操作
req.get(new Callback(Entity.class){

    public void onPlayload(Entity entity){
        //可以直接执行UI操作
    }


    public void onImage(Bitmap image){

    }

    public void onString(String str){
    }
});


#POST
req.setBodyEncoding(RequestBody.ENCODING.WWW_URL_ENCODED);
req.addForm("name1", "some content");
req.addForm("name2", "some content2");
req.post(new Callback(){


});


#图片上传
req.setBodyEncoding(RequestBody.ENCODING.MULTIPART);
req.addForm("file_field", new File("file/path"));
req.addForm("file_field2", new File("file/path"));
req.post(new Callback(){
});


# 使用缓存
req.cache()

# 使用https
req.secure()

# 支持流式调用
req.cache().secure().get(...)



