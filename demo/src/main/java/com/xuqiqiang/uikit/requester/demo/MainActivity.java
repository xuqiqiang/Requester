package com.xuqiqiang.uikit.requester.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void test1(View view) {
        startActivity(new Intent(this, PermissionActivity.class));
    }

    public void test2(View view) {
        startActivity(new Intent(this, PickerActivity.class));
    }

    public void test3(View view) {
        startActivity(new Intent(this, CaptureActivity.class));
    }

    public void test4(View view) {
        startActivity(new Intent(this, KeyguardActivity.class));
    }

    public void test5(View view) {
        startActivity(new Intent(this, ShortcutActivity.class));
    }

    public void test6(View view) {
        startActivity(new Intent(this, RequesterTestActivity.class));
    }

    public void test7(View view) {
        startActivity(new Intent(this, ScreenRecorderActivity.class));
    }
}
