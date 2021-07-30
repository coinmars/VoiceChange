package com.voicechange.audio.common

import android.media.AudioFormat

interface AudioConstans {
    companion object {
        const val FREQUENCY = 16000
        const val RECORD_CHANNEL = AudioFormat.CHANNEL_IN_MONO
        const val PLAY_CHANNEL = AudioFormat.CHANNEL_OUT_MONO
        const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
    }
}