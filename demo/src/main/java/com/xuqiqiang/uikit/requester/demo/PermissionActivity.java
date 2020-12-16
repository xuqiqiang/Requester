package com.xuqiqiang.uikit.requester.demo;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.xuqiqiang.uikit.requester.PermissionRequester;
import com.xuqiqiang.uikit.utils.ArrayUtils;
import com.xuqiqiang.uikit.view.ToastMaster;

public class PermissionActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_permission, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_goto_permission) {
            PermissionRequester.gotoPermissionDetail(this);
            return true;
        } else if (id == R.id.action_goto_app_permission) {
            PermissionRequester.gotoAppDetail(this, 0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 检查打电话有没有权限
    public void checkPermission(View view) {
        ToastMaster.showToast(this, PermissionRequester.checkPermissions(this,
                Manifest.permission.CALL_PHONE) ? "你已经同意了该权限" : "你还没有同意该权限");
    }

    // 我要打电话
    public void requestPermission(View view) {
        PermissionRequester.requestForce(this, "拨号", success -> {
            if (success) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:10086"));
                startActivity(new Intent(intent));
            }
        }, Manifest.permission.CALL_PHONE);
    }

    // 检查读写外部内存、读写联系人、调用相机Api权限
    public void checkPermissions(View view) {
        String[] deniedPermissions = PermissionRequester.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_CONTACTS, Manifest.permission.CAMERA);
        showDeniedPermissions(deniedPermissions);
    }

    // 获取读写外部内存、读写联系人、调用相机Api权限
    public void requestPermissions(View view) {
        PermissionRequester.requestForce(this, "拨号", (success, deniedPermissions, rejectPermissions) -> {
            showDeniedPermissions(deniedPermissions == null ? null : deniedPermissions.toArray(new String[0]));
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_CONTACTS, Manifest.permission.CAMERA);
    }

    private void showDeniedPermissions(String[] rejectedPermissions) {
        if (ArrayUtils.isEmpty(rejectedPermissions)) {
            ToastMaster.showToast(this, "所有权限已经同意");
        } else {
            StringBuilder buffer = new StringBuilder();
            for (String permission : rejectedPermissions) {
                buffer.append(permission).append(",");
            }
            buffer.deleteCharAt(buffer.length() - 1);
            ToastMaster.showToast(this, buffer.toString() + "权限还没有被同意");
        }
    }

    // region 特殊权限
    // 检查是否允许通知
    public void checkDisplayNotification(View view) {
        checkSpecialPermission(PermissionRequester.SpecialPermission.DISPLAY_NOTIFICATION);
    }

    // 请求允许通知
    public void requestDisplayNotification(View view) {
        requestSpecialPermission(PermissionRequester.SpecialPermission.DISPLAY_NOTIFICATION);
    }

    // 检查是否允许安装未知来源应用
    public void checkInstallApp(View view) {
        checkSpecialPermission(PermissionRequester.SpecialPermission.INSTALL_UNKNOWN_APP);
    }

    // 请求允许安装未知来源应用
    public void requestInstallApp(View view) {
        requestSpecialPermission(PermissionRequester.SpecialPermission.INSTALL_UNKNOWN_APP);
    }

    // 检查是否允许修改系统设置
    public void checkWriteSettings(View view) {
        checkSpecialPermission(PermissionRequester.SpecialPermission.WRITE_SYSTEM_SETTINGS);
    }

    // 请求允许修改系统设置
    public void requestWriteSettings(View view) {
        requestSpecialPermission(PermissionRequester.SpecialPermission.WRITE_SYSTEM_SETTINGS);
    }

    // 检查是否允许设置悬浮窗
    public void checkSystemWindow(View view) {
        checkSpecialPermission(PermissionRequester.SpecialPermission.SYSTEM_ALERT_WINDOW);
    }

    // 请求允许设置悬浮窗
    public void requestSystemWindow(View view) {
        requestSpecialPermission(PermissionRequester.SpecialPermission.SYSTEM_ALERT_WINDOW);
    }

    public void checkSpecialPermission(String specialPermission) {
        ToastMaster.showToast(this, PermissionRequester.checkSpecialPermission(this,
                specialPermission) ? "你已经同意了该权限" : "你还没有同意该权限");
    }

    public void requestSpecialPermission(String specialPermission) {
        PermissionRequester.requestSpecialPermission(this, specialPermission,
                success -> ToastMaster.showToast(this, success ? "你已经同意了该权限" : "你还没有同意该权限"));
    }
    // endregion
}
