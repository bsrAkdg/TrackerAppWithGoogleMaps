package com.bsrakdg.trackerappwithgooglemaps.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bsrakdg.trackerappwithgooglemaps.R
import com.bsrakdg.trackerappwithgooglemaps.databinding.FragmentTrackingBinding
import com.bsrakdg.trackerappwithgooglemaps.db.Run
import com.bsrakdg.trackerappwithgooglemaps.services.Polyline
import com.bsrakdg.trackerappwithgooglemaps.services.TrackingService
import com.bsrakdg.trackerappwithgooglemaps.ui.viewmodels.MainViewModel
import com.bsrakdg.trackerappwithgooglemaps.utils.Constants.ACTION_PAUSE_SERVICE
import com.bsrakdg.trackerappwithgooglemaps.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.bsrakdg.trackerappwithgooglemaps.utils.Constants.ACTION_STOP_SERVICE
import com.bsrakdg.trackerappwithgooglemaps.utils.Constants.CANCEL_TRACKING_DIALOG_TAG
import com.bsrakdg.trackerappwithgooglemaps.utils.Constants.MAP_ZOOM
import com.bsrakdg.trackerappwithgooglemaps.utils.Constants.POLYLINE_COLOR
import com.bsrakdg.trackerappwithgooglemaps.utils.Constants.POLYLINE_WIDTH
import com.bsrakdg.trackerappwithgooglemaps.utils.TrackingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()

    private var map: GoogleMap? = null

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()

    private lateinit var binding: FragmentTrackingBinding
    private var curTimeInMillis = 0L

    private var menu: Menu? = null

    @set:Inject
    var weight = 80f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTrackingBinding.bind(view).apply {

            mapView.onCreate(savedInstanceState)

            btnToggleRun.setOnClickListener {
                toggleRun()
            }

            btnFinishRun.setOnClickListener {
                zoomToSeeWholeTrack()
                endRunAndSaveToDb()
            }

            mapView.getMapAsync {
                map = it
                addAllPolyline() // when we rotate fragment or device that gets called
            }
        }

        // Handle configuration changes
        if (savedInstanceState != null) {
            val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(
                CANCEL_TRACKING_DIALOG_TAG) as CancelTrackingDialog?
            cancelTrackingDialog?.setYesListener {
                stopRun()
            }
        }

        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, {
            pathPoints = it
            addLatestPolyline() // new location tracked
            moveCameraToUser() // zoom map
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, {
            curTimeInMillis = it
            val formattedTime = TrackingUtil.getFormattedStopWatchTime(curTimeInMillis, true)
            binding.tvTimer.text = formattedTime
        })
    }

    /** This method triggers service to start/resume or pause timer and listening location updates */
    private fun toggleRun() {
        if (isTracking) {
            // Pause service
            menu?.getItem(0)?.isVisible = true // X button visible
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            // Start service
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    /** Listens service state and updates ui */
    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking && curTimeInMillis > 0L) {
            binding.btnToggleRun.text = getString(R.string.start_tracking)
            binding.btnFinishRun.visibility = VISIBLE
        } else if(isTracking) {
            binding.btnToggleRun.text = getString(R.string.stop_tracking)
            menu?.getItem(0)?.isVisible = true
            binding.btnFinishRun.visibility = GONE
        }
    }

    /** Moves camera to user's location when location is updated */
    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    /** When run is finished, zooms out map to cover all tracking paths */
    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints) {
            for (pos in polyline) {
                bounds.include(pos)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height * 0.05f).toInt()
            )
        )
    }

    /** When run is finished, saves run data with map's snapshot to db */
    private fun endRunAndSaveToDb() {
        map?.snapshot { bmp ->
            var distanceInMeters = 0
            pathPoints.map {
                distanceInMeters += TrackingUtil.calculatePolylineLength(it).toInt()
            }
            val avgSpeed =
                round((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimestamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
            val run =
                Run(bmp, dateTimestamp, avgSpeed, distanceInMeters, curTimeInMillis, caloriesBurned)
            viewModel.insertRun(run)

            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                getString(R.string.tracking_saved),
                Snackbar.LENGTH_LONG
            ).show()

            stopRun()
        }
    }

    /** Draws path on map when page is brought to foreground (notification click) */
    private fun addAllPolyline() {
        pathPoints.map {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(it)

            map?.addPolyline(polylineOptions)
        }
    }

    /** Draws latest path on map when location is updated */
    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)

            map?.addPolyline(polylineOptions)
        }
    }

    // fragment to service communication with using Intent
    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (curTimeInMillis > 0L) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miCancelTracking -> {
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /** Shows cancel tracking dialog when user clicks cancel button from menu*/
    private fun showCancelTrackingDialog() {
        CancelTrackingDialog().apply {
            setYesListener {
                stopRun()
            }
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG_TAG)
    }

    /** Stops run and returs to run fragment */
    private fun stopRun() {
        binding.tvTimer.text = getString(R.string.start_time)
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }
}