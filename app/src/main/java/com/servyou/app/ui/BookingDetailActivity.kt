package com.servyou.app.ui

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.servyou.app.R
import com.servyou.app.data.Booking
import com.servyou.app.databinding.ActivityBookingDetailBinding

class BookingDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingDetailBinding
    private val viewModel: BookingViewModel by viewModels()
    private var currentBooking: Booking? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bookingId = intent.getLongExtra("booking_id", -1)
        if (bookingId == -1L) {
            finish()
            return
        }

        viewModel.loadBooking(bookingId)
        observeBooking()
        setupButtons()
    }

    private fun observeBooking() {
        viewModel.selectedBooking.observe(this) { booking ->
            if (booking == null) {
                finish()
                return@observe
            }
            currentBooking = booking
            displayBooking(booking)
        }
    }

    private fun displayBooking(booking: Booking) {
        binding.tvService.text = booking.service
        binding.tvCustomerName.text = booking.customerName
        binding.tvPhone.text = booking.contactNumber
        binding.tvEmail.text = booking.email
        binding.tvDate.text = booking.date
        binding.tvTime.text = booking.time
        binding.tvStatusBadge.text = booking.status

        if (booking.notes.isNotBlank()) {
            binding.labelNotes.visibility = View.VISIBLE
            binding.tvNotes.visibility = View.VISIBLE
            binding.tvNotes.text = booking.notes
        }

        val statusColor = when (booking.status) {
            Booking.STATUS_PENDING -> R.color.status_pending
            Booking.STATUS_CONFIRMED -> R.color.status_confirmed
            Booking.STATUS_COMPLETED -> R.color.status_completed
            Booking.STATUS_CANCELLED -> R.color.status_cancelled
            else -> R.color.status_pending
        }

        val color = ContextCompat.getColor(this, statusColor)
        val badgeBg = GradientDrawable().apply {
            cornerRadius = 20f * resources.displayMetrics.density
            setColor(color)
        }
        binding.tvStatusBadge.background = badgeBg

        // Show/hide action buttons based on status
        when (booking.status) {
            Booking.STATUS_PENDING -> {
                binding.btnConfirm.visibility = View.VISIBLE
                binding.btnCancel.visibility = View.VISIBLE
                binding.btnComplete.visibility = View.GONE
            }
            Booking.STATUS_CONFIRMED -> {
                binding.btnConfirm.visibility = View.GONE
                binding.btnComplete.visibility = View.VISIBLE
                binding.btnCancel.visibility = View.VISIBLE
            }
            else -> {
                binding.btnConfirm.visibility = View.GONE
                binding.btnComplete.visibility = View.GONE
                binding.btnCancel.visibility = View.GONE
            }
        }
        binding.btnDelete.visibility = View.VISIBLE
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnConfirm.setOnClickListener {
            currentBooking?.let { viewModel.updateStatus(it.id, Booking.STATUS_CONFIRMED) }
        }

        binding.btnComplete.setOnClickListener {
            currentBooking?.let { viewModel.updateStatus(it.id, Booking.STATUS_COMPLETED) }
        }

        binding.btnCancel.setOnClickListener {
            currentBooking?.let { viewModel.updateStatus(it.id, Booking.STATUS_CANCELLED) }
        }

        binding.btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.delete_booking)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.yes) { _, _ ->
                    currentBooking?.let {
                        viewModel.delete(it)
                        finish()
                    }
                }
                .setNegativeButton(R.string.no, null)
                .show()
        }
    }
}
