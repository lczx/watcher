package net.hax.niatool.modes.glyph

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.media.Image
import net.hax.niatool.R
import net.hax.niatool.modes.ModeRegistry
import net.hax.niatool.modes.OperationMode
import net.hax.niatool.overlay.OverlayViewManager
import net.hax.niatool.task.ScreenCaptureTask

class GlyphHackMode : OperationMode {

    companion object {
        const val CAPTURE_MULT_Y_START = .3
        const val CAPTURE_MULT_Y_HEIGHT = .65
    }

    override fun getModeMetadata(res: Resources) = ModeRegistry.Info(
            name = res.getString(R.string.mode_glyph_name),
            description = res.getString(R.string.mode_glyph_description))

    override fun createOverlayManager(context: Context) = GlyphOverlayManager(context)

    override fun makeCaptureProcessTask(overlayManager: OverlayViewManager) = ResizeAndShowTask(overlayManager)

    class ResizeAndShowTask(overlayManager: OverlayViewManager) : ScreenCaptureTask<Void, Bitmap>(overlayManager) {
        override fun processCaptureBackground(image: Image, capture: Bitmap) =
                Bitmap.createBitmap(capture,
                        0, (image.height * CAPTURE_MULT_Y_START).toInt(),
                        image.width, (image.height * CAPTURE_MULT_Y_HEIGHT).toInt())!!

        override fun onPostExecute(result: Bitmap) {
            (overlayManager as GlyphOverlayManager).addImage(result)
        }
    }

}
