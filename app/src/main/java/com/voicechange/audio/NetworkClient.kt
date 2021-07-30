package com.voicechange.audio

class NetworkClient {
    private var mNetworkReceive: NetworkReceiver? = null
    private var mConnectNetworkSuccess = false
    fun connectNetworkService(networkReceiver: NetworkReceiver?): Boolean {
        mNetworkReceive = networkReceiver
        if (mNetworkReceive != null) {
            mConnectNetworkSuccess = true
        }
        return true
    }

    fun disConnectNetworkService(): Boolean {
        mNetworkReceive = null
        mConnectNetworkSuccess = false
        return true
    }

    fun sendAudio(data: ByteArray?): Boolean {
        return if (data == null || mConnectNetworkSuccess == false) {
            false
        } else mNetworkReceive!!.receiveAudio(data)
    }
}