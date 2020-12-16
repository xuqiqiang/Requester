package com.xuqiqiang.uikit.requester.screenrecorder;

import android.media.MediaFormat;
import android.os.Build;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class AudioEncoder extends BaseEncoder {
    private final AudioEncodeConfig mConfig;

    AudioEncoder(AudioEncodeConfig config) {
        super(config.codecName);
        this.mConfig = config;
    }

    @Override
    protected MediaFormat createMediaFormat() {
        return mConfig.toFormat();
    }
}
