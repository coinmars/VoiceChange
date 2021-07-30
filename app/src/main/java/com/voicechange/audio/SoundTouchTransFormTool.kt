package com.voicechange.audio

import com.amoment.audio.JNISoundTouch
import com.voicechange.audio.common.TransFormTool

class SoundTouchTransFormTool : TransFormTool() {
    private val ndkUtil = JNISoundTouch()
    override fun setSampleRate(sampleRate: Int) {
        ndkUtil.setSampleRate(sampleRate)
    }

    override fun setChannels(channel: Int) {
        ndkUtil.setChannels(channel)
    }

    override fun setTempoChange(newTempo: Float) {
        ndkUtil.setTempoChange(newTempo)
    }

    override fun setPitchSemiTones(newPitch: Int) {
        ndkUtil.setPitchSemiTones(newPitch)
    }

    override fun setRateChange(newRate: Float) {
        ndkUtil.setRateChange(newRate)
    }

    override fun putSamples(samples: ShortArray?, len: Int) {
        ndkUtil.putSamples(samples, len)
    }

    override fun receiveSamples(): ShortArray? {
        return ndkUtil.receiveSamples()
    }
}