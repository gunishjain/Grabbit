package com.gunishjain.grabbit.internal.download

import com.gunishjain.grabbit.internal.network.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

class DownloadTask(private val request: DownloadRequest,private val httpClient: HttpClient) {


    private var isPaused = false
    private var file = File(request.dirPath, request.fileName)
    private var downloadedBytes = 0L

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
                    downloadedBytes = file.length()
                }

                if (request.totalBytes <= 0) {
                    request.totalBytes = httpClient.getFileSize(request.url)
                }

                while (!isPaused) {
                    try {
                        httpClient.connect(
                            url = request.url,
                            file = file,
                            startByte = downloadedBytes,
                            timeout = request.connectTimeout
                        ) { currentBytes, totalBytes ->
                            downloadedBytes = currentBytes

                            // Calculate and report progress
                            val progress = if (totalBytes > 0) {
                                ((currentBytes.toFloat() / totalBytes) * 100).toInt()
                            } else {
                                -1
                            }
                            onProgress(progress)
                        }

                        // If we reach here, download is complete
                        break

                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        if (isPaused) {
                            onPause()
                            continue
                        }
                        throw e
                    }
                }

                onCompleted()

            } catch (e: CancellationException){
                throw e
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error Occurred")
            }

        }

    }

    fun pauseDownload() {
        isPaused = true
    }

    fun resumeDownload() {
        isPaused = false
    }

}