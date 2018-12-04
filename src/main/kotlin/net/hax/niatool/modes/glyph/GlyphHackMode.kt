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

    override fun getModeMetadata(res: Resources) = ModeRegistry.Info(
            name = res.getString(R.string.mode_glyph_name),
            description = res.getString(R.string.mode_glyph_description))

    override fun createOverlayManager(context: Context) = GlyphOverlayManager(context)

    override fun makeCaptureProcessTask(overlayManager: OverlayViewManager) = ResizeAndShowTask(overlayManager)

    class ResizeAndShowTask(private val overlayManager: OverlayViewManager) : ScreenCaptureTask<Void, Bitmap>() {
        override fun processCaptureBackground(image: Image, capture: Bitmap) =
                Bitmap.createBitmap(capture, 0, (image.height - image.width) / 2, image.width, image.width)!!

        override fun onPostExecute(result: Bitmap) {
            (overlayManager as GlyphOverlayManager).addImage(result)
        }
    }

}
