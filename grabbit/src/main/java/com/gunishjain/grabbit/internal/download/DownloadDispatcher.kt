package com.gunishjain.grabbit.internal.download

import android.util.Log
import com.gunishjain.grabbit.internal.network.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException

class DownloadDispatcher(private val httpClient: HttpClient) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val downloadTasks = ConcurrentHashMap<Int, DownloadTask>()
    private val activeJobs = ConcurrentHashMap<Int, Job>()

    private fun executeOnMain(block: () -> Unit){
        scope.launch {
            block()
        }
    }

    fun enqueue(req: DownloadRequest) : Int {

        val downloadTask = DownloadTask(req,httpClient)
        downloadTasks[req.downloadId] = downloadTask

        val job = scope.launch {
            execute(req,downloadTask)
        }
        activeJobs[req.downloadId] = job
        req.job = job
        return req.downloadId
    }

    private suspend fun execute(request: DownloadRequest,task: DownloadTask) {
        try {
            task.run(
                onStart = {
                    executeOnMain { request.onStart() }
                },
                onProgress = {
                    executeOnMain { request.onProgress(it) }
                },
                onPause = {
                    executeOnMain { request.onPause() }
                },
                onCompleted = {
                    executeOnMain {
                        request.onCompleted()
                    }
                    cleanup(request.downloadId)

                },
                onError = {
                    executeOnMain { request.onError(it) }
                    cleanup(request.downloadId)
                }
            )
        } catch (e: CancellationException){
            cleanup(request.downloadId)
            throw e
        }

    }

    fun pause(downloadId: Int) {
        Log.d("DownloadDispatcher", "Attempting to pause download: $downloadId")
        downloadTasks[downloadId]?.let { task ->
            task.pauseDownload()
        } ?: Log.e("DownloadDispatcher", "No download task found for id: $downloadId")
    }

    fun resume(downloadId: Int) {
        Log.d("DownloadDispatcher", "Attempting to resume download: $downloadId")
        downloadTasks[downloadId]?.let { task ->
            task.resumeDownload()
        } ?: Log.e("DownloadDispatcher", "No download task found for id: $downloadId")
    }

    fun cancel(req: DownloadRequest) {
        Log.d("DownloadDispatcher", "Attempting to cancel download: ${req.downloadId}")
        downloadTasks[req.downloadId]?.let { task ->
            task.cancelDownload()
            cleanup(req.downloadId)
        }
        activeJobs[req.downloadId]?.cancel()
    }

    fun cancelAll() {
        Log.d("DownloadDispatcher", "Cancelling all downloads")
        downloadTasks.keys.forEach { downloadId ->
            downloadTasks[downloadId]?.cancelDownload()
        }
        downloadTasks.clear()
        activeJobs.values.forEach { it.cancel() }
        activeJobs.clear()
        scope.cancel()
    }

    private fun cleanup(downloadId: Int) {
        downloadTasks.remove(downloadId)
        activeJobs.remove(downloadId)
    }


}