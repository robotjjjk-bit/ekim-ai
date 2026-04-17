package net.ekmai.android.`in`.utilities

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(
    name = "model_pref"
)

object ModelPref {
    val MODEL_NAME = stringPreferencesKey("model_name")
}