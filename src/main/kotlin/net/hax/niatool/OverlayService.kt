package net.hax.niatool

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import net.hax.niatool.overlay.OverlayViewManager

class OverlayService : Service() {

    companion object {
        val MESSAGE_ARMED_STATE_CHANGED = 0

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
                }
            }
        }
    }

    private var overlayManager: OverlayViewManager? = null

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
        stopMediaProjection() // TODO: Only if media projection is active
        overlayManager?.stopOverlay()
        overlayManager = null
        super.onDestroy()
    }

    private fun initializeMediaProjection() {
        Log.i(TAG, "initializeMediaProjection()")
    }

    private fun stopMediaProjection() {
        Log.i(TAG, "stopMediaProjection()")
    }

}
