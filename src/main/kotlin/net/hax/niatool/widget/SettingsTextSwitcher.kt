package net.hax.niatool.widget

import android.content.Context
import android.graphics.Typeface
import android.support.v4.view.GravityCompat
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.TextSwitcher
import android.widget.TextView
import net.hax.niatool.R

class SettingsTextSwitcher(context: Context, attrs: AttributeSet?) : TextSwitcher(context, attrs) {

    constructor(context: Context) : this(context, null)

    init {
        inAnimation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
        outAnimation = AnimationUtils.loadAnimation(context, android.R.anim.slide_out_right)
        setFactory(this::createSwitcherView)
    }

    private fun createSwitcherView(): View {
        val bgTypedValue = TypedValue()
        context.theme.resolveAttribute(
                R.attr.selectableItemBackgroundBorderless, bgTypedValue, true)

        return TextView(context).apply {
            setBackgroundResource(bgTypedValue.resourceId) // Touch ripple
            setTypeface(typeface, Typeface.ITALIC)
            ellipsize = TextUtils.TruncateAt.END
            maxLines = 1
            layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.MATCH_PARENT, GravityCompat.END)
            gravity = GravityCompat.END
        }
    }

}
