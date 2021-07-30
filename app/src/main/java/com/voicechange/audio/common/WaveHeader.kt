package com.voicechange.audio.common

import java.io.ByteArrayOutputStream
import java.io.IOException

class WaveHeader {
    private val fileID = charArrayOf('R', 'I', 'F', 'F')
    private var fileLength: Int
    private val wavTag = charArrayOf('W', 'A', 'V', 'E')
    private val fmtHdrID = charArrayOf('f', 'm', 't', ' ')
    private val fmtHdrLeth = 16
    private val formatTag: Short = 1
    var channels: Short = 1
    var sampleRate: Short = 16000
    var bitsPerSample: Short = 16
    private var blockAlign = (channels * bitsPerSample / 8).toShort()
    private var avgBytesPerSec = blockAlign * sampleRate
    private val dataHdrID = charArrayOf('d', 'a', 't', 'a')
    private var dataHdrLeth: Int

    constructor(fileLength: Int) {
        this.fileLength = fileLength + (44 - 8)
        dataHdrLeth = fileLength
    }

    constructor(fileLength: Int, channels: Short, sampleRate: Short, bitsPerSample: Short) {
        this.fileLength = fileLength + (44 - 8)
        dataHdrLeth = fileLength
        this.channels = channels
        this.sampleRate = sampleRate
        this.bitsPerSample = bitsPerSample
        blockAlign = (channels * bitsPerSample / 8).toShort()
        avgBytesPerSec = blockAlign * sampleRate
    }

    /**
     * @return byte[] 44个字节
     * @throws IOException
     */
    @get:Throws(IOException::class)
    val header: ByteArray
        get() {
            val bos = ByteArrayOutputStream()
            WriteChar(bos, fileID)
            WriteInt(bos, fileLength)
            WriteChar(bos, wavTag)
            WriteChar(bos, fmtHdrID)
            WriteInt(bos, fmtHdrLeth)
            WriteShort(bos, formatTag.toInt())
            WriteShort(bos, channels.toInt())
            WriteInt(bos, sampleRate.toInt())
            WriteInt(bos, avgBytesPerSec)
            WriteShort(bos, blockAlign.toInt())
            WriteShort(bos, bitsPerSample.toInt())
            WriteChar(bos, dataHdrID)
            WriteInt(bos, dataHdrLeth)
            bos.flush()
            val r = bos.toByteArray()
            bos.close()
            return r
        }

    @Throws(IOException::class)
    private fun WriteShort(bos: ByteArrayOutputStream, s: Int) {
        val mybyte = ByteArray(2)
        mybyte[1] = (s shl 16 shr 24).toByte()
        mybyte[0] = (s shl 24 shr 24).toByte()
        bos.write(mybyte)
    }

    @Throws(IOException::class)
    private fun WriteInt(bos: ByteArrayOutputStream, n: Int) {
        val buf = ByteArray(4)
        buf[3] = (n shr 24).toByte()
        buf[2] = (n shl 8 shr 24).toByte()
        buf[1] = (n shl 16 shr 24).toByte()
        buf[0] = (n shl 24 shr 24).toByte()
        bos.write(buf)
    }

    private fun WriteChar(bos: ByteArrayOutputStream, id: CharArray) {
        for (i in id.indices) {
            val c = id[i]
            bos.write(c.toInt())
        }
    }
}