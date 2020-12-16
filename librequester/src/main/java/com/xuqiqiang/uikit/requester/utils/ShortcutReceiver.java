package com.xuqiqiang.uikit.requester.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.xuqiqiang.uikit.utils.Logger;

public class ShortcutReceiver extends BroadcastReceiver {
    private static final String TAG = "ShortcutReceiver";
    private static Runnable mEvent;

    public static void post(Runnable runnable) {
        mEvent = runnable;
    }

    public static void removeCallbacks() {
        mEvent = null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mEvent != null) mEvent.run();
        Logger.d(TAG, "onReceive:" + intent);
    }
}
