package net.hax.niatool.overlay

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import net.hax.niatool.R

class ControlPanelOverlay(private val context: Context, private val commandListener: OnCommandListener? = null) {

    private val uiHandler = Handler(Looper.getMainLooper())
    private val viewCapture = createView(R.layout.overlay_ctrl_capture)
    private val viewBrowse = createView(R.layout.overlay_ctrl_browse)
    val viewport: ViewGroup = FrameLayout(context).apply { addView(viewCapture) }

    private var inBrowseMode = false
        set(value) {
            if (field == value) return
            viewport.removeAllViews()
            viewport.addView(if (value) viewBrowse else viewCapture)
            field = value
            commandListener?.onModeChanged(value)
        }

    init {
        // We use an handler to schedule the view swap for later execution and allow the button to give touch feedback
        val switchModeListener = View.OnLongClickListener { uiHandler.post({ inBrowseMode = !inBrowseMode }); true }

        with(viewCapture.findViewById(R.id.button_capture)) {
            setOnClickListener { commandListener?.onCaptureScreenCommand() }
            setOnLongClickListener(switchModeListener)
        }
        with(viewBrowse.findViewById(R.id.button_back)) {
            setOnClickListener { commandListener?.onBrowseBackCommand() }
        }
        with(viewBrowse.findViewById(R.id.button_forward)) {
            setOnClickListener { commandListener?.onBrowseForwardCommand() }
            setOnLongClickListener(switchModeListener)
        }
    }

    private fun createView(@LayoutRes res: Int): View {
        // tip: use inflater.cloneInContext(ctx) if we want to change themes here on the fly
        val systemInflater = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
        return systemInflater.inflate(res, null)
    }

    interface OnCommandListener {
        fun onModeChanged(inBrowseMode: Boolean)
        fun onCaptureScreenCommand()
        fun onBrowseBackCommand()
        fun onBrowseForwardCommand()
    }

}
