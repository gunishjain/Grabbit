package com.gunishjain.grabbit.internal.download

import com.gunishjain.grabbit.internal.network.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DownloadTask(private val request: DownloadRequest,private val httpClient: HttpClient) {


    suspend fun run(
        onStart: () -> Unit = {},
        onProgress: (value: Int) -> Unit = { _ -> },
        onPause: () -> Unit = {},
        onCompleted: () -> Unit = {},
        onError: (error: String) -> Unit = { _ -> }
    ) {
        withContext(Dispatchers.IO) {



            onStart()

            // use of HttpClient
            httpClient.connect()

            TODO("For Pause and Resume need to use Range Header")

            onCompleted()


        }

    }

}