package com.xuqiqiang.uikit.requester.demo;

import android.os.Bundle;
import android.view.View;

import com.xuqiqiang.uikit.requester.DownloadRequester;
import com.xuqiqiang.uikit.utils.Logger;

public class DownloadActivity extends BaseActivity {

    private static final String DOWNLOAD_URL = "https://down.qq.com/qqweb/PCQQ/PCQQ_EXE/PCQQ2020.exe";
    private long mDownloadId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
    }

    public void startDownload(View view) {
        mDownloadId = DownloadRequester.download(this, DOWNLOAD_URL, new DownloadRequester.DownloadAdapter() {
            @Override
            public void onStart(long size) {
                Logger.d("Download", "onStart", size + "");
            }

            @Override
            public void onError() {
                Logger.d("Download", "onError");
            }

            @Override
            public void onPaused() {
                Logger.d("Download", "onError");
            }

            @Override
            public void onProcess(long size, long downloadedSize) {
                Logger.d("Download", "onProcess", size + "", downloadedSize + "");
            }

            @Override
            public void onComplete(String path) {
                Logger.d("Download", "onComplete", path + "");
            }
        });
    }

    public void cancelDownload(View view) {
        DownloadRequester.cancel(this, mDownloadId);
    }
}
