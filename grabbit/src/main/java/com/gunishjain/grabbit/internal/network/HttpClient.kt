package com.gunishjain.grabbit.internal.network

import java.io.File

interface HttpClient {

    suspend fun connect(
        url: String,
        file: File,
        startByte: Long = 0,
        timeout: Int = 30000,
        onHeadersReceived: (Map<String, String>) -> Boolean = { true },
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit = { _, _ -> }
    )

    suspend fun getFileSize(url: String): Long

}