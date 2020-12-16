package com.xuqiqiang.uikit.requester;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import com.xuqiqiang.uikit.requester.proxy.ShortcutActivity;
import com.xuqiqiang.uikit.requester.utils.ShortcutUtils;
import com.xuqiqiang.uikit.utils.OSUtils;

public class ShortcutRequester {

    public static void requestPinShortcut(final Context context, final String id, final String name,
                                          final Class<?> className, final int iconId, final Bundle data, final OnShortcutListener listener) {
        PermissionRequester.requestForce(context, "创建快捷方式", new PermissionRequester.OnSimplePermissionListener() {
            @Override
            public void onRequestPermission(boolean success) {
                if (success) {
                    if (ShortcutUtils.hasShortcut(context, id, name)) {
                        if (listener != null) listener.onRequest(true);
                        return;
                    }
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || OSUtils.isVivo() || OSUtils.isOppo()) {
                        ShortcutUtils.createShortcut(context, id, name,
                                className, iconId, data);
                        if (listener != null) listener.onRequest(true);
                        return;
                    }

                    ShortcutActivity.start(context, new ShortcutBean(id, name, className, iconId, data), listener);
                }
            }
        }, Manifest.permission.INSTALL_SHORTCUT, Manifest.permission.UNINSTALL_SHORTCUT);
    }

    public interface OnShortcutListener {
        void onRequest(boolean success);
    }

    public static class ShortcutBean {
        public String id;
        public String name;
        public Class<?> className;
        public int iconId;
        public Bundle data;

        public ShortcutBean(String id, String name, Class<?> className, int iconId, Bundle data) {
            this.id = id;
            this.name = name;
            this.className = className;
            this.iconId = iconId;
            this.data = data;
        }
    }
}