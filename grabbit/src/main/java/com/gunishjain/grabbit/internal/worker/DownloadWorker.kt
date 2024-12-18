package com.gunishjain.grabbit.internal.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gunishjain.grabbit.internal.database.DownloadDatabase
import com.gunishjain.grabbit.internal.notifications.DownloadNotificationManager

class DownloadWorker(
    private val context: Context,
    private val workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    private val downloadDao = DownloadDatabase.getInstance(context).downloadDao()
    private var downloadNotificationManager: DownloadNotificationManager? = null

    //Define Notification Manager here

    override suspend fun doWork(): Result {

        // we need data from Download Manager


        val downloadId = inputData.getInt("downloadId", -1) // we will get input data from Download Manager
        if (downloadId == -1) return Result.failure()



        //call Notification Manager and implement Download Task Here



        return Result.success()

    }


}