package com.servyou.app.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BookingDao {

    @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
    fun getAllBookings(): LiveData<List<Booking>>

    @Query("SELECT * FROM bookings WHERE id = :id")
    suspend fun getBookingById(id: Long): Booking?

    @Query("SELECT COUNT(*) FROM bookings")
    fun getTotalCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM bookings WHERE status = :status")
    fun getCountByStatus(status: String): LiveData<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(booking: Booking): Long

    @Transaction
    suspend fun replaceAll(bookings: List<Booking>) {
        deleteAll()
        insertAll(bookings)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bookings: List<Booking>)

    @Query("DELETE FROM bookings")
    suspend fun deleteAll()

    @Update
    suspend fun update(booking: Booking)

    @Delete
    suspend fun delete(booking: Booking)

    @Query("UPDATE bookings SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
}
