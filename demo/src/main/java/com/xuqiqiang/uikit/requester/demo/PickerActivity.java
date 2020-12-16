package com.xuqiqiang.uikit.requester.demo;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.xuqiqiang.uikit.requester.PickerRequester;

import static com.xuqiqiang.uikit.utils.BitmapUtils.getBitmapFromFile;

public class PickerActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker);
    }

    public void pickImage(View view) {
        PickerRequester.pickImage(this, (PickerRequester.OnPickPathListener) path -> {
            if (!TextUtils.isEmpty(path)) {
                ImageView ivImage = findViewById(R.id.iv_image);
                VideoView videoView = findViewById(R.id.video_view);
                ivImage.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.GONE);
                ivImage.setImageBitmap(getBitmapFromFile(path));
            }
        });
    }

    public void pickVideo(View view) {
        PickerRequester.pickVideo(this, (PickerRequester.OnPickPathListener) path -> {
            if (!TextUtils.isEmpty(path)) {
                ImageView ivImage = findViewById(R.id.iv_image);
                VideoView videoView = findViewById(R.id.video_view);
                ivImage.setVisibility(View.GONE);
                videoView.setVisibility(View.VISIBLE);
                videoView.setVideoPath(path);
                MediaController mediaController = new MediaController(this);
                videoView.setMediaController(mediaController);
                videoView.requestFocus();
                videoView.start();
            }
        });
    }
}