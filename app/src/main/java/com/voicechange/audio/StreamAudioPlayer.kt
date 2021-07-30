package com.voicechange.audio

import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import com.voicechange.audio.common.AudioParam

class StreamAudioPlayer {
    private var mAudioParam // 音频参数
            : AudioParam? = null
    private var mAudioTrack // AudioTrack对象
            : AudioTrack? = null
    private var mBReady = false // AudioTrack是否就绪
    private var mPrimePlaySize = 0 // 较优播放块大小

    constructor() {}
    constructor(audioParam: AudioParam?) {
        setAudioParam(audioParam)
    }

    /*
     * 设置音频参数
     */
    fun setAudioParam(audioParam: AudioParam?) {
        mAudioParam = audioParam
    }

    /*
     *  就绪AudioTrack
     */
    fun prepare(): Boolean {
        if (mAudioParam == null) {
            return false
        }
        if (mBReady == true) {
            release()
        }
        try {
            createAudioTrack()
        } catch (e: Exception) {
            // TODO Auto-generated catch block
            e.printStackTrace()
            return false
        }
        mBReady = true
        mAudioTrack!!.play()
        return true
    }

    /*
     * 释放播放源
     */
    fun release(): Boolean {
        releaseAudioTrack()
        mBReady = false
        return true
    }

    /*
     * 播放
     */
    fun play(data: ByteArray?): Boolean {
        if (mBReady == false || data == null) {
            return false
        }
        Log.d(TAG, "play data.size = " + data.size + ", mPrimePlaySize = " + mPrimePlaySize)
        val size = mAudioTrack!!.write(data, 0, data.size)
        return true
    }

    @Throws(Exception::class)
    private fun createAudioTrack() {

        // 获得构建对象的最小缓冲区大小
        val minBufSize = AudioTrack.getMinBufferSize(mAudioParam!!.mFrequency,
                mAudioParam!!.mChannelConfig,
                mAudioParam!!.mSampBitConfig)
        mPrimePlaySize = 2 * minBufSize
        Log.d(TAG, "minBufSize = " + minBufSize + ", audioParam = " + mAudioParam.toString())

//		         STREAM_ALARM：警告声
//		         STREAM_MUSCI：音乐声，例如music等
//		         STREAM_RING：铃声
//		         STREAM_SYSTEM：系统声音
//		         STREAM_VOCIE_CALL：电话声音
        mAudioTrack = AudioTrack(AudioManager.STREAM_MUSIC,
                mAudioParam!!.mFrequency,
                mAudioParam!!.mChannelConfig,
                mAudioParam!!.mSampBitConfig,
                mPrimePlaySize,
                AudioTrack.MODE_STREAM)
        //				AudioTrack中有MODE_STATIC和MODE_STREAM两种分类。
//      		STREAM的意思是由用户在应用程序通过write方式把数据一次一次得写到audiotrack中。
//				这个和我们在socket中发送数据一样，应用层从某个地方获取数据，例如通过编解码得到PCM数据，然后write到audiotrack。
//				这种方式的坏处就是总是在JAVA层和Native层交互，效率损失较大。
//				而STATIC的意思是一开始创建的时候，就把音频数据放到一个固定的buffer，然后直接传给audiotrack，
//				后续就不用一次次得write了。AudioTrack会自己播放这个buffer中的数据。
//				这种方法对于铃声等内存占用较小，延时要求较高的声音来说很适用。
    }

    private fun releaseAudioTrack() {
        if (mAudioTrack != null) {
            mAudioTrack!!.stop()
            mAudioTrack!!.release()
            mAudioTrack = null
        }
    }

    companion object {
        private val TAG = StreamAudioPlayer::class.java.name
    }
}