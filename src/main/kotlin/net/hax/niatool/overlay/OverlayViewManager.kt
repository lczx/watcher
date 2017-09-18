package net.hax.niatool.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager

class OverlayViewManager(private val context: Context) {

    companion object {
        private val TAG = "OverlayManager"

        // WindowManager.LayoutParams - Types
        //   - TYPE_SYSTEM_OVERLAY: Touch events pass through, which cannot be intercepted
        //   - TYPE_SYSTEM_ERROR:   Can be placed over the status bar, also visible from the lock screen
        //   - TYPE_SYSTEM_ALERT:   Like TYPE_PHONE but cannot get SOFT_INPUT_ADJUST_PAN and SOFT_INPUT_ADJUST_RESIZE
        //                          events (move or resize on virtual keyboard shown/hidden)
        //   - TYPE_PHONE:          (standard overlay)

        // WindowManager.LayoutParams - Flags (see also: https://stackoverflow.com/a/8300497)
        //   - FLAG_NOT_FOCUSABLE:       Allows button presses to pass through (includes FLAG_NOT_TOUCH_MODAL)
        //   - FLAG_WATCH_OUTSIDE_TOUCH: Can get touch events outside own area as ACTION_OUTSIDE
        //   - FLAG_LAYOUT_IN_SCREEN:    Can be drawn over the status bar
        private val LAYOUT_PARAMS_STATUS_OVERLAY = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP or Gravity.CENTER  // This may look weird on Essential Phone :D
        }
    }

    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var statusOverlay: StatusPanelOverlay? = null

    fun startOverlay() {
        statusOverlay = StatusPanelOverlay(context)
        windowManager.addView(statusOverlay!!.viewport, LAYOUT_PARAMS_STATUS_OVERLAY)
    }

    fun stopOverlay() {
        windowManager.removeView(statusOverlay!!.viewport)
        statusOverlay = null
    }

}
