package com.xuqiqiang.uikit.requester.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.xuqiqiang.uikit.requester.ActivityRequester;
import com.xuqiqiang.uikit.utils.Logger;
import com.xuqiqiang.uikit.view.ToastMaster;

import static com.xuqiqiang.uikit.requester.demo.RequesterTargetActivity.PARAM_NAME;
import static com.xuqiqiang.uikit.requester.demo.RequesterTargetActivity.PARAM_RESULT;

public class RequesterTestActivity extends BaseActivity {

    private Runnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requester);
    }

    public void startActivityForResult(View view) {
        Intent intent = new Intent(this, RequesterTargetActivity.class);
        intent.putExtra(PARAM_NAME, "Harry");
        ActivityRequester.startActivityForResult(this, intent, (resultCode, data) -> {
            if (Activity.RESULT_OK == resultCode && data != null)
                ToastMaster.showToast(this, data.getStringExtra(PARAM_RESULT));
            else
                ToastMaster.showToast(this, "事件被取消");
        });
    }

    public void postOnResume(View view) {
        startActivity(new Intent(this, RequesterTargetActivity.class));
        ActivityRequester.postOnResume(this, () -> ToastMaster.showToast(this, "onResume"));
    }

    public void postOnDestroyed(View view) {
        ActivityRequester.postOnDestroyed(this, () -> ToastMaster.showToast(this, "onDestroyed"));
        finish();
    }

    public void postDelayed(View view) {
//        mRunnable = () -> ToastMaster.showToast(this, "postDelayed");
        mRunnable = () -> Logger.d("postDelayed");
        ActivityRequester.postDelayed(this, mRunnable, 2000);
    }

    public void removeCallbacks(View view) {
        ActivityRequester.removeCallbacks(mRunnable);
    }
}
