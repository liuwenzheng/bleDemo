package com.fitpolo.ble.demo.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.TextView;

import com.ble.support.bluetooth.BluetoothModule;
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

    @Bind(R.id.tv_msg)
    TextView tvMsg;
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
}
