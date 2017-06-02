package com.fitpolo.ble.demo.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.ble.support.bluetooth.BluetoothModule;
import com.ble.support.callback.ConnStateCallback;
import com.ble.support.callback.ScanDeviceCallback;
import com.ble.support.entity.BleDevice;
import com.ble.support.log.LogModule;
import com.fitpolo.ble.demo.R;
import com.fitpolo.ble.demo.adapter.DeviceAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @Date 2017/6/1
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.fitpolo.ble.demo.activity.MainActivity
 */
public class MainActivity extends Activity implements AdapterView.OnItemClickListener, ScanDeviceCallback, ConnStateCallback {
    private static final int PERMISSION_REQUEST_CODE = 1;

    @Bind(R.id.lv_device)
    ListView lvDevice;

    private ArrayList<BleDevice> mDatas;
    private DeviceAdapter mAdapter;
    private ProgressDialog mDialog;
    private HashMap<String, BleDevice> mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
                return;
            }
        }
        initContentView();
    }

    public void searchDevices(View view) {
        LogModule.i("开始扫描...");
        mMap = new HashMap<>();
        if (mDatas != null) {
            mDatas.clear();
        } else {
            mDatas = new ArrayList<>();
        }
        mAdapter.notifyDataSetChanged();
        BluetoothModule.getInstance().startScanDevice(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BleDevice device = (BleDevice) parent.getItemAtPosition(position);
        BluetoothModule.getInstance().createBluetoothGatt(this, device.address, this);
        mDialog.setMessage("开始连接设备...");
        mDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(MainActivity.this, "This app needs these permissions!", Toast.LENGTH_SHORT).show();
                        MainActivity.this.finish();
                        return;
                    }
                }
                initContentView();
            }
        }
    }

    private void initContentView() {
        setContentView(R.layout.main_layout);
        ButterKnife.bind(this);
        mDialog = new ProgressDialog(this);
        mDatas = new ArrayList<>();
        mAdapter = new DeviceAdapter(this);
        mAdapter.setItems(mDatas);
        lvDevice.setAdapter(mAdapter);
        lvDevice.setOnItemClickListener(this);
    }

    @Override
    public void onConnSuccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDialog.dismiss();
                Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, ShowOrderActivity.class));
            }
        });
    }

    @Override
    public void onConnFailure(int errorCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDisconnect() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                Toast.makeText(MainActivity.this, "断开连接", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStartScan() {
        mDialog.setMessage("正在扫描设备...");
        mDialog.show();
    }

    @Override
    public void onScanDevice(BleDevice device) {
        mMap.put(device.name, device);
    }

    @Override
    public void onStopScan() {
        mDialog.dismiss();
        mDatas.addAll(mMap.values());
        mAdapter.setItems(mDatas);
        mAdapter.notifyDataSetChanged();
    }
}
