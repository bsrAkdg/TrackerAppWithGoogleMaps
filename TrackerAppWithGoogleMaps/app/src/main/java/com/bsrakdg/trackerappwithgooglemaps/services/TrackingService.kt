package com.bsrakdg.trackerappwithgooglemaps.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.bsrakdg.trackerappwithgooglemaps.R
import com.bsrakdg.trackerappwithgooglemaps.utils.Constants
import com.bsrakdg.trackerappwithgooglemaps.utils.Constants.ACTION_PAUSE_SERVICE
import com.bsrakdg.trackerappwithgooglemaps.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.bsrakdg.trackerappwithgooglemaps.utils.Constants.ACTION_STOP_SERVICE
import com.bsrakdg.trackerappwithgooglemaps.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.bsrakdg.trackerappwithgooglemaps.utils.Constants.NOTIFICATION_CHANNEL_NAME
import com.bsrakdg.trackerappwithgooglemaps.utils.Constants.NOTIFICATION_ID
import com.bsrakdg.trackerappwithgooglemaps.utils.Constants.TIMER_UPDATE_INTERVAL
import com.bsrakdg.trackerappwithgooglemaps.utils.TrackingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * We are going to listen LiveData objects from this service.
 * For this reason, this class should be a LifecycleService.
 */

typealias Polyline = MutableList<LatLng> // ...
typealias Polylines = MutableList<Polyline> // ... ... ...

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    var isFirstRun = true
    var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val timeRunInSeconds = MutableLiveData<Long>() // For updating notification test listens by service

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    lateinit var currentNotificationBuilder: NotificationCompat.Builder

    // for fragment
    companion object {
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>() // for add new polyline to map
    }

    /** Resets values before start and after stop service */
    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf()) // empty list
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        currentNotificationBuilder = baseNotificationBuilder // for change notification text

        postInitialValues()

        fusedLocationProviderClient = FusedLocationProviderClient(this)

        // this class extends from LifecycleService, you can observe live data
        isTracking.observe(this, {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Sending intent from Fragment to Service to communication
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        // start new service
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        // Resumes service if it has started before
                        Timber.d("Resuming the service ...")
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> { // When clicked STOP button
                    Timber.d("Paused Service")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> { // When clicked FINISH run button
                    Timber.d("Stopped Service")
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L // total time
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L

    /** Starts timer and updates time variables during tracking to update ui and notification text */
    private fun startTimer() {
        addEmptyPolyline() // first empty polyline
        isTracking.postValue(true)

        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true

        // launch Main scope to update LiveData
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                // time difference between now and timeStarted
                lapTime = System.currentTimeMillis() - timeStarted
                // post the new lapTime
                timeRunInMillis.postValue(timeRun + lapTime)
                if (timeRunInMillis.value!! >= lastSecondTimestamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime // resume service
        }
    }

    private fun killService() {
        serviceKilled = true // stop notification updates
        isFirstRun = true
        isTimerEnabled = false
        postInitialValues()
        stopForeground(true) // removes notification
        stopSelf() // stops service
    }

    /** Updates isTracking to trigger fragment and service to take actions */
    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    /** Updates notification actions and text according to isTracking value.
     * Creates pending intent for pause or resume for corresponding actions. */
    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"

        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 1, resumeIntent, FLAG_UPDATE_CURRENT)
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotificationBuilder, arrayListOf<NotificationCompat.Action>())
        }

        if (!serviceKilled) {
            currentNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_pause, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
        }
    }

    // requestLocationUpdates function doesn't handle third party request permission so use SuppressLint to ignore this reason
    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtil.hasLocationPermissions(this)) {
                val request = LocationRequest().apply {
                    interval = Constants.LOCATION_UPDATE_INTERVAL
                    fastestInterval = Constants.FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }

                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    /** Handle location changes when user changes location, if user is tracking add to list */
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)

            if (isTracking.value!!) { // if we are currently tracking
                result?.locations?.let { locations ->
                    locations.map {
                        addPathPoint(it) // we add each new point to list
                        Timber.d(("NEW LOCATION: ${it.latitude}, ${it.longitude}"))
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos) // add new path point to end of the pathPoints
                pathPoints.postValue(this) // when used apply, you can use this
            }
        }
    }

    /** When user click Stop button while tracking, this method adds an empty polyline in our pathPoints list */
    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this) // empty list, when used apply, you can use this
    } ?: pathPoints.postValue(mutableListOf(mutableListOf())) // when pathPoints is null, initial empty pathPoints list.

    /** Starts timer and shows notification updates */
    private fun startForegroundService() {
        startTimer()
        isTracking.postValue(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        timeRunInSeconds.observe(this, {
            if (!serviceKilled) {
                val notification = currentNotificationBuilder
                    .setContentText(TrackingUtil.getFormattedStopWatchTime(it * 1000L))
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )

        notificationManager.createNotificationChannel(channel)

    }
}