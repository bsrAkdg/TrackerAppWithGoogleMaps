package com.bsrakdg.trackerappwithgooglemaps.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.bsrakdg.trackerappwithgooglemaps.db.RunningDatabase
import com.bsrakdg.trackerappwithgooglemaps.db.RunningDatabase.Companion.RUNNING_DATABASE_NAME
import com.bsrakdg.trackerappwithgooglemaps.utils.Constants
import com.bsrakdg.trackerappwithgooglemaps.utils.Constants.KEY_FIRST_TIME_TOGGLE
import com.bsrakdg.trackerappwithgooglemaps.utils.Constants.KEY_NAME
import com.bsrakdg.trackerappwithgooglemaps.utils.Constants.KEY_WEIGHT
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Singleton // Just using @InstallIn(ApplicationComponent::class) doesn't make it a singleton!
    @Provides
    fun provideRunningDatabase(
        @ApplicationContext app: Context,
    ) = Room.databaseBuilder(
        app,
        RunningDatabase::class.java,
        RUNNING_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideRunDao(db: RunningDatabase) = db.getRunDao()

    @Singleton
    @Provides
    fun provideGlide(@ApplicationContext context: Context): RequestManager = Glide.with(context)

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app: Context) =
        app.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideName(sharedPreferences: SharedPreferences) =
        sharedPreferences.getString(KEY_NAME, "") ?: ""

    @Singleton
    @Provides
    fun provideWeight(sharedPreferences: SharedPreferences) =
        sharedPreferences.getFloat(KEY_WEIGHT, 80f)

    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPreferences: SharedPreferences) =
        sharedPreferences.getBoolean(KEY_FIRST_TIME_TOGGLE, true)
}