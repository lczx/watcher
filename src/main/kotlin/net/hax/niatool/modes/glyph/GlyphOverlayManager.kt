package net.hax.niatool.modes.glyph;

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import net.hax.niatool.OverlayServiceUtil
import net.hax.niatool.R
import net.hax.niatool.overlay.ControlPanelOverlay2
import net.hax.niatool.overlay.OverlayViewManager

class GlyphOverlayManager(context: Context) : OverlayViewManager(context) {

    companion object {
        private const val TAG = "GlyphOverlayManager"

        private val LAYOUT_PARAMS_IMAGE_OVERLAY = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
            flags = OverlayViewManager.LAYOUT_FLAGS_DEFAULT
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.CENTER
        }
    }

    private var imageOverlay: ImageOverlay? = null

    fun addImage(image: Bitmap) {
        imageOverlay!!.addImage(image)
        shotToast.setText(context.getString(R.string.toast_shot_taken, imageOverlay!!.imageCount))
        shotToast.show()
    }

    override fun onProjectionStart() {
        super.onProjectionStart()
        imageOverlay = ImageOverlay(context)
        windowManager.addView(imageOverlay!!.viewport, LAYOUT_PARAMS_IMAGE_OVERLAY)
    }

    override fun onProjectionStop() {
        super.onProjectionStop()
        imageOverlay!!.recycleAll()
        windowManager.removeView(imageOverlay!!.viewport)
        imageOverlay = null
    }

    override fun configureControlOverlay(controlOverlay: ControlPanelOverlay2) {
        controlOverlay.onSceneChangeListener = this::onControlSceneChanged

        controlOverlay.addScene(ControlCaptureScene { OverlayServiceUtil.captureScreen() })
        controlOverlay.addScene(ControlBrowseScene(this::onImageBrowseBack, this::onImageBrowseForward))

        controlOverlay.switchScene(ControlCaptureScene::class.java)
    }

    private fun onControlSceneChanged(scene: ControlPanelOverlay2.Scene?) {
        val inBrowseMode = scene is ControlBrowseScene

        imageOverlay!!.visible = inBrowseMode
        if (!inBrowseMode) imageOverlay!!.recycleAll()

        // Allow overlay to get hardware key events in browse mode
        LAYOUT_PARAMS_CONTROL_OVERLAY.flags = if (inBrowseMode) LAYOUT_FLAGS_FOCUSABLE else LAYOUT_FLAGS_DEFAULT
        windowManager.updateViewLayout(controlOverlay!!.viewport, LAYOUT_PARAMS_CONTROL_OVERLAY)
    }

    private fun onImageBrowseBack() {
        if (imageOverlay?.viewport?.visibility != View.VISIBLE)
            Log.w(TAG, "Flow warning: should not be possible to browse if control & image overlays are hidden")
        imageOverlay?.previousImage()
    }

    private fun onImageBrowseForward() {
        if (imageOverlay?.viewport?.visibility != View.VISIBLE)
            Log.w(TAG, "Flow warning: should not be possible to browse if control & image overlays are hidden")
        imageOverlay?.nextImage()
    }

}
