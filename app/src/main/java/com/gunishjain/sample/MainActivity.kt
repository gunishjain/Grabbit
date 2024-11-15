package com.gunishjain.sample

import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DownloadUI()
        }
    }
}

@Composable
fun DownloadUI() {
    val context = LocalContext.current
    val grabbit = (context.applicationContext as MyApplication).grabbit

    var downloadProgress by remember { mutableIntStateOf(0) }
    var isDownloading by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var downloadId by remember { mutableIntStateOf(-1) }
    var downloadComplete by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    val downloadsDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

    // Start download
    fun startDownload(url: String, dirPath: File?, fileName: String) {
        if (dirPath != null && dirPath.exists()) {
            val request = grabbit.newRequest(url, dirPath.absolutePath, fileName).build()

        downloadId = grabbit.enqueue(
            request,
            onStart = {
                isDownloading = true
                isPaused = false
            },
            onProgress = { progress ->
                downloadProgress = progress
            },
            onPause = {
                isPaused = true
            },
            onCompleted = {
                isDownloading = false
                if (File(downloadsDirectory, "lmao.pdf").exists()) {
                    Log.d("Compose", "File found after download")
                } else {
                    Log.d("Compose", "File  Not found after download",)
                }
                downloadComplete = true

            },
            onError = { error ->
                // Handle error (optional)
            }
        )
    } else {
            // Handle invalid directory (optional)
        }
    }

    // Pause download
    fun pauseDownload() {
        grabbit.pause(downloadId)
    }

    // Resume download
    fun resumeDownload() {
        grabbit.resume(downloadId)
    }

    // Cancel download
    fun cancelDownload() {
        grabbit.cancel(downloadId)
        isDownloading = false
        downloadProgress = 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Progress bar
        Text("Download Progress: $downloadProgress%")
        LinearProgressIndicator(
            progress = downloadProgress / 100f,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        status = if(downloadComplete) {
            "Download Completed"
        } else if(isDownloading) {
            "Downloading"
        } else {
            "Idle"
        }

        Text("Status : $status" )

        Spacer(modifier = Modifier.height(32.dp))

        // Buttons for control
        if (isDownloading) {
            if (isPaused) {
                Button(onClick = { resumeDownload() }) {
                    Text("Resume")
                }
            } else {
                Button(onClick = { pauseDownload() }) {
                    Text("Pause")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { cancelDownload() }) {
                Text("Cancel")
            }
        } else {
            Button(onClick = {
                startDownload(
                    url = "https://www.learningcontainer.com/download/sample-50-mb-pdf-file/?wpdmdl=3675&refresh=6721f942bd70b1730279746",
                    dirPath = downloadsDirectory,
                    fileName = "test.pdf"
                )
            }) {
                Text("Download")
            }
        }
    }
}

