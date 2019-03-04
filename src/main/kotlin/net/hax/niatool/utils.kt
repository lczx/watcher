package net.hax.niatool

import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import org.slf4j.LoggerFactory

@TargetApi(Build.VERSION_CODES.M)
fun isOverlayEnabled(ctx: Context): Boolean {
    if (Settings.canDrawOverlays(ctx))
        return true

    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
        // Workaround for Oreo (API 26) - Settings.canDrawOverlays() returns false; attempt to draw a view to check
        try {
            val windowManager = ctx.getSystemService(Context.WINDOW_SERVICE)!! as WindowManager

            // Use TYPE_APPLICATION_OVERLAY instead of TYPE_SYSTEM_ALERT in API 26+
            val params = WindowManager.LayoutParams(
                    0, 0,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSPARENT)

            val view = View(ctx)
            windowManager.addView(view, params)
            windowManager.removeView(view)

            // If we arrived here w/out exceptions then we can draw overlays (even if canDrawOverlays() returned false)
            return true

        } catch (e: WindowManager.BadTokenException) {
            LoggerFactory.getLogger("isOverlayEnabled").warn("Failed to draw test overlay", e)
        }
    }

    return false
}

fun launchActivityFromOverlay(ctx: Context, cls: Class<*>, flags: Int = 0) {
    // To avoid long startup if invoked from launcher: https://stackoverflow.com/questions/5600084
    val pi = PendingIntent.getActivity(ctx, 0, Intent(ctx, cls).setFlags(flags), 0)
    try {
        pi.send()
    } catch (e: PendingIntent.CanceledException) {
        throw AssertionError("...and why this intent should be canceled???")
    }
}

fun calculateControlToastYOffset(ctx: Context): Int {
    val display = ctx.resources.displayMetrics
    return -Math.min(display.widthPixels, display.heightPixels) / 2 - (60 * display.density).toInt()
}

fun obtainStatusBarHeight(resources: Resources): Int {
    val resId = resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (resId > 0) resources.getDimensionPixelSize(resId) else 0
}

abstract class AnimationAdapter : Animation.AnimationListener {
    override fun onAnimationStart(animation: Animation?) {}

    override fun onAnimationEnd(animation: Animation?) {}

    override fun onAnimationRepeat(animation: Animation?) {}
}

fun Animation.onEnd(callback: (animation: Animation?) -> Unit) {
    this.setAnimationListener(object : AnimationAdapter() {
        override fun onAnimationEnd(animation: Animation?) = callback(animation)
    })
}
