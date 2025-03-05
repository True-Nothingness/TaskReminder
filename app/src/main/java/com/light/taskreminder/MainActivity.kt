package com.light.taskreminder

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val permPref: SharedPreferences = getSharedPreferences("permission", Context.MODE_PRIVATE)

        listView = findViewById(R.id.taskListView)

        val taskGetter = TaskGetter(this)
        val tasks = taskGetter.getAllTasks()

        val adapter = object : ArrayAdapter<Pair<String, String>>(this, android.R.layout.simple_list_item_2, android.R.id.text1, tasks) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val text1 = view.findViewById<TextView>(android.R.id.text1)
                val text2 = view.findViewById<TextView>(android.R.id.text2)

                text1.text = tasks[position].first
                text2.text = tasks[position].second

                return view
            }
        }

        listView.adapter = adapter
        if (!permPref.getBoolean("granted", false)) {
            requestAutoStartPermission(this)
        }
        scheduleDailyNotification()
    }
    private fun scheduleDailyNotification() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = Intent(this, TaskReminderReceiver::class.java)

        // Check if the alarm is already set
        val existingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        if (existingIntent != null) {
            return
        }

        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 6)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            if (timeInMillis < System.currentTimeMillis()) {
                add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setRepeating(
            android.app.AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            android.app.AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun requestAutoStartPermission(context: Context) {
        val manufacturer = android.os.Build.MANUFACTURER
        if (manufacturer.equals("Xiaomi", ignoreCase = true)) {
            try {
                val intent = Intent().apply {
                    component = ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                    )
                }
                context.startActivity(intent)
                val permPref: SharedPreferences = getSharedPreferences("permission", Context.MODE_PRIVATE)
                permPref.edit().putBoolean("granted", true).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}
