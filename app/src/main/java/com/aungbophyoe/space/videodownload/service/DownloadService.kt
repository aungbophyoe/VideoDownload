package com.aungbophyoe.space.videodownload.service

import android.content.Intent
import androidx.lifecycle.LifecycleService

class DownloadService : LifecycleService() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }
}