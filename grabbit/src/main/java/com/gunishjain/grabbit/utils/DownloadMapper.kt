package com.gunishjain.grabbit.utils

import com.gunishjain.grabbit.DownloadModel
import com.gunishjain.grabbit.internal.database.DownloadEntity

internal fun DownloadEntity.toDownloadModel() =
    DownloadModel(
        url = url,
        path = dirPath,
        fileName = fileName,
        tag = tag,
        downloadId = downloadId,
        status = DownloadStatus.entries.find { it.name == status } ?: DownloadStatus.DEFAULT,
        total = totalBytes,
        progress = if (totalBytes.toInt() != 0) ((downloadedBytes * 100) / totalBytes).toInt() else 0,
        speedInBytePerMs = speedInBytePerMs,
        eTag = eTag,

    )