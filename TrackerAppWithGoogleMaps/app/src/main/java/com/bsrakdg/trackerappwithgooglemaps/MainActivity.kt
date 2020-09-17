package com.bsrakdg.trackerappwithgooglemaps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bsrakdg.trackerappwithgooglemaps.db.RunDAO
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var runDao: RunDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("runDao", "RUNDAO: ${runDao.hashCode()}")
    }
}
