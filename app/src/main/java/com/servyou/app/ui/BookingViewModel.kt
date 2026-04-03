package com.servyou.app.ui

import android.app.Application
import androidx.lifecycle.*
import com.servyou.app.ServYouApp
import com.servyou.app.data.Booking
import com.servyou.app.data.BookingRepository
import kotlinx.coroutines.launch

class BookingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BookingRepository = (application as ServYouApp).repository

    val allBookings: LiveData<List<Booking>> = repository.allBookings
    val totalCount: LiveData<Int> = repository.totalCount
    val pendingCount: LiveData<Int> = repository.pendingCount
    val confirmedCount: LiveData<Int> = repository.confirmedCount

    private val _bookingResult = MutableLiveData<Long>()
    val bookingResult: LiveData<Long> = _bookingResult

    private val _selectedBooking = MutableLiveData<Booking?>()
    val selectedBooking: LiveData<Booking?> = _selectedBooking

    fun insert(booking: Booking) = viewModelScope.launch {
        val id = repository.insert(booking)
        _bookingResult.postValue(id)
    }

    fun loadBooking(id: Long) = viewModelScope.launch {
        _selectedBooking.postValue(repository.getBookingById(id))
    }

    fun updateStatus(id: Long, status: String) = viewModelScope.launch {
        repository.updateStatus(id, status)
        _selectedBooking.postValue(repository.getBookingById(id))
    }

    fun delete(booking: Booking) = viewModelScope.launch {
        repository.delete(booking)
    }
}
