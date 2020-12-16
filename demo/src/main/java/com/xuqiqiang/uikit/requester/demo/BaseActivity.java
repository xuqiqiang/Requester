package com.xuqiqiang.uikit.requester.demo;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;

import com.xuqiqiang.uikit.activity.BaseAppCompatActivity;

public class BaseActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
