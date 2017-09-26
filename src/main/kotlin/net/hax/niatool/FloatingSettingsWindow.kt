package net.hax.niatool

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.support.v7.widget.SwitchCompat
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextSwitcher

class FloatingSettingsWindow(activity: Activity) {

    companion object {
        private val TAG = "FloatingSettings"
        private val POPUP_WIDTH_DIP = 260
    }

    private val mView = activity.layoutInflater.inflate(R.layout.floating_settings,
            activity.findViewById(android.R.id.content) as ViewGroup, false)
    private val mPopup = PopupWindow(mView,
            (POPUP_WIDTH_DIP * activity.resources.displayMetrics.density).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        setBackgroundDrawable(activity.resources.getDrawable(R.drawable.floating_settings_background, activity.theme))
        setOnDismissListener(ApplicationSettings::saveApplicationPreferences)
        animationStyle = R.style.Animation_App_DropUpRightDownLeft
        isOutsideTouchable = true
        elevation = 10f
    }

    private val mAnimationsToggle = mView.findViewById(R.id.anim_toggle) as SwitchCompat
    private val mThemeSelector = mView.findViewById(R.id.theme_selector) as TextSwitcher

    init {
        mAnimationsToggle.setOnCheckedChangeListener { _, isChecked ->
            ApplicationSettings.animationsEnabled = isChecked
        }

        mThemeSelector.setOnClickListener {
            val themeNames = ApplicationSettings.OVERLAY_THEME_NAMES.map(activity::getString).toTypedArray()
            val currentThemeIndex = ApplicationSettings.overlayThemeIndex

            AlertDialog.Builder(activity)
                    .setTitle(R.string.settings_caption_theme)
                    .setSingleChoiceItems(themeNames, currentThemeIndex, { view, selection ->
                        if (currentThemeIndex != selection) {
                            ApplicationSettings.overlayThemeIndex = selection
                            mThemeSelector.setText(themeNames[selection])
                        }
                        view.dismiss()
                    })
                    .show()
        }
    }

    val isShowing: Boolean
        get() = mPopup.isShowing

    fun showAtLocation(view: View, gravity: Int, x: Int, y: Int) {
        mThemeSelector.setCurrentText(ApplicationSettings.overlayThemeName)
        mAnimationsToggle.isChecked = ApplicationSettings.animationsEnabled
        mPopup.showAtLocation(view, gravity, x, y)
    }

    fun dismiss() = mPopup.dismiss()

}
