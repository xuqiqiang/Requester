package com.xuqiqiang.uikit.requester;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodecInfo;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.util.Range;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.xuqiqiang.uikit.requester.screenrecorder.AudioEncodeConfig;
import com.xuqiqiang.uikit.requester.screenrecorder.Notifications;
import com.xuqiqiang.uikit.requester.screenrecorder.ScreenRecorder;
import com.xuqiqiang.uikit.requester.screenrecorder.ScreenRecorderUtils;
import com.xuqiqiang.uikit.requester.screenrecorder.VideoEncodeConfig;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MEDIA_PROJECTION_SERVICE;
import static com.xuqiqiang.uikit.requester.screenrecorder.ScreenRecorder.AUDIO_AAC;
import static com.xuqiqiang.uikit.requester.screenrecorder.ScreenRecorder.VIDEO_AVC;
import static com.xuqiqiang.uikit.utils.Utils.mMainHandler;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ScreenRecorderRequester {
    public static final String ACTION_STOP = BuildConfig.LIBRARY_PACKAGE_NAME + ".action.STOP";
    // members below will be initialized in onCreate()
    private final MediaProjectionManager mMediaProjectionManager;
    private final Notifications mNotifications;
    //    private static final int REQUEST_PERMISSIONS = 2;
    private Activity mContext;
    /**
     * <b>NOTE:</b>
     * {@code ScreenRecorder} should run in background Service
     * instead of a foreground Activity in this demonstrate.
     */
    private ScreenRecorder mRecorder;
    private final BroadcastReceiver mStopActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_STOP.equals(intent.getAction())) {
                stopRecorder();
            }
        }
    };
    private final MediaProjection.Callback mProjectionCallback = new MediaProjection.Callback() {
        @Override
        public void onStop() {
            if (mRecorder != null) {
                stopRecorder();
            }
        }
    };
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaCodecInfo[] mAvcCodecInfos; // avc codecs
    private MediaCodecInfo[] mAacCodecInfos; // aac codecs
    //    private String mVideoCodec;
    private MediaCodecInfo mAvcCodecInfo; // avc codecs
    private MediaCodecInfo mAacCodecInfo; // aac codecs
    private int mWidth;
    private int mHeight;
    private int framerate;
    private int iframe;
    private int videoBitrate; //kbps
    private MediaCodecInfo.CodecProfileLevel profileLevel;
    private boolean withAudio;
    private List<Integer> audioBitrates;
    private int audioBitrate;
    private int[] sampleRates;
    private int sampleRate;
    private int audioChannelCount;
    private int audioProfile;
    private ScreenRecorderListener mScreenRecorderListener;

    public ScreenRecorderRequester(Activity context) {
        mContext = context;
        mMediaProjectionManager = (MediaProjectionManager) context.getApplicationContext()
                .getSystemService(MEDIA_PROJECTION_SERVICE);
        mNotifications = new Notifications(context.getApplicationContext());
        initParam();
    }

    private static File getSavingDir() {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                "Screenshots");
    }

    private void initParam() {
        mAvcCodecInfos = ScreenRecorderUtils.findEncodersByType(VIDEO_AVC);
        if (mAvcCodecInfos.length > 0)
            mAvcCodecInfo = mAvcCodecInfos[0];
        mAacCodecInfos = ScreenRecorderUtils.findEncodersByType(AUDIO_AAC);
        if (mAacCodecInfos.length > 0)
            mAacCodecInfo = mAacCodecInfos[0];

        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        android.graphics.Point point = new android.graphics.Point();
        display.getRealSize(point);
        mWidth = point.x;
        mHeight = point.y;
//        mWidth = 1080;
//        mHeight = 1920;
        framerate = 60;
        iframe = 1;
        videoBitrate = 10000 * 1000;
        profileLevel = ScreenRecorderUtils.toProfileLevel("Default");

        withAudio = true;

        MediaCodecInfo.CodecCapabilities capabilities = mAacCodecInfo.getCapabilitiesForType(AUDIO_AAC);
        Range<Integer> bitrateRange = capabilities.getAudioCapabilities().getBitrateRange();
        int lower = Math.max(bitrateRange.getLower() / 1000, 80);
        int upper = bitrateRange.getUpper() / 1000;
        audioBitrates = new ArrayList<>();
        for (int rate = lower; rate < upper; rate += lower) {
            audioBitrates.add(rate);
        }
        audioBitrates.add(upper);
        audioBitrate = audioBitrates.get(0) * 1000;

        sampleRates = capabilities.getAudioCapabilities().getSupportedSampleRates();
        sampleRate = sampleRates[0];
        for (int rate : sampleRates) {
            if (rate == 44100) {
                sampleRate = rate;
            }
        }

        audioChannelCount = 1;
        audioProfile = MediaCodecInfo.CodecProfileLevel.AACObjectMain;
    }

    public void setWithAudio(boolean withAudio) {
        this.withAudio = withAudio;
    }

    public void startCapturing(final ScreenRecorderListener listener) {
        PermissionRequester.requestForce(mContext, withAudio ? "存储和录音" : "存储",
                new PermissionRequester.OnSimplePermissionListener() {
                    @Override
                    public void onRequestPermission(boolean success) {
                        if (success) {
                            mScreenRecorderListener = listener;
                            if (mMediaProjection == null) {
                                requestMediaProjection();
                            } else {
                                startCapturing(mMediaProjection);
                            }
                        }
                    }
                },
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                withAudio ? Manifest.permission.RECORD_AUDIO : null);
    }

    public boolean hasCaptured() {
        return mMediaProjection != null;
    }

    public void stopCapturing() {
        stopRecorder();
    }

    public boolean isCapturing() {
        return mRecorder != null;
    }

    private void requestMediaProjection() {
        Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
        ActivityRequester.startActivityForResult(mContext, captureIntent, new ActivityRequester.OnActivityResultListener() {
            @Override
            public void onActivityResult(final int resultCode, @Nullable final Intent data) {
                ActivityRequester.postOnResume(mContext, new Runnable() {
                    @Override
                    public void run() {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            return;
                        }
                        mMainHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ScreenRecorderRequester.this.onActivityResult(resultCode, data);
                            }
                        }, 500);
                    }
                });
            }
        });
    }

    private boolean startCapturing(MediaProjection mediaProjection) {
        VideoEncodeConfig video = createVideoConfig();
        AudioEncodeConfig audio = createAudioConfig(); // audio can be null
        if (video == null) {
//            toast(getString(R.string.create_screenRecorder_error));
            if (mScreenRecorderListener != null)
                mScreenRecorderListener.onError(new RuntimeException(
                        mContext.getString(R.string.create_screenRecorder_error)));
            return false;
        }

        File dir = getSavingDir();
        if (!dir.exists() && !dir.mkdirs()) {
            stopRecorder();
            if (mScreenRecorderListener != null)
                mScreenRecorderListener.onError(new RuntimeException("No storage permission"));
            return false;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US);
        final File file = new File(dir, "Screenshots-" + format.format(new Date())
                + "-" + video.width + "x" + video.height + ".mp4");
        Log.d("@@", "Create recorder with :" + video + " \n " + audio + "\n " + file);
        mRecorder = newRecorder(mediaProjection, video, audio, file);
        startRecorder();
        return true;
    }

    private AudioEncodeConfig createAudioConfig() {
        if (!withAudio) return null;
//        String codec = getSelectedAudioCodec();
//        if (codec == null) {
//            return null;
//        }
//        int bitrate = getSelectedAudioBitrate();
//        int samplerate = getSelectedAudioSampleRate();
//        int channelCount = getSelectedAudioChannelCount();
//        int profile = getSelectedAudioProfile();
//
//        return new AudioEncodeConfig(codec, AUDIO_AAC, bitrate, samplerate, channelCount, profile);
        if (mAacCodecInfo == null) return null;
        return new AudioEncodeConfig(mAacCodecInfo.getName(), AUDIO_AAC, audioBitrate,
                sampleRate, audioChannelCount, audioProfile);
    }

    private VideoEncodeConfig createVideoConfig() {
//        final String codec = getSelectedVideoCodec();
//        if (codec == null) {
//            // no selected codec ??
//            return null;
//        }
        // video size
//        int[] selectedWithHeight = getSelectedWithHeight();
//        boolean isLandscape = isLandscape();
//        int width = selectedWithHeight[isLandscape ? 0 : 1];
//        int height = selectedWithHeight[isLandscape ? 1 : 0];
//        int framerate = getSelectedFramerate();
//        int iframe = getSelectedIFrameInterval();
//        int bitrate = getSelectedVideoBitrate();
//        MediaCodecInfo.CodecProfileLevel profileLevel = getSelectedProfileLevel();
//        return new VideoEncodeConfig(width, height, bitrate,
//                framerate, iframe, mAvcCodecInfo.getName(), VIDEO_AVC, profileLevel);

        if (mAvcCodecInfo == null) return null;
        return new VideoEncodeConfig(mWidth, mHeight, videoBitrate,
                framerate, iframe, mAvcCodecInfo.getName(), VIDEO_AVC, profileLevel);
    }

    private ScreenRecorder newRecorder(MediaProjection mediaProjection, VideoEncodeConfig video,
                                       AudioEncodeConfig audio, final File output) {
        final VirtualDisplay display = getOrCreateVirtualDisplay(mediaProjection, video);
        ScreenRecorder r = new ScreenRecorder(video, audio, display, output.getAbsolutePath());
        r.setCallback(new ScreenRecorder.Callback() {
            long startTime = 0;

            @Override
            public void onStop(Throwable error) {
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopRecorder();
                    }
                });
                if (error != null) {
//                    toast("Recorder error ! See logcat for more details");
                    error.printStackTrace();
                    output.delete();
                    if (mScreenRecorderListener != null)
                        mScreenRecorderListener.onError(error);
                } else {
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                            .addCategory(Intent.CATEGORY_DEFAULT)
                            .setData(Uri.fromFile(output));
                    mContext.sendBroadcast(intent);
                    if (mScreenRecorderListener != null)
                        mScreenRecorderListener.onComplete(output);
                }
            }

            @Override
            public void onStart() {
                mNotifications.recording(0);
                if (mScreenRecorderListener != null) mScreenRecorderListener.onStart();
            }

            @Override
            public void onRecording(long presentationTimeUs) {
                if (startTime <= 0) {
                    startTime = presentationTimeUs;
                }
                long time = (presentationTimeUs - startTime) / 1000;
                mNotifications.recording(time);
                if (mScreenRecorderListener != null)
                    mScreenRecorderListener.onRecording(presentationTimeUs);
            }
        });
        return r;
    }

    private VirtualDisplay getOrCreateVirtualDisplay(MediaProjection mediaProjection, VideoEncodeConfig config) {
        if (mVirtualDisplay == null) {
            mVirtualDisplay = mediaProjection.createVirtualDisplay("ScreenRecorder-display0",
                    config.width, config.height, 1 /*dpi*/,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    null /*surface*/, null, null);
        } else {
            // resize if size not matched
            Point size = new Point();
            mVirtualDisplay.getDisplay().getSize(size);
            if (size.x != config.width || size.y != config.height) {
                mVirtualDisplay.resize(config.width, config.height, 1);
            }
        }
        return mVirtualDisplay;
    }

    private void startRecorder() {
        if (mRecorder == null) return;
        mRecorder.start();
//        mButton.setText(getString(R.string.stop_recorder));
        mContext.registerReceiver(mStopActionReceiver, new IntentFilter(ACTION_STOP));
//        moveTaskToBack(true);
    }

    private void stopRecorder() {
        mNotifications.clear();
        if (mRecorder != null) {
            mRecorder.quit();
        }
        mRecorder = null;
//        mButton.setText(getString(R.string.restart_recorder));
        try {
            mContext.unregisterReceiver(mStopActionReceiver);
        } catch (Exception e) {
            //ignored
        }
    }

