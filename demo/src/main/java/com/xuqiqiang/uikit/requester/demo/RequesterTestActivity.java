package com.xuqiqiang.uikit.requester.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.xuqiqiang.uikit.requester.ActivityRequester;
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
        ToastMaster.showToast(this, "回来后会提示onDestroyed");
        mRunnable = () -> ToastMaster.showToast(this, "onResume");
        startActivity(new Intent(this, RequesterTargetActivity.class));
        ActivityRequester.postOnResume(this, mRunnable);
    }

    public void postOnDestroyed(View view) {
        ToastMaster.showToast(this, "退出后会提示onDestroyed");
        mRunnable = () -> ToastMaster.showToast(this, "onDestroyed");
        ActivityRequester.postOnDestroyed(this, mRunnable);
//        finish();
    }

    public void postDelayed(View view) {
        mRunnable = () -> ToastMaster.showToast(this, "postDelayed");
        ActivityRequester.postDelayed(this, mRunnable, 2000);
    }

    public void removeCallbacks(View view) {
        ToastMaster.showToast(this, "已移除Callback");
        ActivityRequester.removeCallbacks(mRunnable);
    }
}
