package com.aungbophyoe.space.videodownload.util

sealed class DownloadEvent{
    object Downloading : DownloadEvent()
    object Downloaded : DownloadEvent()
}
