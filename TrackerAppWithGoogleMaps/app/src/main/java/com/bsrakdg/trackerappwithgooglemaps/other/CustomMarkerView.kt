package com.bsrakdg.trackerappwithgooglemaps.other

import android.content.Context
import android.view.LayoutInflater
import com.bsrakdg.trackerappwithgooglemaps.databinding.MarkerViewBinding
import com.bsrakdg.trackerappwithgooglemaps.db.Run
import com.bsrakdg.trackerappwithgooglemaps.utils.TrackingUtil
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import java.text.SimpleDateFormat
import java.util.*

class CustomMarkerView(
    val runs: List<Run>,
    c: Context,
    layoutId: Int
) : MarkerView(c, layoutId) {

    var binding: MarkerViewBinding

    init {
        val inflater = LayoutInflater.from(c)
        binding = MarkerViewBinding.inflate(inflater, this, true)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if (e == null) {
            return
        }
        val curRunId = e.x.toInt()
        val run = runs[curRunId]

        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timeStamp
        }
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        binding.tvDate.text = dateFormat.format(calendar.time)

        val avgSpeed = "${run.avgSpeedInKMH}km/h"
        binding.tvAvgSpeed.text = avgSpeed

        val distanceInKm = "${run.distanceInMeters / 1000f}km"
        binding.tvDistance.text = distanceInKm

        binding.tvDuration.text = TrackingUtil.getFormattedStopWatchTime(run.timeInMillis)

        val caloriesBurned = "${run.caloriesBurned}kcal"
        binding.tvCaloriesBurned.text = caloriesBurned
    }
}