package com.fitpolo.ble.demo.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ble.support.bluetooth.BluetoothModule;
import com.ble.support.utils.DigitalConver;
import com.fitpolo.ble.demo.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @Date 2017/6/1
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.fitpolo.ble.demo.activity.ShowOrderActivity
 */
public class ShowOrderActivity extends Activity {
    public final static String REGEX_HEX = "^[0-9a-fA-F]+$";

    @Bind(R.id.tv_msg)
    TextView tvMsg;
    @Bind(R.id.et_order)
    EditText etOrder;
    private LocalBroadcastManager mBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_order_layout);
        ButterKnife.bind(this);
        mBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction("ACTION_ORDER_RESULT");
        mBroadcastManager.registerReceiver(mReceiver, filter);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if ("ACTION_ORDER_RESULT".equals(action)) {
                    String result = intent.getStringExtra("order");
                    StringBuilder sb = new StringBuilder(tvMsg.getText().toString());
                    sb.append("\n");
                    sb.append(result);
                    tvMsg.setText(sb.toString());
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBroadcastManager.unregisterReceiver(mReceiver);
        BluetoothModule.getInstance().disConnectBle();
    }


    public void disConn(View view) {
        finish();
    }

    public void sendOrder(View view) {
        String order = etOrder.getText().toString().replaceAll(" ", "").toUpperCase();
        if (TextUtils.isEmpty(order)) {
            Toast.makeText(this, "命令不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (order.length() % 2 != 0 || !order.matches(REGEX_HEX)) {
            Toast.makeText(this, "请填写2位十六进制数", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] strArray = new String[order.length() / 2];
        for (int i = 0, j = 0; i < order.length(); i += 2) {
            strArray[j] = order.substring(i, i + 2);
            j++;
        }
        byte[] byteArray = DigitalConver.hexStringArray2byteArray(strArray);
        BluetoothModule.getInstance().sendOrder(byteArray);
    }
}
