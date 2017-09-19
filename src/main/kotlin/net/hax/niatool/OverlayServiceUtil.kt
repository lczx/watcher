package net.hax.niatool

import android.media.projection.MediaProjection
import android.os.Message

object OverlayServiceUtil {

    fun setArmed(armed: Boolean) =
            sendMessageToService(OverlayService.MESSAGE_ARMED_STATE_CHANGED, armed)

    fun setMediaProjection(mediaProjection: MediaProjection?) =
            sendMessageToService(OverlayService.MESSAGE_SET_MEDIA_PROJECTION, mediaProjection)

    private fun sendMessageToService(code: Int, data: Any?) =
            OverlayService.handler.sendMessage(Message.obtain(OverlayService.handler, code, data))

}
