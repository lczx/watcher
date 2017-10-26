package net.hax.niatool

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

class CaptureRequestActivity : AppCompatActivity() {

    companion object {
        private val TAG = "CaptureRequestActivity"
        private val REQUEST_CODE_SCREEN_CAPTURE = 666
    }

    private lateinit var projectionManager: MediaProjectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        Log.d(TAG, "Requesting screen capture permission")
        startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_CODE_SCREEN_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SCREEN_CAPTURE) {
            finish() // Call this first to avoid strange screen flickering on activity dismiss
            if (resultCode == AppCompatActivity.RESULT_OK) {
                // Same check is done in MediaProjectionManager#getMediaProjection() by the API,
                // but we do it here to avoid passing more parameters around
                OverlayServiceUtil.setMediaProjectionIntent(data)
            }

            // We don't need to check resultCode: getMediaProjection returns null if not RESULT_OK
            Log.d(TAG, "Got screen capture permission result")
        }
    }

}
