package com.example.soundtouch.common

import android.os.Handler
import android.os.Message
import java.lang.ref.WeakReference

class Registrant(h: Handler?, what: Int, obj: Any?) {
    fun clear() {
        refH = null
        userObj = null
    }

    fun notifyRegistrant() {
        internalNotifyRegistrant(null, null)
    }

    fun notifyResult(result: Any?) {
        internalNotifyRegistrant(result, null)
    }

    fun notifyException(exception: Throwable?) {
        internalNotifyRegistrant(null, exception)
    }

    fun notifyRegistrant(ar: AsyncResult) {
        internalNotifyRegistrant(ar.result, ar.exception)
    }

    fun internalNotifyRegistrant(result: Any?, exception: Throwable?) {
        val h = handler
        if (h == null) {
            clear()
        } else {
            val msg = Message.obtain()
            msg.what = what
            msg.obj = AsyncResult(userObj, result, exception)
            h.sendMessage(msg)
        }
    }

    /**
     * NOTE: May return null if weak reference has been collected
     */
    fun messageForRegistrant(): Message? {
        val h = handler
        return if (h == null) {
            clear()
            null
        } else {
            var msg = h.obtainMessage()
            msg.what = what
            msg.obj = userObj
            msg
        }
    }

    val handler: Handler?
        get() = if (refH == null) null else refH!!.get() as Handler?
    var refH: WeakReference<*>?
    var what: Int
    private var userObj: Any?

    init {
        refH = WeakReference<Any?>(h)
        this.what = what
        userObj = obj
    }
}