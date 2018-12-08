package net.hax.niatool

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.annotation.StyleRes
import android.util.Log

object ApplicationSettings {

    private val TAG = "ApplicationSettings"

    private val PREF_USE_ANIMATIONS_KEY = "use_animations"
    private val PREF_USE_ANIMATIONS_DEFAULT = true

    private val PREF_OVERLAY_THEME_KEY = "overlay_theme"
    private val PREF_OVERLAY_THEME_DEFAULT = "OverlayCtrl.Default"

    private val PREF_CURRENT_MODE = "current_mode"
    private val PREF_CURRENT_MODE_DEFAULT = "glyph"

    private val OVERLAY_THEME_IDS = intArrayOf(
            R.style.OverlayCtrl_Default,
            R.style.OverlayCtrl_Kahifrex,
            R.style.OverlayCtrl_KahifrexGreen)

    val OVERLAY_THEME_NAMES = intArrayOf(
            R.string.theme_overlay_default_title,
            R.string.theme_overlay_kahifrex_title,
            R.string.theme_overlay_kahifrex_green_title)

    var overlayThemeIndex: Int = -1
    var animationsEnabled: Boolean = false

    val overlayTheme: Int
        get() = OVERLAY_THEME_IDS[overlayThemeIndex]

    val overlayThemeName: String
        get() = applicationContext.getString(OVERLAY_THEME_NAMES[overlayThemeIndex])

    var currentMode: String = ""

    private lateinit var applicationContext: Context
    private lateinit var applicationPreferences: SharedPreferences

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
        applicationPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        animationsEnabled = applicationPreferences.getBoolean(PREF_USE_ANIMATIONS_KEY, PREF_USE_ANIMATIONS_DEFAULT)
        overlayThemeIndex = OVERLAY_THEME_IDS.indexOf(fetchCurrentOverlayThemeId())

        if (overlayThemeIndex == -1)
            throw NoSuchElementException("Developer is an oaf and forgot to add this theme to the index")

        currentMode = applicationPreferences.getString(PREF_CURRENT_MODE, PREF_CURRENT_MODE_DEFAULT)
    }

    fun saveApplicationPreferences() {
        val prefEditor = applicationPreferences.edit()
        prefEditor.putBoolean(PREF_USE_ANIMATIONS_KEY, animationsEnabled)
        prefEditor.putString(PREF_OVERLAY_THEME_KEY,
                applicationContext.resources.getResourceEntryName(OVERLAY_THEME_IDS[overlayThemeIndex]))
        prefEditor.putString(PREF_CURRENT_MODE, currentMode)
        prefEditor.apply()
        Log.i(TAG, "Application preferences saved to disk")
    }

    @StyleRes
    private fun fetchCurrentOverlayThemeId(): Int {
        // Get the theme style name from preferences, if not set use default theme
        var themeStyleName = applicationPreferences.getString(PREF_OVERLAY_THEME_KEY, PREF_OVERLAY_THEME_DEFAULT)

        // Then we get the resource id of that theme
        @StyleRes var themeId: Int = applicationContext.resources.getIdentifier(
                themeStyleName, "style", applicationContext.packageName)

        // If we failed, and that was not the default theme, fallback to default
        if (themeId == 0 && themeStyleName != PREF_OVERLAY_THEME_DEFAULT) {
            // Retry with default theme
            Log.w(TAG, "Theme named \"$themeStyleName\" not found, using default")
            themeStyleName = PREF_OVERLAY_THEME_DEFAULT
            themeId = applicationContext.resources.getIdentifier(
                    themeStyleName, "style", applicationContext.packageName)
        }

        // IF we failed or failed again with the default theme, use hardcoded
        if (themeId == 0) {
            Log.e(TAG, "Default theme \"$themeStyleName\" not found, this is strange, using hardcoded ID")
            themeId = R.style.OverlayCtrl_Default
        }

        return themeId
    }

}
