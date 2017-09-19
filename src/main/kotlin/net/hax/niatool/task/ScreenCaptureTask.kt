package net.hax.niatool.task

import android.graphics.Bitmap
import android.media.Image
import android.media.ImageReader
import android.os.AsyncTask

// TODO JavaDoc that postProcess is executed on the working thread and should not save copied of the passed
// TODO   in image because it will be recycled

class ScreenCaptureTask(private val postProcess: ((Image, Bitmap) -> Bitmap)? = null,
                        private val callback: (Bitmap) -> Unit) : AsyncTask<ImageReader, Void, Bitmap>() {

    override fun doInBackground(vararg params: ImageReader?): Bitmap {
        val image = params[0]!!.acquireLatestImage()
        // TODO verify that is safe to use image.width and height instead of windowManager.defaultDisplay.getSize
        // TODO NOT CHECKED FOR ORIENTATION SAFETY

        val plane = image.planes[0]
        val rowPadding = plane.rowStride - plane.pixelStride * image.width

        // create bitmap
        val capture = Bitmap.createBitmap(// TODO IS ARGB_8888 OK?
                image.width + rowPadding / plane.pixelStride, image.height, Bitmap.Config.ARGB_8888)
        capture.copyPixelsFromBuffer(plane.buffer)

        val result = postProcess?.invoke(image, capture) ?: capture
        if (result != capture) capture.recycle()
        image.close()
        return result
    }

    override fun onPostExecute(result: Bitmap?) = callback(result!!)

}
