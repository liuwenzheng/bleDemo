package com.ble.support.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.ble.support.FitConstant;
import com.ble.support.callback.ConnStateCallback;
import com.ble.support.callback.GattCallback;
import com.ble.support.callback.ScanDeviceCallback;
import com.ble.support.entity.BleDevice;
import com.ble.support.log.LogModule;
import com.ble.support.utils.BaseHandler;
import com.ble.support.utils.DigitalConver;

import java.util.List;
import java.util.UUID;

/**
 * @Date 2017/5/10
 * @Author wenzheng.liu
 * @Description 蓝牙工具类
 * @ClassPath com.ble.support.bluetooth.BluetoothModule
 */
public class BluetoothModule {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCallback mGattCallback;
    private static final UUID SERVIE_UUID =
            UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    private static final UUID CHARACTERISTIC_DESCRIPTOR_UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    /**
     * Notify, wristbands send data to APP using this characteristic
     */
    private static final UUID CHARACTERISTIC_UUID_NOTIFY =
            UUID.fromString("00000003-0000-1000-8000-00805f9b34fb");
    /**
     * Write, APP send command to wristbands using this characteristic
     */
    private static final UUID CHARACTERISTIC_UUID_WRITE =
            UUID.fromString("00000002-0000-1000-8000-00805f9b34fb");

    private static final Object LOCK = new Object();


    private static volatile BluetoothModule INSTANCE;
    private LocalBroadcastManager mBroadcastManager;


    private BluetoothModule() {
    }

    public static BluetoothModule getInstance() {
        if (INSTANCE == null) {
            synchronized (BluetoothModule.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BluetoothModule();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 创建蓝牙适配器
     */
    public void createBluetoothAdapter(Context context) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mHandler = new ServiceHandler(this);
        mBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 扫描设备
     */
    public void startScanDevice(final ScanDeviceCallback callback) {
        final FitLeScanCallback fitLeScanCallback = new FitLeScanCallback(callback);
        mBluetoothAdapter.startLeScan(fitLeScanCallback);
        callback.onStartScan();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothAdapter.stopLeScan(fitLeScanCallback);
                callback.onStopScan();
            }
        }, FitConstant.SCAN_PERIOD);
    }

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 连接gatt
     */
    public void createBluetoothGatt(Context context, String address, ConnStateCallback connCallBack) {
        if (TextUtils.isEmpty(address)) {
            connCallBack.onConnFailure(FitConstant.CONN_ERROR_CODE_ADDRESS_NULL);
            return;
        }
        if (!isBluetoothOpen()) {
            connCallBack.onConnFailure(FitConstant.CONN_ERROR_CODE_BLUTOOTH_CLOSE);
            return;
        }
        if (isConnDevice(context, address)) {
            connCallBack.onConnFailure(FitConstant.CONN_ERROR_CODE_CONNECTED);
            return;
        }
        mDeviceAddress = address;
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (mGattCallback == null) {
            mGattCallback = getBluetoothGattCallback(context, connCallBack);
        }
        disConnectBle();
        mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
    }

    /**
     * @param context
     * @param connCallBack
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 获取蓝牙连接回调
     */
    private BluetoothGattCallback getBluetoothGattCallback(final Context context, final ConnStateCallback connCallBack) {
        BluetoothGattCallback callback = new CustomGattCallback(new GattCallback() {
            @Override
            public void onServicesDiscovered() {
                setCharacteristicNotify(mBluetoothGatt);
                connCallBack.onConnSuccess();
            }

            @Override
            public void onConnSuccess() {
                mBluetoothGatt.discoverServices();
            }

            @Override
            public void onConnFailure() {
                disConnectBle();
                connCallBack.onConnFailure(FitConstant.CONN_ERROR_CODE_FAILURE);
            }

            @Override
            public void onDisConn() {
                disConnectBle();
                connCallBack.onDisconnect();
            }

            @Override
            public void onResponse(BluetoothGattCharacteristic characteristic) {
                byte[] data = characteristic.getValue();
                LogModule.i("接收数据：");
                String formatDatas = DigitalConver.formatData(data);
                Intent intent = new Intent(new Intent("ACTION_ORDER_RESULT"));
                intent.putExtra("order", formatDatas);
                mBroadcastManager.sendBroadcast(intent);
            }
        });
        return callback;
    }


