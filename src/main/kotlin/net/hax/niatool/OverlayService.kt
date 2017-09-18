package net.hax.niatool

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class OverlayService : Service() {

    companion object {
        private val TAG = "OverlayService"
    }

    override fun onBind(intent: Intent?): IBinder {
        throw UnsupportedOperationException("This service does not support binding")
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Service created...")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "...service destroyed")
    }

}