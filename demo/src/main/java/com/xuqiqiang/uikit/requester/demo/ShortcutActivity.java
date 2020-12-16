package com.xuqiqiang.uikit.requester.demo;

import android.os.Bundle;
import android.view.View;

import com.xuqiqiang.uikit.requester.ShortcutRequester;
import com.xuqiqiang.uikit.utils.Logger;
import com.xuqiqiang.uikit.view.ToastMaster;

public class ShortcutActivity extends BaseActivity {

    private int mId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shortcut);
    }

    public void requestPinShortcut(View view) {
        ShortcutRequester.requestPinShortcut(this, "shortcut_" + mId++,
                "快捷方式", ShortcutActivity.class, R.mipmap.ic_launcher_round, null, success -> {
                    ToastMaster.showToast(ShortcutActivity.this,
                            success ? ("已创建快捷方式，id为" + (mId - 1)) : "无法创建快捷方式");
                });
    }
}
