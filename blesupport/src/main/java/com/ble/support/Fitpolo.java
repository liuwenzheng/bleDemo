package com.ble.support;

import android.content.Context;

import com.ble.support.bluetooth.BluetoothModule;
import com.ble.support.log.LogModule;

/**
 * @Date 2017/5/13 0013
 * @Author wenzheng.liu
 * @Description 初始化类
 * @ClassPath com.ble.support.Fitpolo
 */
public class Fitpolo {

    public static void init(Context context) {
        BluetoothModule.getInstance().createBluetoothAdapter(context);
    }
}
