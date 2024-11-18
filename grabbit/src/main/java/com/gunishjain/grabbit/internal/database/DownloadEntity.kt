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
    val status: DownloadStatus = DownloadStatus.PENDING,
    val tag: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
