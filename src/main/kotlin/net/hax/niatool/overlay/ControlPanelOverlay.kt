package net.hax.niatool.overlay

import android.content.Context
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import net.hax.niatool.R

class ControlPanelOverlay(private val context: Context) {

    val viewport: View = createView(R.layout.overlay_ctrl)

    private fun createView(@LayoutRes res: Int) : View {
        // tip: use inflater.cloneInContext(ctx) if we want to change themes here on the fly
        val systemInflater = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
        return systemInflater.inflate(res, null)
    }

}
