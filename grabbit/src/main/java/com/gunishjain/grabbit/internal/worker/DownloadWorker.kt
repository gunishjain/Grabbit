package com.gunishjain.grabbit.internal.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gunishjain.grabbit.internal.database.DownloadDatabase

class DownloadWorker(
    private val context: Context,
    private val workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    private val downloadDao = DownloadDatabase.getInstance(context).downloadDao()

    //Define Notification Manager here

    override suspend fun doWork(): Result {

        val downloadId = inputData.getInt("downloadId", -1) // we will get input data from Download Manager
        if (downloadId == -1) return Result.failure()



        //call Notification Manager and implement Download Task Here



        return Result.success()

    }


}