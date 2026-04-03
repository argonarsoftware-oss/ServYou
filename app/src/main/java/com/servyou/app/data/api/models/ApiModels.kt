package com.servyou.app.data.api.models

import com.google.gson.annotations.SerializedName

// -- Requests --

data class BookingRequest(
    @SerializedName("customer_name") val customerName: String,
    @SerializedName("contact_number") val contactNumber: String,
    val email: String,
    val service: String,
    val date: String,
    val time: String,
    val notes: String = "",
    val status: String = "Pending"
)

data class StatusRequest(val status: String)

// -- Responses --

data class BookingListResponse(
    val success: Boolean,
    val data: List<ApiBooking>,
    val total: Int,
    val page: Int,
    val limit: Int
)

data class BookingResponse(
    val success: Boolean,
    val data: ApiBooking
)

data class CreateBookingResponse(
    val success: Boolean,
    val id: Long,
    val message: String
)

data class StatsResponse(
    val success: Boolean,
    val data: StatsData
)

data class StatsData(
    val total: Int,
    val pending: Int?,
    val confirmed: Int?,
    val completed: Int?,
    val cancelled: Int?
)

data class MessageResponse(
    val success: Boolean,
    val message: String
)

data class ApiBooking(
    val id: Long,
    @SerializedName("customer_name") val customerName: String,
    @SerializedName("contact_number") val contactNumber: String,
    val email: String,
    val service: String,
    val date: String,
    val time: String,
    val notes: String,
    val status: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)
