package com.gunishjain.grabbit

import com.gunishjain.grabbit.internal.network.DefaultHttpClient
import com.gunishjain.grabbit.internal.network.HttpClient

data class DownloadConfig(
    val httpClient: HttpClient = DefaultHttpClient(),
    val connectTimeoutInMs: Int = Constants.DEFAULT_CONNECT_TIMEOUT_MILLS,
    val readTimeoutInMs: Int = Constants.DEFAULT_READ_TIMEOUT_MILLS,
)
