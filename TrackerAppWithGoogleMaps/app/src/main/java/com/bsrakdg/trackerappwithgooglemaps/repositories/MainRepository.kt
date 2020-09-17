package com.bsrakdg.trackerappwithgooglemaps.repositories

import com.bsrakdg.trackerappwithgooglemaps.db.Run
import com.bsrakdg.trackerappwithgooglemaps.db.RunDAO
import javax.inject.Inject

/**
 * There is no provide function for MainRepository, however we can inject it.
 * Because there is only one dependency for MainRepository which is RunDAO.
 * In our AppModule we created a provide function for RunDAO.
 * For this reason, Dagger knows how to generate RunDAO and also, how to generate MainRepository.
 */
class MainRepository
@Inject
constructor(
    val runDAO: RunDAO
) {
    suspend fun insertRun(run: Run) = runDAO.insertRun(run)

    suspend fun deleteRun(run: Run) = runDAO.deleteRun(run)

    fun getAllRunsSortedByDate() = runDAO.getAllRunsSortedByDate()

    fun getAllRunsSortedByDistance() = runDAO.getAllRunsSortedByDistanceInMeters()

    fun getAllRunsSortedByTimeInMillis() = runDAO.getAllRunsSortedByTimeInMillis()

    fun getAllRunsSortedByAvgSpeed() = runDAO.getAllRunsSortedByAvgSpeedInKMH()

    fun getAllRunsSortedByCaloriesBurned() = runDAO.getAllRunsSortedByCaloriesBurned()

    fun getTotalAvgSpeed() = runDAO.getTotalAvgSpeed()

    fun getTotalDistance() = runDAO.getTotalTotalDistance()

    fun getTotalCaloriesBurned() = runDAO.getTotalCaloriesBurned()

    fun getTotalTimeInMillis() = runDAO.getTotalTimeInMillis()


}