package com.voicechange

import android.annotation.SuppressLint
import android.app.Activity
import android.media.AudioFormat
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.soundtouch.common.Utils
import com.voicechange.audio.SampleAudioPlayer
import com.voicechange.audio.common.AudioConstans
import com.voicechange.audio.common.AudioParam
import com.voicechange.audio.common.PlayState
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException

class AudioPlayerActivity : Activity(), View.OnClickListener {
    private var mTextViewState // 播放状态
            : TextView? = null
    private var mBtnPlayButton // 播放
            : Button? = null
    private var mBtnPauseButton // 暂停
            : Button? = null
    private var mBtnStopButton // 停止
            : Button? = null
    private var mAudioPlayer // 播放器
            : SampleAudioPlayer? = null
    private var frequencyEditView: TextView? = null
    private var channelEditView: TextView? = null
    private var mTVFilePath: TextView? = null
    private var mFrequency = AudioConstans.FREQUENCY
    private var mChannel = 1
    private val mSampBit = AudioConstans.ENCODING
    private val mHandler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        @SuppressLint("HandlerLeak")
        override fun handleMessage(msg: Message) {
            // TODO Auto-generated method stub
            when (msg.what) {
                SampleAudioPlayer.STATE_MSG_ID -> showState(msg.obj as Int)
            }
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.play_main)
        initView()
        initLogic()
    }

    override fun onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy()
        mAudioPlayer!!.release()
    }

    private fun initView() {
        frequencyEditView = findViewById(R.id.et_sample_rate)
        channelEditView = findViewById(R.id.et_channels)
        mBtnPlayButton = findViewById(R.id.buttonPlay)
        mBtnPlayButton?.setOnClickListener(this)
        mBtnPauseButton = findViewById(R.id.buttonPause)
        mBtnPauseButton?.setOnClickListener(this)

        mBtnStopButton = findViewById(R.id.buttonStop)
        mBtnStopButton?.setOnClickListener(this)
        mTextViewState = findViewById(R.id.tvPlayState)
        mTVFilePath = findViewById(R.id.tv_filePaths)
        mTVFilePath?.text = filePath
    }

    private fun initLogic() {
        mAudioPlayer = SampleAudioPlayer(mHandler)
        frequencyEditView!!.text = mFrequency.toString()
        channelEditView!!.text = mChannel.toString()
    }

    override fun onClick(view: View) {
        // TODO Auto-generated method stub
        when (view.id) {
            R.id.buttonPlay -> play()
            R.id.buttonPause -> pause()
            R.id.buttonStop -> stop()
        }
    }

    @SuppressLint("SetTextI18n")
    fun play() {
        if (mAudioPlayer!!.playState == PlayState.MPS_PAUSE) {
            mAudioPlayer!!.play()
            return
        }

        // 获取音频数据
        val data = pCMData
        if (data == null) {
            mTextViewState!!.text = "$filePath：该路径下不存在文件！"
            return
        }

        // 获取音频参数
        val audioParam = audioParam
        mAudioPlayer!!.setAudioParam(audioParam)
        mAudioPlayer!!.setDataSource(data)

        // 音频源就绪
        mAudioPlayer!!.prepare()
        mAudioPlayer!!.play()
    }

    fun pause() {
        mAudioPlayer!!.pause()
    }

    fun stop() {
        mAudioPlayer!!.stop()
    }

    fun showState(state: Int) {
        var showString = ""
        when (state) {
            PlayState.MPS_UNINIT -> showString = "MPS_UNINIT"
            PlayState.MPS_PREPARE -> showString = "MPS_PREPARE"
            PlayState.MPS_PLAYING -> showString = "MPS_PLAYING"
            PlayState.MPS_PAUSE -> showString = "MPS_PAUSE"
        }
        showState(showString)
    }

    fun showState(str: String?) {
        mTextViewState!!.text = str
    }

    /*
	 * 获得PCM音频数据参数
	 */
    private val audioParam: AudioParam
        get() {
            val frequency = frequencyEditView!!.text.toString()
            mFrequency = Integer.valueOf(frequency)
            val channel = channelEditView!!.text.toString()
            mChannel = Integer.valueOf(channel)
            val audioParam = AudioParam()
            audioParam.mFrequency = mFrequency
            audioParam.mChannelConfig = if (mChannel == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO
            audioParam.mSampBitConfig = AudioFormat.ENCODING_PCM_16BIT
            return audioParam
        }
    val filePath = Utils.localExternalPath + "/soundtouch.pcm"// TODO Auto-generated catch block// TODO Auto-generated catch block

    /*
       * 获得PCM音频数据
       */
    val pCMData: ByteArray?
        get() {
            val file = File(filePath)
            if (!file.exists()) {
                Log.d(TAG, "pcm  can't find path:$filePath")
                return null
            }
            Log.d(TAG, "pcm  find path:$filePath")
            val inStream: FileInputStream
            inStream = try {
                FileInputStream(file)
            } catch (e: FileNotFoundException) {
                Log.e(TAG, "FileNotFoundException:" + e.message)
                // TODO Auto-generated catch block
                e.printStackTrace()
                return null
            }
            var data_pack: ByteArray? = null
            if (inStream != null) {
                val size = file.length()
                data_pack = ByteArray(size.toInt())
                try {
                    inStream.read(data_pack)
                } catch (e: IOException) {
                    Log.e(TAG, "IOException:" + e.message)
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                    return null
                }
            }
            return data_pack
        }

    companion object {
        /** Called when the activity is first created.  */
        private const val TAG = "AudioPlayerDemoActivity"
    }
}