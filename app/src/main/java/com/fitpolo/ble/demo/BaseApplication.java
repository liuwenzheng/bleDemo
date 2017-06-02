package com.fitpolo.ble.demo;

import android.app.Application;

import com.ble.support.Fitpolo;

/**
 * @Date 2017/6/1
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.fitpolo.ble.demo.BaseApplication
 */
public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化
        Fitpolo.init(this);
    }
}
