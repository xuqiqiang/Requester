package com.xuqiqiang.uikit.requester.proxy;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.xuqiqiang.uikit.requester.DeviceRequester;

import static com.xuqiqiang.uikit.requester.DeviceRequester.DEVICE_BLUETOOTH_DISABLE;
import static com.xuqiqiang.uikit.requester.DeviceRequester.DEVICE_BLUETOOTH_ENABLE;

public class DeviceActivity extends Activity {
    private static final int REQUEST_DEVICE = 0x004001;
    private static final String PARAM_TYPE = "PARAM_TYPE";
    private static final String
        ACTION_REQUEST_BLUETOOTH_DISABLE = "android.bluetooth.adapter.action.REQUEST_DISABLE";
    private static DeviceRequester.OnDeviceListener mOnDeviceListener;

    public static void start(final Context context, final int type,
        final DeviceRequester.OnDeviceListener listener) {
        mOnDeviceListener = listener;
        Intent intent = new Intent(context, DeviceActivity.class);
        intent.putExtra(PARAM_TYPE, type);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        int type = intent.getIntExtra(PARAM_TYPE, DEVICE_BLUETOOTH_ENABLE);
        Intent contentIntent = null;
        if (type == DEVICE_BLUETOOTH_ENABLE) {
            contentIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        } else if (type == DEVICE_BLUETOOTH_DISABLE) {
            contentIntent = new Intent(ACTION_REQUEST_BLUETOOTH_DISABLE);
        }
        if (contentIntent != null) {
            startActivityForResult(contentIntent, REQUEST_DEVICE);
        } else {
            if (mOnDeviceListener != null) {
                mOnDeviceListener.onRequest(false);
            }
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_DEVICE) {
            if (mOnDeviceListener != null) {
                mOnDeviceListener.onRequest(
                    resultCode == Activity.RESULT_OK);
            }
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        mOnDeviceListener = null;
        super.onDestroy();
    }
}
