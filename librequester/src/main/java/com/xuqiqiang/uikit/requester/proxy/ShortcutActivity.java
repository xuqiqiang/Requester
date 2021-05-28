package com.xuqiqiang.uikit.requester.proxy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.xuqiqiang.uikit.requester.PermissionRequester;
import com.xuqiqiang.uikit.requester.ShortcutRequester;
import com.xuqiqiang.uikit.requester.utils.ShortcutReceiver;
import com.xuqiqiang.uikit.requester.utils.ShortcutUtils;
import com.xuqiqiang.uikit.view.ToastMaster;

import static com.xuqiqiang.uikit.utils.Utils.mMainHandler;

public class ShortcutActivity extends Activity {
    private static final long REQUEST_SHORTCUT_WAIT_TIME = 400;
    private static final long REQUEST_SHORTCUT_DISABLE_TIME = 500;
    private static ShortcutRequester.ShortcutBean mShortcutBean;
    private static ShortcutRequester.OnShortcutListener mListener;
    private boolean isRequestShortcut;
    private boolean isRequestPermission;
    private boolean hasCreateShortcut;
    private long createShortcutTime;
    private Runnable mEvent;

    public static void start(Context context, ShortcutRequester.ShortcutBean shortcutBean,
        ShortcutRequester.OnShortcutListener listener) {
        mShortcutBean = shortcutBean;
        mListener = listener;
        Intent intent = new Intent(context, ShortcutActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mShortcutBean == null) {
            finish();
            return;
        }
        ShortcutReceiver.post(new Runnable() {
            @Override
            public void run() {
                hasCreateShortcut = true;
            }
        });
        ShortcutUtils.createShortcut(this, mShortcutBean.id, mShortcutBean.name,
            mShortcutBean.className, mShortcutBean.iconId, mShortcutBean.data);
        createShortcutTime = System.currentTimeMillis();
        mEvent = new Runnable() {
            @Override
            public void run() {
                if (createShortcutTime > 0) {
                    ToastMaster.showToast(ShortcutActivity.this, "请开启快捷方式访问权限");
                    isRequestPermission = true;
                    PermissionRequester.gotoPermissionDetail(ShortcutActivity.this);
                }
            }
        };
        mMainHandler.postDelayed(mEvent, REQUEST_SHORTCUT_DISABLE_TIME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (createShortcutTime > 0) {
            if (System.currentTimeMillis() - createShortcutTime < REQUEST_SHORTCUT_WAIT_TIME) {
                createShortcutTime = 0;
                removeCallbacks();
            }
            isRequestShortcut = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRequestPermission) {
            isRequestPermission = false;
            ShortcutUtils.createShortcut(this, mShortcutBean.id, mShortcutBean.name,
                mShortcutBean.className, mShortcutBean.iconId, mShortcutBean.data);
            createShortcutTime = System.currentTimeMillis();
            removeCallbacks();
            mEvent = new Runnable() {
                @Override
                public void run() {
                    if (createShortcutTime > 0) {
                        if (mListener != null) mListener.onRequest(false);
                        finish();
                    }
                }
            };
            mMainHandler.postDelayed(mEvent, REQUEST_SHORTCUT_DISABLE_TIME);
            return;
        }
        if (isRequestShortcut) {
            if (mListener != null) mListener.onRequest(hasCreateShortcut);
            finish();
        }
    }

    private void removeCallbacks() {
        if (mEvent != null) {
            mMainHandler.removeCallbacks(mEvent);
            mEvent = null;
        }
    }

    @Override
    protected void onDestroy() {
        ShortcutReceiver.removeCallbacks();
        mShortcutBean = null;
        mListener = null;
        removeCallbacks();
        super.onDestroy();
    }
}
