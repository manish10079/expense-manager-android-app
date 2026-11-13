package com.mknlabs.expensetracker.voice

import com.mknlabs.expensetracker.voice.audio.WavUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WavUtilsTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun writeWavHeader_generatesValidStandard44ByteHeader() {
        val pcmAudioLen = 32000L // 1 second of 16kHz 16-bit mono
        val totalDataLen = pcmAudioLen + 36L

        val header = WavUtils.writeWavHeader(
            totalAudioLen = pcmAudioLen,
            totalDataLen = totalDataLen,
            longSampleRate = 16000L,
            channels = 1,
            byteRate = 32000L
        )

        assertEquals(44, header.size)

        val buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)

        // "RIFF"
        val riff = ByteArray(4)
        buffer.get(riff)
        assertEquals("RIFF", String(riff, Charsets.US_ASCII))

        // Total data len
        val readTotalDataLen = buffer.getInt().toLong()
        assertEquals(totalDataLen, readTotalDataLen)

        // "WAVE"
        val wave = ByteArray(4)
        buffer.get(wave)
        assertEquals("WAVE", String(wave, Charsets.US_ASCII))

        // "fmt "
        val fmt = ByteArray(4)
        buffer.get(fmt)
        assertEquals("fmt ", String(fmt, Charsets.US_ASCII))

        // Subchunk1Size = 16
        val subchunk1Size = buffer.getInt()
        assertEquals(16, subchunk1Size)

        // AudioFormat = 1 (PCM)
        val audioFormat = buffer.getShort().toInt()
        assertEquals(1, audioFormat)

        // Channels = 1 (Mono)
        val channels = buffer.getShort().toInt()
        assertEquals(1, channels)

        // SampleRate = 16000
        val sampleRate = buffer.getInt()
        assertEquals(16000, sampleRate)

        // ByteRate = 32000
        val byteRate = buffer.getInt()
        assertEquals(32000, byteRate)

        // BlockAlign = 2
        val blockAlign = buffer.getShort().toInt()
        assertEquals(2, blockAlign)

        // BitsPerSample = 16
        val bitsPerSample = buffer.getShort().toInt()
        assertEquals(16, bitsPerSample)

        // "data"
        val dataChunk = ByteArray(4)
        buffer.get(dataChunk)
        assertEquals("data", String(dataChunk, Charsets.US_ASCII))

        // Subchunk2Size (audio length)
        val readAudioLen = buffer.getInt().toLong()
        assertEquals(pcmAudioLen, readAudioLen)
    }

    @Test
    fun convertPcmToWav_prependsHeaderCorrectly() {
        val pcmFile = tempFolder.newFile("test_sample.pcm")
        val sampleData = ByteArray(100) { it.toByte() }
        pcmFile.writeBytes(sampleData)

        val wavFile = File(tempFolder.root, "test_sample.wav")
        WavUtils.convertPcmToWav(pcmFile, wavFile, sampleRate = 16000, channels = 1)

        assertTrue(wavFile.exists())
        assertEquals(144, wavFile.length()) // 44 byte header + 100 byte audio

        val wavBytes = wavFile.readBytes()
        assertEquals("RIFF", String(wavBytes.sliceArray(0..3), Charsets.US_ASCII))
        assertEquals("WAVE", String(wavBytes.sliceArray(8..11), Charsets.US_ASCII))
        assertEquals("data", String(wavBytes.sliceArray(36..39), Charsets.US_ASCII))

        // Verify audio payload matches original PCM byte-for-byte
        val payload = wavBytes.sliceArray(44 until 144)
        for (i in sampleData.indices) {
            assertEquals(sampleData[i], payload[i])
        }
    }
}
