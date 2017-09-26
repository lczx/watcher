package net.hax.niatool.updater

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.util.Log
import net.hax.niatool.R
import java.io.File

class UpdateInstaller(val context: Context) {

    companion object {
        private val TAG = "UpdateInstaller"
        private val UPDATE_FILE_NAME = "update.apk"
        private val APK_MIME_TYPE = "application/vnd.android.package-archive"
    }

    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    fun downloadAndInstall(update: UpdateData) {
        val destination = File(context.externalCacheDir, UPDATE_FILE_NAME)

        //Delete update binary file if it already exists
        if (destination.exists()) destination.delete()

        // Try to download (note: we can make the request to be handled only on wifi)
        Log.d(TAG, "Starting update download, source: \"${update.downloadURL}")
        val downloadId = downloadManager.enqueue(DownloadManager.Request(Uri.parse(update.downloadURL))
                .setTitle(context.getString(R.string.notification_update_download_title))
                .setDescription(context.getString(R.string.notification_update_download_description, update.version.pretty))
                .setDestinationUri(Uri.fromFile(destination))
                .setVisibleInDownloadsUi(false))

        // Get notified when the download is done
        context.registerReceiver(DownloadCompleteReceiver(downloadId, destination),
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    inner class DownloadCompleteReceiver(
            private val downloadId: Long, private val destination: File) : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent!!.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id != downloadId) return

            val dlProviderDestURI = downloadManager.getUriForDownloadedFile(downloadId)
            Log.d(TAG, "Downloaded update to \"$destination\" [DLM URI: \"$dlProviderDestURI\"]")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Log.d(TAG, "Attempting install using DownloadManager's FileProvider URL (Android 7.0+, Nougat)")
                // In alternative, we can download the file elsewhere and use a FileProvider
                context!!.startActivity(Intent(Intent.ACTION_INSTALL_PACKAGE)
                        .setDataAndType(dlProviderDestURI, APK_MIME_TYPE)
                        .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
            } else {
                Log.d(TAG, "Attempting install with ACTION_VIEW on downloaded package (Pre-Android 7.0)")
                context!!.startActivity(Intent(Intent.ACTION_VIEW)
                        .setDataAndType(Uri.fromFile(destination), APK_MIME_TYPE)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }

            context.unregisterReceiver(this)
            //activity.finish()
        }

    }

}
