package com.xuqiqiang.uikit.requester.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.ActionBar;
import com.xuqiqiang.uikit.activity.BaseAppCompatActivity;
import com.xuqiqiang.uikit.view.ToastMaster;

public class BaseActivity extends BaseAppCompatActivity {

    private static final String SOURCE_CODE_URL =
        "https://github.com/xuqiqiang/Requester/blob/main/demo/src/main/java/com/xuqiqiang/uikit/requester/demo/";

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu.size() == 0) {
            getMenuInflater().inflate(R.menu.menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_source_code) {
            Uri uri = Uri.parse(SOURCE_CODE_URL + getClass().getSimpleName() + ".java");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                ToastMaster.showToast(this, "未安装浏览器");
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
