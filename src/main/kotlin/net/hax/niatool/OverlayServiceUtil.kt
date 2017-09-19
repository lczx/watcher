package net.hax.niatool

import android.os.Message

object OverlayServiceUtil {

    fun setArmed(armed: Boolean) =
            sendMessageToService(OverlayService.MESSAGE_ARMED_STATE_CHANGED, armed)

    private fun sendMessageToService(code: Int, data: Any?) =
            OverlayService.handler.sendMessage(Message.obtain(OverlayService.handler, code, data))

}
