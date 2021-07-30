package com.example.soundtouch.common

import android.os.Message

class AsyncResult
/** please note, this sets m.obj to be this  */(
        /*************************** Instance Variables  */ // Expect either exception or result to be null
        var userObj: Any?, var result: Any?, var exception: Throwable?) {
    companion object {
        /***************************** Class Methods  */
        /** Saves and sets m.obj  */
        fun forMessage(m: Message, r: Any?, ex: Throwable?): AsyncResult {
            val ret: AsyncResult
            ret = AsyncResult(m.obj, r, ex)
            m.obj = ret
            return ret
        }

        /** Saves and sets m.obj  */
        fun forMessage(m: Message): AsyncResult {
            val ret: AsyncResult
            ret = AsyncResult(m.obj, null, null)
            m.obj = ret
            return ret
        }
    }
}