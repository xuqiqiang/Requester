package com.xuqiqiang.uikit.requester;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.xuqiqiang.uikit.requester.proxy.PickerActivity;

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