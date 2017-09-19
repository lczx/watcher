package net.hax.niatool.overlay

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.widget.ImageView

class ImageOverlay(context: Context) {

    companion object {
        private val TAG = "ImageOverlay"
    }

    private val mImageList = mutableListOf<Bitmap>()
    private var mImageIndex = 0 // Better use this than an Iterator, also Kotlin has only ArrayLists

    val viewport = ImageView(context).apply {
        visibility = View.INVISIBLE
        alpha = .4f
    }

    var visible: Boolean
        set(value) {
            if (value) {
                // Reset carousel on visibility change
                mImageIndex = 0
                if (mImageList.isNotEmpty()) viewport.setImageBitmap(mImageList[mImageIndex])
                viewport.visibility = View.VISIBLE
            } else {
                viewport.visibility = View.INVISIBLE
            }
        }
        get() = viewport.visibility == View.VISIBLE

    val imageCount: Int
        get() = mImageList.size

    fun addImage(bitmap: Bitmap) {
        mImageList.add(bitmap)
        Log.d(TAG, "Added bitmap $bitmap to collection")
    }

    fun previousImage() {
        if (mImageIndex > 0) {
            viewport.setImageBitmap(mImageList[--mImageIndex])
        }
    }

    fun nextImage() {
        if (mImageIndex < mImageList.size - 1) {
            viewport.setImageBitmap(mImageList[++mImageIndex])
        }
    }

    fun recycleAll() {
        viewport.setImageDrawable(null)
        for (image in mImageList) image.recycle()
        Log.d(TAG, "Image collection cleared and bitmaps recycled (${mImageList.size} elements)")
        mImageList.clear()
    }

}
