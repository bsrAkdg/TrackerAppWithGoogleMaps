package com.bsrakdg.trackerappwithgooglemaps.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bsrakdg.trackerappwithgooglemaps.db.Run
import com.bsrakdg.trackerappwithgooglemaps.repositories.MainRepository
import kotlinx.coroutines.launch

class MainViewModel
@ViewModelInject
constructor(
    val mainRepository: MainRepository
) : ViewModel() {

    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }
}