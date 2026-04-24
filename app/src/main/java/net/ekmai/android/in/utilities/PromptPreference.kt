package net.ekmai.android.`in`.utilities

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PromptPreference(
    context: Context
): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $TABLE_NAME($ID CHAR PRIMARY KEY, $COL_NAME TEXT);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME;")
        onCreate(db)
    }

    fun saveData(id: String, data: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(ID, id)
            put(COL_NAME, data)
        }
        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    fun getData(id: String): String? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NAME, arrayOf(COL_NAME),
            "$ID = ?", arrayOf(id),
            null, null, null
        )

        val result = if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME))
        } else null

        cursor.close()
        return result
    }

    companion object {
        private const val DATABASE_NAME = "prompt_pref.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "DATA"
        private const val ID = "id"
        private const val COL_NAME = "data"
    }
}