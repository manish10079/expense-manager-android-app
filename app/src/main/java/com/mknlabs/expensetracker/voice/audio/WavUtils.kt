package com.mknlabs.expensetracker.voice.audio

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object WavUtils {
    const val SAMPLE_RATE = 16000
    const val CHANNELS = 1
    const val BITS_PER_SAMPLE = 16

    /**
     * Builds the standard 44-byte RIFF/WAV header for 16-bit PCM.
     */
    fun writeWavHeader(
        totalAudioLen: Long,
        totalDataLen: Long,
        longSampleRate: Long = SAMPLE_RATE.toLong(),
        channels: Int = CHANNELS,
        byteRate: Long = (SAMPLE_RATE * CHANNELS * (BITS_PER_SAMPLE / 8)).toLong(),
    ): ByteArray {
        val header = ByteArray(44)
        val buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)

        // RIFF header
        buffer.put('R'.code.toByte())
        buffer.put('I'.code.toByte())
        buffer.put('F'.code.toByte())
        buffer.put('F'.code.toByte())
        buffer.putInt(totalDataLen.toInt())
        buffer.put('W'.code.toByte())
        buffer.put('A'.code.toByte())
        buffer.put('V'.code.toByte())
        buffer.put('E'.code.toByte())

        // fmt chunk
        buffer.put('f'.code.toByte())
        buffer.put('m'.code.toByte())
        buffer.put('t'.code.toByte())
        buffer.put(' '.code.toByte())
        buffer.putInt(16) // Subchunk1Size = 16 for PCM
        buffer.putShort(1.toShort()) // AudioFormat 1 = PCM
        buffer.putShort(channels.toShort())
        buffer.putInt(longSampleRate.toInt())
        buffer.putInt(byteRate.toInt())
        buffer.putShort((channels * (BITS_PER_SAMPLE / 8)).toShort()) // BlockAlign
        buffer.putShort(BITS_PER_SAMPLE.toShort())

        // data chunk
        buffer.put('d'.code.toByte())
        buffer.put('a'.code.toByte())
        buffer.put('t'.code.toByte())
        buffer.put('a'.code.toByte())
        buffer.putInt(totalAudioLen.toInt())

        return header
    }

    /**
     * Wraps raw 16-bit PCM bytes from [pcmFile] with the standard WAV header and writes to [wavFile].
     */
    fun convertPcmToWav(
        pcmFile: File,
        wavFile: File,
        sampleRate: Int = SAMPLE_RATE,
        channels: Int = CHANNELS,
    ) {
        val pcmSize = pcmFile.length()
        val header = writeWavHeader(
            totalAudioLen = pcmSize,
            totalDataLen = pcmSize + 36,
            longSampleRate = sampleRate.toLong(),
            channels = channels,
            byteRate = (sampleRate * channels * (BITS_PER_SAMPLE / 8)).toLong(),
        )

        FileOutputStream(wavFile).use { out ->
            out.write(header)
            FileInputStream(pcmFile).use { input ->
                input.copyTo(out)
            }
        }
    }
}
