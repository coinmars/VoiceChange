package com.voicechange.audio

import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.util.Log
import com.example.soundtouch.common.RegistrantList
import com.voicechange.audio.common.AudioConstans
import com.voicechange.audio.common.RecordState
import java.util.concurrent.BlockingQueue

class RecordAudioClient {
    private val mRecordStateChangeRegistrants: RegistrantList = RegistrantList()
    private var mRecrodThread: RecordThread? = null

    @Volatile
    private var mRecordAudioComplete = true
    fun unregisterForRecordStateChanged(h: Handler?) {
        if (h != null) {
            mRecordStateChangeRegistrants.remove(h)
        }
    }

    fun registerForRecordStateChanged(h: Handler?, what: Int) {
        if (h != null) {
            mRecordStateChangeRegistrants.addUnique(h, what, null)
        }
    }

    fun startRecord(recordQueue: BlockingQueue<ShortArray>): Boolean {
        if (mRecrodThread != null) {
            return true
        }
        mRecrodThread = RecordThread(recordQueue)
        mRecrodThread!!.start()
        mRecordAudioComplete = false
        return true
    }

    fun stopRecord(): Boolean {
        if (mRecrodThread == null) {
            return true
        }
        mRecrodThread!!.quitThread()
        mRecrodThread = null
        waitRecordAudioComplete()
        return true
    }

    private fun waitRecordAudioComplete() {
        while (!mRecordAudioComplete) {
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    inner class RecordThread(private val mRecordQueue: BlockingQueue<ShortArray>) : Thread() {
        @Volatile
        private var mExitFlag = false
        fun quitThread() {
            mExitFlag = true
        }

        override fun run() {
            val audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC, FREQUENCY,
                    CHANNEL, ENCODING, bufferSize)
            val state = audioRecord.state
            if (state == AudioRecord.STATE_INITIALIZED) {
                val buffer = ShortArray(bufferSize)
                var flag = true
                audioRecord.startRecording()
                notifyRecordStart()
                while (!mExitFlag) {
                    val len = audioRecord.read(buffer, 0, buffer.size)
                    Log.d(TAG, "audioRecord read len = $len")
                    // 去掉全0数据
                    if (flag) {
                        var sum = 0.0
                        for (i in 0 until len) {
                            sum += buffer[i]
                        }
                        flag = if (sum == 0.0) {
                            continue
                        } else {
                            false
                        }
                    }
                    val data = ShortArray(len)
                    System.arraycopy(buffer, 0, data, 0, len)
                    mRecordQueue.add(data)
                }
                audioRecord.release()
                notifyRecordStop()
            } else {
                notifyRecordError()
            }
            mRecordAudioComplete = true
        }

        private fun notifyRecordStart() {
            mRecordStateChangeRegistrants.notifyResult(RecordState.MSG_RECORDING_START)
        }

        private fun notifyRecordStop() {
            mRecordStateChangeRegistrants.notifyResult(RecordState.MSG_RECORDING_STOP)
        }

        private fun notifyRecordError() {
            mRecordStateChangeRegistrants.notifyResult(RecordState.MSG_RECORDING_STATE_ERROR)
        }
    }

    companion object {
        private val TAG = RecordAudioClient::class.java.name
        private const val FREQUENCY = AudioConstans.FREQUENCY
        private const val CHANNEL = AudioConstans.RECORD_CHANNEL
        private const val ENCODING = AudioConstans.ENCODING
        private val bufferSize = AudioRecord.getMinBufferSize(FREQUENCY, CHANNEL, ENCODING)
    }
}