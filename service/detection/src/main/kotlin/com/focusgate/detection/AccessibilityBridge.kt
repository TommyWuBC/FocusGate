package com.focusgate.detection

import com.focusgate.domain.detect.DetectionEvent
import com.focusgate.domain.detect.DetectionSource
import com.focusgate.domain.detect.HomeNavigator
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Process-wide link between the Android-managed service instance and the rest of the app. */
@Singleton
class AccessibilityBridge @Inject constructor() : DetectionSource, HomeNavigator {
    private val _events = MutableSharedFlow<DetectionEvent>(extraBufferCapacity = 16, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val events: SharedFlow<DetectionEvent> = _events

    @Volatile var homeAction: (() -> Unit)? = null

    fun emit(event: DetectionEvent) { _events.tryEmit(event) }
    override fun goHome() { homeAction?.invoke() }
}
