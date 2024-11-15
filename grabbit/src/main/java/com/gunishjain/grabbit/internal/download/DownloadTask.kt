package com.gunishjain.grabbit.internal.download

import android.util.Log
import com.gunishjain.grabbit.internal.network.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.cancellation.CancellationException

class DownloadTask(private val request: DownloadRequest,private val httpClient: HttpClient) {


    private var isPaused = AtomicBoolean(false)
    private var isCompleted = false
    private var file = File(request.dirPath, request.fileName)
    private var downloadedBytes = AtomicLong(0L)  // Use AtomicLong for thread safety
    private val pauseLock = Object()
    private var eTag: String? = null  // Store ETag for consistency checking
    private var lastModified: String? = null

    suspend fun run(
        onStart: () -> Unit = {},
        onProgress: (value: Int) -> Unit = { _ -> },
        onPause: () -> Unit = {},
        onCompleted: () -> Unit = {},
        onError: (error: String) -> Unit = { _ -> }
    ) {
        withContext(Dispatchers.IO) {

            try {

                onStart()

                // Get initial file size if resuming
                if (file.exists()) {
                    downloadedBytes.set(file.length())
                }

                if (request.totalBytes <= 0) {
                    request.totalBytes = httpClient.getFileSize(request.url)
                    Log.d("Download Task", "Total bytes: ${request.totalBytes}")
                }

                while (!isCompleted) {
                    if (isPaused.get()) {
                        Log.d("DownloadTask", "Download paused at byte: ${downloadedBytes.get()}")
                        onPause()
                        waitForResume()
                        Log.d("DownloadTask", "Download resumed from byte: ${downloadedBytes.get()}")
                        continue
                    }

                    try {
                        Log.d("DownloadTask", "Connecting to download from byte: ${downloadedBytes.get()}")
                        httpClient.connect(
                            url = request.url,
                            file = file,
                            startByte = downloadedBytes.get(),
                            timeout = request.connectTimeout,
                            onHeadersReceived = { headers ->
                                eTag = headers["ETag"]
                                lastModified = headers["Last-Modified"]
                                true
                            }
                        ) { currentBytes, totalBytes ->
                            downloadedBytes.set(currentBytes)

                            // Calculate and report progress
                            val progress = if (totalBytes > 0) {
                                ((currentBytes.toFloat() / totalBytes) * 100).toInt()
                            } else {
                                -1
                            }
                            onProgress(progress)

                            // Check pause status during download
                            if (isPaused.get()) {
                                throw PauseException()
                            }
                        }

                        isCompleted = true
                        onCompleted()
                        Log.d("DownloadTask", "Download completed successfully")
                        break

                    } catch (e: PauseException) {
                        // Handle pause specifically
                        continue
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        if (isPaused.get()) {
                            onPause()
                            waitForResume()
                            continue
                        }
                        throw e
                    }
                }

            } catch (e: CancellationException){
                throw e
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error Occurred")
            }

        }

    }

    fun pauseDownload() {
        Log.d("DownloadTask", "pauseDownload() called. Current bytes: ${downloadedBytes.get()}")
        isPaused.set(true)
    }

    fun resumeDownload() {
        Log.d("DownloadTask", "resumeDownload() called.")
        isPaused.set(false)
        synchronized(pauseLock) {
            pauseLock.notify()
        }
    }

    private suspend fun waitForResume() {
        withContext(Dispatchers.IO) {
        while (isPaused.get()) {
            Log.d("DownloadTask", "Waiting for resume.")
            delay(100)  // Use delay in coroutines to suspend efficiently without blocking the thread
        }
        }
    }

    fun cancelDownload() {
        Log.d("DownloadTask", "cancelDownload() called at byte: ${downloadedBytes.get()}")
        isCompleted = true
    }

    private class PauseException : Exception()


}