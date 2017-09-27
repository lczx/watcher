package net.hax.niatool.updater

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL

abstract class ReleaseMetaDownloadTask(val context: Context) : AsyncTask<String, Unit, UpdateData?>() {

    companion object {
        private val TAG = "ReleaseMetaDownloadTask"
    }

    override fun doInBackground(vararg params: String?): UpdateData? {
        val connection = URL(params[0]).openConnection()

        // Remove sensible data (especially phone name) from request to avoid tracking
        val ua = System.getProperty("http.agent")
        connection.setRequestProperty("User-Agent", ua.take(ua.indexOf('(') - 1))

        val data = try {
            connection.getInputStream().use { it.readBytes().toString(Charsets.UTF_8) }
        } catch (e: IOException) {
            Log.e(TAG, "Update check failed, connection error", e)
            return null
        }

        val dataJson = try {
            JSONObject(data)
        } catch (e: JSONException) {
            Log.e(TAG, "Failed to parse release data as JSON, aborting update check", e)
            return null
        }

        val newVersionString = try {
            dataJson.getString("tag_name")
        } catch (e: JSONException) {
            Log.e(TAG, "Failed to get new release version, aborting update check", e)
            return null
        }

        val downloadURL = try {
            dataJson.getJSONArray("assets").getJSONObject(0).getString("browser_download_url")
        } catch (e: JSONException) {
            Log.e(TAG, "Failed to get new release download URL, aborting update check", e)
            return null
        }

        val newVersion = Version.fromStringOrNull(newVersionString) ?: return null

        return UpdateData(newVersion, downloadURL)
    }

    abstract override fun onPostExecute(result: UpdateData?)

}
