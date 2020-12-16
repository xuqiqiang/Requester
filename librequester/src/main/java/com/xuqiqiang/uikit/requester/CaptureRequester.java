package com.xuqiqiang.uikit.requester;

import android.content.Context;

import com.xuqiqiang.uikit.requester.proxy.CaptureActivity;
import com.xuqiqiang.uikit.utils.Cache;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CaptureRequester {

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    public static void capImage(Context context, OnCaptureListener listener) {
        CaptureActivity.start(context, MEDIA_TYPE_IMAGE, null,
                1, 0, 0, listener);
    }

    public static void capVideo(Context context, OnCaptureListener listener) {
        CaptureActivity.start(context, MEDIA_TYPE_VIDEO, null,
                1, 0, 0, listener);
    }

    public static void capture(Context context, int type, String outputPath,
                               int videoQuality, long sizeLimit,
                               int durationLimit, OnCaptureListener listener) {
        CaptureActivity.start(context, type, outputPath, videoQuality,
                sizeLimit, durationLimit, listener);
    }

    public static String getOutputMediaFilePath(int type) {
        String saveDirPath = Cache.getStoreDir().getPath() + "/uikit/requester";
        if (!Cache.createDir(saveDirPath)) {
            return null;
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        if (type == MEDIA_TYPE_IMAGE) {
            return saveDirPath + File.separator + "IMG_" + "_" + timeStamp + ".jpg";
        } else if (type == MEDIA_TYPE_VIDEO) {
            return saveDirPath + File.separator + "VID_" + "_" + timeStamp + ".mp4";
        }
        return null;
    }

    public interface OnCaptureListener {
        void onCapture(String path);
    }
}