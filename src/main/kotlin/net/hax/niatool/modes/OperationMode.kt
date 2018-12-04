package net.hax.niatool.modes

import android.content.Context
import android.content.res.Resources
import net.hax.niatool.overlay.OverlayViewManager
import net.hax.niatool.task.ScreenCaptureTask

/**
 * An operation mode for Watcher
 *
 * Classes implementing this interface can be considered as "factories" of components required for the regular workings
 * of a Watcher _mode of operation_.
 *
 * *A class implementing this must be registered in [ModeRegistry] so that it can be selected by the user in the UI.*
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
     * Builds a new implementation of [ScreenCaptureTask] to process captured images
     *
     * The returned [ScreenCaptureTask] allows the mode logic to process a new screenshot in background, update the UI
     * while processing and when the task itself is finished; in the same manner as Android's async tasks.
     *
     * @param overlayManager The current [OverlayViewManager] as it is returned by [OperationMode.createOverlayManager],
     *                       for ease of access
     */
    fun makeCaptureProcessTask(overlayManager: OverlayViewManager): ScreenCaptureTask<*, *>

}
