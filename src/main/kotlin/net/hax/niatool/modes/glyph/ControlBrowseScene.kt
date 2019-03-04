package net.hax.niatool.modes.glyph

import android.view.KeyEvent
import android.view.View
import android.widget.ImageButton
import net.hax.niatool.R
import net.hax.niatool.overlay.ControlPanelOverlay2
import net.hax.niatool.widget.DispatcherLayout

class ControlBrowseScene(private val onBrowseBackCommand: () -> Unit, private val onBrowseForwardCommand: () -> Unit) :
        ControlPanelOverlay2.Scene(R.layout.overlay_ctrl_browse) {

    override fun onCreateView(view: View, controller: ControlPanelOverlay2) {
        with(view.findViewById<ImageButton>(R.id.button_back)) {
            setOnClickListener { onBrowseBackCommand() }
        }
        with(view.findViewById<ImageButton>(R.id.button_forward)) {
            setOnClickListener { onBrowseForwardCommand() }
            setOnLongClickListener {
                controller.switchScene(ControlCaptureScene::class.java); true
            }
        }
        (view as DispatcherLayout).onDispatchKeyHandler = { keyEvent -> keyHandler(keyEvent) }
    }

    private fun keyHandler(event: KeyEvent?): Boolean? = when (event!!.keyCode) {
    // Bind hardware volume buttons as browse controls,
    // buttons are inverted because of a problem with VOLUME_DOWN being too slow
        KeyEvent.KEYCODE_VOLUME_DOWN -> {
            if (event.action != KeyEvent.ACTION_UP) onBrowseBackCommand(); true
        }
        KeyEvent.KEYCODE_VOLUME_UP -> {
            if (event.action != KeyEvent.ACTION_UP) onBrowseForwardCommand(); true
        }
        else -> null
    }

}
