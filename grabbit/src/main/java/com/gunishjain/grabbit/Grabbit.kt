package com.gunishjain.grabbit

import com.gunishjain.grabbit.internal.download.DownloadRequest

class Grabbit private constructor(private val config: DownloadConfig){

    companion object {
        fun create(config: DownloadConfig = DownloadConfig()) : Grabbit {
            return Grabbit(config)
        }
    }

    fun newRequest(url: String,dirPath: String,fileName: String) : DownloadRequest.Builder {
        return DownloadRequest.Builder(url,dirPath,fileName)
            .readTimeOut(config.readTimeoutInMs)
            .connectTimeOut(config.connectTimeoutInMs)

    }

    //Need to create Req Queue and add requests to it with callbacks

    fun enqueue(
        request: DownloadRequest,
        onStart: () -> Unit = {},
        onProgress: (value: Int) -> Unit = {_ ->},
        onPause: () -> Unit= {},
        onCompleted: () -> Unit = {},
        onError: (error: String) -> Unit = {_->}
    ) {
        request.onStart = onStart
        request.onProgress = onProgress
        request.onPause = onPause
        request.onCompleted = onCompleted
        request.onError = onError

        //Add request to queue

    }
    fun pause(id: Int){

    }

    fun resume(id: Int){

    }

    fun cancel(id: Int){

    }

    fun cancel(tag: String){

    }

    fun cancelAll(){

    }


}