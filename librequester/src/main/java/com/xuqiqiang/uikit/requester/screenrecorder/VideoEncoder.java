package com.xuqiqiang.uikit.requester.screenrecorder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.RequiresApi;

import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class VideoEncoder extends BaseEncoder {
    private static final boolean VERBOSE = false;
    private final VideoEncodeConfig mConfig;
    private Surface mSurface;

    VideoEncoder(VideoEncodeConfig config) {
        super(config.codecName);
        this.mConfig = config;
    }

    @Override
    protected void onEncoderConfigured(MediaCodec encoder) {
        mSurface = encoder.createInputSurface();
        if (VERBOSE) Log.i("@@", "VideoEncoder create input surface: " + mSurface);
    }

    @Override
    protected MediaFormat createMediaFormat() {
        return mConfig.toFormat();
    }

    /**
     * @throws NullPointerException if prepare() not call
     */
    Surface getInputSurface() {
        return Objects.requireNonNull(mSurface, "doesn't prepare()");
    }

    @Override
    public void release() {
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        super.release();
    }
}
