package net.hax.niatool

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources

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
