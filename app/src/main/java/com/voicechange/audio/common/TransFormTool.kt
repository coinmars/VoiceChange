package com.voicechange.audio.common

abstract class TransFormTool {
    abstract fun setSampleRate(sampleRate: Int)

    /**
     * 原声通道数
     */
    abstract fun setChannels(channel: Int)

    /**
     * 调整节拍
     *
     * @param newTempo 指定节拍，原始值为0，大快小慢,(-50 .. +100)
     */
    abstract fun setTempoChange(newTempo: Float)

    /**
     * 调整音调
     *
     * @param newPitch (-12 .. +12)
     */
    abstract fun setPitchSemiTones(newPitch: Int)

    /**
     * 改变播放速度
     *
     * @param newRate 指定速度，原始值为0，大快小慢,(-50 .. +100)
     */
    abstract fun setRateChange(newRate: Float)
    abstract fun putSamples(samples: ShortArray?, len: Int)
    abstract fun receiveSamples(): ShortArray?
}