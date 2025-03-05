package com.light.taskreminder

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri

class TaskGetter(private val context: Context) {
    companion object {
        private const val AUTHORITY = "com.light.todo.provider"
        private const val PATH = "tasks"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$PATH")
    }


    fun getTasksForToday(): List<String> {
        return getTasks("date = ?", arrayOf(getTodayDate())).map { it.first }
    }

    fun getAllTasks(): List<Pair<String, String>> {
        return getTasks(null, null)
    }

    private fun getTasks(selection: String?, selectionArgs: Array<String>?): List<Pair<String, String>> {
        val resolver: ContentResolver = context.contentResolver
        val cursor: Cursor? = resolver.query(
            CONTENT_URI,
            arrayOf("_id", "name", "date"),
            selection,
            selectionArgs,
            null
        )

        val tasks = mutableListOf<Pair<String, String>>()
        cursor?.use {
            while (it.moveToNext()) {
                val taskName = it.getString(it.getColumnIndexOrThrow("name"))
                val taskDate = it.getString(it.getColumnIndexOrThrow("date"))
                tasks.add(taskName to taskDate)
            }
        }
        return tasks
    }


    private fun getTodayDate(): String {
        val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}
