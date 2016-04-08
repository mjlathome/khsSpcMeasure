package com.khs.spcmeasure.library;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

import java.net.URLEncoder;

/**
 * Created by mlees on 4/8/2016.
 * see: http://stackoverflow.com/questions/16704597/how-do-you-get-the-user-defined-device-name-in-android
 */
public class DeviceUtils {
    public static final String TAG_DEVICE_NAME = "deviceName";

    // returns user entered device name
    public static String getDeviceName() {
        BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
        return myDevice.getName();
    }

    // returns URL query string for device name
    public static String getUrlQuery() {
        String url = "";
        try {
            url = URLEncoder.encode(TAG_DEVICE_NAME + "=" + getDeviceName(), "UTF-8");
        } catch(Exception e) {
            e.printStackTrace();
        }
        return url;
    }
}


