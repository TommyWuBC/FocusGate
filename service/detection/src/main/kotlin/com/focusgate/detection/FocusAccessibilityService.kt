package com.focusgate.detection

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager
import com.focusgate.domain.detect.DetectionEvent
import com.focusgate.domain.usecase.InterruptCoordinator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import javax.inject.Inject

/**
 * Only translates Android accessibility events into [DetectionEvent]s. No blocking logic lives here.
 */
@AndroidEntryPoint
class FocusAccessibilityService : AccessibilityService() {
    @Inject lateinit var bridge: AccessibilityBridge
    @Inject lateinit var coordinator: InterruptCoordinator

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var job: Job? = null
    private var lastPackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        bridge.homeAction = { performGlobalAction(GLOBAL_ACTION_HOME) }
        job?.cancel()
        job = coordinator.start(scope)
        Log.i(TAG, "connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName || pkg == lastPackage || pkg in IGNORED || isInputMethod(pkg)) return
        lastPackage = pkg
        Log.d(TAG, "foreground: $pkg")
        bridge.emit(DetectionEvent.ForegroundAppChanged(pkg))
    }

    /** Keyboards fire window events too; treating them as app switches would re-trigger the interrupt. */
    private fun isInputMethod(pkg: String): Boolean =
        getSystemService(InputMethodManager::class.java)?.enabledInputMethodList?.any { it.packageName == pkg } == true

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        bridge.homeAction = null
        scope.cancel()
        super.onDestroy()
    }

    private companion object {
        const val TAG = "FocusDetection"
        /** System surfaces that report window changes but are not "apps the user opened". */
        val IGNORED = setOf("com.android.systemui", "android")
    }
}
