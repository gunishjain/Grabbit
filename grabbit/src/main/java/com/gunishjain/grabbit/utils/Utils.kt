package com.gunishjain.grabbit.utils

import com.gunishjain.grabbit.internal.download.DownloadRequest
import com.gunishjain.grabbit.internal.notifications.NotificationConfig
import kotlinx.serialization.encodeToString
import java.io.File
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.experimental.and
import kotlinx.serialization.json.Json

fun getUniqueDownloadId(url: String, dirPath: String, fileName: String): Int {

    val string = url + File.separator + dirPath + File.separator + fileName

    val hash: ByteArray = try {
        MessageDigest.getInstance("MD5").digest(string.toByteArray(charset("UTF-8")))
    } catch (e: NoSuchAlgorithmException) {
        throw RuntimeException("NoSuchAlgorithmException", e)
    } catch (e: UnsupportedEncodingException) {
        throw RuntimeException("UnsupportedEncodingException", e)
    }

    val hex = StringBuilder(hash.size * 2)

    for (b in hash) {
        if (b and 0xFF.toByte() < 0x10) hex.append("0")
        hex.append(Integer.toHexString((b and 0xFF.toByte()).toInt()))
    }

    return hex.toString().hashCode()

}

enum class DownloadStatus {
    QUEUED, STARTED, DOWNLOADING, PAUSED, COMPLETED, FAILED, DEFAULT
}

fun DownloadRequest.toJson(): String {
    return Json.encodeToString(this)
}

fun NotificationConfig.toJson(): String {
    return Json.encodeToString(this)
}