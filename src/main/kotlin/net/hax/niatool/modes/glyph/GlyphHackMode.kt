package net.hax.niatool.modes.glyph

import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import net.hax.niatool.modes.OperationMode

class GlyphHackMode : OperationMode {

    override fun createOverlayManager(context: Context) = GlyphOverlayManager(context)

    override fun processCaptureBackground(image: Image, capture: Bitmap) =
            Bitmap.createBitmap(capture, 0, (image.height - image.width) / 2, image.width, image.width)!!

}
