package com.gunishjain.grabbit.internal.download

import com.gunishjain.grabbit.utils.getUniqueDownloadId
import kotlinx.coroutines.Job


//Using Builder Design pattern
//using internal keyword to restrict access to the class from outside the module
class DownloadRequest private constructor(
    internal val url: String,
    internal val tag: String?,
    internal val dirPath: String,
    internal val downloadId: Int,
    internal val fileName: String,
    internal val readTimeout: Int,
    internal val connectTimeout: Int
){

    internal var totalBytes: Long = 0
    internal var downloadedBytes: Long = 0
    internal lateinit var job: Job                                          //using lateintit since we are not initializing it here
    internal lateinit var onStart: () -> Unit
    internal lateinit var onProgress: (value: Int) -> Unit
    internal lateinit var onPause: () -> Unit
    internal lateinit var onCompleted: () -> Unit
    internal lateinit var onError: (error: String) -> Unit

    data class Builder(
        private val url: String,
        private val dirPath: String,
        private val fileName: String
    ) {

        private var tag: String? = null
        private var readTimeOut: Int = 0
        private var connectTimeOut: Int = 0

        fun tag(tag: String) = apply {
            this.tag = tag
            return this
        }

        fun readTimeOut(timeout: Int) = apply {
            this.readTimeOut = timeout
            return this
        }

        fun connectTimeOut(timeout: Int) = apply {
            this.connectTimeOut = timeout
            return this
        }


        fun build() : DownloadRequest {
            return DownloadRequest(
                url = url,
                tag = tag,
                dirPath = dirPath,
                fileName = fileName,
                downloadId = getUniqueDownloadId(),
                readTimeout = readTimeOut,
                connectTimeout = connectTimeOut
            )
        }
    }

}