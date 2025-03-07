package com.light.taskreminder


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import androidx.core.content.edit
import androidx.work.OneTimeWorkRequestBuilder

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
        createNotificationChannel()
        scheduleDailyNotification()

    }
    private fun createNotificationChannel() {
        val channelId = "TASK_REMINDER_CHANNEL"
        val channelName = "Task Reminder"
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders for daily tasks"
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun scheduleDailyNotification() {
        val workManager = WorkManager.getInstance(this)

        val constraints = Constraints.Builder().build()


        val dailyWorkRequest = PeriodicWorkRequestBuilder<TaskReminderWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .build()

        Log.d("WorkManagerDebug", "Scheduling daily notification work request")

        workManager.enqueueUniquePeriodicWork(
            "daily_task_reminder",
            ExistingPeriodicWorkPolicy.REPLACE,
            dailyWorkRequest
        )
    }


    private fun calculateInitialDelay(): Long {
        val now = java.util.Calendar.getInstance()
        val target = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 6)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            if (before(now)) {
                add(java.util.Calendar.DAY_OF_YEAR, 1) // Schedule for next day if past 6 AM today
            }
        }
        return target.timeInMillis - now.timeInMillis
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

                val permPref: SharedPreferences = context.getSharedPreferences("permission", Context.MODE_PRIVATE)
                permPref.edit { putBoolean("granted", true) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
