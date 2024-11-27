package com.gunishjain.grabbit

import com.gunishjain.grabbit.utils.DownloadStatus

data class DownloadModel(
    val url: String,
    val path: String,
    val fileName: String,
    val tag: String?,
    val downloadId: Int,
    var status: DownloadStatus,
    val total: Long,
    val progress: Int,
    val eTag: String,
    val speedInBytePerMs: Float,



    )
