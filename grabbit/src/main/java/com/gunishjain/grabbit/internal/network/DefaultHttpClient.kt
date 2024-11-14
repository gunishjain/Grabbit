package com.gunishjain.grabbit.internal.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Buffer
import okio.IOException
import okio.buffer
import okio.sink
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

class DefaultHttpClient : HttpClient {

    private val okHttpClient : OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30,TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun connect(
        url: String,
        file: File,
        startByte: Long,
        timeout: Int,
        onProgress: (Long, Long) -> Unit
    )  = withContext(Dispatchers.IO){

        try {
            val request = Request.Builder()
                .url(url)
                .apply {
                    if (startByte > 0) {
                        header("Range", "bytes=$startByte-")
                    }
                }
                .build()

           okHttpClient.newCall(request).execute().use { response ->

               if (!response.isSuccessful) {
                   throw IOException("Unexpected response code: ${response.code}")
               }

               // Get content length from header
               val contentLength = response.header("Content-Length")?.toLong() ?: -1L
               val totalBytes = if (contentLength != -1L) contentLength + startByte else -1L


               Log.d("Default HTTPCLIENT", "Total File Size: $totalBytes")

               // Create parent directories if they don't exist
               file.parentFile?.mkdirs()

               // Use response body to write to file
               response.body?.let { body ->

                   Log.d("HTTPCLIENT",body.toString())
                   val bufferedSink = file.sink(append = startByte > 0).buffer()
                   val source = body.source()
                   val buffer = Buffer()
                   var downloadedBytes = startByte

                   while (true) {
                       val read = source.read(buffer, 8192L) // Read chunks of 8KB
                       if (read == -1L) break

                       bufferedSink.write(buffer, read)
                       downloadedBytes += read

                       // Report progress

//                       Log.d("HTTPCLIENT",downloadedBytes.toString())

                       onProgress(downloadedBytes, totalBytes)
                   }

                   bufferedSink.close()
                   source.close()
               } ?: throw IOException("Response body is null")
           }

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("HTTPCLEINT", "Error occurred: ${e.message}", e)
            throw IOException("Download failed: ${e.message}", e)
        }

    }

    override suspend fun getFileSize(url: String): Long = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .head() // Use HEAD request to get only headers
            .build()

        val response = okHttpClient.newCall(request).execute()
        response.header("Content-Length")?.toLong() ?: -1L
    }

}