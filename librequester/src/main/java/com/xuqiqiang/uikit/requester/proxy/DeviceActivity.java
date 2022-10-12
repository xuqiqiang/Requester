package com.xuqiqiang.uikit.requester.proxy;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import com.xuqiqiang.uikit.requester.DeviceRequester;

import static com.xuqiqiang.uikit.requester.DeviceRequester.DEVICE_BLUETOOTH_DISABLE;
import static com.xuqiqiang.uikit.requester.DeviceRequester.DEVICE_BLUETOOTH_ENABLE;
import static com.xuqiqiang.uikit.requester.DeviceRequester.DEVICE_LOCATION_DISABLE;
import static com.xuqiqiang.uikit.requester.DeviceRequester.DEVICE_LOCATION_ENABLE;

public class DeviceActivity extends Activity {
    private static final int REQUEST_DEVICE = 0x004001;
    private static final String PARAM_TYPE = "PARAM_TYPE";
    private static final String
            ACTION_REQUEST_BLUETOOTH_DISABLE = "android.bluetooth.adapter.action.REQUEST_DISABLE";
    private static DeviceRequester.OnDeviceListener mOnDeviceListener;
    private int mType;
    private boolean isJumpToSettingPage;
    private long mJumpTime;

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
        mType = intent.getIntExtra(PARAM_TYPE, DEVICE_BLUETOOTH_ENABLE);
        Intent contentIntent = null;
        if (mType == DEVICE_BLUETOOTH_ENABLE) {
            contentIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        } else if (mType == DEVICE_BLUETOOTH_DISABLE) {
            contentIntent = new Intent(ACTION_REQUEST_BLUETOOTH_DISABLE);
        } else if (mType == DEVICE_LOCATION_ENABLE || mType == DEVICE_LOCATION_DISABLE) {
            contentIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            contentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        boolean start = false;
        if (contentIntent != null) {
            try {
                startActivityForResult(contentIntent, REQUEST_DEVICE);
                start = true;
                isJumpToSettingPage = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!start) {
            if (mOnDeviceListener != null) {
                mOnDeviceListener.onRequest(false);
            }
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_DEVICE
                && (mType == DEVICE_BLUETOOTH_ENABLE || mType == DEVICE_BLUETOOTH_DISABLE)) {
            if (mOnDeviceListener != null) {
                mOnDeviceListener.onRequest(resultCode == Activity.RESULT_OK);
            }
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isJumpToSettingPage && mJumpTime > 0 && System.currentTimeMillis() - mJumpTime > 100) {
            if (mType == DEVICE_LOCATION_ENABLE || mType == DEVICE_LOCATION_DISABLE) {
                boolean result = false;
                if (mType == DEVICE_LOCATION_ENABLE) {
                    result = DeviceRequester.isLocationEnable(this);
                } else if (mType == DEVICE_LOCATION_DISABLE) {
                    result = !DeviceRequester.isLocationEnable(this);
                }
                if (mOnDeviceListener != null) {
                    mOnDeviceListener.onRequest(result);
                }
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isJumpToSettingPage && mJumpTime == 0) {
            mJumpTime = System.currentTimeMillis();
        }
    }

    @Override
    protected void onDestroy() {
        mOnDeviceListener = null;
        super.onDestroy();
    }
}
