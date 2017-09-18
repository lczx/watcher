package net.hax.niatool

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import net.hax.niatool.overlay.OverlayViewManager

class OverlayService : Service() {

    companion object {
        private val TAG = "OverlayService"
    }

    private var overlayManager: OverlayViewManager? = null

    override fun onBind(intent: Intent?): IBinder {
        throw UnsupportedOperationException("This service does not support binding")
    }

    override fun onCreate() {
        super.onCreate()
        overlayManager = OverlayViewManager(baseContext)
        overlayManager?.startOverlay()
        Log.i(TAG, "Service created...")
    }

    override fun onDestroy() {
        overlayManager?.stopOverlay()
        overlayManager = null
        super.onDestroy()
        Log.i(TAG, "...service destroyed")
    }

}
