package com.gunishjain.sample

import android.app.Application
import com.gunishjain.grabbit.Grabbit

class MyApplication : Application() {

    lateinit var grabbit: Grabbit

    override fun onCreate() {
        super.onCreate()
        grabbit = Grabbit.create()
    }

}