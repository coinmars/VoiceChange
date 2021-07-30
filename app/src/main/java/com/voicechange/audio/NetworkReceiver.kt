package com.voicechange.audio

import com.voicechange.audio.common.AudioConstans
import com.voicechange.audio.common.AudioParam

class NetworkReceiver {
    private val mSteamAudioPlay = StreamAudioPlayer()
    fun init() {
        val audioParam = AudioParam()
        audioParam.mFrequency = FREQUENCY
        audioParam.mChannelConfig = CHANNEL
        audioParam.mSampBitConfig = ENCODING
        mSteamAudioPlay.setAudioParam(audioParam)
        mSteamAudioPlay.prepare()
    }

    fun unInit() {
        mSteamAudioPlay.release()
    }

    fun receiveAudio(data: ByteArray?): Boolean {
        return if (data == null) {
            false
        } else mSteamAudioPlay.play(data)
    }

    companion object {
        private const val FREQUENCY = AudioConstans.FREQUENCY
        private const val CHANNEL = AudioConstans.PLAY_CHANNEL
        private const val ENCODING = AudioConstans.ENCODING
    }
}