package com.bsrakdg.trackerappwithgooglemaps.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bsrakdg.trackerappwithgooglemaps.R
import com.bsrakdg.trackerappwithgooglemaps.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * While injecting viewModels int the fragment we do not have to write inject
 * private val viewModel: MainViewModel by viewModels() this line of code
 * and Dagger will select the correct viewModel for us
 * and assign it to this viewModel variable here.
 */
@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run) {

    private val viewModel: MainViewModel by viewModels()
}