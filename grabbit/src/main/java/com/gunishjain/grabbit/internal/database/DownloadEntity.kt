package com.gunishjain.grabbit.internal.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gunishjain.grabbit.utils.DownloadStatus

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey
    val downloadId: Int,
    val url: String,
    val fileName: String,
    val dirPath: String,
    val totalBytes: Long,
    val downloadedBytes: Long = 0,
    val status: String = DownloadStatus.DEFAULT.toString(),
    val tag: String? = null,
    var speedInBytePerMs: Float = 0f,
    var eTag: String = "",
    var lastModified: Long = 0,
    var workerID: String = "",

    )
