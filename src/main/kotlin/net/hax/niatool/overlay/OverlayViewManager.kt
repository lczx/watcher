package net.hax.niatool.overlay

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.WindowManager
import net.hax.niatool.OverlayServiceUtil
import net.hax.niatool.R

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
        @SuppressLint("RtlHardcoded")
        private val LAYOUT_PARAMS_CONTROL_OVERLAY = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            x = 0
            y = 200
            type = WindowManager.LayoutParams.TYPE_PHONE
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP or Gravity.LEFT
        }
        private val LAYOUT_PARAMS_IMAGE_OVERLAY = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.CENTER
        }

        // TODO: Consider moving these somewhere else
        fun launchActivityFromOverlay(ctx: Context, cls: Class<*>, flags: Int = 0) {
            // To avoid long startup if invoked from launcher: https://stackoverflow.com/questions/5600084
            val pi = PendingIntent.getActivity(ctx, 0, Intent(ctx, cls).setFlags(flags), 0)
            try {
                pi.send()
            } catch (e: PendingIntent.CanceledException) {
                throw AssertionError("...and why this intent should be canceled???")
            }
        }
    }

    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var statusOverlay: StatusPanelOverlay? = null
    private var controlOverlay: ControlPanelOverlay? = null
    private var imageOverlay: ImageOverlay? = null

    fun startOverlay() {
        statusOverlay = StatusPanelOverlay(context, ArmedStatusListener())
        windowManager.addView(statusOverlay!!.viewport, LAYOUT_PARAMS_STATUS_OVERLAY)
        }

    fun onProjectionStart() {
        imageOverlay = ImageOverlay(context)
        windowManager.addView(imageOverlay!!.viewport, LAYOUT_PARAMS_IMAGE_OVERLAY)
        controlOverlay = ControlPanelOverlay(ContextThemeWrapper(context, R.style.OverlayCtrl_Default), CommandListener())
        windowManager.addView(controlOverlay!!.viewport, LAYOUT_PARAMS_CONTROL_OVERLAY)
    }

    fun onProjectionStartAborted() {
        // TODO: Current implementation of StatusPanelOverlay re-sends MESSAGE_ARMED_STATE CHANGED, avoid if possible
        statusOverlay!!.armed = false
    }

    fun onProjectionStop() {
        assert(controlOverlay != null, { "onProjectionStop() should not be called before onProjectionStart()" })
        windowManager.removeView(controlOverlay!!.viewport)
        controlOverlay = null
        windowManager.removeView(imageOverlay!!.viewport)
        imageOverlay = null
    }

    fun stopOverlay() {
        assert(controlOverlay == null, { "stopOverlay() should not be called before onProjectionStop()" })
        windowManager.removeView(statusOverlay!!.viewport)
        statusOverlay = null
    }

    class ArmedStatusListener : StatusPanelOverlay.OnArmedStatusListener {
        override fun onArmedStatusChange(armed: Boolean) {
            OverlayServiceUtil.setArmed(armed)
        }
    }

    inner class CommandListener : ControlPanelOverlay.OnCommandListener {
        override fun onModeChanged(inBrowseMode: Boolean) {
            imageOverlay!!.visible = inBrowseMode
        }

        override fun onCaptureScreenCommand() {
            TODO("not implemented")
        }

        override fun onBrowseBackCommand() {
            TODO("not implemented")
        }

        override fun onBrowseForwardCommand() {
            TODO("not implemented")
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
