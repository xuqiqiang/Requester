package com.xuqiqiang.uikit.requester;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.xuqiqiang.uikit.R;
import com.xuqiqiang.uikit.requester.proxy.KeyguardActivity;
import com.xuqiqiang.uikit.view.ToastMaster;
import com.xuqiqiang.uikit.view.dialog.BaseDialog;

public class KeyguardRequester {

    public static boolean isKeyguardSecure(Context context) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardManager.isKeyguardSecure();
    }

    public static boolean checkKeyguardSecureForKeyword(final Context context) {
        boolean isKeyguardSecure = isKeyguardSecure(context);
        if (!isKeyguardSecure) {
            new BaseDialog.Builder(context)
                    .setTitle("设置密码")
                    .setMessage("当前设备无密码，是否设置密码？")
                    .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                Intent intent = new Intent("/");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                ComponentName cm = new ComponentName("com.android.settings",
                                        "com.android.settings.ChooseLockGeneric");
                                intent.setComponent(cm);
                                context.startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                                try {
                                    Intent intent = new Intent("/");
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    ComponentName cm = new ComponentName("com.android.settings",
                                            "com.android.settings.Settings$SecurityDashboardActivity");
                                    intent.setComponent(cm);
                                    context.startActivity(intent);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                    ToastMaster.showToast(context, "请前往'设置'界面设置解锁密码");
                                }
                            }
                            dialog.cancel();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create().show();
        }
        return isKeyguardSecure;
    }

    /**
     * 跳转到系统解锁页面
     */
    public static void requestAuthentication(Context context, OnAuthenticationListener listener) {
        if (isKeyguardSecure(context)) {
            KeyguardActivity.start(context, listener);
        } else {
            if (listener != null) listener.onRequestAuthentication(true);
        }
    }

    public static boolean requestAuthentication(Activity activity, int requestCode) {
        KeyguardManager keyguardManager = (KeyguardManager) activity.getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager.isKeyguardSecure()) {
            // Create the Confirm Credentials screen. You can customize the title and description. Or
            // we will provide a generic one for you if you leave it null
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(null, null);
                if (intent != null) {
                    activity.startActivityForResult(intent, requestCode);
                    return true;
                }
            }
        }
        return false;
    }

    public interface OnAuthenticationListener {
        void onRequestAuthentication(boolean success);
    }
}