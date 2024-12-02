package com.gunishjain.grabbit.internal.download

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.gunishjain.grabbit.Constants
import com.gunishjain.grabbit.DownloadConfig
import com.gunishjain.grabbit.internal.notifications.NotificationConfig
import com.gunishjain.grabbit.internal.database.DownloadDao
import com.gunishjain.grabbit.internal.database.DownloadEntity
import com.gunishjain.grabbit.internal.network.HttpClient
import com.gunishjain.grabbit.internal.worker.DownloadWorker
import com.gunishjain.grabbit.utils.DownloadStatus
import com.gunishjain.grabbit.utils.toJson
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

            //checking if already exist in DB- implement later

            //create download worker request map it to workerId of Entity
            val inputDataBuilder = Data.Builder()
                .putString(Constants.DOWNLOAD_REQUEST_KEY, downloadRequest.toJson())
                .putString(Constants.NOTIFICATION_CONFIG_KEY, notificationConfig.toJson())

            val inputData = inputDataBuilder.build()

            val constraints = Constraints
                .Builder()
                .build()

            val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(inputData)
                .addTag(Constants.DOWNLOAD_TAG)
                .setConstraints(constraints)
                .build()

            downloadDao.insertDownload(
                DownloadEntity(
                    downloadId = downloadRequest.downloadId,
                    url = downloadRequest.url,
                    fileName = downloadRequest.fileName,
                    dirPath = downloadRequest.dirPath,
                    totalBytes = downloadRequest.totalBytes,
                    downloadedBytes = downloadRequest.downloadedBytes,
                    status = DownloadStatus.QUEUED.toString(),
                    tag = downloadRequest.tag,
                    lastModified = System.currentTimeMillis(),
                    workerID = downloadWorkRequest.id.toString()

                )

            )

            //ENQUEUE UNIQUE WORK REQUEST
            workManager.enqueueUniqueWork(
                downloadRequest.downloadId.toString(),
                ExistingWorkPolicy.KEEP,
                downloadWorkRequest
            )

        }

        // Start the download task
//        startDownloadTask(downloadRequest)
    }


}