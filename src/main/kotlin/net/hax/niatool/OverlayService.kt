package net.hax.niatool

import android.app.Service
import android.content.Intent
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import net.hax.niatool.overlay.OverlayViewManager

class OverlayService : Service() {

    companion object {
        val MESSAGE_ARMED_STATE_CHANGED = 0
        val MESSAGE_SET_MEDIA_PROJECTION = 1

        val handler = ServiceHandler()

        private val TAG = "OverlayService"
        private var instance: OverlayService? = null

        class ServiceHandler : Handler() {
            override fun handleMessage(msg: Message?) {
                if (instance == null) {
                    Log.w(TAG, "Message $msg with code ${msg?.what} was dropped because" +
                            " ${OverlayService::class.java.simpleName} is not running")
                    return
                }
                when (msg?.what) {
                    MESSAGE_ARMED_STATE_CHANGED ->
                        if (msg.obj as Boolean)
                            instance!!.initializeMediaProjection()
                        else
                            instance!!.stopMediaProjection()
                    MESSAGE_SET_MEDIA_PROJECTION ->
                        instance!!.onMediaProjectionAvailable(msg.obj as MediaProjection?)
                }
            }
        }
    }

    private var overlayManager: OverlayViewManager? = null
    private var mediaProjection: MediaProjection? = null

    override fun onBind(intent: Intent?): IBinder {
        throw UnsupportedOperationException("This service does not support binding")
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        overlayManager = OverlayViewManager(baseContext)
        overlayManager?.startOverlay()
    }

    override fun onDestroy() {
        instance = null
        stopMediaProjection()
        overlayManager?.stopOverlay()
        overlayManager = null
        super.onDestroy()
    }

    private fun initializeMediaProjection() {
        OverlayViewManager.launchActivityFromOverlay(this, CaptureRequestActivity::class.java)
    }

    private fun stopMediaProjection() {
        if (mediaProjection == null) {
            // This will surely be triggered by onProjectionStop() toggling 'armed' and re-sending ARMED_STATE_CHANGED
            Log.w(TAG, "Flow warning: you have tried to stop MediaProjection without starting it first")
            return
        }

        mediaProjection!!.stop()
        mediaProjection = null
    }

    private fun onMediaProjectionAvailable(mediaProjection: MediaProjection?) {
        if (mediaProjection == null) {
            Log.d(TAG, "The user did not allow initialization of the MediaProjection API")
        } else {
            Log.d(TAG, "Permission granted, initializing MediaProjection")
            this.mediaProjection = mediaProjection
        }
    }

}
