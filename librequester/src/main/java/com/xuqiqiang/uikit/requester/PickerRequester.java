package com.xuqiqiang.uikit.requester;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.provider.MediaStore;

import androidx.annotation.Nullable;

import com.xuqiqiang.uikit.requester.proxy.PickerActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static com.xuqiqiang.uikit.requester.CaptureRequester.MEDIA_TYPE_IMAGE;
import static com.xuqiqiang.uikit.requester.CaptureRequester.getOutputMediaFilePath;

public class PickerRequester {

    public static void pickImage(Context context, OnPickUriListener listener) {
        PickerActivity.start(context, "image/*", listener);
    }

    public static void pickImage(Context context, OnPickPathListener listener) {
        PickerActivity.start(context, "image/*", listener);
    }

    public static void pickVideo(Context context, OnPickUriListener listener) {
        PickerActivity.start(context, "video/*", listener);
    }

    public static void pickVideo(Context context, OnPickPathListener listener) {
        PickerActivity.start(context, "video/*", listener);
    }

    /**
     * @param type The MIME type of the data being picked.
     */
    public static void pickFile(Context context, String type, OnPickUriListener listener) {
        PickerActivity.start(context, type, listener);
    }

    /**
     * @param type The MIME type of the data being picked.
     */
    public static void pickFile(Context context, String type, OnPickPathListener listener) {
        PickerActivity.start(context, type, listener);
    }

    public static void cropImage(final Context context, final String srcPath, final OnPickPathListener listener) {
        cropImage(context, srcPath, getOutputMediaFilePath(MEDIA_TYPE_IMAGE), listener);
    }

    public static void cropImage(final Context context, final String srcPath, final String dstPath, final OnPickPathListener listener) {
        PermissionRequester.requestForce(context, "存储", new PermissionRequester.OnSimplePermissionListener() {
            @Override
            public void onRequestPermission(boolean success) {
                if (success) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                        StrictMode.setVmPolicy(builder.build());
                        builder.detectFileUriExposure();
                    }
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(Uri.fromFile(new File(srcPath)), "image/*");
                    intent.putExtra("crop", "true");
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(dstPath)));
                    intent.putExtra("return-data", false);
                    intent.putExtra("scaleUpIfNeeded", true);
                    intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                    intent.putExtra("noFaceDetection", true); // no face detection
                    ActivityRequester.startActivityForResult(context, intent, new ActivityRequester.OnActivityResultListener() {
                        @Override
                        public void onActivityResult(final int resultCode, @Nullable final Intent data) {
                            if (listener != null)
                                listener.onPick(resultCode == Activity.RESULT_OK ? dstPath : null);
                        }
                    });
                }
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public static void pickContact(final Context context, final OnPickUriListener listener) {
        PermissionRequester.requestForce(context, "联系人", new PermissionRequester.OnSimplePermissionListener() {
            @Override
            public void onRequestPermission(boolean success) {
                if (success) {
                    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    ActivityRequester.startActivityForResult(context, intent, new ActivityRequester.OnActivityResultListener() {
                        @Override
                        public void onActivityResult(final int resultCode, @Nullable final Intent data) {
                            if (listener != null)
                                listener.onPick(resultCode == Activity.RESULT_OK && data != null ? data.getData() : null);
                        }
                    });
                }
            }
        }, Manifest.permission.READ_CONTACTS);
    }

    public static InputStream getContactPhoto(Context context, long contactId) {
        ContentResolver resolver = context.getContentResolver();
        if (resolver == null) return null;
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri displayPhotoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);
        try {
            AssetFileDescriptor fd = resolver.openAssetFileDescriptor(displayPhotoUri, "r");
            return fd.createInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("ResourceType")
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static Uri ensureUriPermission(Context context, Intent intent) {
        Uri uri = intent.getData();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final int takeFlags = intent.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
            context.getContentResolver().takePersistableUriPermission(uri, takeFlags);
        }
        return uri;
    }

    public interface OnPickUriListener {
        void onPick(Uri uri);
    }

    public interface OnPickPathListener {
        void onPick(String path);
    }
}