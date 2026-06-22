package com.otori.zjcstools

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast

private const val APK_MIME_TYPE = "application/vnd.android.package-archive"

enum class AppUpdateInstallResult {
    Started,
    NeedPermission,
    Failed
}

data class AppUpdateDownloadState(
    val text: String,
    val progress: Float?,
    val isDownloading: Boolean,
    val isComplete: Boolean,
    val isFailed: Boolean
)

fun enqueueAppUpdateDownload(context: Context, updateInfo: AppUpdateInfo): Long {
    val fileName = "zjcsTools-v${updateInfo.versionName.ifBlank { updateInfo.versionCode }}-release.apk"
    val request = DownloadManager.Request(Uri.parse(updateInfo.apkUrl)).apply {
        setTitle("杖剑工具 ${updateInfo.versionName.ifBlank { "新版本" }}")
        setDescription("正在下载更新安装包")
        setMimeType(APK_MIME_TYPE)
        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
        setAllowedOverMetered(true)
        setAllowedOverRoaming(false)
    }

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    return downloadManager.enqueue(request)
}

fun queryAppUpdateDownloadStatus(context: Context, downloadId: Long): String {
    return queryAppUpdateDownloadState(context, downloadId).text
}

fun queryAppUpdateDownloadState(context: Context, downloadId: Long): AppUpdateDownloadState {
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val query = DownloadManager.Query().setFilterById(downloadId)

    downloadManager.query(query)?.use { cursor ->
        if (!cursor.moveToFirst()) {
            return AppUpdateDownloadState(
                text = "等待下载开始",
                progress = 0f,
                isDownloading = true,
                isComplete = false,
                isFailed = false
            )
        }

        val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
        val downloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
        val totalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
        val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
        val status = cursor.getInt(statusIndex)

        return when (status) {
            DownloadManager.STATUS_PENDING -> AppUpdateDownloadState(
                text = "等待下载开始",
                progress = 0f,
                isDownloading = true,
                isComplete = false,
                isFailed = false
            )

            DownloadManager.STATUS_PAUSED -> AppUpdateDownloadState(
                text = "下载已暂停",
                progress = null,
                isDownloading = false,
                isComplete = false,
                isFailed = false
            )

            DownloadManager.STATUS_RUNNING -> {
                val downloaded = cursor.getLong(downloadedIndex)
                val total = cursor.getLong(totalIndex)
                if (total > 0L) {
                    val progress = (downloaded.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                    AppUpdateDownloadState(
                        text = "正在下载 ${(progress * 100).toInt()}%",
                        progress = progress,
                        isDownloading = true,
                        isComplete = false,
                        isFailed = false
                    )
                } else {
                    AppUpdateDownloadState(
                        text = "正在下载",
                        progress = null,
                        isDownloading = true,
                        isComplete = false,
                        isFailed = false
                    )
                }
            }

            DownloadManager.STATUS_SUCCESSFUL -> AppUpdateDownloadState(
                text = "下载完成，点击安装",
                progress = 1f,
                isDownloading = false,
                isComplete = true,
                isFailed = false
            )

            DownloadManager.STATUS_FAILED -> AppUpdateDownloadState(
                text = "下载失败，错误码 ${cursor.getInt(reasonIndex)}",
                progress = null,
                isDownloading = false,
                isComplete = false,
                isFailed = true
            )

            else -> AppUpdateDownloadState(
                text = "正在准备下载",
                progress = 0f,
                isDownloading = true,
                isComplete = false,
                isFailed = false
            )
        }
    }

    return AppUpdateDownloadState(
        text = "等待下载开始",
        progress = 0f,
        isDownloading = true,
        isComplete = false,
        isFailed = false
    )
}

fun canRequestAppPackageInstalls(context: Context): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            context.packageManager.canRequestPackageInstalls()
}

fun installDownloadedAppUpdate(context: Context, downloadId: Long): AppUpdateInstallResult {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
        !context.packageManager.canRequestPackageInstalls()
    ) {
        val settingsIntent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(settingsIntent)
        Toast.makeText(context, "请允许安装未知应用后返回继续安装", Toast.LENGTH_LONG).show()
        return AppUpdateInstallResult.NeedPermission
    }

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val apkUri = downloadManager.getUriForDownloadedFile(downloadId)

    if (apkUri == null) {
        Toast.makeText(context, "未找到下载完成的安装包", Toast.LENGTH_SHORT).show()
        return AppUpdateInstallResult.Failed
    }

    val installIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(apkUri, APK_MIME_TYPE)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    runCatching {
        context.startActivity(installIntent)
        return AppUpdateInstallResult.Started
    }.onFailure {
        Toast.makeText(context, "无法打开安装界面", Toast.LENGTH_SHORT).show()
    }

    return AppUpdateInstallResult.Failed
}
