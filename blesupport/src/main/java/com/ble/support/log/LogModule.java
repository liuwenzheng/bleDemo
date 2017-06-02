package com.ble.support.log;


import android.util.Log;

/**
 * @Date 2017/5/9
 * @Author wenzheng.liu
 * @Description 日志模块
 * @ClassPath com.ble.support.log.LogModule
 */
public class LogModule {
    private static final String TAG = "bleDemo";

    public static void v(String msg) {
        Log.v(TAG, msg);
    }

    public static void d(String msg) {
        Log.d(TAG, msg);
    }

    public static void i(String msg) {
        Log.i(TAG, msg);
    }

    public static void w(String msg) {
        Log.w(TAG, msg);
    }

    public static void e(String msg) {
        Log.e(TAG, msg);
    }
}
