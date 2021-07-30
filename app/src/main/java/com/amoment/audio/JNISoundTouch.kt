package com.amoment.audio

/*
速率：
setRate(double) 指定播放速率，原始值为1.0，大快小慢
setTempo(double) 指定节拍，原始值为1.0，大快小慢
音调：
setPitch(double) 指定音调值，原始值为1.0
setPitchOctaves(double) 在原音调基础上以八度音为单位进行调整，取值为[-1.00,+1.00]
setPitchSemiTones(int) 在原音调基础上以半音为单位进行调整，取值为[-12,+12]
 */
class JNISoundTouch {
    /**
     * 原声采样率
     */
    external fun setSampleRate(sampleRate: Int)

    /**
     * 原声通道数
     */
    external fun setChannels(channel: Int)
    /**
     * 调整节拍
     *
     * @param newTempo 指定节拍，原始值为1.0，大快小慢
     */
    //    public native void setTempo(float newTempo);
    /**
     * 调整节拍
     *
     * @param newTempo 指定节拍，原始值为0，大快小慢,(-50 .. +100)
     */
    external fun setTempoChange(newTempo: Float)

    /**
     * 调整音调
     *
     * @param newPitch (-12 .. +12)
     */
    external fun setPitchSemiTones(newPitch: Int)
    /**
     * 改变播放速度
     *
     * @param newRate 指定速度，原始值为1.0，大快小慢
     */
    //    public native void setRate(float newRate);
    /**
     * 改变播放速度
     *
     * @param newRate 指定速度，原始值为0，大快小慢,(-50 .. +100)
     */
    external fun setRateChange(newRate: Float)
    external fun putSamples(samples: ShortArray?, len: Int)
    external fun receiveSamples(): ShortArray?

    companion object {
        init {
            System.loadLibrary("soundtouch")
        }
    }
}