package com.servyou.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.servyou.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: BookingViewModel by viewModels()
    private lateinit var adapter: BookingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSwipeRefresh()
        observeData()

        binding.fabNewBooking.setOnClickListener {
            startActivity(Intent(this, BookingFormActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.syncFromServer()
    }

    private fun setupRecyclerView() {
        adapter = BookingAdapter { booking ->
            val intent = Intent(this, BookingDetailActivity::class.java)
            intent.putExtra("booking_id", booking.id)
            startActivity(intent)
        }
        binding.rvBookings.layoutManager = LinearLayoutManager(this)
        binding.rvBookings.adapter = adapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(
            com.servyou.app.R.color.brown_primary,
            com.servyou.app.R.color.gold
        )
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.syncFromServer()
        }
    }

    private fun observeData() {
        viewModel.allBookings.observe(this) { bookings ->
            adapter.submitList(bookings)
            val hasBookings = bookings.isNotEmpty()
            binding.swipeRefresh.visibility = if (hasBookings) View.VISIBLE else View.GONE
            binding.tvEmptyState.visibility = if (hasBookings) View.GONE else View.VISIBLE
            binding.swipeRefresh.isRefreshing = false
        }

        viewModel.totalCount.observe(this) { count ->
            binding.tvTotalBookings.text = count.toString()
        }

        viewModel.pendingCount.observe(this) { count ->
            binding.tvPendingBookings.text = count.toString()
        }

        viewModel.confirmedCount.observe(this) { count ->
            binding.tvConfirmedBookings.text = count.toString()
        }

        viewModel.syncError.observe(this) { error ->
            binding.swipeRefresh.isRefreshing = false
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
