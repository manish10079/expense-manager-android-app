package com.mknlabs.expensetracker.voice

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mknlabs.expensetracker.voice.audio.WavUtils
import com.mknlabs.expensetracker.voice.transcription.OfflineVoiceModelManager
import com.mknlabs.expensetracker.voice.transcription.WhisperTranscriptionEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class WhisperTranscriptionEngineTest {

    @Test
    fun testOfflineModelDetectionAndTranscription() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val modelManager = OfflineVoiceModelManager(context)
        val modelDir = modelManager.getModelDirectory()

        assertNotNull("Model directory should exist", modelDir)
        assertTrue("Model directory must be a directory", modelDir!!.isDirectory)

        val engine = WhisperTranscriptionEngine(modelManager)
        val initResult = engine.initialize()
        assertTrue("Whisper engine initialization should succeed", initResult.isSuccess)

        // Create a 1-second 16kHz silent WAV file to test inference pipeline
        val pcmFile = File(context.cacheDir, "test_silent.pcm")
        pcmFile.writeBytes(ByteArray(32000)) // 1 second of 16kHz 16-bit mono PCM

        val wavFile = File(context.cacheDir, "test_silent.wav")
        WavUtils.convertPcmToWav(pcmFile, wavFile, 16000, 1)
        pcmFile.delete()

        assertTrue(wavFile.exists())
        val transcriptionResult = engine.transcribe(wavFile)
        wavFile.delete()

        assertTrue("Transcription result should succeed even on silence", transcriptionResult.isSuccess)
        engine.release()
    }
}
