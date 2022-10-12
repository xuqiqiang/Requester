package com.xuqiqiang.uikit.requester;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.location.LocationManager;
import android.text.TextUtils;
import com.xuqiqiang.uikit.requester.proxy.DeviceActivity;
import com.xuqiqiang.uikit.view.ToastMaster;

public class DeviceRequester {

    public static final int DEVICE_BLUETOOTH_ENABLE = 1;
    public static final int DEVICE_BLUETOOTH_DISABLE = 2;
    public static final int DEVICE_LOCATION_ENABLE = 3;
    public static final int DEVICE_LOCATION_DISABLE = 4;

    private static BluetoothAdapter mBluetoothAdapter;
    private static LocationManager mLocationManager;

    public static boolean isSupportBle(Context context) {
        return context.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le");
    }

    @SuppressLint("MissingPermission")
    public static boolean isBluetoothEnable() {
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
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

    public static boolean isLocationEnable(Context context) {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }
        boolean isGPS = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetwork = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return isGPS || isNetwork;
    }

    public static void enableLocation(Context context, OnDeviceListener listener) {
        enableLocation(context, listener, null);
    }

    public static void enableLocation(Context context, OnDeviceListener listener, String prompt) {
        if (isLocationEnable(context)) {
            if (listener != null) {
                listener.onRequest(true);
            }
            return;
        }
        DeviceActivity.start(context, DEVICE_LOCATION_ENABLE, listener);
        if (!TextUtils.isEmpty(prompt)) {
            ToastMaster.showToast(context, prompt);
        }
    }

    public static void disableLocation(Context context, OnDeviceListener listener) {
        if (!isLocationEnable(context)) {
            if (listener != null) {
                listener.onRequest(true);
            }
            return;
        }
        DeviceActivity.start(context, DEVICE_LOCATION_DISABLE, listener);
    }

    public interface OnDeviceListener {
        void onRequest(boolean success);
    }
}