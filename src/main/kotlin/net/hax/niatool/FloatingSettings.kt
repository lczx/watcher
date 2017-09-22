package net.hax.niatool

import android.app.Activity
import android.support.v4.widget.Space
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow

class FloatingSettings(activity: Activity) {

    companion object {
        private val TAG = "FloatingSettings"
        private val POPUP_WIDTH_DIP = 240
    }

    private val popup = PopupWindow(Space(activity).apply { minimumHeight = 400 },
            (POPUP_WIDTH_DIP * activity.resources.displayMetrics.density).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        setBackgroundDrawable(activity.resources.getDrawable(R.drawable.floating_settings_background, activity.theme))
        isOutsideTouchable = true
        elevation = 10f
        setOnDismissListener {
            // TODO: Save settings here
            Log.i(TAG, "Dismissed")
        }
    }

    fun showAtLocation(view: View, gravity: Int, x: Int, y: Int) =
            popup.showAtLocation(view, gravity, x, y)

}
