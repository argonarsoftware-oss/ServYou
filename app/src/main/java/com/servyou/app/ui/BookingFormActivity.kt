package com.servyou.app.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Patterns
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.servyou.app.R
import com.servyou.app.data.Booking
import com.servyou.app.databinding.ActivityBookingFormBinding
import com.servyou.app.util.ReminderScheduler
import java.text.SimpleDateFormat
import java.util.*

class BookingFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingFormBinding
    private val viewModel: BookingViewModel by viewModels()
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupServiceDropdown()
        setupDateTimePickers()
        setupButtons()
        observeResult()
    }

    private fun setupServiceDropdown() {
        val services = resources.getStringArray(R.array.services)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, services)
        binding.actvService.setAdapter(adapter)
    }

    private fun setupDateTimePickers() {
        binding.etDate.setOnClickListener {
            DatePickerDialog(
                this,
                R.style.Theme_ServYou,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    binding.etDate.setText(format.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.minDate = System.currentTimeMillis()
            }.show()
        }

        binding.etTime.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    binding.etTime.setText(format.format(calendar.time))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        }
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnConfirmBooking.setOnClickListener {
            if (validateForm()) {
                val booking = Booking(
                    customerName = binding.etFullName.text.toString().trim(),
                    contactNumber = binding.etContactNumber.text.toString().trim(),
                    email = binding.etEmail.text.toString().trim(),
                    service = binding.actvService.text.toString().trim(),
                    date = binding.etDate.text.toString().trim(),
                    time = binding.etTime.text.toString().trim(),
                    notes = binding.etNotes.text.toString().trim()
                )
                viewModel.insert(booking)
            }
        }
    }

    private fun observeResult() {
        viewModel.bookingResult.observe(this) { bookingId ->
            if (bookingId > 0) {
                // Schedule reminder
                val booking = Booking(
                    id = bookingId,
                    customerName = binding.etFullName.text.toString().trim(),
                    contactNumber = binding.etContactNumber.text.toString().trim(),
                    email = binding.etEmail.text.toString().trim(),
                    service = binding.actvService.text.toString().trim(),
                    date = binding.etDate.text.toString().trim(),
                    time = binding.etTime.text.toString().trim(),
                    notes = binding.etNotes.text.toString().trim()
                )
                ReminderScheduler.scheduleReminder(this, booking)
                showSuccessDialog(booking)
            }
        }
    }

    private fun showSuccessDialog(booking: Booking) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_booking_success)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)

        dialog.findViewById<TextView>(R.id.tvSuccessMessage).text =
            "${booking.service}\n${booking.date} at ${booking.time}\n\nA confirmation will be sent to ${booking.email}"

        dialog.findViewById<MaterialButton>(R.id.btnOk).setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }

    private fun validateForm(): Boolean {
        var isValid = true

        if (binding.etFullName.text.isNullOrBlank()) {
            binding.tilFullName.error = getString(R.string.field_required)
            isValid = false
        } else {
            binding.tilFullName.error = null
        }

        if (binding.etContactNumber.text.isNullOrBlank()) {
            binding.tilContactNumber.error = getString(R.string.field_required)
            isValid = false
        } else {
            binding.tilContactNumber.error = null
        }

        val email = binding.etEmail.text.toString().trim()
        if (email.isBlank()) {
            binding.tilEmail.error = getString(R.string.field_required)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.invalid_email)
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (binding.actvService.text.isNullOrBlank()) {
            binding.tilService.error = getString(R.string.field_required)
            isValid = false
        } else {
            binding.tilService.error = null
        }

        if (binding.etDate.text.isNullOrBlank()) {
            binding.tilDate.error = getString(R.string.field_required)
            isValid = false
        } else {
            binding.tilDate.error = null
        }

        if (binding.etTime.text.isNullOrBlank()) {
            binding.tilTime.error = getString(R.string.field_required)
            isValid = false
        } else {
            binding.tilTime.error = null
        }

        return isValid
    }
}
