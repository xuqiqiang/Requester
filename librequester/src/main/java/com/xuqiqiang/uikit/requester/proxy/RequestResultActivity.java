package com.xuqiqiang.uikit.requester.proxy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.xuqiqiang.uikit.requester.ActivityRequester;

public class RequestResultActivity extends Activity {
    private static final int REQUEST_ACTIVITY_RESULT = 0x005000;
    private static Intent mIntent;
    private static ActivityRequester.OnActivityResultListener mListener;

    public static void start(Context context, Intent intent,
        ActivityRequester.OnActivityResultListener listener) {
        mIntent = intent;
        mListener = listener;
        Intent i = new Intent(context, RequestResultActivity.class);
        if (!(context instanceof Activity)) {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mIntent == null) {
            finish();
            return;
        }
        startActivityForResult(mIntent, REQUEST_ACTIVITY_RESULT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ACTIVITY_RESULT) {
            if (mListener != null) {
                mListener.onActivityResult(resultCode, data);
            }
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        mIntent = null;
        mListener = null;
        super.onDestroy();
    }
}
