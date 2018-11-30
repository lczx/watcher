package net.hax.niatool.modes

import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import net.hax.niatool.overlay.OverlayViewManager

interface OperationMode {

    fun createOverlayManager(context: Context): OverlayViewManager

    fun processCaptureBackground(image: Image, capture: Bitmap): Bitmap

}
