package com.voicechange.audio.common

class AudioParam {
    @JvmField
	var mFrequency // 采样率
            = 0
    @JvmField
	var mChannelConfig // 声道config
            = 0
    @JvmField
	var mSampBitConfig // 采样精度
            = 0

    override fun toString(): String {
        return "[mFrequency = $mFrequency, mChannelConfig = $mChannelConfig, mSampBitConfig = $mSampBitConfig]"
    }
}