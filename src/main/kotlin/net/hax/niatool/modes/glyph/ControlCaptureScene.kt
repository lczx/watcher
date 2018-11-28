package net.hax.niatool.modes.glyph

import net.hax.niatool.R
import net.hax.niatool.overlay.ControlPanelOverlay2

class ControlCaptureScene(private val onCaptureScreenCommand: () -> Unit) :
        ControlPanelOverlay2.Scene(R.layout.overlay_ctrl_capture) {

    override val buttonIds: List<Int>
        get() = listOf(R.id.button_capture)

    override fun supportsLongClick(buttonId: Int) = true

    override fun onButtonClick(buttonId: Int, controller: ControlPanelOverlay2) {
        onCaptureScreenCommand()
    }

    override fun onLongButtonClick(buttonId: Int, controller: ControlPanelOverlay2): Boolean {
        controller.switchScene(ControlBrowseScene::class.java)
        return true
    }

}
