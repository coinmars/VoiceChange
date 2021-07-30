package com.voicechange.audio

import android.media.AudioManager
import android.media.AudioTrack
import android.os.Handler
import android.util.Log
import com.voicechange.audio.common.AudioParam
import com.voicechange.audio.common.IPlayComplete
import com.voicechange.audio.common.PlayState

class SampleAudioPlayer : IPlayComplete {
    private var mHandler: Handler?
    private var mAudioParam // 音频参数
            : AudioParam? = null
    private var mData // 音频数据
            : ByteArray? = null
    private var mAudioTrack // AudioTrack对象
            : AudioTrack? = null
    private var mBReady = false // 播放源是否就绪
    private var mPlayAudioThread // 播放线程
            : PlayAudioThread? = null

    @JvmOverloads
    constructor(handler: Handler? = null) {
        mHandler = handler
    }

    constructor(handler: Handler?, audioParam: AudioParam?) {
        mHandler = handler
        setAudioParam(audioParam)
    }

    /*
	 * 设置音频参数
	 */
    fun setAudioParam(audioParam: AudioParam?) {
        mAudioParam = audioParam
    }

    /*
	 * 设置音频源
	 */
    fun setDataSource(data: ByteArray?) {
        mData = data
    }

    /*
	 *  就绪播放源
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
        playState = PlayState.MPS_PREPARE
        return true
    }

    /*
	 * 释放播放源
	 */
    fun release(): Boolean {
        stop()
        releaseAudioTrack()
        mBReady = false
        playState = PlayState.MPS_UNINIT
        return true
    }

    /*
	 * 播放
	 */
    fun play(): Boolean {
        if (mBReady == false || mData == null) {
            return false
        }
        when (mPlayState) {
            PlayState.MPS_PREPARE -> {
                mPlayOffset = 0
                playState = PlayState.MPS_PLAYING
                startThread()
            }
            PlayState.MPS_PAUSE -> {
                playState = PlayState.MPS_PLAYING
                startThread()
            }
        }
        return true
    }

    /*
	 * 暂停
	 */
    fun pause(): Boolean {
        if (mBReady == false) {
            return false
        }
        if (mPlayState == PlayState.MPS_PLAYING) {
            playState = PlayState.MPS_PAUSE
            stopThread()
        }
        return true
    }

    /*
	 * 停止
	 */
    fun stop(): Boolean {
        if (mBReady == false) {
            return false
        }
        playState = PlayState.MPS_PREPARE
        stopThread()
        return true
    }

    @set:Synchronized
    var playState: Int
        get() = mPlayState
        private set(state) {
            mPlayState = state
            if (mHandler != null) {
                val msg = mHandler!!.obtainMessage(STATE_MSG_ID)
                msg.obj = mPlayState
                msg.sendToTarget()
            }
        }

    @Throws(Exception::class)
    private fun createAudioTrack() {

        // 获得构建对象的最小缓冲区大小
        val minBufSize = AudioTrack.getMinBufferSize(mAudioParam!!.mFrequency,
                mAudioParam!!.mChannelConfig,
                mAudioParam!!.mSampBitConfig)
        mPrimePlaySize = minBufSize * 2
        Log.d(TAG, "mPrimePlaySize = " + mPrimePlaySize + ", audioParam = " + mAudioParam.toString())

//		         STREAM_ALARM：警告声
//		         STREAM_MUSCI：音乐声，例如music等
//		         STREAM_RING：铃声
//		         STREAM_SYSTEM：系统声音
//		         STREAM_VOCIE_CALL：电话声音
        mAudioTrack = AudioTrack(AudioManager.STREAM_MUSIC,
                mAudioParam!!.mFrequency,
                mAudioParam!!.mChannelConfig,
                mAudioParam!!.mSampBitConfig,
                minBufSize,
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

    private fun startThread() {
        if (mPlayAudioThread == null) {
            mThreadExitFlag = false
            mPlayAudioThread = PlayAudioThread()
            mPlayAudioThread!!.start()
        }
    }

    private fun stopThread() {
        if (mPlayAudioThread != null) {
            mThreadExitFlag = true
            mPlayAudioThread = null
        }
    }

    private var mThreadExitFlag = false // 线程退出标志
    private var mPrimePlaySize = 0 // 较优播放块大小
    private var mPlayOffset = 0 // 当前播放位置
    private var mPlayState = 0 // 当前播放状态

    /*
	 *  播放音频的线程
	 */
    internal inner class PlayAudioThread : Thread() {
        override fun run() {
            // TODO Auto-generated method stub
            Log.d(TAG, "PlayAudioThread run mPlayOffset = $mPlayOffset, mPrimePlaySize = $mPrimePlaySize")
            mAudioTrack!!.play()
            while (true) {
                if (mThreadExitFlag == true) {
                    break
                }
                mPlayOffset += try {
                    Log.d(TAG, "ready to write")
                    val size = mAudioTrack!!.write(mData!!, mPlayOffset, mPrimePlaySize)
                    Log.d(TAG, "write success size = $size")
                    mPrimePlaySize
                } catch (e: Exception) {
                    // TODO: handle exception
                    e.printStackTrace()
                    onPlayComplete()
                    break
                }
                if (mPlayOffset >= mData!!.size) {
                    onPlayComplete()
                    break
                }
            }
            mAudioTrack!!.stop()
            Log.d(TAG, "PlayAudioThread complete...")
        }
    }

    override fun onPlayComplete() {
        // TODO Auto-generated method stub
        mPlayAudioThread = null
        if (mPlayState != PlayState.MPS_PAUSE) {
            playState = PlayState.MPS_PREPARE
        }
    }

    companion object {
        private val TAG = SampleAudioPlayer::class.java.name
        const val STATE_MSG_ID = 0x0010
    }
}