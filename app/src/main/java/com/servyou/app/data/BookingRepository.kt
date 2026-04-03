package com.servyou.app.data

import androidx.lifecycle.LiveData

class BookingRepository(private val bookingDao: BookingDao) {

    val allBookings: LiveData<List<Booking>> = bookingDao.getAllBookings()
    val totalCount: LiveData<Int> = bookingDao.getTotalCount()
    val pendingCount: LiveData<Int> = bookingDao.getCountByStatus(Booking.STATUS_PENDING)
    val confirmedCount: LiveData<Int> = bookingDao.getCountByStatus(Booking.STATUS_CONFIRMED)

    suspend fun insert(booking: Booking): Long = bookingDao.insert(booking)

    suspend fun update(booking: Booking) = bookingDao.update(booking)

    suspend fun delete(booking: Booking) = bookingDao.delete(booking)

    suspend fun getBookingById(id: Long): Booking? = bookingDao.getBookingById(id)

    suspend fun updateStatus(id: Long, status: String) = bookingDao.updateStatus(id, status)
}
