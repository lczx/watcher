package net.hax.niatool.task

import android.graphics.Bitmap
import android.media.Image
import android.media.ImageReader
import android.os.AsyncTask

abstract class ScreenCaptureTask<P, R> : AsyncTask<ImageReader, P, R>() {

    override fun doInBackground(vararg params: ImageReader?): R {
        val image = params[0]!!.acquireLatestImage()
        // TODO verify that is safe to use image.width and height instead of windowManager.defaultDisplay.getSize
        // TODO NOT CHECKED FOR ORIENTATION SAFETY

        val plane = image.planes[0]
        val rowPadding = plane.rowStride - plane.pixelStride * image.width

        // create bitmap
        val capture = Bitmap.createBitmap(// TODO IS ARGB_8888 OK?
                image.width + rowPadding / plane.pixelStride, image.height, Bitmap.Config.ARGB_8888)
        capture.copyPixelsFromBuffer(plane.buffer)

        val result = processCaptureBackground(image, capture) ?: capture
        if (result != capture) capture.recycle()
        image.close()

        @Suppress("UNCHECKED_CAST") // It is a problem of the mode provider if this cast fails
        return result as R
    }

    /**
     * Callwd when a capture bitmap is ready for further processing
     *
     * This method is invoked from a worker thread, UI should not be updated directly from this thread.
     * Furthermore, the passed in `image` must not be stored as it is recycled just after this method is called.
     *
     * @param image The raw captured image, use only to get capture information
     * @param capture Thr built capture bitmap to process
     * @return The result of the computation
     */
    abstract fun processCaptureBackground(image: Image, capture: Bitmap): R

}
