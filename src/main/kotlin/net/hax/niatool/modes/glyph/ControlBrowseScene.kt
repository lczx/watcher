package net.hax.niatool.modes.glyph

import android.view.KeyEvent
import net.hax.niatool.R
import net.hax.niatool.overlay.ControlPanelOverlay2

class ControlBrowseScene(private val onBrowseBackCommand: () -> Unit, private val onBrowseForwardCommand: () -> Unit) :
        ControlPanelOverlay2.Scene(R.layout.overlay_ctrl_browse) {

    override val buttonIds: List<Int>
        get() = listOf(R.id.button_back, R.id.button_forward)

    override fun supportsLongClick(buttonId: Int) = buttonId == R.id.button_forward

    override fun supportsDispatcherKeyEvents() = true

    override fun onButtonClick(buttonId: Int, controller: ControlPanelOverlay2) {
        when (buttonId) {
            R.id.button_back -> onBrowseBackCommand()
            R.id.button_forward -> onBrowseForwardCommand()
        }
    }

    override fun onLongButtonClick(buttonId: Int, controller: ControlPanelOverlay2): Boolean {
        controller.switchScene(ControlCaptureScene::class.java)
        return true
    }

    override fun onDispatchKeyHandler(event: KeyEvent?, controller: ControlPanelOverlay2): Boolean? {
        if (event!!.action != KeyEvent.ACTION_UP) return null

        // Bind hardware volume buttons as browse controls,
        // buttons are inverted because of a problem with VOLUME_DOWN being too slow
        return when (event!!.keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                onBrowseBackCommand(); true
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                onBrowseForwardCommand(); true
            }
            else -> null
        }
    }

}
