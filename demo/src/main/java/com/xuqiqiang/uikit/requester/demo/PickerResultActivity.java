package com.xuqiqiang.uikit.requester.demo;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.xuqiqiang.uikit.requester.PickerRequester;

import java.io.IOException;
import java.io.InputStream;

import static com.xuqiqiang.uikit.utils.BitmapUtils.getBitmapFromFile;

public class PickerResultActivity extends BaseActivity {
    public static final int TYPE_IMAGE = 0;
    public static final int TYPE_VIDEO = 1;
    public static final int TYPE_CONTACT = 2;

    private static final String PARAM_TYPE = "PARAM_TYPE";
    private static final String PARAM_PATH = "PARAM_PATH";
    private static final String PARAM_URI = "PARAM_URI";

    private MediaController mMediaController;

    public static void start(final Context context, final int type, String path, Uri uri) {
        Intent intent = new Intent(context, PickerResultActivity.class);
        intent.putExtra(PARAM_TYPE, type);
        intent.putExtra(PARAM_PATH, path);
        intent.putExtra(PARAM_URI, uri);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker_result);
        int type = getIntent().getIntExtra(PARAM_TYPE, TYPE_IMAGE);
        if (type == TYPE_IMAGE) {
            showImage();
        } else if (type == TYPE_VIDEO) {
            showVideo();
        } else if (type == TYPE_CONTACT) {
            showContact();
        }
    }

    private void showImage() {
        String path = getIntent().getStringExtra(PARAM_PATH);
        ImageView ivImage = findViewById(R.id.iv_image);
        ivImage.setVisibility(View.VISIBLE);
        ivImage.setImageBitmap(getBitmapFromFile(path));
    }

    private void showVideo() {
        String path = getIntent().getStringExtra(PARAM_PATH);
        VideoView videoView = findViewById(R.id.video_view);
        videoView.setVisibility(View.VISIBLE);
        videoView.setVideoPath(path);
        if (mMediaController == null) {
            mMediaController = new MediaController(this);
            videoView.setMediaController(mMediaController);
        }
        videoView.requestFocus();
        videoView.start();
    }

    private void showContact() {
        new Thread() {
            public void run() {
                ContentResolver resolver = getContentResolver();
                if (resolver == null) return;
                Uri uri = getIntent().getParcelableExtra(PARAM_URI);
                Cursor cursor = managedQuery(uri, null, null, null, null);
                cursor.moveToFirst();
                String username = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                long contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                StringBuilder info = new StringBuilder("姓名: " + username + "\n\n");
                int index = 1;
                Cursor phone = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                        null,
                        null);
                while (phone.moveToNext()) {
                    String phoneNumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    info.append("电话").append(index++).append(": ").append(phoneNumber).append("\n");
                }
                info.append("\n");
                Cursor email = resolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId,
                        null,
                        null);
                index = 1;
                while (email.moveToNext()) {
                    String emailAddress = email.getString(email.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                    info.append("邮箱").append(index++).append(": ").append(emailAddress).append("\n");
                }

                runOnUiThread(() -> {
                    TextView tvContact = findViewById(R.id.tv_contact);
                    tvContact.setVisibility(View.VISIBLE);
                    tvContact.setText(info.toString());
                });

                InputStream inputStream = PickerRequester.getContactPhoto(PickerResultActivity.this, contactId);
                if (inputStream != null) {
                    try {
                        BitmapFactory.Options opt = new BitmapFactory.Options();
                        opt.inSampleSize = 1;
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, opt);
                        runOnUiThread(() -> {
                            ImageView ivImage = findViewById(R.id.iv_image);
                            ivImage.setVisibility(View.VISIBLE);
                            ivImage.setImageBitmap(bitmap);
                        });
                    } finally {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.start();
    }

    // android.view.WindowLeaked: Activity has leaked window DecorView@c9d2553[] that was originally added here
    @Override
    protected void onPause() {
        if (mMediaController != null && isFinishing())
            mMediaController.hide();
        super.onPause();
    }
}
