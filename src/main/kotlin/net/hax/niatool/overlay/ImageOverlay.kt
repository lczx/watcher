package net.hax.niatool.overlay

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.TransitionDrawable
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import net.hax.niatool.R
import net.hax.niatool.calculateControlToastYOffset

class ImageOverlay(private val context: Context) {

    companion object {
        private val TAG = "ImageOverlay"
        private val USE_TRANSITIONS = true
        private val TRANSITION_DURATION_MS = 150
    }

    private val indexToast = Toast.makeText(context, null, Toast.LENGTH_SHORT).apply {
        setGravity(Gravity.CENTER, 0, calculateControlToastYOffset(context))
    }
    private val mImageList = mutableListOf<Bitmap>()
    private var mImageIndex = 0 // Better use this than an Iterator, also Kotlin has only ArrayLists

    val viewport: ImageView = ImageView(context).apply {
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
            if (USE_TRANSITIONS)
                animateTo(mImageList[mImageIndex], mImageList[--mImageIndex])
            else
                viewport.setImageBitmap(mImageList[--mImageIndex])
            showIndexToast()
        }
    }

    fun nextImage() {
        if (mImageIndex < mImageList.size - 1) {
            if (USE_TRANSITIONS)
                animateTo(mImageList[mImageIndex], mImageList[++mImageIndex])
            else
                viewport.setImageBitmap(mImageList[++mImageIndex])
            showIndexToast()
        }
    }

    fun recycleAll() {
        viewport.setImageDrawable(null)
        for (image in mImageList) image.recycle()
        Log.d(TAG, "Image collection cleared and bitmaps recycled (${mImageList.size} elements)")
        mImageList.clear()
    }

    private fun showIndexToast() {
        val indexMessage = context.getString(R.string.toast_shot_browse, mImageIndex + 1, mImageList.size)
        indexToast.setText(indexMessage)
        indexToast.show()
    }

    private fun animateTo(previous: Bitmap, next: Bitmap) {
        val transition = TransitionDrawable(arrayOf(
                BitmapDrawable(context.resources, previous),
                BitmapDrawable(context.resources, next)))
        transition.isCrossFadeEnabled = true
        viewport.setImageDrawable(transition)
        transition.startTransition(TRANSITION_DURATION_MS)
    }

}
