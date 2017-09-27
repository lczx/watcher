package net.hax.niatool.updater

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log

class UpdateManager(val context: Context, var onUpdateListener: ((UpdateManager, UpdateData) -> Unit)? = null) {

    companion object {
        private val TAG = "UpdateManager"

        private val PREF_LAST_UPDATE_CHECK_KEY = "last_update_check_time"
        private val PREF_LAST_UPDATE_CHECK_DEFAULT = 0L

        //private val RELEASE_ENDPOINT_URI = "https://api.github.com/repos/lczx/watcher/releases/latest"
        private val RELEASE_ENDPOINT_URI = "http://prime.lan/latest.json"

        // TODO: We may want let the user configure these
        // For now, since this app will primarily be used on the go, it is pointless to check for updates on WiFi
        private val UPDATE_ONLY_ON_WIFI = false
        private val UPDATE_CHECK_INTERVAL_MS: Long = 3600000 * 24 * 3 // Check for update every 3 days
    }

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val updaterDataStore =
            context.getSharedPreferences(context.packageName + ".updater_preferences", Context.MODE_PRIVATE)

    private var lastUpdateCheckTime: Long
        get() = updaterDataStore.getLong(PREF_LAST_UPDATE_CHECK_KEY, PREF_LAST_UPDATE_CHECK_DEFAULT)
        set(value) = updaterDataStore.edit().putLong(PREF_LAST_UPDATE_CHECK_KEY, value).apply()

    val canDownload: Boolean
        get() = connectivityManager.activeNetworkInfo.let {
            it?.isConnected == true && (!UPDATE_ONLY_ON_WIFI || it.type == ConnectivityManager.TYPE_WIFI)
        }

    fun run(force: Boolean = false) {
        if (force || shouldCheckForUpdate()) {
            Log.d(TAG, "Fetching latest release metadata...")
            UpdaterTask().execute(RELEASE_ENDPOINT_URI)
        } else {
            Log.d(TAG, "Better stay offline and check if we remember about an available update...")
            processCachedUpdate()
        }
    }

    private fun shouldCheckForUpdate(): Boolean {
        if (!canDownload) {
            Log.d(TAG, "Shouldn't check for update: offline or wrong network type")
            return false
        }

        if ((System.currentTimeMillis() - lastUpdateCheckTime) < UPDATE_CHECK_INTERVAL_MS) {
            Log.d(TAG, "Shouldn't check for update: not enough time passed since last check")
            return false
        }

        Log.d(TAG, "It is time to check for updates!")
        return true
    }

    private fun isUpdate(updateData: UpdateData): Boolean {
        // Returns `true` if the given update version is greater than our own. If we have problems interpreting our
        // current version name for comparison, this still returns `true` in the hope that the update will fix that.
        val currentVersionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        val currentVersion = Version.fromStringOrNull(currentVersionName)
        return currentVersion == null || updateData.version > currentVersion
    }

    private fun processCachedUpdate() {
        // If we remember about having an update available (and it it actually an update), show it without going on
        // the internet (probably canDownload is false here, but we can be online and got a local error)
        UpdateData.load(updaterDataStore)?.let {
            if (isUpdate(it)) {
                Log.d(TAG, "I remember about finding an update to version ${it.version}!")
                onUpdateListener?.invoke(this, it)
            }
        }
    }

    private inner class UpdaterTask : ReleaseMetaDownloadTask(context) {
        override fun onPostExecute(result: UpdateData?) {
            if (result == null) {
                Log.w(TAG, "Got a problem on update check, proceeding as if we are offline and use cached")
                processCachedUpdate()
            } else {
                // We found a release (update or not), update the last check time...
                lastUpdateCheckTime = System.currentTimeMillis()
                // ...then we check if we actually have an update and in that case notify and persist it
                if (isUpdate(result)) {
                    Log.i(TAG, "We have an update to ${result.version} available! Hurry up and DL that thing!")
                    result.persist(updaterDataStore)
                    onUpdateListener?.invoke(this@UpdateManager, result)
                }
            }
        }
    }

}
