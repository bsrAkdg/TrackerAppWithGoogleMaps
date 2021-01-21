package com.bsrakdg.trackerappwithgooglemaps.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bsrakdg.trackerappwithgooglemaps.R
import com.bsrakdg.trackerappwithgooglemaps.databinding.ActivityMainBinding
import com.bsrakdg.trackerappwithgooglemaps.db.RunDAO
import com.bsrakdg.trackerappwithgooglemaps.utils.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var runDao: RunDAO

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.rootView)

        navigateToTrackingFragmentIfNeeded(intent)

        // custom toolbar
        setSupportActionBar(binding.toolbar)

        binding.apply {
            setSupportActionBar(toolbar)
            val navController = findNavController(R.id.navHostFragment)
            bottomNavigationView.setupWithNavController(navController)
            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.settingsFragment, R.id.runFragment, R.id.statisticsFragment ->
                        bottomNavigationView.visibility = View.VISIBLE
                    else -> bottomNavigationView.visibility = View.GONE
                }
            }
        }
    }


    // when clicked notification (singleTop launch mode)
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
            findNavController(R.id.navHostFragment).navigate(R.id.action_global_tracking_fragment)
        }
    }
}
