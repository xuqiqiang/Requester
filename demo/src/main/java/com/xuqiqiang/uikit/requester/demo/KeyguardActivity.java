package com.xuqiqiang.uikit.requester.demo;

import android.os.Bundle;
import android.view.View;

import com.xuqiqiang.uikit.requester.KeyguardRequester;
import com.xuqiqiang.uikit.view.ToastMaster;

public class KeyguardActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyguard);
    }

    public void checkKeyguardSecure(View view) {
        ToastMaster.showToast(this, KeyguardRequester.isKeyguardSecure(this) ?
                "当前已经设置了密码锁" : "当前还没设置密码锁");
    }

    public void checkKeyguardSecureForKeyword(View view) {
        if (KeyguardRequester.checkKeyguardSecureForKeyword(this))
            ToastMaster.showToast(this, "当前已经设置了密码锁");
    }

    public void requestAuthentication(View view) {
        KeyguardRequester.requestAuthentication(this, success -> {
            ToastMaster.showToast(this, success ?
                    "解锁成功" : "解锁验证出错");
        });
    }
}
