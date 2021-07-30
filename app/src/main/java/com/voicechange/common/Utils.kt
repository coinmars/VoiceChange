package com.example.soundtouch.common

import android.os.Environment
import kotlin.experimental.and


object Utils {
    @JvmField
	var localExternalPath = Environment.getExternalStorageDirectory().path
    fun shortToByteSmall(buf: ShortArray): ByteArray {
        val bytes = ByteArray(buf.size * 2)
        var i = 0
        var j = 0
        while (i < buf.size) {
            var s: Short = buf[i]
            val b3 = (s.toInt() and 0xff)
            val b1 = b3.toByte()
            var b2 = (s.toInt() shr 8 and 0xff)
            val b0 = b2.toByte()
            bytes[j] = b1
            bytes[j + 1] = b0
            i++
            j += 2
        }
        return bytes
    }

    /**
     * 通过byte数组取到short
     *
     * @param b
     * @param index
     * 第几位开始取
     * @return
     */
    fun getShort(b: ByteArray?): ShortArray? {
        if (b == null) {
            return null
        }
        val s = ShortArray(b.size / 2)
        for (i in s.indices) {
            s[i] = (b[i * 2 + 1].toInt() shl 8 or b[i * 2 + 0].toInt() and 0xff) as Short
        }
        return s
    }
}
