package net.ekmai.android.`in`.utilities

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

data class ModelInfo(
    val id: String,
    val provider: String,
    val contextWindow: String,
    val initials: String,
    val tintColor: Color,
    val name: String,
    val apiKey: String,
    val baseUrl: String,
    val type: String
)

private class NativeManager {
    fun getGemini(): String {
        return try {
            getGeminiNative()
        } catch (_: Throwable) {
            "MASK_API_KEY"
        }
    }

    fun getGroq(): String {
        return try {
            getGroqNative()
        } catch (_: Throwable) {
            "MASK_API_KEY"
        }
    }

    private external fun getGeminiNative(): String
    private external fun getGroqNative(): String

    private companion object {
        init {
            try {
                System.loadLibrary("native-lib")
            } catch (_: Throwable) {

            }
        }
    }
}

object ModelsManager {
    private const val GOOGLE_URL = "https://generativelanguage.googleapis.com/v1beta/"
    private const val GROQ_URL = "https://api.groq.com/openai/v1/"
    private const val GROQ = "openai"
    private const val GOOGLE = "google"
    private val nativeManager: NativeManager = NativeManager()
    private val models = listOf(
        ModelInfo(
            id = "openai/gpt-oss-120b",
            initials = "GO",
            name = "GPT Oss 120B",
            apiKey = nativeManager.getGroq(),
            baseUrl = GROQ_URL,
            tintColor = Color(0xFF7C6AF7),
            contextWindow = "200K",
            provider = "OpenAI",
            type = GROQ
        ),
        ModelInfo(
            id = "openai/gpt-oss-20b",
            initials = "GO",
            name = "GPT Oss 20B",
            apiKey = nativeManager.getGroq(),
            baseUrl = GROQ_URL,
            tintColor = Color(0xFF7C6AF7),
            contextWindow = "200K",
            provider = "OpenAI",
            type = GROQ
        ),
        ModelInfo(
            id = "whisper-large-v3",
            initials = "WI",
            name = "Whisper Large v3",
            apiKey = nativeManager.getGroq(),
            baseUrl = GROQ_URL,
            tintColor = Color(0xFF7C6AF7),
            contextWindow = "-",
            provider = "OpenAI",
            type = GROQ
        ),
        ModelInfo(
            id = "whisper-large-v3-turbo",
            initials = "WI",
            name = "Whisper Large v3-turbo",
            apiKey = nativeManager.getGroq(),
            baseUrl = GROQ_URL,
            tintColor = Color(0xFF7C6AF7),
            contextWindow = "-",
            provider = "OpenAI",
            type = GROQ
        ),
        ModelInfo(
            id = "meta-llama/llama-4-scout-17b-16e-instruct",
            initials = "ML",
            name = "Llama 4 17b",
            apiKey = nativeManager.getGroq(),
            baseUrl = GROQ_URL,
            tintColor = Color(0xFFD97706),
            contextWindow = "500K",
            provider = "Meta",
            type = GROQ
        ),
        ModelInfo(
            id = "llama-3.1-8b-instant",
            initials = "ML",
            name = "Llama 3.1 8b Instant",
            apiKey = nativeManager.getGroq(),
            baseUrl = GROQ_URL,
            tintColor = Color(0xFFD97706),
            contextWindow = "500K",
            provider = "Meta",
            type = GROQ
        ),
        ModelInfo(
            id = "qwen/qwen3-32b",
            initials = "QN",
            name = "Qwen 3-32B",
            apiKey = nativeManager.getGroq(),
            baseUrl = GROQ_URL,
            tintColor = Color(0xFF0D9488),
            contextWindow = "500K",
            provider = "Qwen",
            type = GROQ
        ),
        ModelInfo(
            id = "moonshotai/kimi-k2-instruct-0905",
            name = "Kimi K2",
            initials = "MK",
            apiKey = nativeManager.getGroq(),
            baseUrl = GROQ_URL,
            tintColor = Color(0xFFE24B4A),
            contextWindow = "300K",
            provider = "MoonshotAI",
            type = GROQ
        ),
        ModelInfo(
            id = "gemini-2.5-flash",
            initials = "GF",
            apiKey = nativeManager.getGemini(),
            baseUrl = GOOGLE_URL,
            tintColor = Color(0xFF16A34A),
            contextWindow = "NULL",
            provider = "Google",
            name = "Gemini 2.5 Flash",
            type = GOOGLE
        ),
        ModelInfo(
            id = "gemma-4-31b-it",
            initials = "GG",
            name = "Gemma 4 31b it",
            apiKey = nativeManager.getGemini(),
            baseUrl = GOOGLE_URL,
            tintColor = Color(0xFF16A34A),
            contextWindow = "NULL",
            provider = "Google",
            type = GOOGLE
        ),
        ModelInfo(
            id = "gemini-3-flash-preview",
            initials = "F3",
            name = "Gemini 3 flash preview",
            apiKey = nativeManager.getGemini(),
            baseUrl = GOOGLE_URL,
            tintColor = Color(0xFF16A34A),
            contextWindow = "NULL",
            provider = "Google",
            type = GOOGLE
        ),
        ModelInfo(
            id = "allam-2-7b",
            initials = "AL",
            name = "Allam 2-7b",
            apiKey = nativeManager.getGroq(),
            baseUrl = GROQ_URL,
            tintColor = Color(0xFF00E3FF),
            contextWindow = "500K",
            provider = "NCAI",
            type = GROQ
        )
    )

    private val _currentModel = MutableStateFlow(models[2])

    fun getCurrentModel(): ModelInfo = _currentModel.value

    fun getAllModels(): List<ModelInfo> = models

    suspend fun loadSavedModel(context: Context) {
        val prefs = context.dataStore.data.first()
        val savedName = prefs[ModelPref.MODEL_NAME]
        val model = models.find { it.name == savedName }
        if (model != null) {
            _currentModel.value = model
        }
    }

    suspend fun setModel(context: Context, modelName: String) {
        val model = models.find { it.name == modelName }
        model?.let {
            _currentModel.value = it
            context.dataStore.edit { preference ->
                preference[ModelPref.MODEL_NAME] = modelName
            }
        }
    }
}