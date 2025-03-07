package com.light.taskreminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class TaskReminderWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.d("TaskReminderWorker", "Worker started")
        sendNotification()
        return Result.success()
    }

    private fun sendNotification() {
        val tasks = TaskGetter(applicationContext).getTasksForToday()
        val message = if (tasks.isEmpty()) "You are free today, sit back and relax!" else tasks.joinToString("\n")

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "TASK_REMINDER_CHANNEL"

        if (notificationManager.getNotificationChannel(channelId) == null) {
            Log.e("TaskReminderWorker", "Notification channel does not exist! Creating it now.")
            val channel = NotificationChannel(
                channelId,
                "Task Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Reminders for daily tasks" }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("Today's Tasks")
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
        Log.d("TaskReminderWorker", "Notification sent")
    }

}
