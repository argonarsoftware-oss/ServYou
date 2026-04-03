package com.servyou.app.ui

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.servyou.app.R
import com.servyou.app.data.Booking
import com.servyou.app.databinding.ItemBookingBinding

class BookingAdapter(
    private val onItemClick: (Booking) -> Unit
) : ListAdapter<Booking, BookingAdapter.ViewHolder>(BookingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemBookingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: Booking) {
            binding.tvServiceName.text = booking.service
            binding.tvCustomerName.text = booking.customerName
            binding.tvDate.text = booking.date
            binding.tvTime.text = booking.time
            binding.tvStatus.text = booking.status

            val statusColor = when (booking.status) {
                Booking.STATUS_PENDING -> R.color.status_pending
                Booking.STATUS_CONFIRMED -> R.color.status_confirmed
                Booking.STATUS_COMPLETED -> R.color.status_completed
                Booking.STATUS_CANCELLED -> R.color.status_cancelled
                else -> R.color.status_pending
            }

            val color = ContextCompat.getColor(binding.root.context, statusColor)
            binding.statusBar.setBackgroundColor(color)

            val badgeBg = binding.tvStatus.background as? GradientDrawable
                ?: GradientDrawable().apply {
                    cornerRadius = 20f * binding.root.context.resources.displayMetrics.density
                }
            badgeBg.setColor(color)
            binding.tvStatus.background = badgeBg

            binding.root.setOnClickListener { onItemClick(booking) }
        }
    }

    class BookingDiffCallback : DiffUtil.ItemCallback<Booking>() {
        override fun areItemsTheSame(oldItem: Booking, newItem: Booking) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Booking, newItem: Booking) = oldItem == newItem
    }
}
