package net.hax.niatool.overlay

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.support.annotation.LayoutRes
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import net.hax.niatool.ApplicationSettings
import net.hax.niatool.R
import net.hax.niatool.onEnd
import java.util.*

class ControlPanelOverlay2(private val context: Context) {

    private val uiHandler = Handler(Looper.getMainLooper())
    private val animOut = AnimationUtils.loadAnimation(context, R.anim.overlay_ctrl_slide_out)
    private val animIn = AnimationUtils.loadAnimation(context, R.anim.overlay_ctrl_slide_in)

    private val mScenes = LinkedList<Scene>()
    private val inflatedSceneViews = SparseArray<View>()
    private var currentScene: Scene? = null
    val viewport: ViewGroup = FrameLayout(context)
    var onSceneChangeListener: ((Scene) -> Unit)? = null

    fun addScene(scene: Scene) {
        val sceneView = createSceneView(scene.layoutId)
        scene.onCreateView(sceneView, this)
        //sceneView.isFocusableInTouchMode = true // May be required on 1st scene to gain focus on first show up

        inflatedSceneViews.append(scene.layoutId, sceneView)
        mScenes.add(scene)
    }

    fun switchScene(sceneClass: Class<out Scene>) {
        val targetScene = mScenes.find { it::class.java == sceneClass }!!
        if (currentScene == targetScene) return

        uiHandler.post {
            val targetSceneView = inflatedSceneViews.get(targetScene.layoutId)!!
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

    abstract class Scene(@LayoutRes val layoutId: Int) {
        abstract fun onCreateView(view: View, controller: ControlPanelOverlay2)
    }

}
