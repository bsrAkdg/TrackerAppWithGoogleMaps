package com.bsrakdg.trackerappwithgooglemaps.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bsrakdg.trackerappwithgooglemaps.R
import com.bsrakdg.trackerappwithgooglemaps.databinding.FragmentStatisticsBinding
import com.bsrakdg.trackerappwithgooglemaps.ui.viewmodels.StatisticsViewModel
import com.bsrakdg.trackerappwithgooglemaps.utils.TrackingUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.round

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private lateinit var binding: FragmentStatisticsBinding

    private val viewModel: StatisticsViewModel by viewModels()

    private fun subscribeToObserver() {
        viewModel.totalTimeRun.observe(viewLifecycleOwner, {
            it?.let {
                val totalTimeRun = TrackingUtil.getFormattedStopWatchTime(it)
                binding.tvTotalTime.text = totalTimeRun
            }
        })
        viewModel.totalDistance.observe(viewLifecycleOwner, {
            it?.let {
                val km = it / 1000f
                val totalDistance = round(km * 10f) / 10f
                val totalDistanceString = "${totalDistance}km"
                binding.tvTotalDistance.text = totalDistanceString
            }
        })
        viewModel.totalAvgSpeed.observe(viewLifecycleOwner, {
            it?.let {
                val avgSpeed = round(it * 10f) / 10f
                val avgSpeedString = "${avgSpeed}km/h"
                binding.tvAverageSpeed.text = avgSpeedString
            }
        })
        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner, {
            it?.let {
                val totalCalories = "${it}kcal"
                binding.tvTotalCalories.text = totalCalories
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentStatisticsBinding.bind(view)

        subscribeToObserver()
    }
}