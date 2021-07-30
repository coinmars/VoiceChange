package com.voicechange.audio.common

interface IHandleAudioCallback {
    fun onHandleStart()
    fun onHandleProcess(data: ByteArray?)
    fun onHandleComplete()
}