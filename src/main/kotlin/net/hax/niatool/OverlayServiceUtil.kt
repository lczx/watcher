package net.hax.niatool

import android.content.Context
import android.content.Intent
import android.os.Message

object OverlayServiceUtil {

    fun createStartIntent(context: Context, modeId: String): Intent =
            Intent(context, OverlayService::class.java).setAction(OverlayService.ACTION_START)
                    .putExtra(OverlayService.EXTRA_MODE_ID, modeId)

    fun createStopIntent(context: Context): Intent =
            Intent(context, OverlayService::class.java).setAction(OverlayService.ACTION_STOP)

    fun setArmed(armed: Boolean) =
            sendMessageToService(OverlayService.MESSAGE_ARMED_STATE_CHANGED, armed)

    fun setMediaProjectionIntent(data: Intent?) =
            sendMessageToService(OverlayService.MESSAGE_SET_MEDIA_PROJECTION_INTENT, data)

    fun captureScreen() =
            OverlayService.handler.sendEmptyMessage(OverlayService.MESSAGE_CAPTURE_SCREEN)

    private fun sendMessageToService(code: Int, data: Any?) =
            OverlayService.handler.sendMessage(Message.obtain(OverlayService.handler, code, data))

}