//    private void stopRecordingAndOpenFile(Context context) {
//        File file = new File(mRecorder.getSavedPath());
//        stopRecorder();
////        Toast.makeText(context, getString(R.string.recorder_stopped_saved_file) + " " + file, Toast.LENGTH_LONG).show();
//
//    }

    public void openFile(File file) {
        StrictMode.VmPolicy vmPolicy = StrictMode.getVmPolicy();
        try {
            // disable detecting FileUriExposure on public file
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().build());
            viewResult(file);
        } finally {
            StrictMode.setVmPolicy(vmPolicy);
        }
    }

    private void viewResult(File file) {
        Intent view = new Intent(Intent.ACTION_VIEW);
        view.addCategory(Intent.CATEGORY_DEFAULT);
        view.setDataAndType(Uri.fromFile(file), VIDEO_AVC);
        view.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            mContext.startActivity(view);
        } catch (ActivityNotFoundException e) {
            // no activity can open this video
        }
    }

    public void onActivityResult(int resultCode, Intent data) {
        // NOTE: Should pass this result data into a Service to run ScreenRecorder.
        // The following codes are merely exemplary.
        MediaProjection mediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
        if (mediaProjection == null) {
            Log.e("@@", "media projection is null");
            if (mScreenRecorderListener != null)
                mScreenRecorderListener.onCancel();
            return;
        }

        mMediaProjection = mediaProjection;
        mMediaProjection.registerCallback(mProjectionCallback, new Handler());
        startCapturing(mediaProjection);
    }

    public void onDestroy() {
        stopRecorder();
        if (mVirtualDisplay != null) {
            mVirtualDisplay.setSurface(null);
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        mContext = null;
    }

    public static class ScreenRecorderListener {
        public void onStart() {
        }

        public void onComplete(File file) {
        }

        public void onError(Throwable error) {
        }

        public void onCancel() {
        }

        public void onRecording(long presentationTimeUs) {
        }
    }
}