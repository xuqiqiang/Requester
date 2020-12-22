package com.xuqiqiang.uikit.requester.demo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.xuqiqiang.uikit.requester.CaptureRequester;

import static com.xuqiqiang.uikit.requester.demo.PickerResultActivity.TYPE_IMAGE;
import static com.xuqiqiang.uikit.requester.demo.PickerResultActivity.TYPE_VIDEO;

public class CaptureActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
    }

    public void pickImage(View view) {
        CaptureRequester.capImage(this, path -> {
            if (!TextUtils.isEmpty(path)) {
                PickerResultActivity.start(this, TYPE_IMAGE, path, null);
            }
        });
    }

    public void pickVideo(View view) {
        CaptureRequester.capVideo(this, path -> {
            if (!TextUtils.isEmpty(path)) {
                PickerResultActivity.start(this, TYPE_VIDEO, path, null);
            }
        });
    }
}
