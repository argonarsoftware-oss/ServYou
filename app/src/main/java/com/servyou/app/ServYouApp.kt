package com.servyou.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.servyou.app.data.AppDatabase
import com.servyou.app.data.BookingRepository

class ServYouApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { BookingRepository(database.bookingDao()) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Booking Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for upcoming booking reminders"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "booking_reminders"
    }
}
