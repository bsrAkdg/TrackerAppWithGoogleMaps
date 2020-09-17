package com.bsrakdg.trackerappwithgooglemaps.di

import android.content.Context
import androidx.room.Room
import com.bsrakdg.trackerappwithgooglemaps.db.RunningDatabase
import com.bsrakdg.trackerappwithgooglemaps.db.RunningDatabase.Companion.RUNNING_DATABASE_NAME
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
}