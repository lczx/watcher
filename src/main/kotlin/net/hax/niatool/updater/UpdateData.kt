package net.hax.niatool.updater

import android.content.SharedPreferences

data class UpdateData(val version: Version, val downloadURL: String) {

    companion object {
        private val PREF_UPDATE_AVAIL_VERSION = "available_update_version"
        private val PREF_UPDATE_AVAIL_DOWNLOAD_URL = "available_update_download_url"

        fun load(prefs: SharedPreferences): UpdateData? {
            val versionString = prefs.getString(PREF_UPDATE_AVAIL_VERSION, null) ?: return null
            val downloadURL = prefs.getString(PREF_UPDATE_AVAIL_DOWNLOAD_URL, null) ?: return null
            return try {
                UpdateData(Version.fromString(versionString), downloadURL)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    fun persist(sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        editor.putString(PREF_UPDATE_AVAIL_VERSION, version.canonical)
        editor.putString(PREF_UPDATE_AVAIL_DOWNLOAD_URL, downloadURL)
        editor.apply()
    }

}
