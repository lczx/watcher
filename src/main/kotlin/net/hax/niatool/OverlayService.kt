package net.hax.niatool

import android.app.Activity
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import net.hax.niatool.modes.ModeRegistry
import net.hax.niatool.modes.OperationMode
import net.hax.niatool.overlay.OverlayViewManager
import net.hax.niatool.task.ScreenCaptureTask

// TODO: We may want to change this service to another type that may run on a different thread,
// TODO:   so we must ensure that calls to UI components are safely handled on the UI thread.

class OverlayService : Service() {

    companion object {
        private const val TAG = "OverlayService"

        const val MESSAGE_ARMED_STATE_CHANGED = 0
        const val MESSAGE_SET_MEDIA_PROJECTION_INTENT = 1
        const val MESSAGE_CAPTURE_SCREEN = 2

        const val ACTION_START = "start"
        const val ACTION_STOP = "stop"
        const val EXTRA_MODE_ID = "mode_id"

        private var instance: OverlayService? = null
        val handler = MessageHandler()
    }

    private var currentMode: OperationMode? = null
    private var overlayManager: OverlayViewManager? = null
    private var mediaProjectionIntent: Intent? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    override fun onBind(intent: Intent?): IBinder {
        throw UnsupportedOperationException("This service does not support binding")
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(1339, NotificationCompat.Builder(baseContext)
                .setContentTitle(resources.getString(R.string.notification_title))
                .setContentText(resources.getStringArray(R.array.notification_text_random)
                        .let { it[(System.nanoTime() % it.size).toInt()] })
                .setColor(ContextCompat.getColor(baseContext, R.color.main_activity_color_etched))
                .setSmallIcon(R.drawable.launch_screen_logo)
                .setContentIntent(PendingIntent.getActivity(baseContext, 0,
                        Intent(baseContext, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0))
                .addAction(0, resources.getString(R.string.notification_action_stop), PendingIntent.getService(
                        baseContext, 0, Intent(baseContext, this::class.java).setAction(ACTION_STOP), 0))
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build())

        instance = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent!!.action) {
            ACTION_START -> run {
                if (currentMode != null) {
                    Log.w(TAG, "Attempted to start an already started service")
                    return@run
                }
                val modeId = intent.getStringExtra(EXTRA_MODE_ID)
                if (modeId == null) {
                    Log.e(TAG, "Attempted to start with no operation mode")
                    return@run
                }
                val opMode = ModeRegistry.getModeModule(modeId)
                if (opMode == null) {
                    Log.e(TAG, "Mode named \"$modeId\" not found, aborting start")
                    return@run
                }

                currentMode = opMode
                overlayManager = opMode.createOverlayManager(baseContext)
                overlayManager?.startOverlay()
            }
            ACTION_STOP -> stopSelf()
            else -> Log.w(TAG, "No command provided while starting service")
        }
        return START_STICKY
    }

    override fun onDestroy() {
        instance = null
        stopMediaProjection()
        overlayManager?.stopOverlay()
        overlayManager = null
        currentMode = null

        super.onDestroy()
    }

    private fun initializeMediaProjection() {
        if (mediaProjectionIntent == null)
            launchActivityFromOverlay(this, CaptureRequestActivity::class.java)
        else {
            val projectionManager = baseContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val mediaProjection = projectionManager.getMediaProjection(Activity.RESULT_OK, mediaProjectionIntent)
            instance!!.onMediaProjectionAvailable(mediaProjection)
        }
    }

    private fun stopMediaProjection() {
        if (mediaProjection == null) {
            // This will surely be triggered by onProjectionStop() toggling 'armed' and re-sending ARMED_STATE_CHANGED
            Log.w(TAG, "Flow warning: you have tried to stop MediaProjection without starting it first")
            return
        }
        overlayManager!!.onProjectionStop()

        virtualDisplay!!.release()
        virtualDisplay = null
        imageReader!!.close()
        imageReader = null
        mediaProjection!!.stop()
        mediaProjection = null
    }

    private fun onMediaProjectionAvailable(mediaProjection: MediaProjection?) {
        if (mediaProjection == null) {
            Log.d(TAG, "The user did not allow initialization of the MediaProjection API")
            overlayManager!!.onProjectionStartAborted()
        } else {
            Log.d(TAG, "Permission granted, initializing MediaProjection")
            this.mediaProjection = mediaProjection
            // TODO: NOT ORIENTATION SAFE, we should reinitialize imageReader & co. on orientation change
            val screenSize = Point()
            overlayManager!!.windowManager.defaultDisplay.getSize(screenSize) // getWindowManager() in Activity
            imageReader = ImageReader.newInstance(screenSize.x, screenSize.y, PixelFormat.RGBA_8888, 2)
            virtualDisplay = mediaProjection.createVirtualDisplay("capture-overlay",
                    screenSize.x, screenSize.y, resources.displayMetrics.densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    imageReader!!.surface, null, null)

            overlayManager!!.onProjectionStart()
        }
    }

    private fun takeShot() {
        assert(mediaProjection != null) {
            "Capture button should not be available unless armed/MediaProjection available" }

        // Start an async task to capture the screen, we pass in a postProcess lambda to crop the image and keep
        // only the center square of the screen, as a callback (on UI thread) we pass overlayManager.onImageAvailable()
        currentMode!!.makeCaptureProcessTask(overlayManager!!).execute(imageReader!!)
    }

    class MessageHandler : Handler() {
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
                MESSAGE_SET_MEDIA_PROJECTION_INTENT -> {
                    instance!!.mediaProjectionIntent = msg.obj as Intent?
                    instance!!.initializeMediaProjection()
                }
                MESSAGE_CAPTURE_SCREEN ->
                    instance!!.takeShot()
            }
        }
    }

}
