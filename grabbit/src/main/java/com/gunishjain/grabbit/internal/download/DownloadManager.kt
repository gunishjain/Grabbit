package com.gunishjain.grabbit.internal.download

import android.content.Context
import android.util.Log
import androidx.work.WorkManager
import com.gunishjain.grabbit.DownloadConfig
import com.gunishjain.grabbit.internal.NotificationConfig
import com.gunishjain.grabbit.internal.database.DownloadDao
import com.gunishjain.grabbit.internal.network.HttpClient
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DownloadManager(
    private val context: Context,
    private val downloadDao: DownloadDao,
    private val workManager: WorkManager,
    private val downloadConfig: DownloadConfig,
    private val notificationConfig: NotificationConfig,
    private val httpClient: HttpClient
) {

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.d("Download Manager",
             "Exception in DownloadManager Scope: ${throwable.message}"
        )
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob() + exceptionHandler)

    fun enqueueDownload(downloadRequest: DownloadRequest) {
        // Persist the download request to the database
        scope.launch {
            downloadDao.insertDownload(downloadRequest.toEntity())
        }

        // Start the download task
        startDownloadTask(downloadRequest)
    }

    private fun startDownloadTask(downloadRequest: DownloadRequest) {
        val downloadTask = DownloadTask(downloadRequest, httpClient)

        // Execute the task (download)


        // Update the download status to "DOWNLOADING"
        CoroutineScope(Dispatchers.IO).launch {
            val entity = downloadRequest.toEntity().copy(status = "DOWNLOADING")
            downloadDao.updateDownload(entity)
        }
    }

    //Need to Create a Separate Download Model which will interact with a client
    //and also convert to Entity and vice versa


}