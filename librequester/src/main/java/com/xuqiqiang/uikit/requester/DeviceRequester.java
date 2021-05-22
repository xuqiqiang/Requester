package com.xuqiqiang.uikit.requester;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import com.xuqiqiang.uikit.requester.proxy.DeviceActivity;

public class DeviceRequester {

    public static final int DEVICE_BLUETOOTH_ENABLE = 1;
    public static final int DEVICE_BLUETOOTH_DISABLE = 2;

    private static BluetoothAdapter mBluetoothAdapter;

    public static boolean isSupportBle(Context context) {
        return context.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le");
    }

    @SuppressLint("MissingPermission")
    public static boolean isBluetoothEnable() {
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        return mBluetoothAdapter.isEnabled();
    }

    public static void enableBluetooth(Context context, OnDeviceListener listener) {
        if (isBluetoothEnable()) {
            if (listener != null) {
                listener.onRequest(true);
            }
            return;
        }
        DeviceActivity.start(context, DEVICE_BLUETOOTH_ENABLE, listener);
    }

    public static void disableBluetooth(Context context, OnDeviceListener listener) {
        if (!isBluetoothEnable()) {
            if (listener != null) {
                listener.onRequest(true);
            }
            return;
        }
        DeviceActivity.start(context, DEVICE_BLUETOOTH_DISABLE, listener);
    }

    public interface OnDeviceListener {
        void onRequest(boolean success);
    }
}