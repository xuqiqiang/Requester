package com.xuqiqiang.uikit.requester.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class RequesterTargetActivity extends BaseActivity {
    public static final String PARAM_NAME = "PARAM_NAME";
    public static final String PARAM_RESULT = "PARAM_RESULT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requester_target);
    }

    public void sendResult(View view) {
        Intent intent = new Intent();
        intent.putExtra(PARAM_RESULT, "Hello " + getIntent().getStringExtra(PARAM_NAME));
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
