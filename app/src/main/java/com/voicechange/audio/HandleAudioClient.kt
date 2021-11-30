package com.voicechange.audio

import com.voicechange.audio.SoundTouchTransFormTool
import com.voicechange.audio.common.IHandleAudioCallback
import com.voicechange.audio.common.TransFormParam
import com.voicechange.audio.common.TransFormTool
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

class HandleAudioClient {
    private var mHandleAudioThread: HandleAudioThread? = null
    private val mTransFormTool: TransFormTool = SoundTouchTransFormTool()

    @Volatile
    private var mHandleAudioComplete = true

    @Volatile
    private var mHandleAudioRegistrants: IHandleAudioCallback? = null
    fun unRegisterForHandleCallback() {
        mHandleAudioRegistrants = null
    }

    fun registerForHandleCallback(callback: IHandleAudioCallback?) {
        mHandleAudioRegistrants = callback
    }

    fun startHandleAudio(recordQueue: BlockingQueue<ShortArray>, resultData: LinkedList<ByteArray>, transFormParam: TransFormParam): Boolean {
        if (mHandleAudioThread != null) {
            return true
        }
        mHandleAudioThread = HandleAudioThread(recordQueue, resultData, transFormParam)
        mHandleAudioThread!!.start()
        mHandleAudioComplete = false
        return true
    }

    fun stopHandleAudio(): Boolean {
        if (mHandleAudioThread == null) {
            return true
        }
        mHandleAudioThread!!.quitThread()
        mHandleAudioThread = null
        waitHandleAudioComplete()
        return true
    }

    private fun waitHandleAudioComplete() {
        while (!mHandleAudioComplete) {
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    inner class HandleAudioThread(private val mRecordQueue: BlockingQueue<ShortArray>, private val mAudioDatas: LinkedList<ByteArray>, private val mTransFormParam: TransFormParam) : Thread() {
        @Volatile
        private var mExitFlag = false
        fun quitThread() {
            mExitFlag = true
        }

        override fun run() {
            notifyHandleStart()
            mTransFormTool.setSampleRate(mTransFormParam.mSampleRate) //设置声音的采样频率
            mTransFormTool.setChannels(mTransFormParam.mChannel) //设置声音的声道
            mTransFormTool.setPitchSemiTones(mTransFormParam.mNewPitch) //设置声音的pitch
            mTransFormTool.setRateChange(mTransFormParam.mNewRate) //设置声音的速率
            mTransFormTool.setTempoChange(mTransFormParam.mNewTempo) //这个就是传说中的变速不变调
            var recordingData: ShortArray?
            while (true) {
                try {
                    recordingData = mRecordQueue.poll(TIME_WAIT_RECORDING, TimeUnit.MILLISECONDS)
                    if (recordingData != null) {
                        mTransFormTool.putSamples(recordingData, recordingData.size)
                        var buffer: ShortArray?
                        do {
                            buffer = mTransFormTool.receiveSamples()
                            val bytes: ByteArray? = buffer?.let {
                                com.voicechange.module.Utils.shortToByteSmall(
                                    it
                                )
                            }
                            if (bytes != null) {
                                if (bytes.isNotEmpty()) {
                                    mAudioDatas.add(bytes)
                                    notifyHandleProcess(bytes)
                                }
                            }
                        } while (buffer!!.isNotEmpty())
                    }
                    if (mExitFlag && mRecordQueue.size == 0) {
                        break
                    }
                } catch (e: Exception) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }
            }
            mHandleAudioComplete = true
            notifyHandleComplete()
        }

        private fun notifyHandleStart() {
            if (mHandleAudioRegistrants != null) {
                mHandleAudioRegistrants!!.onHandleStart()
            }
        }

        private fun notifyHandleProcess(data: ByteArray) {
            if (mHandleAudioRegistrants != null) {
                mHandleAudioRegistrants!!.onHandleProcess(data)
            }
        }

        private fun notifyHandleComplete() {
            if (mHandleAudioRegistrants != null) {
                mHandleAudioRegistrants!!.onHandleComplete()
            }
        }
    }

    companion object {
        private val TAG = HandleAudioClient::class.java.name
        private const val TIME_WAIT_RECORDING: Long = 100
    }
}