    private String mDeviceAddress;

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 判断是否已连接手环
     */
    public boolean isConnDevice(Context context, String address) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        int connState = bluetoothManager.getConnectionState(mBluetoothAdapter.getRemoteDevice(address), BluetoothProfile.GATT);
        return connState == BluetoothProfile.STATE_CONNECTED;
    }

    class FitLeScanCallback implements BluetoothAdapter.LeScanCallback {
        private ScanDeviceCallback mCallback;

        public FitLeScanCallback(ScanDeviceCallback callback) {
            this.mCallback = callback;
        }

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (device != null) {
                if (TextUtils.isEmpty(device.getName())) {
                    return;
                }
                BleDevice bleDevice = new BleDevice();
                bleDevice.name = device.getName();
                bleDevice.address = device.getAddress();
                bleDevice.rssi = rssi;
                bleDevice.scanRecord = scanRecord;
                mCallback.onScanDevice(bleDevice);
            }
        }
    }

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 蓝牙是否开启
     */
    public boolean isBluetoothOpen() {
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 断开gattt
     */
    public void disConnectBle() {
        if (mBluetoothGatt != null) {
            LogModule.i("断开连接");
            synchronized (LOCK) {
                mNotifyCharacteristic = null;
            }
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    /**
     * @Date 2017/6/2
     * @Author wenzheng.liu
     * @Description 发送命令
     */
    public void sendOrder(byte[] byteArray) {
        writeCharacteristicData(mBluetoothGatt, byteArray);
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    private ServiceHandler mHandler;

    private class ServiceHandler extends BaseHandler<BluetoothModule> {

        public ServiceHandler(BluetoothModule module) {
            super(module);
        }

        @Override
        protected void handleMessage(BluetoothModule module, Message msg) {
        }
    }

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 将所有手环特征设置为notify方式
     */
    private void setCharacteristicNotify(BluetoothGatt mBluetoothGatt) {
        List<BluetoothGattService> gattServices = mBluetoothGatt.getServices();
        if (gattServices == null)
            return;
        String uuid;
        // 遍历所有服务，找到手环的服务
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            if (uuid.startsWith("0000ffe0")) {
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                // 遍历所有特征，找到发出的特征
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    uuid = gattCharacteristic.getUuid().toString();
                    if (uuid.startsWith("00000003")) {
                        int charaProp = gattCharacteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            if (mNotifyCharacteristic != null) {
                                setCharacteristicNotification(mBluetoothGatt, mNotifyCharacteristic, false);
                                synchronized (LOCK) {
                                    mNotifyCharacteristic = null;
                                }
                            }
                            mBluetoothGatt.readCharacteristic(gattCharacteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            synchronized (LOCK) {
                                mNotifyCharacteristic = gattCharacteristic;
                            }
                            setCharacteristicNotification(mBluetoothGatt, gattCharacteristic, true);
                        }
                    }
                }
            }
        }
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification. False otherwise.
     */
    private void setCharacteristicNotification(BluetoothGatt mBluetoothGatt,
                                               BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        /**
         * 打开数据FFF4
         */
        // This is specific to Heart Rate Measurement.
        if (CHARACTERISTIC_UUID_NOTIFY.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CHARACTERISTIC_DESCRIPTOR_UUID);
            if (descriptor == null) {
                return;
            }
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 发送数据
     */
    private void writeCharacteristicData(BluetoothGatt mBluetoothGatt, byte[] byteArray) {
        if (mBluetoothGatt == null) {
            return;
        }
        BluetoothGattService service = mBluetoothGatt.getService(SERVIE_UUID);
        if (service == null) {
            return;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_UUID_WRITE);
        if (characteristic == null) {
            return;
        }
        LogModule.i("发送数据：");
        DigitalConver.formatData(byteArray);
        characteristic.setValue(byteArray);
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mBluetoothGatt.writeCharacteristic(characteristic);
    }
}
