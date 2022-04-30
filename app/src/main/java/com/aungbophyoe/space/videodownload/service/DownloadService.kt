package com.aungbophyoe.space.videodownload.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.aungbophyoe.space.videodownload.util.Constants
import com.aungbophyoe.space.videodownload.util.Constants.START_DOWNLOAD
import com.aungbophyoe.space.videodownload.util.DownloadEvent
import com.aungbophyoe.space.videodownload.util.Utility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.net.URL
import javax.inject.Inject

@AndroidEntryPoint
class DownloadService : LifecycleService() {
    @Inject
    lateinit var notificationManager: NotificationManager
    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder
    private val DOWNLOAD_NOTI_EVERY_PERCENT = 5
    private val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    private var downloadLink : String? = null

    companion object{
        val downloadEvent = MutableLiveData<DownloadEvent>()
        val downloadProgress = MutableLiveData<Int>()
    }

    private fun initValue(){
        downloadEvent.value = DownloadEvent.Downloaded
        downloadProgress.value = 0
    }

    override fun onCreate() {
        initValue()
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            downloadLink = it?.getStringExtra("link")
            when(it?.action){
                START_DOWNLOAD -> startForegroundService()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService(){
        Log.d("Service","start service")
        downloadEvent.value =DownloadEvent.Downloading
        startDownload()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel()
        }
        startForeground(Constants.NOTIFICATION_ID,notificationBuilder.build())
        downloadProgress.observe(this) {
            if(it == 100){
                notificationBuilder.setOnlyAlertOnce(false)
                notificationBuilder.setAutoCancel(true)
                notificationBuilder.setContentTitle("Download Complete.")
                notificationBuilder.setContentText("")
                notificationBuilder.setProgress(0,0,false)
                notificationManager.notify(Constants.NOTIFICATION_ID, notificationBuilder.build())
                downloadEvent.value = DownloadEvent.Downloaded
                notificationManager.cancel(Constants.NOTIFICATION_ID)
            }else{
                notificationBuilder.setContentTitle("Downloading")
                notificationBuilder.setContentText("$it %")
                notificationBuilder.setProgress(100,it,false)
                notificationManager.notify(Constants.NOTIFICATION_ID, notificationBuilder.build())
            }
        }
    }

    private fun startDownload(){
        try {
            val dd: File = File(path.path)
            if (!dd.exists()) {
                dd.mkdir()
            }
            val filename = downloadLink!!.substring(downloadLink!!.lastIndexOf('/')+1)
            val file = File("$dd/${Utility.getCurrentTimeInMillis()}_$filename")

            CoroutineScope(Dispatchers.IO).launch {
                val url = URL(downloadLink)
                val connection = url.openConnection()
                connection.connect()
                val fileLength = connection.contentLength
                val output: OutputStream = FileOutputStream(file.path)
                val input: InputStream = BufferedInputStream(connection.getInputStream())
                val data = ByteArray(1024)
                var total: Long = 0
                var count: Int
                var prevProgress = 0
                var progress: Int

                while ((input.read(data)).also {dd-> count = dd  } > 0){
                    total += count
                    progress = ((total*100)/fileLength).toInt()
                    output.write(data, 0, count)
                    withContext(Dispatchers.Main){
                        if (progress == 0) {
                            downloadProgress.value = progress
                        }
                        if (progress - prevProgress == DOWNLOAD_NOTI_EVERY_PERCENT) {
                            downloadProgress.value = progress
                            prevProgress = progress
                        }
                    }
                }
                output.flush()
                output.close()
                input.close()
            }
        }catch (e:Exception){
         Log.d("service","${e.message}")
        }
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(){
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            Constants.NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }
}