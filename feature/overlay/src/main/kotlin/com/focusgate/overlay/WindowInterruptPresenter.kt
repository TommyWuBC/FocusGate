package com.focusgate.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.focusgate.domain.detect.InterruptPresenter
import com.focusgate.domain.detect.InterruptResult
import com.focusgate.domain.model.Clip
import com.focusgate.domain.model.InterruptConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/** Shows [InterruptScreen] in a full-screen TYPE_APPLICATION_OVERLAY window. Needs "display over other apps". */
@Singleton
class WindowInterruptPresenter @Inject constructor(
    @ApplicationContext private val context: Context,
) : InterruptPresenter {

    override suspend fun present(packageName: String, clip: Clip?, config: InterruptConfig): InterruptResult =
        withContext(Dispatchers.Main.immediate) {
            val wm = context.getSystemService(WindowManager::class.java)
            val result = CompletableDeferred<InterruptResult>()
            val owner = OverlayOwner()
            val label = appLabel(packageName)
            val view = ComposeView(context).apply {
                // Cover the app instantly; the first Compose frame can take seconds on a cold start.
                setBackgroundColor(android.graphics.Color.BLACK)
                setViewTreeLifecycleOwner(owner)
                setViewTreeSavedStateRegistryOwner(owner)
                setViewTreeViewModelStoreOwner(owner)
                setContent { InterruptScreen(label, clip, config) { result.complete(it) } }
            }
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT,
            )
            owner.start()
            wm.addView(view, params)
            try {
                result.await()
            } finally {
                owner.stop()
                wm.removeView(view)
            }
        }

    private fun appLabel(pkg: String): String = try {
        val pm = context.packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
    } catch (_: Exception) { pkg }
}

private class OverlayOwner : LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {
    private val registry = LifecycleRegistry(this)
    private val savedState = SavedStateRegistryController.create(this)
    override val viewModelStore = ViewModelStore()
    override val lifecycle: Lifecycle get() = registry
    override val savedStateRegistry: SavedStateRegistry get() = savedState.savedStateRegistry

    fun start() {
        savedState.performRestore(null)
        registry.currentState = Lifecycle.State.RESUMED
    }

    fun stop() {
        registry.currentState = Lifecycle.State.DESTROYED
        viewModelStore.clear()
    }
}
