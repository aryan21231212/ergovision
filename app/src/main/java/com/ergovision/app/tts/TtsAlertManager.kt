package com.ergovision.app.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.ergovision.app.data.model.HazardType
import java.util.Locale

/**
 * Offline Android Text-to-Speech manager.
 * Delivers deterministic Tier 1 spoken warnings (<50ms trigger) without blocking on LLM inference.
 */
class TtsAlertManager(context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var isSpeaking = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                isInitialized = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
            }
        }

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                isSpeaking = true
            }

            override fun onDone(utteranceId: String?) {
                isSpeaking = false
            }

            override fun onError(utteranceId: String?) {
                isSpeaking = false
            }
        })
    }

    fun triggerTemplateAlert(hazardType: HazardType) {
        if (!isInitialized || isSpeaking) return

        val utteranceId = "ErgoAlert_${System.currentTimeMillis()}"
        val text = hazardType.defaultSpokenAlert

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
