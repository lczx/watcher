package net.hax.niatool.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.support.v7.view.ContextThemeWrapper
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import net.hax.niatool.ApplicationSettings
import net.hax.niatool.OverlayServiceUtil
import net.hax.niatool.calculateControlToastYOffset

abstract class OverlayViewManager(protected val context: Context) {

    companion object {
        @JvmStatic
        protected val LAYOUT_FLAGS_DEFAULT =
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN

        @JvmStatic
        protected val LAYOUT_FLAGS_FOCUSABLE =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN

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
        @JvmStatic
        private val LAYOUT_PARAMS_STATUS_OVERLAY = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
            flags = LAYOUT_FLAGS_DEFAULT
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP or Gravity.CENTER  // This may look weird on Essential Phone :D
        }
        @JvmStatic
        protected val LAYOUT_PARAMS_CONTROL_OVERLAY = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            x = 0
            y = 200
            type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
            flags = LAYOUT_FLAGS_DEFAULT // Switches to LAYOUT_FLAGS_FOCUSABLE on mode change
            format = PixelFormat.TRANSLUCENT
            @SuppressLint("RtlHardcoded")
            gravity = Gravity.TOP or Gravity.LEFT
        }
    }

    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var statusOverlay: StatusPanelOverlay? = null
    protected var controlOverlay: ControlPanelOverlay2? = null

    protected val shotToast = Toast.makeText(context, null, Toast.LENGTH_SHORT)!!.apply {
        setGravity(Gravity.CENTER, 0, calculateControlToastYOffset(context))
    }

    fun startOverlay() {
        statusOverlay = StatusPanelOverlay(context, ArmedStatusListener())
        windowManager.addView(statusOverlay!!.viewport, LAYOUT_PARAMS_STATUS_OVERLAY)
    }

    open fun onProjectionStart() {
        controlOverlay = ControlPanelOverlay2(ContextThemeWrapper(context, ApplicationSettings.overlayTheme))
        configureControlOverlay(controlOverlay!!)
        windowManager.addView(controlOverlay!!.viewport, LAYOUT_PARAMS_CONTROL_OVERLAY)
    }

    fun onProjectionStartAborted() {
        // TODO: Current implementation of StatusPanelOverlay re-sends MESSAGE_ARMED_STATE_CHANGED, avoid if possible
        statusOverlay!!.armed = false
    }

    open fun onProjectionStop() {
        assert(controlOverlay != null) { "onProjectionStop() should not be called before onProjectionStart()" }
        windowManager.removeView(controlOverlay!!.viewport)
        controlOverlay = null
    }

    fun stopOverlay() {
        assert(controlOverlay == null) { "stopOverlay() should not be called before onProjectionStop()" }
        windowManager.removeView(statusOverlay!!.viewport)
        statusOverlay = null
    }

    abstract fun onImageAvailable(bitmap: Bitmap)

    abstract fun configureControlOverlay(controlOverlay: ControlPanelOverlay2)

    class ArmedStatusListener : StatusPanelOverlay.OnArmedStatusListener {
        override fun onArmedStatusChange(armed: Boolean) {
            OverlayServiceUtil.setArmed(armed)
        }
    }

}

/* --- HOORAY A BEAUTIFUL END OF PAGE MEGA-BLOCK-COMMENT, MUCH BEAUTIFUL, VERY POLISHED! ---
 * ---    FOR REFERENCE, HERE WE HAVE SOME CODE FOR A VIEW THAT CAN BE DRAGGED AROUND    ---

class MovableView(context: Context, private val windowManager: WindowManager) : View(context) {

    private val mTimeMoveThreshold = ViewConfiguration.getLongPressTimeout()
    private var _xDelta: Int = 0
    private var _yDelta: Int = 0

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.rawX.toInt()
        val y = event.rawY.toInt()
        val lParams = layoutParams as WindowManager.LayoutParams

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                _xDelta = x - lParams.x
                _yDelta = y - lParams.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.eventTime - event.downTime > mTimeMoveThreshold) {
                    lParams.x = x - _xDelta
                    lParams.y = y - _yDelta
                    windowManager.updateViewLayout(this, lParams)
                    invalidate()
                }
            }
        }
        return true
    }

}

*/
