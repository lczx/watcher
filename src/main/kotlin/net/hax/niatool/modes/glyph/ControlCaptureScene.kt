package net.hax.niatool.modes.glyph

import android.view.View
import android.widget.ImageButton
import net.hax.niatool.R
import net.hax.niatool.overlay.ControlPanelOverlay2

class ControlCaptureScene(private val onCaptureScreenCommand: () -> Unit) :
        ControlPanelOverlay2.Scene(R.layout.overlay_ctrl_capture) {

    override fun onCreateView(view: View, controller: ControlPanelOverlay2) {
        with(view.findViewById<ImageButton>(R.id.button_capture)) {
            setOnClickListener { onCaptureScreenCommand() }
            setOnLongClickListener {
                controller.switchScene(ControlBrowseScene::class.java); true
            }
        }
    }

}
