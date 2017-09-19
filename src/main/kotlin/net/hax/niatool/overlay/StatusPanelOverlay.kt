package net.hax.niatool.overlay

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import net.hax.niatool.MainActivity

class StatusPanelOverlay(context: Context, private val armedStatusListener: OnArmedStatusListener? = null) {

    companion object {
        private val SIZE_BACKGROUND_INNER_WIDTH_DIP = 84f
        private val SIZE_TEXT_DIP = 12f
        private val SIZE_TEXT_LETTER_SPACING_DIP = 0.04f
        private val SIZE_TEXT_LOCATION_Y_DIP = 16f
        private val COLOR_BACKGROUND = 0x70000000
        private val COLOR_FOREGROUND = 0xffdddddd.toInt()
    }

    val viewport: View = StatusView(context)

    var armed = false
        set(value) {
            if (field == value) return
            field = value
            viewport.postInvalidate() // Like invalidate, but thread-safe; no harm if called from strange services
            armedStatusListener?.onArmedStatusChange(value)
        }

    interface OnArmedStatusListener {
        fun onArmedStatusChange(armed: Boolean)
    }

    inner class StatusView(context: Context) : View(context) {

        // We want to use dp instead of sp also for the text
        // and ignore system font size because the status bar is always the same height
        private val dp = resources.displayMetrics.density

        private val mStatusBarHeight = run {
            val resId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resId > 0) resources.getDimensionPixelSize(resId) else 0
        }
        private val mTextPaint = Paint().apply {
            isAntiAlias = true
            textSize = SIZE_TEXT_DIP * dp
            color = COLOR_FOREGROUND
            textAlign = Paint.Align.CENTER
            letterSpacing = SIZE_TEXT_LETTER_SPACING_DIP * dp
        }
        private val mBgPaint = Paint().apply {
            isAntiAlias = true
            color = COLOR_BACKGROUND
        }
        private val mBgPath = Path().apply {
            lineTo(mStatusBarHeight.toFloat(), mStatusBarHeight.toFloat())
            lineTo(SIZE_BACKGROUND_INNER_WIDTH_DIP * dp + mStatusBarHeight, mStatusBarHeight.toFloat())
            lineTo(SIZE_BACKGROUND_INNER_WIDTH_DIP * dp + mStatusBarHeight * 2, 0f)
        }

        init {
            setWillNotDraw(false)
            setOnClickListener { armed = !armed }
            setOnLongClickListener {
                OverlayViewManager.launchActivityFromOverlay(
                        context, MainActivity::class.java, Intent.FLAG_ACTIVITY_NEW_TASK); true
            }
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawPath(mBgPath, mBgPaint)
            canvas.drawText(if (armed) "⚡ ARMED" else "\uD83E\uDD18 READY",
                    SIZE_BACKGROUND_INNER_WIDTH_DIP * dp / 2 + mStatusBarHeight,
                    SIZE_TEXT_LOCATION_Y_DIP * dp, mTextPaint)
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val width = 2 * mStatusBarHeight + (SIZE_BACKGROUND_INNER_WIDTH_DIP * dp).toInt()
            setMeasuredDimension(width, mStatusBarHeight)
        }

    }

}
