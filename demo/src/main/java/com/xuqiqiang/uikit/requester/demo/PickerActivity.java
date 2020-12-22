package com.xuqiqiang.uikit.requester.demo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.xuqiqiang.uikit.requester.PickerRequester;

import static com.xuqiqiang.uikit.requester.demo.PickerResultActivity.TYPE_CONTACT;
import static com.xuqiqiang.uikit.requester.demo.PickerResultActivity.TYPE_IMAGE;
import static com.xuqiqiang.uikit.requester.demo.PickerResultActivity.TYPE_VIDEO;

public class PickerActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker);
    }

    public void pickImage(View view) {
        PickerRequester.pickImage(this, (PickerRequester.OnPickPathListener) path -> {
            if (!TextUtils.isEmpty(path)) {
                PickerResultActivity.start(this, TYPE_IMAGE, path, null);
            }
        });
    }

    public void pickVideo(View view) {
        PickerRequester.pickVideo(this, (PickerRequester.OnPickPathListener) path -> {
            if (!TextUtils.isEmpty(path)) {
                PickerResultActivity.start(this, TYPE_VIDEO, path, null);
            }
        });
    }

    public void cropImage(View view) {
        PickerRequester.pickImage(this, (PickerRequester.OnPickPathListener) path -> {
            if (!TextUtils.isEmpty(path)) {
                PickerRequester.cropImage(this, path, cropPath -> {
                    if (!TextUtils.isEmpty(cropPath)) {
                        PickerResultActivity.start(this, TYPE_IMAGE, cropPath, null);
                    }
                });
            }
        });
    }

    public void pickContact(View view) {
        PickerRequester.pickContact(this, uri -> {
            if (uri != null) {
                PickerResultActivity.start(this, TYPE_CONTACT, null, uri);
            }
        });
    }
}
