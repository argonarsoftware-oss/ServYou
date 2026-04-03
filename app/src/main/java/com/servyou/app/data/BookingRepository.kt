package com.servyou.app.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.servyou.app.data.api.RetrofitClient
import com.servyou.app.data.api.models.BookingRequest
import com.servyou.app.data.api.models.StatusRequest

class BookingRepository(private val bookingDao: BookingDao) {

    private val api = RetrofitClient.apiService

    val allBookings: LiveData<List<Booking>> = bookingDao.getAllBookings()
    val totalCount: LiveData<Int> = bookingDao.getTotalCount()
    val pendingCount: LiveData<Int> = bookingDao.getCountByStatus(Booking.STATUS_PENDING)
    val confirmedCount: LiveData<Int> = bookingDao.getCountByStatus(Booking.STATUS_CONFIRMED)

    private val _syncError = MutableLiveData<String?>()
    val syncError: LiveData<String?> = _syncError

    /**
     * Sync bookings from server into local Room cache
     */
    suspend fun syncFromServer() {
        try {
            val response = api.getBookings()
            if (response.isSuccessful) {
                val apiBookings = response.body()?.data ?: return
                val localBookings = apiBookings.map { remote ->
                    Booking(
                        id = remote.id,
                        customerName = remote.customerName,
                        contactNumber = remote.contactNumber,
                        email = remote.email,
                        service = remote.service,
                        date = remote.date,
                        time = remote.time,
                        notes = remote.notes,
                        status = remote.status
                    )
                }
                bookingDao.replaceAll(localBookings)
                _syncError.postValue(null)
            } else {
                _syncError.postValue("Sync failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("BookingRepo", "Sync failed", e)
            _syncError.postValue("Offline mode — using cached data")
        }
    }

    /**
     * Create booking on server, then cache locally
     */
    suspend fun insert(booking: Booking): Long {
        return try {
            val request = BookingRequest(
                customerName = booking.customerName,
                contactNumber = booking.contactNumber,
                email = booking.email,
                service = booking.service,
                date = booking.date,
                time = booking.time,
                notes = booking.notes
            )
            val response = api.createBooking(request)
            if (response.isSuccessful) {
                val serverId = response.body()?.id ?: 0
                val cached = booking.copy(id = serverId)
                bookingDao.insert(cached)
                _syncError.postValue(null)
                serverId
            } else {
                // Fallback to local
                bookingDao.insert(booking)
            }
        } catch (e: Exception) {
            Log.e("BookingRepo", "Create failed, saving locally", e)
            _syncError.postValue("Saved offline — will sync when connected")
            bookingDao.insert(booking)
        }
    }

    suspend fun getBookingById(id: Long): Booking? = bookingDao.getBookingById(id)

    /**
     * Update status on server and locally
     */
    suspend fun updateStatus(id: Long, status: String) {
        try {
            val response = api.updateStatus(id, StatusRequest(status))
            if (response.isSuccessful) {
                _syncError.postValue(null)
            }
        } catch (e: Exception) {
            Log.e("BookingRepo", "Status update failed on server", e)
            _syncError.postValue("Updated offline — will sync when connected")
        }
        bookingDao.updateStatus(id, status)
    }

    /**
     * Delete on server and locally
     */
    suspend fun delete(booking: Booking) {
        try {
            api.deleteBooking(booking.id)
        } catch (e: Exception) {
            Log.e("BookingRepo", "Delete failed on server", e)
        }
        bookingDao.delete(booking)
    }
}
