package com.xuqiqiang.uikit.requester.demo;

import android.os.Bundle;
import android.view.View;
import com.xuqiqiang.uikit.requester.DeviceRequester;
import com.xuqiqiang.uikit.utils.Logger;
import com.xuqiqiang.uikit.view.ToastMaster;

public class DeviceActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
    }

    public void checkBluetooth(View view) {
        ToastMaster.showToast(this, DeviceRequester.isBluetoothEnable() ?
            "蓝牙已打开" : "蓝牙未打开");
    }

    public void enableBluetooth(View view) {
        DeviceRequester.enableBluetooth(this, new DeviceRequester.OnDeviceListener() {
            @Override public void onRequest(boolean success) {
                Logger.d("onRequest:" + success);
            }
        });
    }

    public void disableBluetooth(View view) {
        DeviceRequester.disableBluetooth(this, new DeviceRequester.OnDeviceListener() {
            @Override public void onRequest(boolean success) {
                Logger.d("onRequest:" + success);
            }
        });
    }
}
