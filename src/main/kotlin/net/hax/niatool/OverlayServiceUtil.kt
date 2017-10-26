package net.hax.niatool

import android.content.Intent
import android.os.Message

object OverlayServiceUtil {

    fun setArmed(armed: Boolean) =
            sendMessageToService(OverlayService.MESSAGE_ARMED_STATE_CHANGED, armed)

    fun setMediaProjectionIntent(data: Intent?) =
            sendMessageToService(OverlayService.MESSAGE_SET_MEDIA_PROJECTION_INTENT, data)

    fun captureScreen() =
            OverlayService.handler.sendEmptyMessage(OverlayService.MESSAGE_CAPTURE_SCREEN)

    private fun sendMessageToService(code: Int, data: Any?) =
            OverlayService.handler.sendMessage(Message.obtain(OverlayService.handler, code, data))

}
