package com.bsrakdg.trackerappwithgooglemaps.other

import android.content.Context
import android.view.LayoutInflater
import com.bsrakdg.trackerappwithgooglemaps.databinding.MarkerViewBinding
import com.bsrakdg.trackerappwithgooglemaps.db.Run
import com.bsrakdg.trackerappwithgooglemaps.utils.TrackingUtil
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.*

class CustomMarkerView(
    val runs: List<Run>,
    c: Context,
    layoutId: Int,
) : MarkerView(c, layoutId) {

    private var binding: MarkerViewBinding = MarkerViewBinding.inflate(LayoutInflater.from(context), this, true)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if (e == null) {
            return
        }
        val currentRunId = e.x.toInt()
        val run = runs[currentRunId]

        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timeStamp
        }

        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val avgSpeed = "${run.avgSpeedInKMH}km/h"
        val distanceInKm = "${run.distanceInMeters / 1000f}km"
        val caloriesBurned = "${run.caloriesBurned}kcal"

        binding.apply {
            tvDate.text = dateFormat.format(calendar.time)
            tvAvgSpeed.text = avgSpeed
            tvDistance.text = distanceInKm
            tvDuration.text = TrackingUtil.getFormattedStopWatchTime(run.timeInMillis)
            tvCaloriesBurned.text = caloriesBurned
        }
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())
    }
}


