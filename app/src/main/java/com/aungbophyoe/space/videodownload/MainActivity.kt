package com.aungbophyoe.space.videodownload

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission()
    }

    private fun checkPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            if(Environment.isExternalStorageManager()){
                Log.d("main", "ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION Access.")
            }else{
                Toast.makeText(this,"Storage Permission Denied.",Toast.LENGTH_SHORT).show()
                val i = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:"+this.packageName))
                i.addCategory(Intent.CATEGORY_DEFAULT)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(i)
            }
        }else{
            val dialogMultiplePermissionsListener: MultiplePermissionsListener =
                DialogOnAnyDeniedMultiplePermissionsListener.Builder
                    .withContext(this)
                    .withTitle("Read & Write External Storage Permission")
                    .withMessage("Read and write external storage permission are needed to save download file.")
                    .withButtonText("Ok")
                    .withIcon(R.mipmap.ic_launcher)
                    .build()

            Dexter.withContext(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(dialogMultiplePermissionsListener)
                .onSameThread()
                .check()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}