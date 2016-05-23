package com.huanchi.lightpay.util;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.huanchi.lightpay.R;

import java.io.InputStream;

/**
 * Created by odin on 5/21/16.
 */
public class ApplicationUtil {

    private static Application context;
    private static String userAgent;

    public static void init(Application context){
        ApplicationUtil.context = context;

        getUserAgent();
    }

    public static String getUserAgent(){
        if (userAgent == null){
            Device device = getDevice();
            userAgent = String.format("LightPay/%s (%s,%s; android %s)", getClientVersion(), device.manufacturer, device.imei, Build.VERSION.SDK_INT);
        }

        return userAgent;
    }


    public static String getClientVersion(){
        String versionName = "0.00";
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                throw new Exception("versionName is bad");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return versionName;
    }

    public static class Device {
        public int sdkVersion;// sdk版本
        public String manufacturer;// 厂商名称
        public String model;// 机型
        public String imei;
    }
    /**
     * 获取手机信息
     *
     * @return
     */
    public static Device getDevice() {
        Device deviceInfo = new Device();
        // sdk版本
        deviceInfo.sdkVersion = Build.VERSION.SDK_INT;
        deviceInfo.manufacturer = Build.MANUFACTURER;
        deviceInfo.model = Build.MODEL;
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        deviceInfo.imei = tm.getDeviceId() + (tm.getSimState() == TelephonyManager.SIM_STATE_READY ? 1 : 0);
        return deviceInfo;
    }


    public static String getPackageName(){
        PackageManager pm = context.getPackageManager();
        PackageInfo pi = null;
        try {
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            return pi.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "com.huanchi.lightpay";
    }

    public static String getCachePath(){
        return "/sdcard/" + getPackageName() + "/cache";
    }

    public static InputStream getCertificate(){
        return context.getResources().openRawResource(R.raw.certificate);
    }
}
