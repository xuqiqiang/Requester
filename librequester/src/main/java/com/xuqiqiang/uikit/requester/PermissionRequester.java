package com.xuqiqiang.uikit.requester;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.xuqiqiang.uikit.requester.proxy.PermissionActivity;
import com.xuqiqiang.uikit.utils.PermissionPageUtils;

import java.util.ArrayList;
import java.util.List;

import static com.xuqiqiang.uikit.utils.NotificationsUtils.isNotificationEnabled;

public class PermissionRequester {

    public static void request(Context context, OnPermissionListener listener, String... permissions) {
        String[] requestPermissions = checkSelfPermission(context, permissions);
        if (requestPermissions.length == 0) {
            if (listener != null)
                listener.onRequestPermission(true, null, null);
            return;
        }
        PermissionActivity.start(context, requestPermissions, null, false, listener);
    }

    public static void request(Context context, final OnSimplePermissionListener listener, String... permissions) {
        OnPermissionListener onPermissionListener = null;
        if (listener != null) {
            onPermissionListener = new OnPermissionListener() {
                @Override
                public void onRequestPermission(boolean success, @Nullable List<String> deniedPermissions, @Nullable List<String> rejectPermissions) {
                    listener.onRequestPermission(success);
                }
            };
        }
        request(context, onPermissionListener, permissions);
    }

    public static void requestForce(Context context, String permissionName, OnPermissionListener listener, String... permissions) {
        String[] requestPermissions = checkSelfPermission(context, permissions);
        if (requestPermissions.length == 0) {
            if (listener != null)
                listener.onRequestPermission(true, null, null);
            return;
        }
        PermissionActivity.start(context, requestPermissions, permissionName, true, listener);
    }

    public static void requestForce(Context context, String permissionName, final OnSimplePermissionListener listener, String... permissions) {
        OnPermissionListener onPermissionListener = null;
        if (listener != null) {
            onPermissionListener = new OnPermissionListener() {
                @Override
                public void onRequestPermission(boolean success, @Nullable List<String> deniedPermissions, @Nullable List<String> rejectPermissions) {
                    listener.onRequestPermission(success);
                }
            };
        }
        requestForce(context, permissionName, onPermissionListener, permissions);
    }

    public static boolean checkPermissions(Context context, String... permissions) {
        return checkSelfPermission(context, permissions).length == 0;
    }

    /**
     * 检查多个权限是否申请过
     *
     * @param permissions 多个权限数组
     * @return 返回还没有被授权同意的权限数组，如果数组.length==0, 说明permissions都被授权同意了
     */
    public static String[] checkSelfPermission(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return new String[]{};
        List<String> needRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (!TextUtils.isEmpty(permission) && ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                needRequest.add(permission);
            }
        }
        return needRequest.toArray(new String[needRequest.size()]);//CommonUtil.toArray(rejectedPermissions);
    }

    // region 特殊权限
    public static void requestSpecialPermission(Context context, @NonNull String specialPermission, final OnSimplePermissionListener listener) {
        if (checkSpecialPermission(context, specialPermission)) {
            if (listener != null)
                listener.onRequestPermission(true);
            return;
        }

        OnPermissionListener onPermissionListener = null;
        if (listener != null) {
            onPermissionListener = new OnPermissionListener() {
                @Override
                public void onRequestPermission(boolean success, @Nullable List<String> deniedPermissions, @Nullable List<String> rejectPermissions) {
                    listener.onRequestPermission(success);
                }
            };
        }
        PermissionActivity.start(context, specialPermission, onPermissionListener);
    }

    /**
     * 检查特殊权限是否申请过
     *
     * @param special 要检查的特殊权限
     * @return true表示申请过，false反之
     */
    public static boolean checkSpecialPermission(Context context, String special) {
        boolean isGranted = false;
        switch (special) {
            case SpecialPermission.DISPLAY_NOTIFICATION:
                isGranted = isNotificationEnabled(context);
                break;
            case SpecialPermission.INSTALL_UNKNOWN_APP:
                isGranted = checkSpecialInstallUnkownApp(context);
                break;
            case SpecialPermission.WRITE_SYSTEM_SETTINGS:
                isGranted = checkSpecialWriteSystemSettings(context);
                break;
            case SpecialPermission.SYSTEM_ALERT_WINDOW:
                isGranted = checkSpecialSystemAlertWindow(context);
                break;
            default:
                break;
        }
        return isGranted;
    }

    /**
     * 检查特殊权限 - 安装未知来源应用
     *
     * @return true表示用户同意授权，false表示用户拒绝授权
     */
    public static boolean checkSpecialInstallUnkownApp(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return true;
        return context.getPackageManager().canRequestPackageInstalls();
    }

    /**
     * 检查特殊权限 - 修改系统设置
     *
     * @return true表示用户同意授权，false表示用户拒绝授权
     */
    public static boolean checkSpecialWriteSystemSettings(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        return Settings.System.canWrite(context);
    }

    /**
     * 检查特殊权限 - 悬浮窗权限
     *
     * @return true表示用户同意授权，false表示用户拒绝授权
     */
    public static boolean checkSpecialSystemAlertWindow(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        return Settings.canDrawOverlays(context);
    }

    public static boolean gotoPermissionDetail(Context context) {
        try {
            new PermissionPageUtils(context).jumpPermissionPage();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    // endregion

    public static void gotoAppDetail(Activity context, int requestCode) {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivityForResult(intent, requestCode);
    }

    public interface SpecialPermission {
        String DISPLAY_NOTIFICATION = "DISPLAY_NOTIFICATION"; // 允许通知
        String INSTALL_UNKNOWN_APP = "INSTALL_UNKNOWN_APP"; // 安装未知来源应用
        String SYSTEM_ALERT_WINDOW = "SYSTEM_ALERT_WINDOW"; // 设置悬浮窗
        String WRITE_SYSTEM_SETTINGS = "WRITE_SYSTEM_SETTINGS"; // 修改系统设置
    }

    public interface OnPermissionListener {

        /**
         * 权限请求的回调
         *
         * @param success           权限请求成功（等价于deniedPermissions为空）/之前已经同意了无需再授权此权限/系统版本小于M
         * @param deniedPermissions 用户拒绝授权的权限列表（包含rejectPermissions）
         * @param rejectPermissions 用户拒绝授权并勾选了don’t ask again的权限列表
         */
        void onRequestPermission(boolean success, @Nullable List<String> deniedPermissions, @Nullable List<String> rejectPermissions);
    }

    public interface OnSimplePermissionListener {
        /**
         * 权限请求的回调
         *
         * @param success 权限请求成功/之前已经同意了无需再授权此权限/系统版本小于M
         */
        void onRequestPermission(boolean success);
    }
}