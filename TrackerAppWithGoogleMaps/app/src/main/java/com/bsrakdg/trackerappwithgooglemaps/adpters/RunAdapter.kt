package com.bsrakdg.trackerappwithgooglemaps.adpters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bsrakdg.trackerappwithgooglemaps.databinding.ItemRunBinding
import com.bsrakdg.trackerappwithgooglemaps.db.Run
import com.bsrakdg.trackerappwithgooglemaps.utils.TrackingUtil
import com.bumptech.glide.RequestManager
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class RunAdapter
@Inject
constructor(
    val requestManager: RequestManager
) : ListAdapter<Run, RunAdapter.RunViewHolder>(RunDiffCallback()) {

    inner class RunViewHolder(private val binding: ItemRunBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindRun(run: Run) {

            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timeStamp
            }

            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val avgSpeed = "${run.avgSpeedInKMH}km/h"
            val distanceInKm = "${run.distanceInMeters / 1000f}km"
            val caloriesBurned = "${run.caloriesBurned}kcal"

            binding.apply {
                requestManager.load(run.img).into(ivRunImage)
                tvDate.text = dateFormat.format(calendar.time)
                tvAvgSpeed.text = avgSpeed
                tvDistance.text = distanceInKm
                tvTime.text = TrackingUtil.getFormattedStopWatchTime(run.timeInMillis)
                tvCalories.text = caloriesBurned
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        val binding = ItemRunBinding.inflate(LayoutInflater.from(parent.context))
        return RunViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        holder.bindRun(getItem(position))
    }

}

class RunDiffCallback : DiffUtil.ItemCallback<Run>() {
    override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
        return oldItem.hashCode() == newItem.hashCode()
    }
}
