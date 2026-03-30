package net.ekmai.android.`in`.utilities

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ModelInfo(
    val id: String,
    val provider: String,
    val contextWindow: String,
    val initials: String,
    val tintColor: Color,
    val name: String,
    val apiKey: String,
    val baseUrl: String
)

private class NativeManager {
    fun getGemini(): String {
        return try {
            getGeminiNative()
        } catch (e: Throwable) {
            "MOCK_GEMINI_KEY"
        }
    }

    fun getGroq(): String {
        return try {
            getGroqNative()
        } catch (e: Throwable) {
            "MOCK_GROQ_KEY"
        }
    }

    private external fun getGeminiNative(): String
    private external fun getGroqNative(): String

    private companion object {
        init {
            try {
                System.loadLibrary("native")
            } catch (e: Throwable) {
                // Ignore for previews or other environments without the native library
            }
        }
    }
}

object ModelsManager {
    private val nativeManager: NativeManager = NativeManager()
    private val models = listOf(
        ModelInfo(
            id = "openai/gpt-oss-120b",
            initials = "GO",
            name = "GPT Oss 120B",
            apiKey = nativeManager.getGroq(),
            baseUrl = "https://api.groq.com/openai/v1/",
            tintColor = Color(0xFF7C6AF7),
            contextWindow = "200K",
            provider = "OpenAI"
        ),
        ModelInfo(
            id = "meta-llama/llama-4-scout-17b-16e-instruct",
            initials = "ML",
            name = "Llama 4 17b",
            apiKey = nativeManager.getGroq(),
            baseUrl = "https://api.groq.com/openai/v1/",
            tintColor = Color(0xFFD97706),
            contextWindow = "500K",
            provider = "Meta"
        ),
        ModelInfo(
            id = "qwen/qwen3-32b",
            initials = "QN",
            name = "Qwen 3-32B",
            apiKey = nativeManager.getGroq(),
            baseUrl = "https://api.groq.com/openai/v1/",
            tintColor = Color(0xFF0D9488),
            contextWindow = "500K",
            provider = "Qwen"
        ),
        ModelInfo(
            id = "moonshotai/kimi-k2-instruct-0905",
            name = "Kimi K2",
            initials = "MK",
            apiKey = nativeManager.getGroq(),
            baseUrl = "https://api.groq.com/openai/v1/",
            tintColor = Color(0xFFE24B4A),
            contextWindow = "300K",
            provider = "MoonshotAI"
        ),
        ModelInfo(
            id = "gemini-2.5-flash",
            initials = "GF",
            apiKey = nativeManager.getGemini(),
            baseUrl = "",
            tintColor = Color(0xFF16A34A),
            contextWindow = "NULL",
            provider = "Google",
            name = "Gemini 2.5 Flash"
        ),
        ModelInfo(
            id = "gemma-3n-e4b-it",
            initials = "GG",
            name = "Gemma 3n e4b it",
            apiKey = nativeManager.getGemini(),
            baseUrl = "",
            tintColor = Color(0xFF16A34A),
            contextWindow = "NULL",
            provider = "Google"
        ),
        ModelInfo(
            id = "gemini-3-flash-preview",
            initials = "F3",
            name = "Gemini 3 flash preview",
            apiKey = nativeManager.getGemini(),
            baseUrl = "",
            tintColor = Color(0xFF16A34A),
            contextWindow = "NULL",
            provider = "Google"
        )
    )

    private val _currentModel = MutableStateFlow(models[4])
    var currentModel: StateFlow<ModelInfo> = _currentModel

    fun getCurrentModel(): ModelInfo = _currentModel.value

    fun getAllModels(): List<ModelInfo> = models

    fun setModel(modelName: String) {
        val model = models.find { it.name == modelName }
        model?.let {
            _currentModel.value = it
        }
    }
}