package com.ble.support.callback;

import com.ble.support.entity.BleDevice;

/**
 * @Date 2017/5/10
 * @Author wenzheng.liu
 * @Description 扫描设备回调
 * @ClassPath com.ble.support.callback.ScanDeviceCallback
 */
public interface ScanDeviceCallback {
    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 开始扫描
     */
    void onStartScan();

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 扫描的设备
     */
    void onScanDevice(BleDevice device);

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 结束扫描
     */
    void onStopScan();
}
