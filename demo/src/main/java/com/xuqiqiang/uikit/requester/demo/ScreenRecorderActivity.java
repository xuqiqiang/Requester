package com.xuqiqiang.uikit.requester.demo;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.xuqiqiang.uikit.requester.ScreenRecorderRequester;
import com.xuqiqiang.uikit.view.ToastMaster;

import java.io.File;

import static com.xuqiqiang.uikit.requester.demo.PickerResultActivity.TYPE_VIDEO;
import static com.xuqiqiang.uikit.utils.Utils.mMainHandler;

public class ScreenRecorderActivity extends BaseActivity {

    private ScreenRecorderRequester mScreenRecorderRequester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_recorder);
    }

    public void startRecording(View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ToastMaster.showToast(this, "不支持5.0以下的系统版本");
            return;
        }
        if (mScreenRecorderRequester == null)
            mScreenRecorderRequester = new ScreenRecorderRequester(this);
        if (mScreenRecorderRequester.isCapturing()) return;
        mScreenRecorderRequester.startCapturing(new ScreenRecorderRequester.ScreenRecorderListener() {

            @Override
            public void onStart() {
                moveTaskToBack(true);
            }

            @Override
            public void onComplete(File file) {
                mMainHandler.post(() -> {
                    PickerResultActivity.start(ScreenRecorderActivity.this, TYPE_VIDEO, file.getPath(), null);
                });
            }
        });
    }

    public void stopRecording(View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ToastMaster.showToast(this, "不支持5.0以下的系统版本");
            return;
        }
        if (mScreenRecorderRequester != null)
            mScreenRecorderRequester.stopCapturing();
    }

    @Override
    public void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mScreenRecorderRequester != null) {
            mScreenRecorderRequester.onDestroy();
        }
        super.onDestroy();
    }
}
