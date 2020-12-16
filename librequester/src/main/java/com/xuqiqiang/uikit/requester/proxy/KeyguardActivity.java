package com.xuqiqiang.uikit.requester.proxy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.xuqiqiang.uikit.R;
import com.xuqiqiang.uikit.requester.KeyguardRequester;
import com.xuqiqiang.uikit.utils.Logger;
import com.xuqiqiang.uikit.view.ToastMaster;

public class KeyguardActivity extends Activity {
    private static final int REQUEST_DEVICE_CREDENTIALS = 0x001000;
    private static KeyguardRequester.OnAuthenticationListener mListener;

    public static void start(Context context, KeyguardRequester.OnAuthenticationListener listener) {
        mListener = listener;
        Intent intent = new Intent(context, KeyguardActivity.class);
        if (!(context instanceof Activity))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!KeyguardRequester.requestAuthentication(this, REQUEST_DEVICE_CREDENTIALS)) {
            ToastMaster.showToast(this, "无法请求解锁");
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_DEVICE_CREDENTIALS) {
            Logger.d("PHOTO_DEVICE_CREDENTIALS:" + resultCode);
            if (mListener != null)
                mListener.onRequestAuthentication(resultCode == Activity.RESULT_OK);
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        mListener = null;
        super.onDestroy();
    }
}
