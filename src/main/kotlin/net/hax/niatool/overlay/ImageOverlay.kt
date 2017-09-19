package net.hax.niatool.overlay

import android.content.Context
import android.view.View
import android.widget.ImageView
import net.hax.niatool.R

class ImageOverlay(context: Context) {

    companion object {
        private val TAG = "ImageOverlay"
    }

    val viewport = ImageView(context).apply {
        visibility = View.INVISIBLE
        alpha = .4f
        setImageDrawable(resources.getDrawable(R.drawable.the_truth, context.theme))
    }

    var visible: Boolean
        set(value) {
            if (value) {
                viewport.visibility = View.VISIBLE
            } else {
                viewport.visibility = View.INVISIBLE
            }
        }
        get() = viewport.visibility == View.VISIBLE

}
