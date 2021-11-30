package com.example.soundtouch.common

import android.os.Handler
import java.util.*

class RegistrantList {
    //var registrants: ArrayList<*> = ArrayList<Any?>() // of Registrant
    var registrants: ArrayList<Registrant?> = ArrayList<Registrant?>()
    @Synchronized
    fun add(h: Handler?, what: Int, obj: Any?) {
        add(Registrant(h, what, obj))
    }

    @Synchronized
    fun addUnique(h: Handler, what: Int, obj: Any?) {
        // if the handler is already in the registrant list, remove it
        remove(h)
        add(Registrant(h, what, obj))
    }

    @Synchronized
    fun add(r: Registrant?) {
        removeCleared()
        registrants.add(r)
    }

    @Synchronized
    fun removeCleared() {
        for (i in registrants.indices.reversed()) {
            val r = registrants[i] as Registrant
            if (r.refH == null) {
                registrants.removeAt(i)
            }
        }
    }

    @Synchronized
    fun size(): Int {
        return registrants.size
    }

    @Synchronized
    operator fun get(index: Int): Registrant? {
        return registrants[index]
    }

    @Synchronized
    private fun internalNotifyRegistrants(result: Any?, exception: Throwable?) {
        var i = 0
        val s = registrants.size
        while (i < s) {
            val r = registrants[i] as Registrant
            r.internalNotifyRegistrant(result, exception)
            i++
        }
    }

    /*synchronized*/   fun notifyRegistrants() {
        internalNotifyRegistrants(null, null)
    }

    /*synchronized*/   fun notifyException(exception: Throwable?) {
        internalNotifyRegistrants(null, exception)
    }

    /*synchronized*/   fun notifyResult(result: Any?) {
        internalNotifyRegistrants(result, null)
    }

    /*synchronized*/   fun notifyRegistrants(ar: AsyncResult) {
        internalNotifyRegistrants(ar.result, ar.exception)
    }

    @Synchronized
    fun remove(h: Handler) {
        var i = 0
        val s = registrants.size
        while (i < s) {
            val r = registrants[i] as Registrant
            var rh: Handler?
            rh = r.handler

            /* Clean up both the requested registrant and
             * any now-collected registrants
             */if (rh == null || rh === h) {
                r.clear()
            }
            i++
        }
        removeCleared()
    }
}