package com.voicechange

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.*

import com.example.soundtouch.common.AsyncResult
import com.example.soundtouch.common.Utils
import com.tbruyelle.rxpermissions2.RxPermissions
import com.voicechange.audio.AudioEngine
import com.voicechange.audio.NetworkClient
import com.voicechange.audio.NetworkReceiver
import com.voicechange.audio.common.IHandleAudioCallback
import com.voicechange.audio.common.RecordState
import com.voicechange.audio.common.TransFormParam

class MainRecordActivity : Activity(), RadioGroup.OnCheckedChangeListener, View.OnClickListener, OnTouchListener, IHandleAudioCallback {
    private var mRadioGroup: RadioGroup? = null

    private var mEt_sample_rate: EditText? = null
    private var mEt_channels: EditText? = null
    private var mEt_pitch_semi_tones: EditText? = null
    private var mEt_rate_change: EditText? = null
    private var mEt_tempo_change: EditText? = null

    private var mBtnRecord: Button? = null
    private var mBtnPlay: Button? = null
    private var mBtnSave: Button? = null
    private var mBtnJump: Button? = null
    private var mTvRecord: TextView? = null
    private var mCheckBoxPlaying: CheckBox? = null
    private var mRecordStateHandler: Handler? = null
    private var mNetworkClient: NetworkClient? = null
    private var mNetworkReceiver: NetworkReceiver? = null
    private var mAudioEngine: AudioEngine? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record_layout)
        val btnRecord = findViewById<View>(R.id.switch1) as Switch
        btnRecord.setOnClickListener {
            val str = btnRecord.text.toString()
            if (str == "录音") {
                startRecord()
                btnRecord.text = "结束"
                initView()
            } else {
                stopRecord()
                btnRecord.text = "录音"
            }
        }
        initView()
        initLogic()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        mRadioGroup = findViewById(R.id.radioGroup)
        mRadioGroup?.setOnCheckedChangeListener(this)
        val rbCustom = findViewById<RadioButton>(R.id.radioButtonCustom)
        rbCustom.isChecked = true
        mCheckBoxPlaying = findViewById(R.id.checkBoxPlaying)
        mCheckBoxPlaying?.isChecked = true
        mEt_sample_rate = findViewById(R.id.et_sample_rate)
        mEt_sample_rate?.setEnabled(false)
        mEt_channels = findViewById(R.id.et_channel)
        mEt_channels?.isEnabled = false
        mEt_pitch_semi_tones = findViewById(R.id.et_pitch)
        mEt_rate_change = findViewById(R.id.et_ratch)
        mEt_tempo_change = findViewById(R.id.et_tempo_change)
        mBtnRecord = findViewById(R.id.switch1)
        mBtnRecord?.setOnTouchListener(this)
        mBtnPlay = findViewById(R.id.btnPlay)
        mBtnPlay?.setOnClickListener(this)
        mBtnSave = findViewById(R.id.btnSave)
        mBtnSave?.setOnClickListener(this)
        mBtnJump = findViewById(R.id.btnJump)
        mBtnJump?.setOnClickListener(this)
        mTvRecord = findViewById(R.id.tv_recordState)
    }

    private fun initLogic() {
        initAudioEngine()
        requestPermisson()
    }

    @SuppressLint("CheckResult")
    private fun requestPermisson() {
        val rxPermission = RxPermissions(this)
        rxPermission.request(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO)
                .subscribe { granted ->
                    if (granted) {
                        Log.d(TAG, "granted success")
                    } else {
                        Toast.makeText(this@MainRecordActivity, "请授予相关权限再使用该应用", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
    }

    private fun initAudioEngine() {
        mRecordStateHandler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                Log.i(TAG, "msg.what = " + msg.what)
                when (msg.what) {
                    MSG_RECORD_STATE -> {
                        val ar = msg.obj as AsyncResult
                        handleRecordState(ar)
                    }
                    else -> {
                    }
                }
            }
        }
        mAudioEngine = AudioEngine()
        mAudioEngine!!.registerForRecordStateChanged(mRecordStateHandler, MSG_RECORD_STATE)
        mAudioEngine!!.registerForHandleCallback(this)
        mNetworkClient = NetworkClient()
        mNetworkReceiver = NetworkReceiver()
        mNetworkReceiver!!.init()
        mNetworkClient!!.connectNetworkService(mNetworkReceiver)
    }

    private fun unInitAudioEngine() {
        mAudioEngine!!.stopReplayAudioCache()
        mAudioEngine!!.unregisterForRecordStateChanged(mRecordStateHandler)
        mAudioEngine!!.unRegisterForHandleCallback()
        mNetworkClient!!.disConnectNetworkService()
        mNetworkReceiver!!.unInit()
    }

    override fun onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy()
        unInitAudioEngine()
    }

    /*    float[] pitch = {0, 10F, 7.0F, 7.0F, -11.0F, 0.0F, -8.0F, 7.8F, 0.0F, 5F, 12F, -10F, -5, 0, -15};
    float[] rate = {0, -0.7f, 20.5F, -12.0F, -2.0F, 50.0F, -1.7F, 1.0F, -50.0F, 12, 5, 5, -30, -50, -10};
    float[] tempo = {0, 0.5f, -9.0F, -15.0F, 20.0F, 50.0F, -3.6F, 0.0F, 50.0F, 10, 15, 50, 50, 100, 60};*/
    private val pitch = floatArrayOf(5f, 12f, -10f, -5f, 0f, -15f)
    private val rate = floatArrayOf(12f, 5f, 5f, -30f, -50f, -10f)
    private val tempo = floatArrayOf(10f, 15f, 50f, 50f, 100f, 60f)
    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        enableEditText(false)
        when (checkedId) {
            R.id.radioButtonCustom -> {
                enableEditText(true)
                updateAudioParamUI(0f, 0f, 0f)
            }
            R.id.radioButton1 -> updateAudioParamUI(pitch[0], rate[0], tempo[0])
            R.id.radioButton2 -> updateAudioParamUI(pitch[1], rate[1], tempo[1])
            R.id.radioButton3 -> updateAudioParamUI(pitch[2], rate[2], tempo[2])
            R.id.radioButton4 -> updateAudioParamUI(pitch[3], rate[3], tempo[3])
            R.id.radioButton5 -> updateAudioParamUI(pitch[4], rate[4], tempo[4])
            R.id.radioButton6 -> updateAudioParamUI(pitch[5], rate[5], tempo[5])
        }
    }

    private fun enableEditText(enable: Boolean) {
        mEt_pitch_semi_tones!!.isEnabled = enable
        mEt_rate_change!!.isEnabled = enable
        mEt_tempo_change!!.isEnabled = enable
    }

    @SuppressLint("SetTextI18n")
    private fun updateAudioParamUI(pitch: Float, rate: Float, tempo: Float) {
        mEt_pitch_semi_tones!!.setText(pitch.toString() + "")
        mEt_rate_change!!.setText(rate.toString() + "")
        mEt_tempo_change!!.setText(tempo.toString() + "")
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnPlay -> playCacheAudio()
            R.id.btnSave -> saveToFile()
            R.id.btnJump -> btnJumpToPlayUI()
        }
    }

    private fun playCacheAudio() {
        mAudioEngine!!.replayAudioCache()
    }

    private fun saveToFile() {
        val ret = mAudioEngine!!.saveToPCMFile(Utils.localExternalPath + "/soundtouch.pcm")
        if (ret) {
            Toast.makeText(this, "保存成功:" + Utils.localExternalPath + "/soundtouch.pcm", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "保存失败:" + Utils.localExternalPath + "/soundtouch.pcm", Toast.LENGTH_SHORT).show()
        }
        mAudioEngine!!.saveToWAVFile(Utils.localExternalPath + "/soundtouch.wav")
    }

    private fun btnJumpToPlayUI() {
        val intent = Intent()
        intent.setClass(this, AudioPlayerActivity::class.java)
        startActivity(intent)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        /*
        switch(v.getId()) {
            case R.id.btnRecord:
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        startRecord();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_OUTSIDE:
                    case MotionEvent.ACTION_UP:
                        stopRecord();
                        break;
                    default:
                }
                break;
        }*/
        return false
    }

    private fun startRecord() {
        mAudioEngine!!.start(transFormParam)
    }

    private fun stopRecord() {
        mAudioEngine!!.stop()
    }

    private val transFormParam: TransFormParam
        get() {
            val transFormParam = TransFormParam()
            val newPitch = mEt_pitch_semi_tones!!.text.toString()
            transFormParam.mSampleRate = if (TextUtils.isEmpty(newPitch)) 0 else newPitch.toFloat().toInt()
            val newRate = mEt_rate_change!!.text.toString()
            transFormParam.mNewRate = if (TextUtils.isEmpty(newRate)) 0F else newRate.toFloat()
            val newTempo = mEt_tempo_change!!.text.toString()
            transFormParam.mNewTempo = if (TextUtils.isEmpty(newTempo)) 0F else newTempo.toFloat()
            return transFormParam
        }

    private fun handleRecordState(ar: AsyncResult) {
        val recordState = ar.result as Int
        when (recordState) {
            RecordState.MSG_RECORDING_START -> mTvRecord!!.text = "录音中...请插入耳机"
            RecordState.MSG_RECORDING_STOP -> mTvRecord!!.text = ""
            RecordState.MSG_RECORDING_STATE_ERROR -> mTvRecord!!.text = "录音异常"
        }
    }

    override fun onHandleStart() {}
    override fun onHandleProcess(data: ByteArray?) {
        if (mCheckBoxPlaying!!.isChecked) {
            mNetworkClient!!.sendAudio(data)
        }
    }

    override fun onHandleComplete() {}

    companion object {
        private val TAG = MainRecordActivity::class.java.name
        private const val MSG_RECORD_STATE = 0x01
    }
}