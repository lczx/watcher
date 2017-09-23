package net.hax.niatool

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextSwitcher

class FloatingSettingsWindow(activity: Activity) {

    companion object {
        private val TAG = "FloatingSettings"
        private val POPUP_WIDTH_DIP = 240
    }

    private val mView = activity.layoutInflater.inflate(R.layout.floating_settings,
            activity.findViewById(android.R.id.content) as ViewGroup, false)
    private val mPopup = PopupWindow(mView,
            (POPUP_WIDTH_DIP * activity.resources.displayMetrics.density).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        setBackgroundDrawable(activity.resources.getDrawable(R.drawable.floating_settings_background, activity.theme))
        animationStyle = R.style.Animation_App_DropUpRightDownLeft
        isOutsideTouchable = true
        elevation = 10f
        setOnDismissListener {
            // TODO: Save settings here
            Log.i(TAG, "Dismissed")
        }
    }

    init {
        (mView.findViewById(R.id.theme_selector) as TextSwitcher).setCurrentText("Placeholder")
    }

    val isShowing: Boolean
        get() = mPopup.isShowing

    fun showAtLocation(view: View, gravity: Int, x: Int, y: Int) =
            mPopup.showAtLocation(view, gravity, x, y)

    fun dismiss() = mPopup.dismiss()

}
