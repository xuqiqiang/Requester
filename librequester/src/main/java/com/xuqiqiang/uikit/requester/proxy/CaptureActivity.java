package com.xuqiqiang.uikit.requester.proxy;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.xuqiqiang.uikit.requester.CaptureRequester;
import com.xuqiqiang.uikit.requester.PermissionRequester;
import com.xuqiqiang.uikit.view.ToastMaster;

import java.io.File;

import static android.provider.MediaStore.EXTRA_DURATION_LIMIT;
import static android.provider.MediaStore.EXTRA_OUTPUT;
import static android.provider.MediaStore.EXTRA_SIZE_LIMIT;
import static android.provider.MediaStore.EXTRA_VIDEO_QUALITY;
import static com.xuqiqiang.uikit.requester.CaptureRequester.MEDIA_TYPE_IMAGE;
import static com.xuqiqiang.uikit.requester.CaptureRequester.getOutputMediaFilePath;

public class CaptureActivity extends Activity {
    private static final int REQUEST_CAPTURE = 0x003001;
    private static final String PARAM_TYPE = "PARAM_TYPE";
    private static CaptureRequester.OnCaptureListener mOnCaptureListener;
    private Uri mCaptureUri;
    private String mCapturePath;

    public static void start(final Context context, final int type, final String outputPath, final int videoQuality, final long sizeLimit,
                             final int durationLimit, final CaptureRequester.OnCaptureListener listener) {
        PermissionRequester.requestForce(context, "存储/相机", new PermissionRequester.OnSimplePermissionListener() {
            @Override
            public void onRequestPermission(boolean success) {
                if (success) {
                    mOnCaptureListener = listener;
                    Intent intent = new Intent(context, CaptureActivity.class);
                    intent.putExtra(PARAM_TYPE, type);
                    intent.putExtra(EXTRA_OUTPUT, outputPath);
                    intent.putExtra(EXTRA_VIDEO_QUALITY, videoQuality);
                    intent.putExtra(EXTRA_SIZE_LIMIT, sizeLimit);
                    intent.putExtra(EXTRA_DURATION_LIMIT, durationLimit);
                    if (!(context instanceof Activity))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        int type = intent.getIntExtra(PARAM_TYPE, MEDIA_TYPE_IMAGE);
        mCapturePath = intent.getStringExtra(EXTRA_OUTPUT);
        if (TextUtils.isEmpty(mCapturePath)) mCapturePath = getOutputMediaFilePath(type);
        if (TextUtils.isEmpty(mCapturePath)) {
            ToastMaster.showToast(this, "没有足够的权限");
            if (mOnCaptureListener != null) mOnCaptureListener.onCapture(null);
            finish();
            return;
        }
        mCaptureUri = Uri.fromFile(new File(mCapturePath));

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        Intent i = new Intent(type == MEDIA_TYPE_IMAGE ? MediaStore.ACTION_IMAGE_CAPTURE
                : MediaStore.ACTION_VIDEO_CAPTURE);
        i.putExtra(EXTRA_OUTPUT, mCaptureUri);
        if (type != MEDIA_TYPE_IMAGE) {
            i.putExtra(EXTRA_VIDEO_QUALITY, intent.getIntExtra(EXTRA_VIDEO_QUALITY, 1));
            i.putExtra(EXTRA_SIZE_LIMIT, intent.getLongExtra(EXTRA_SIZE_LIMIT, 0));
            i.putExtra(EXTRA_DURATION_LIMIT, intent.getIntExtra(EXTRA_DURATION_LIMIT, 0));
        }
        startActivityForResult(i, REQUEST_CAPTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CAPTURE) {
            if (mOnCaptureListener != null) mOnCaptureListener.onCapture(
                    resultCode == Activity.RESULT_OK ? mCapturePath : null);
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        mOnCaptureListener = null;
        super.onDestroy();
    }
}
