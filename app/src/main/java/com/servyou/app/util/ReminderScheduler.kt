package com.servyou.app.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.servyou.app.data.Booking
import java.text.SimpleDateFormat
import java.util.*

object ReminderScheduler {

    fun scheduleReminder(context: Context, booking: Booking) {
        try {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
            val dateTime = dateFormat.parse("${booking.date} ${booking.time}") ?: return

            // Remind 1 hour before
            val reminderTime = dateTime.time - (60 * 60 * 1000)
            if (reminderTime <= System.currentTimeMillis()) return

            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("booking_id", booking.id)
                putExtra("service_name", booking.service)
                putExtra("time", booking.time)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context, booking.id.toInt(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTime,
                pendingIntent
            )
        } catch (_: Exception) {
            // Handle parse errors gracefully
        }
    }
}
