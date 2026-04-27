package net.ekmai.android.`in`.utilities

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import kotlin.time.Clock

object ImportExport {
    fun export(context: Context, chatList: List<Message>) {
        val timeMillis = Clock.System.now().toEpochMilliseconds()
        val fileName = "chats_${timeMillis}.json"
        val jsonArray = JSONArray()

        chatList.forEach {
            val jsonObject = JSONObject().apply {
                put("text", it.text)
                put("isUser", it.isUser)
                put("model", it.model)
                put("thinking", it.thinking)
            }

            jsonArray.put(jsonObject)
        }

        val success = saveToInternalStorage(context, fileName, jsonArray.toString(4))

        if (success) {
            Toast.makeText(context, "Saved $fileName successfully", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "An error occurred", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveToInternalStorage(context: Context, fileName: String, data: String): Boolean {
        return try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(
                MediaStore.Files.getContentUri("external"),
                contentValues
            ) ?: return false
            resolver.openOutputStream(uri)?.use { output ->
                output.write(data.toByteArray())
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun import(context: Context, uri: Uri): List<Message>? {
        return try {
            val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            val jsonArray = JSONArray(json)
            val list = mutableListOf<Message>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)

                list.add(
                    Message(
                        text = obj.getString("text"),
                        isUser = obj.getBoolean("isUser"),
                        model = obj.getString("model"),
                        thinking = obj.optString("thinking", null)
                    )
                )
            }

            list
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}