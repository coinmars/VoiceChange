package com.voicechange.audio.common

interface PlayState {
    companion object {
        const val MPS_UNINIT = 0 // 未就绪
        const val MPS_PREPARE = 1 // 准备就绪(停止)
        const val MPS_PLAYING = 2 // 播放中
        const val MPS_PAUSE = 3 // 暂停
    }
}