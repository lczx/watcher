package net.hax.niatool.overlay

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import net.hax.niatool.R
import net.hax.niatool.onEnd

class ControlPanelOverlay(private val context: Context, private val commandListener: OnCommandListener? = null) {

    companion object {
        private val USE_ANIMATIONS = true
    }

    private val uiHandler = Handler(Looper.getMainLooper())
    private val mViewCapture = createView(R.layout.overlay_ctrl_capture)
    private val mViewBrowse = createView(R.layout.overlay_ctrl_browse)
    val viewport: ViewGroup = FrameLayout(context).apply { addView(mViewCapture) }

    private var inBrowseMode = false
        set(value) {
            if (field == value) return
            switchViews(if (value) mViewBrowse else mViewCapture)
            field = value
            commandListener?.onModeChanged(value)
        }

    init {
        // We use an handler to schedule the view swap for later execution and allow the button to give touch feedback
        val switchModeListener = View.OnLongClickListener { uiHandler.post({ inBrowseMode = !inBrowseMode }); true }

        with(mViewCapture.findViewById(R.id.button_capture)) {
            setOnClickListener { commandListener?.onCaptureScreenCommand() }
            setOnLongClickListener(switchModeListener)
        }
        with(mViewBrowse.findViewById(R.id.button_back)) {
            setOnClickListener { commandListener?.onBrowseBackCommand() }
        }
        with(mViewBrowse.findViewById(R.id.button_forward)) {
            setOnClickListener { commandListener?.onBrowseForwardCommand() }
            setOnLongClickListener(switchModeListener)
        }
    }

    private fun createView(@LayoutRes res: Int): View {
        // tip: use inflater.cloneInContext(ctx) if we want to change themes here on the fly
        val systemInflater = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
        return systemInflater.inflate(res, null)
    }

    private fun switchViews(nextView: View) {
        if (!USE_ANIMATIONS) {
            viewport.removeAllViews()
            viewport.addView(nextView)
            return
        }

        val animOut = AnimationUtils.loadAnimation(context, R.anim.overlay_ctrl_slide_out)
        val animIn = AnimationUtils.loadAnimation(context, R.anim.overlay_ctrl_slide_in)

        animOut.onEnd {
            viewport.removeAllViews()
            viewport.addView(nextView)
            nextView.startAnimation(animIn)
        }
        viewport.getChildAt(0).startAnimation(animOut)
    }

    interface OnCommandListener {
        fun onModeChanged(inBrowseMode: Boolean)
        fun onCaptureScreenCommand()
        fun onBrowseBackCommand()
        fun onBrowseForwardCommand()
    }

}
