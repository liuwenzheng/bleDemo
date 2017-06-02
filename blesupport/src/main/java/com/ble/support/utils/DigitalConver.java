package com.ble.support.utils;


import com.ble.support.log.LogModule;

/**
 * @Date 2017/5/15
 * @Author wenzheng.liu
 * @Description 数字转换类
 * @ClassPath com.ble.support.utils.DigitalConver
 */
public class DigitalConver {
    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 格式化数据
     */
    public static String formatData(byte[] data) {
        if (data != null && data.length > 0) {
            StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data)
                stringBuilder.append(byte2HexString(byteChar));
            LogModule.i(stringBuilder.toString());
            return stringBuilder.toString();
        }
        return null;
    }

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description byte转16进制
     */
    public static String byte2HexString(byte b) {
        return String.format("%02X ", b);
    }

    /**
     * @Date 2017/5/15
     * @Author wenzheng.liu
     * @Description 16进制转10进制
     */
    public static String decodeToString(String data) {
        String string = Integer.toString(Integer.parseInt(data, 16));
        return string;
    }

    /**
     * @Date 2017/5/16
     * @Author wenzheng.liu
     * @Description 16进制转2进制
     */
    public static String hexString2binaryString(String hexString) {
        if (hexString == null || hexString.length() % 2 != 0)
            return null;
        String bString = "", tmp;
        for (int i = 0; i < hexString.length(); i++) {
            tmp = "0000"
                    + Integer.toBinaryString(Integer.parseInt(
                    hexString.substring(i, i + 1), 16));
            bString += tmp.substring(tmp.length() - 4);
        }
        return bString;
    }

    /**
     * @Date 2017/6/2
     * @Author wenzheng.liu
     * @Description 16进制数组转byte数组
     */
    public static byte[] hexStringArray2byteArray(String[] hexString) {
        byte[] data = new byte[hexString.length];
        for (int i = 0; i < hexString.length; i++) {
            data[i] = (byte) Integer.parseInt(hexString[i], 16);
        }
        return data;
    }
}
