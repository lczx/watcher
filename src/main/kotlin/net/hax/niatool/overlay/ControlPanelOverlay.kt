package net.hax.niatool.overlay

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.support.annotation.LayoutRes
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import net.hax.niatool.ApplicationSettings
import net.hax.niatool.R
import net.hax.niatool.onEnd
import net.hax.niatool.widget.DispatcherLayout

class ControlPanelOverlay(private val context: Context, private val commandListener: OnCommandListener? = null) {

    private val uiHandler = Handler(Looper.getMainLooper())
    private val mViewCapture = createView(R.layout.overlay_ctrl_capture)
    private val mViewBrowse = createView(R.layout.overlay_ctrl_browse)
    private val animOut = AnimationUtils.loadAnimation(context, R.anim.overlay_ctrl_slide_out)
    private val animIn = AnimationUtils.loadAnimation(context, R.anim.overlay_ctrl_slide_in)
    val viewport: ViewGroup = FrameLayout(context)

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

        // Bind hardware volume buttons as browse controls
        (mViewBrowse as DispatcherLayout).onDispatchKeyHandler = { event: KeyEvent? ->
            when (event!!.keyCode) {
                KeyEvent.KEYCODE_VOLUME_DOWN -> { // Inverted cause a problem with VOLUME_DOWN being too slow
                    if (event.action == KeyEvent.ACTION_UP) commandListener?.onBrowseBackCommand(); true
                }
                KeyEvent.KEYCODE_VOLUME_UP -> { // Inverted cause a problem with VOLUME_DOWN being too slow
                    if (event.action == KeyEvent.ACTION_UP) commandListener?.onBrowseForwardCommand(); true
                }
                else -> null
            }
        }

        mViewCapture.isFocusableInTouchMode = true // Somewhat required to gain focus on first show up
        showNextView(mViewCapture, ApplicationSettings.animationsEnabled)
    }

    private fun createView(@LayoutRes res: Int): View {
        // tip: use inflater.cloneInContext(ctx) if we want to change themes here on the fly
        val systemInflater = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
        return systemInflater.inflate(res, null)
    }

    private fun switchViews(nextView: View) {
        if (ApplicationSettings.animationsEnabled) {
            animOut.onEnd { showNextView(nextView, true) }
            viewport.getChildAt(0).startAnimation(animOut)
        } else {
            showNextView(nextView, false)
        }
    }

    private fun showNextView(nextView: View, animate: Boolean) {
        viewport.removeAllViews()
        viewport.addView(nextView)
        if (animate) nextView.startAnimation(animIn)
        nextView.requestFocus()
    }

    interface OnCommandListener {
        fun onModeChanged(inBrowseMode: Boolean)
        fun onCaptureScreenCommand()
        fun onBrowseBackCommand()
        fun onBrowseForwardCommand()
    }

}
