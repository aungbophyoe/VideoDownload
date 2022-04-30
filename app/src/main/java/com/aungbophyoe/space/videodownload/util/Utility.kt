package com.aungbophyoe.space.videodownload.util

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Environment
import pub.devrel.easypermissions.EasyPermissions

object Utility {
    fun hasStoragePermission(context: Context) =
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            Environment.isExternalStorageManager()
        }else{
            EasyPermissions.hasPermissions(context,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

    fun getCurrentTimeInMillis() = "${System.currentTimeMillis()}"
}