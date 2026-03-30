package net.ekmai.android.`in`.utilities

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Timeout
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object ApiManager {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun sendMessage(
        chatList: LinkedList
    ): String = withContext(Dispatchers.IO) {
        val model = ModelsManager.getCurrentModel()

        try {
            when (model.type) {
                "google" -> return@withContext handleGoogle(model, chatList)
                "openai" -> return@withContext handleOpenAI(model, chatList)
                else -> return@withContext "Unsupported Model"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext "Error: ${e.message}"
        }
    }

    fun handleOpenAI(model: ModelInfo, chatList: LinkedList): String {
        val url = model.baseUrl + "chat/completions"

        val messagesArray = JSONArray()

        chatList.toApiMessages().forEach {
            val obj = JSONObject()
            obj.put("role", it["role"])
            obj.put("content", it["content"])
            messagesArray.put(obj)
        }

        val body = JSONObject()
        body.put("model", model.id)
        body.put("messages", messagesArray)

        val requestBody = body.toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${model.apiKey}")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->

            val responseBody = response.body.string()
            if (!response.isSuccessful) {
                return "API Error ${response.code}: $responseBody"
            }
            val json = JSONObject(responseBody)

            return json
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        }
    }

    fun handleGoogle(model: ModelInfo, chatList: LinkedList): String {
        val url = "${model.baseUrl}models/${model.id}:generateContent?key=${model.apiKey}"
        val contents = chatList.toGeminiMessages()

        val body = JSONObject()
        body.put("contents", contents)
        val requestBody = body.toString()
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->

            val responseBody = response.body.string()

            val json = JSONObject(responseBody)

            return json
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
        }
    }
}