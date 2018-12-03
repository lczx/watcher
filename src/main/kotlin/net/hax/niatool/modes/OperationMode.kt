package net.hax.niatool.modes

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.media.Image
import net.hax.niatool.overlay.OverlayViewManager

/**
 * An operation mode for Watcher
 */
interface OperationMode {

    /**
     * Retrieves information for this mode, used for display in the UI
     *
     * @param res The application context
     */
    fun getModeMetadata(res: Resources): ModeRegistry.Info

    /**
     * Creates an object extending [OverlayViewManager], used to control the view flow of the overlay
     *
     * @param context The themed context to use while creating the overlay
     * @return An [OverlayViewManager] implementation
     */
    fun createOverlayManager(context: Context): OverlayViewManager

    /**
     * Called when a capture is made to process the screenshot
     *
     * This method is invoked from a worker thread, UI should not be updated directly from this thread.
     *
     * @param image The raw captured image, use only to get capture information
     * @param capture The built capture bitmap to process
     * @return Any data to be passed to [OverlayViewManager.onDataAvailable] on the UI thread
     */
    fun processCaptureBackground(image: Image, capture: Bitmap): Any?

}
