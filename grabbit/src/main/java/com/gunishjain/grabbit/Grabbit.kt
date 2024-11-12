package com.gunishjain.grabbit

import com.gunishjain.grabbit.internal.download.DownloadDispatcher
import com.gunishjain.grabbit.internal.download.DownloadRequest
import com.gunishjain.grabbit.internal.download.DownloadRequestQueue

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

    private val requestQueue = DownloadRequestQueue(DownloadDispatcher(config.httpClient))

    fun enqueue(
        request: DownloadRequest,
        onStart: () -> Unit = {},
        onProgress: (value: Int) -> Unit = {_ ->},
        onPause: () -> Unit= {},
        onCompleted: () -> Unit = {},
        onError: (error: String) -> Unit = {_->}
    ) : Int {
        request.onStart = onStart
        request.onProgress = onProgress
        request.onPause = onPause
        request.onCompleted = onCompleted
        request.onError = onError

        return requestQueue.enqueue(request)

    }
    fun pause(id: Int){
        requestQueue.pause(id)
    }

    fun resume(id: Int){
        requestQueue.resume(id)
    }

    fun cancel(id: Int){
        requestQueue.cancel(id)
    }

    fun cancel(tag: String){
        requestQueue.cancel(tag)
    }

    fun cancelAll(){
        requestQueue.cancelAll()
    }


}