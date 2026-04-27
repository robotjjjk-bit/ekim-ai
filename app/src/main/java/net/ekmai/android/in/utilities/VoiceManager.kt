package net.ekmai.android.`in`.utilities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class VoiceState {
    object Idle : VoiceState()
    object Listening : VoiceState()
    object Processing : VoiceState()
    data class Result(val text: String) : VoiceState()
    data class Error(val message: String) : VoiceState()
}

class VoiceManager(private val context: Context) {

    private val _state = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val state: StateFlow<VoiceState> = _state.asStateFlow()
    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private var recognizer: SpeechRecognizer? = null

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening() {
        if (_state.value is VoiceState.Listening) return
        _state.value = VoiceState.Listening
        _rmsLevel.value = 0f

        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {

                override fun onReadyForSpeech(params: Bundle?) {
                    _state.value = VoiceState.Listening
                }

                override fun onBeginningOfSpeech() {
                    _state.value = VoiceState.Listening
                }

                override fun onRmsChanged(rmsdB: Float) {
                    val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                    _rmsLevel.value = normalized
                }

                override fun onEndOfSpeech() {
                    _state.value = VoiceState.Processing
                    _rmsLevel.value = 0f
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull()?.trim() ?: ""
                    _state.value = if (text.isNotEmpty()) VoiceState.Result(text)
                    else VoiceState.Error("Couldn't hear anything")
                }

                override fun onError(error: Int) {
                    _rmsLevel.value = 0f
                    val msg = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH        -> "No speech detected"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT  -> "Listening timed out"
                        SpeechRecognizer.ERROR_AUDIO           -> "Audio error"
                        SpeechRecognizer.ERROR_NETWORK         -> "Network error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                        else -> "Voice error ($error)"
                    }
                    _state.value = VoiceState.Error(msg)
                }

                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onPartialResults(partial: Bundle?) {
                    val matches = partial?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull()?.trim() ?: ""
                    if (text.isNotEmpty()) _state.value = VoiceState.Result(text)
                }
                override fun onEvent(type: Int, params: Bundle?) {}
            })

            startListening(
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
                }
            )
        }
    }

    fun stopListening() {
        recognizer?.stopListening()
        _state.value = VoiceState.Processing
        _rmsLevel.value = 0f
    }

    fun reset() {
        recognizer?.cancel()
        _state.value = VoiceState.Idle
        _rmsLevel.value = 0f
    }

    fun destroy() {
        recognizer?.destroy()
        recognizer = null
        _state.value = VoiceState.Idle
    }
}