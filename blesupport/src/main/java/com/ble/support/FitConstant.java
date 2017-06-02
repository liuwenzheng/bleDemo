package com.ble.support;

/**
 * @Date 2017/5/10
 * @Author wenzheng.liu
 * @Description 蓝牙常量
 */
public class FitConstant {
    // 扫描结束时间
    public static final long SCAN_PERIOD = 5000;
    // 连接失败错误码
    public static final int CONN_ERROR_CODE_ADDRESS_NULL = 0;// 地址为空
    public static final int CONN_ERROR_CODE_BLUTOOTH_CLOSE = 1;// 蓝牙关闭
    public static final int CONN_ERROR_CODE_CONNECTED = 2;// 已连接
    public static final int CONN_ERROR_CODE_FAILURE = 3;// 连接失败
}
