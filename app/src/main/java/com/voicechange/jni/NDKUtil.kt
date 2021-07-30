package com.voicechange.jni

class NDKUtil {
    external fun stringFromJNI(): String?

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}