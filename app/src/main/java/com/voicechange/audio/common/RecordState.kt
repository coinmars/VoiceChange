package com.voicechange.audio.common

interface RecordState {
    companion object {
        const val MSG_RECORDING_START = 1
        const val MSG_RECORDING_STOP = 2
        const val MSG_RECORDING_STATE_ERROR = 3
    }
}