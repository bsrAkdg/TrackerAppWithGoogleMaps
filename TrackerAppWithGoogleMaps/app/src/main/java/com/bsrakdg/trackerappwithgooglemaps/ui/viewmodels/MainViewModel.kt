package com.bsrakdg.trackerappwithgooglemaps.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.bsrakdg.trackerappwithgooglemaps.repositories.MainRepository

class MainViewModel
@ViewModelInject
constructor(
    val mainRepository: MainRepository
) : ViewModel() {


}