package net.hax.niatool.overlay

import android.view.Display
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager


class WindowManagerWrapper(private val delegate: WindowManager) : WindowManager {

    private val views = HashMap<View, ViewData>()

    fun temporaryHideViews() {
        for ((k, v) in views) {
            v.visibility = k.visibility
            k.visibility = View.INVISIBLE
        }
    }

    fun restoreViews() {
        for ((k, v) in views) k.visibility = v.visibility
    }

    override fun getDefaultDisplay(): Display =
            delegate.defaultDisplay

    override fun addView(view: View?, params: ViewGroup.LayoutParams?) {
        delegate.addView(view, params)
        if (view != null) views.put(view, ViewData(view.visibility))
    }

    override fun updateViewLayout(view: View?, params: ViewGroup.LayoutParams?) =
            delegate.updateViewLayout(view, params)

    override fun removeView(view: View?) {
        views.remove(view)
        delegate.removeView(view)
    }

    override fun removeViewImmediate(view: View?) {
        views.remove(view)
        delegate.removeViewImmediate(view)
    }

    private data class ViewData(var visibility: Int)

}
