package com.gunishjain.grabbit.internal.download

import android.util.Log
import com.gunishjain.grabbit.internal.network.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.internal.notify
import okhttp3.internal.wait
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume

class DownloadTask(private val request: DownloadRequest,private val httpClient: HttpClient) {


    private var isPaused = AtomicBoolean(false)
    private var isCompleted = false
    private var file = File(request.dirPath, request.fileName)
    private var downloadedBytes = AtomicLong(0L)  // Use AtomicLong for thread safety
    private val pauseLock = Object()

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
                    Log.d("Download Task",request.totalBytes.toString())
                }

                while (!isCompleted) {
                    if (isPaused.get()) {
                        Log.d("DownloadTask", "Download paused.")
                        onPause()
                        waitForResume()
                        Log.d("DownloadTask", "Download resumed.")
                        continue  // Important: restart the loop after resume
                    }

                    try {
                        Log.d("DownloadTask", "Connecting to download from byte: ${downloadedBytes.get()}")
                        httpClient.connect(
                            url = request.url,
                            file = file,
                            startByte = downloadedBytes.get(),
                            timeout = request.connectTimeout
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
                        Log.d("DownloadTask", "Download completed.")
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
        Log.d("DownloadTask", "pauseDownload() called.")
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
        suspendCancellableCoroutine<Unit> { continuation ->
            synchronized(pauseLock) {
                while (isPaused.get()) {
                    try {
                        Log.d("DownloadTask", "Waiting for resume.")
                        pauseLock.wait()
                    } catch (e: InterruptedException) {
                        continuation.resume(Unit)
                        return@synchronized
                    }
                }
                continuation.resume(Unit)
            }
        }
    }

    private class PauseException : Exception()


}