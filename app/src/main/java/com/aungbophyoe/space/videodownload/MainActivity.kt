package com.aungbophyoe.space.videodownload

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.aungbophyoe.space.videodownload.databinding.ActivityMainBinding
import com.aungbophyoe.space.videodownload.service.DownloadService
import com.aungbophyoe.space.videodownload.util.Constants
import com.aungbophyoe.space.videodownload.util.Constants.REQUEST_STORAGE_READ_WRITE_PERMISSION
import com.aungbophyoe.space.videodownload.util.DownloadEvent
import com.aungbophyoe.space.videodownload.util.Utility
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


@AndroidEntryPoint
class MainActivity : AppCompatActivity(),EasyPermissions.PermissionCallbacks {
    private var binding : ActivityMainBinding? = null
    private val TAG : String = "MainActivity"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        binding!!.apply {
            btnDownload.setOnClickListener {
                if(edtMainActivity.text.isNullOrBlank()){
                    edtMainActivity.error = "Require link!"
                    return@setOnClickListener
                }
                val link = edtMainActivity.text
                sendCommandToService(Constants.START_DOWNLOAD,"$link")
            }
        }
        observedDownloadData()
        requestPermission()
    }

    private fun observedDownloadData(){
        try {
            binding!!.apply {
                DownloadService.downloadEvent.observe(this@MainActivity){
                    it?.let { downloadEvent ->
                        when(downloadEvent){
                            is DownloadEvent.Downloading -> {
                                btnDownload.text = "Downloading"
                                progressBar.visibility = View.VISIBLE
                                btnDownload.isEnabled = false
                                edtMainActivity.isEnabled = false
                            }
                            is DownloadEvent.Downloaded -> {
                                btnDownload.text = "Downloaded"
                                btnDownload.isEnabled = true
                                edtMainActivity.isEnabled = true
                                progressBar.visibility = View.INVISIBLE
                            }
                        }
                    }
                }

                DownloadService.downloadProgress.observe(this@MainActivity){
                    it?.let { value ->
                        progressBar.progress = value
                        if(value == 100){
                            btnDownload.text = "Downloaded"
                            btnDownload.isEnabled = true
                            edtMainActivity.isEnabled = true
                            progressBar.progress = 100
                            progressBar.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        }catch (e:Exception){
            Log.d(TAG,"${e.message} ")
        }
    }

    private fun sendCommandToService(action:String,link:String){
        startService(
            Intent(this,DownloadService::class.java).apply {
                this.action = action
                this.putExtra("link","$link")
            }
        )
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
        binding = null
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