package com.aungbophyoe.space.videodownload

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aungbophyoe.space.videodownload.util.Constants.REQUEST_STORAGE_READ_WRITE_PERMISSION
import com.aungbophyoe.space.videodownload.util.Utility
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


class MainActivity : AppCompatActivity(),EasyPermissions.PermissionCallbacks {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermission()
    }

    private fun requestPermission(){
        if(Utility.hasStoragePermission(this)){
            return
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            Toast.makeText(this,"Storage Permission Denied.",Toast.LENGTH_SHORT).show()
            val i = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:"+this.packageName))
            i.addCategory(Intent.CATEGORY_DEFAULT)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(i)
        }else{
            EasyPermissions.requestPermissions(
                this,
                "Read and write external storage permission are needed to save download file.",
                REQUEST_STORAGE_READ_WRITE_PERMISSION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
            AppSettingsDialog.Builder(this).build().show()
        }else{
            requestPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }
}