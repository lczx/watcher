package net.hax.niatool.widget

import android.content.Context
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Region
import android.support.annotation.AttrRes
import android.support.annotation.StyleRes
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.FrameLayout
import net.hax.niatool.R

/**
 * A [FrameLayout] that binds [Regions][Region] to its children; Touch events are dispatched to the first view whose
 * region contains the touch point.
 *
 * Regions are defined by strings in a resource string array passed as the `dispatcherRegions` attribute. The index
 * of the string in the array is the index of the bound view in the layout. The string follows the same format as
 * SVG `d` paths or Android vector resources `pathData`.
 */
class DispatcherLayout(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int, @StyleRes defStyleRes: Int)
    : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?)
            : this(context, attrs, 0)

    constructor(context: Context)
            : this(context, null)

    companion object {
        private val TAG = "DispatcherLayout"
        private val REFLECT_PATH_PARSER_CLASS = "android.util.PathParser"
        private val REFLECT_PATH_PARSER_METHOD = "createPathFromPathData"
        // We are using this instead of a styleable because there are no reasons to make this styleable
        private val CUSTOM_ATTRS = intArrayOf(R.attr.dispatcherRegions)
    }

    // This is only used by ControlPanelOverlay to listen on volume controls, can return null to invoke super
    var onDispatchKeyHandler: ((event: KeyEvent?) -> Boolean?)? = null

    private val mDensity = resources.displayMetrics.density
    private val mRegions: List<Region>
    private var mCurrentGestureViewIdx: Int = -1

    init {
        if (attrs == null)
            throw IllegalArgumentException("DispatcherLayout requires a dispatcherRegions attribute to work")

        val data = context.obtainStyledAttributes(attrs, CUSTOM_ATTRS, defStyleAttr, defStyleRes)
        val paths = data.getTextArray(0) ?:
                throw IllegalArgumentException("DispatcherLayout requires a dispatcherRegions attribute to work")
        data.recycle()

        mRegions = parsePaths(paths)
    }

    private fun parsePaths(paths: Array<out CharSequence>): List<Region> {
        val createPathMethod = try {
            Class.forName(REFLECT_PATH_PARSER_CLASS).getMethod(REFLECT_PATH_PARSER_METHOD, String::class.java)
        } catch (e: ReflectiveOperationException) {
            throw UnsupportedOperationException(
                    "Could not find $REFLECT_PATH_PARSER_CLASS#$REFLECT_PATH_PARSER_METHOD(String) through reflection", e)
        }

        return paths.map {
            val path = try {
                createPathMethod.invoke(null, it) as Path
            } catch (e: ReflectiveOperationException) {
                throw UnsupportedOperationException("Reflection exception while parsing a path", e)
            }

            Region().apply {
                val pb = RectF()
                path.computeBounds(pb, true)
                setPath(path, Region(pb.left.toInt(), pb.top.toInt(), pb.right.toInt(), pb.bottom.toInt()))
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        val actionMasked = ev!!.action and MotionEvent.ACTION_MASK
        val posX = (ev.x / mDensity).toInt()
        val posY = (ev.y / mDensity).toInt()

        // If we had a gesture going on and we exited its region, we cancel the gesture
        if (mCurrentGestureViewIdx != -1 && !mRegions[mCurrentGestureViewIdx].contains(posX, posY)) {
            val prevAction = ev.action
            ev.action = MotionEvent.ACTION_CANCEL
            val target = getChildAt(mCurrentGestureViewIdx)
            target.requestFocusFromTouch()
            target.dispatchTouchEvent(ev)
            ev.action = prevAction
            mCurrentGestureViewIdx = -1
        }

        // Find the first region containing the event point - if we have no region, return
        val idx = mRegions.indexOfFirst { it.contains(posX, posY) }
        if (idx == -1) {
            if (actionMasked != MotionEvent.ACTION_MOVE) // Do not spam this too
                Log.d(TAG, "No region covering touch area ($posX, $posY), ignoring")
            return true
        }

        if (idx >= childCount) {
            Log.w(TAG, "Region #$idx covers touch area ($posX, $posY), but no matching view exists")
            return true
        }

        // Adjust event offsets for the child view and dispatch
        mCurrentGestureViewIdx = idx
        val view = getChildAt(idx)
        val offsetX = (scrollX - view.left).toFloat()
        val offsetY = (scrollY - view.top).toFloat()
        ev.offsetLocation(offsetX, offsetY)
        view.requestFocusFromTouch()
        view.dispatchTouchEvent(ev)
        ev.offsetLocation(-offsetX, -offsetY)
        return true
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount > mRegions.size)
            Log.w(TAG, "View count is greater than region count, exceeding views will always be ignored")
        else if (childCount < mRegions.size)
            Log.w(TAG, "View count is lower than region count, exceeding paths will always be ignored")
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean =
            onDispatchKeyHandler?.invoke(event) ?: super.dispatchKeyEvent(event)

}
