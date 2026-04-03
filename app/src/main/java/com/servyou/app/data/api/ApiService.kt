package com.servyou.app.data.api

import com.servyou.app.data.api.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET("bookings")
    suspend fun getBookings(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("status") status: String? = null
    ): Response<BookingListResponse>

    @GET("bookings/{id}")
    suspend fun getBooking(@Path("id") id: Long): Response<BookingResponse>

    @GET("bookings/stats")
    suspend fun getStats(): Response<StatsResponse>

    @POST("bookings")
    suspend fun createBooking(@Body request: BookingRequest): Response<CreateBookingResponse>

    @PUT("bookings/{id}")
    suspend fun updateBooking(
        @Path("id") id: Long,
        @Body request: BookingRequest
    ): Response<MessageResponse>

    @PATCH("bookings/{id}")
    suspend fun updateStatus(
        @Path("id") id: Long,
        @Body request: StatusRequest
    ): Response<MessageResponse>

    @DELETE("bookings/{id}")
    suspend fun deleteBooking(@Path("id") id: Long): Response<MessageResponse>
}
