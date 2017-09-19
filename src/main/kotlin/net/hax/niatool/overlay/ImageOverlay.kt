package net.hax.niatool.overlay

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView

class ImageOverlay(context: Context) {

    companion object {
        private val TAG = "ImageOverlay"
    }

    private var currentImage: Bitmap? = null

    val viewport = ImageView(context).apply {
        visibility = View.INVISIBLE
        alpha = .4f
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

    fun addImage(bitmap: Bitmap) {
        viewport.setImageBitmap(bitmap)
        currentImage?.recycle()
        currentImage = bitmap
    }

    fun recycleAll() {
        viewport.setImageDrawable(null)
        currentImage?.recycle()
        currentImage = null
    }

}
