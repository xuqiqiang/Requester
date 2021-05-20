package com.xuqiqiang.uikit.requester.proxy;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.xuqiqiang.uikit.requester.PermissionRequester;
import com.xuqiqiang.uikit.requester.PickerRequester;
import com.xuqiqiang.uikit.utils.UriUtils;

public class PickerActivity extends Activity {
    private static final int REQUEST_PICK_IMAGE = 0x002001;
    private static final int REQUEST_SAF_PICK_IMAGE = 0x002002;
    private static final String PARAM_TYPE = "PARAM_TYPE";
    private static PickerRequester.OnPickUriListener mOnPickUriListener;
    private static PickerRequester.OnPickPathListener mOnPickPathListener;

    public static void start(final Context context, final String type, final PickerRequester.OnPickUriListener listener) {
        PermissionRequester.requestForce(context, "存储", new PermissionRequester.OnSimplePermissionListener() {
            @Override
            public void onRequestPermission(boolean success) {
                if (success) {
                    mOnPickUriListener = listener;
                    Intent intent = new Intent(context, PickerActivity.class);
                    intent.putExtra(PARAM_TYPE, type);
                    if (!(context instanceof Activity))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public static void start(final Context context, final String type, final PickerRequester.OnPickPathListener listener) {
        PermissionRequester.requestForce(context, "存储", new PermissionRequester.OnSimplePermissionListener() {
            @Override
            public void onRequestPermission(boolean success) {
                if (success) {
                    mOnPickPathListener = listener;
                    Intent intent = new Intent(context, PickerActivity.class);
                    intent.putExtra(PARAM_TYPE, type);
                    if (!(context instanceof Activity))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickFile(getIntent().getStringExtra(PARAM_TYPE));
    }

    private void pickFile(String type) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType(type),
                    REQUEST_PICK_IMAGE);
        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType(type);
            startActivityForResult(intent, REQUEST_SAF_PICK_IMAGE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_IMAGE || requestCode == REQUEST_SAF_PICK_IMAGE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                if (requestCode == REQUEST_PICK_IMAGE) onPick(data.getData());
                else if (requestCode == REQUEST_SAF_PICK_IMAGE)
                    onPick(PickerRequester.ensureUriPermission(this, data));
            } else {
                onPick(null);
            }
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onPick(Uri uri) {
        if (mOnPickUriListener != null) mOnPickUriListener.onPick(uri);
        else if (mOnPickPathListener != null) {
            String path = null;
            if (uri != null) {
                try {
                    path = UriUtils.getPath(this, uri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mOnPickPathListener.onPick(path);
        }
    }

    @Override
    protected void onDestroy() {
        mOnPickUriListener = null;
        mOnPickPathListener = null;
        super.onDestroy();
    }
}
