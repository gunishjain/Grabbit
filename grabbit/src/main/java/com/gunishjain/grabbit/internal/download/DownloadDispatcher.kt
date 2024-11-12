package com.gunishjain.grabbit.internal.download

import com.gunishjain.grabbit.internal.network.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DownloadDispatcher(private val httpClient: HttpClient) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val downloadTasks = mutableMapOf<Int, DownloadTask>()

    private fun executeOnMain(block: () -> Unit){
        scope.launch {
            block()
        }
    }

    fun enqueue(req: DownloadRequest) : Int {

        val downloadTask = DownloadTask(req,httpClient)
        downloadTasks[req.downloadId] = downloadTask

        val job = scope.launch {
            execute(req)
        }
        req.job = job
        return req.downloadId
    }

    private suspend fun execute(request: DownloadRequest) {

        DownloadTask(request,httpClient).run (
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
                downloadTasks.remove(request.downloadId)

            },
            onError = {
                executeOnMain { request.onError(it) }
                downloadTasks.remove(request.downloadId)
            }
        )

    }

    fun pause(downloadId: Int) {
        downloadTasks[downloadId]?.pauseDownload()
    }

    fun resume(downloadId: Int) {
        downloadTasks[downloadId]?.resumeDownload()
    }

    fun cancel(req: DownloadRequest) {
        req.job.cancel()
    }

    fun cancelAll() {
        scope.cancel()
    }


}