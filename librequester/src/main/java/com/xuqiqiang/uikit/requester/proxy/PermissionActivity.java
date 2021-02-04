package com.xuqiqiang.uikit.requester.proxy;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.xuqiqiang.uikit.requester.PermissionRequester;
import com.xuqiqiang.uikit.utils.ArrayUtils;
import com.xuqiqiang.uikit.view.dialog.BaseDialog;

import java.util.ArrayList;
import java.util.List;

import static com.xuqiqiang.uikit.requester.PermissionRequester.SpecialPermission.DISPLAY_NOTIFICATION;
import static com.xuqiqiang.uikit.requester.PermissionRequester.SpecialPermission.INSTALL_UNKNOWN_APP;
import static com.xuqiqiang.uikit.requester.PermissionRequester.SpecialPermission.SYSTEM_ALERT_WINDOW;
import static com.xuqiqiang.uikit.requester.PermissionRequester.SpecialPermission.WRITE_SYSTEM_SETTINGS;
import static com.xuqiqiang.uikit.utils.NotificationsUtils.getAppDetailSettingIntent;
import static com.xuqiqiang.uikit.utils.Utils.mMainHandler;

public class PermissionActivity extends Activity {
    private static final int REQUEST_PERMISSION = 0x004001;
    private static final int REQUEST_PERMISSION_SPECIAL = 0x004002;
    private static final String PARAM_FORCE = "PARAM_FORCE";
    private static final String PARAM_NAME = "PARAM_NAME";
    private static String[] mRequestPermissions;
    private static String mSpecialPermission;
    private static PermissionRequester.OnPermissionListener mListener;
    private boolean isJumpToPermissionPage;
    private long mPauseTime;
    private Runnable mRunnableOnResume;

    public static void start(Context context, String[] requestPermissions, String permissionName, boolean force, PermissionRequester.OnPermissionListener listener) {
        mRequestPermissions = requestPermissions;
        mListener = listener;
        Intent intent = new Intent(context, PermissionActivity.class);
        intent.putExtra(PARAM_NAME, permissionName);
        intent.putExtra(PARAM_FORCE, force);
        if (!(context instanceof Activity))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void start(Context context, String specialPermission, PermissionRequester.OnPermissionListener listener) {
        mSpecialPermission = specialPermission;
        mListener = listener;
        Intent intent = new Intent(context, PermissionActivity.class);
        if (!(context instanceof Activity))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!ArrayUtils.isEmpty(mRequestPermissions))
            ActivityCompat.requestPermissions(this, mRequestPermissions, REQUEST_PERMISSION);
        else if (!TextUtils.isEmpty(mSpecialPermission) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestSpecialPermission(mSpecialPermission, REQUEST_PERMISSION_SPECIAL);
        } else {
            if (mListener != null)
                mListener.onRequestPermission(false, null, null);
            finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void requestSpecialPermission(String specialPermission, int requestCode) {
        Intent intent = null;
        switch (specialPermission) {
            case DISPLAY_NOTIFICATION:
                getAppDetailSettingIntent(this, requestCode);
                return;
            case INSTALL_UNKNOWN_APP:
                Uri selfPackageUri = Uri.parse("package:" + getPackageName());
                intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, selfPackageUri);
                break;
            case WRITE_SYSTEM_SETTINGS:
                intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                break;
            case SYSTEM_ALERT_WINDOW:
                intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                break;
            default:
                intent = new Intent();
                break;
        }
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isJumpToPermissionPage) {
            String[] requestPermissions = PermissionRequester.checkSelfPermission(this, mRequestPermissions);
            final List<String> deniedPermissions = new ArrayList<>();
            final List<String> askNeverAgainPermissions = new ArrayList<>();
            for (String permission : requestPermissions) {
                deniedPermissions.add(permission);
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    askNeverAgainPermissions.add(permission);
                }
            }
            if (mListener != null)
                mListener.onRequestPermission(deniedPermissions.isEmpty(),
                        deniedPermissions, askNeverAgainPermissions);
            finish();
        }
        if (mRunnableOnResume != null) {
            mRunnableOnResume.run();
            mRunnableOnResume = null;
        }
    }

    @Override
    protected void onPause() {
        mPauseTime = System.currentTimeMillis();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            final List<String> deniedPermissions = new ArrayList<>();
            final List<String> askNeverAgainPermissions = new ArrayList<>();
            for (int i = 0; i < grantResults.length; i++) {
                int grantResult = grantResults[i];
                String permission = permissions[i];
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permission);
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        askNeverAgainPermissions.add(permission);
                    }
                }
            }
            if (!askNeverAgainPermissions.isEmpty() && getIntent().getBooleanExtra(PARAM_FORCE, false)) {
                String permissionName = getIntent().getStringExtra(PARAM_NAME);
                new BaseDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage("请到应用设置页授予" + (TextUtils.isEmpty(permissionName) ? "" : permissionName) + "权限")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                if (PermissionRequester.gotoPermissionDetail(PermissionActivity.this)) {
                                    isJumpToPermissionPage = true;
                                } else {
                                    if (mListener != null)
                                        mListener.onRequestPermission(deniedPermissions.isEmpty(),
                                                deniedPermissions, askNeverAgainPermissions);
                                    finish();
                                }
//                                try {
//                                    new PermissionPageUtils(PermissionActivity.this).jumpPermissionPage();
//                                    isJumpToPermissionPage = true;
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                    if (mListener != null)
//                                        mListener.onRequestPermission(deniedPermissions.isEmpty(),
//                                                deniedPermissions, askNeverAgainPermissions);
//                                    finish();
//                                }
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                if (mListener != null)
                                    mListener.onRequestPermission(deniedPermissions.isEmpty(),
                                            deniedPermissions, askNeverAgainPermissions);
                                finish();
                            }
                        })
                        .create().show();
            } else {
                if (mListener != null)
                    mListener.onRequestPermission(deniedPermissions.isEmpty(),
                            deniedPermissions, askNeverAgainPermissions);
                finish();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PERMISSION_SPECIAL) {
            if (System.currentTimeMillis() - mPauseTime < 10) {
                mMainHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRunnableOnResume = new Runnable() {
                            @Override
                            public void run() {
                                if (mListener != null)
                                    mListener.onRequestPermission(PermissionRequester.checkSpecialPermission(
                                            PermissionActivity.this, mSpecialPermission),
                                            null, null);
                                finish();
                            }
                        };
                    }
                }, 100);
            } else {
                if (mListener != null)
                    mListener.onRequestPermission(PermissionRequester.checkSpecialPermission(this, mSpecialPermission),
                            null, null);
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        mListener = null;
        mRequestPermissions = null;
        mSpecialPermission = null;
        mRunnableOnResume = null;
        super.onDestroy();
    }
}
