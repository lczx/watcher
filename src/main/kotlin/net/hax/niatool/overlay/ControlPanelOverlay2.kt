package net.hax.niatool.overlay

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import net.hax.niatool.ApplicationSettings
import net.hax.niatool.R
import net.hax.niatool.onEnd
import net.hax.niatool.widget.DispatcherLayout
import java.util.*

class ControlPanelOverlay2(private val context: Context) {

    val uiHandler = Handler(Looper.getMainLooper())
    private val animOut = AnimationUtils.loadAnimation(context, R.anim.overlay_ctrl_slide_out)
    private val animIn = AnimationUtils.loadAnimation(context, R.anim.overlay_ctrl_slide_in)

    private val mScenes = LinkedList<Scene>()
    private val inflatedSceneViews = HashMap<Int, View>()
    val viewport: ViewGroup = FrameLayout(context)

    private var currentScene: Scene? = null

    var onSceneChangeListener: ((Scene) -> Unit)? = null

    fun addScene(scene: Scene) {
        val sceneView = createSceneView(scene.layoutId)
        bindSceneListeners(scene, sceneView)

        inflatedSceneViews[scene.layoutId] = sceneView
        mScenes.add(scene)
    }

    fun switchScene(sceneClass: Class<out Scene>) {
        val targetScene = mScenes.find { it::class.java == sceneClass }!!
        if (currentScene == targetScene) return

        uiHandler.post {
            val targetSceneView = inflatedSceneViews[targetScene.layoutId]!!
            displayOnViewport(targetSceneView, ApplicationSettings.animationsEnabled)

            currentScene = targetScene
            onSceneChangeListener?.invoke(targetScene)
        }
    }

    private fun displayOnViewport(nextView: View, animate: Boolean) {
        fun showNext() {
            viewport.removeAllViews()
            viewport.addView(nextView)
            if (animate) nextView.startAnimation(animIn)
            nextView.requestFocus()
        }

        if (animate && currentScene != null) {
            animOut.onEnd { showNext() }
            viewport.getChildAt(0).startAnimation(animOut)
        } else {
            showNext()
        }

    }

    private fun createSceneView(@LayoutRes res: Int): View {
        // tip: use inflater.cloneInContext(ctx) if we want to change themes here on the fly
        val systemInflater = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
        return systemInflater.inflate(res, null)
    }

    private fun bindSceneListeners(scene: Scene, sceneView: View) {
        for (buttonId in scene.buttonIds) {
            with(sceneView.findViewById(buttonId)) {
                setOnClickListener { v -> scene.onButtonClick(v.id, this@ControlPanelOverlay2) }
                if (scene.supportsLongClick(buttonId))
                    setOnLongClickListener { v -> scene.onLongButtonClick(v.id, this@ControlPanelOverlay2) }
            }
            if (scene.supportsDispatcherKeyEvents()) {
                (sceneView as DispatcherLayout).onDispatchKeyHandler = { keyEvent ->
                    scene.onDispatchKeyHandler(keyEvent, this@ControlPanelOverlay2)
                }
            }
        }
    }

    abstract class Scene(@LayoutRes val layoutId: Int) {

        abstract val buttonIds: List<Int>

        open fun supportsLongClick(@IdRes buttonId: Int) = false

        open fun supportsDispatcherKeyEvents() = false

        abstract fun onButtonClick(@IdRes buttonId: Int, controller: ControlPanelOverlay2)

        open fun onLongButtonClick(@IdRes buttonId: Int, controller: ControlPanelOverlay2): Boolean =
                false /* no-op, did not consume */

        open fun onDispatchKeyHandler(event: KeyEvent?, controller: ControlPanelOverlay2) : Boolean? =
                false /* no-op, did not consume */

    }

}
