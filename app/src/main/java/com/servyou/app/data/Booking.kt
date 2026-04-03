package com.servyou.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerName: String,
    val contactNumber: String,
    val email: String,
    val service: String,
    val date: String,
    val time: String,
    val notes: String = "",
    val status: String = STATUS_PENDING,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_PENDING = "Pending"
        const val STATUS_CONFIRMED = "Confirmed"
        const val STATUS_COMPLETED = "Completed"
        const val STATUS_CANCELLED = "Cancelled"
    }
}
