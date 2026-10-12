package com.focusgate.domain.detect

import kotlinx.coroutines.flow.Flow

/** Events emitted by whatever observes the device. Decision-making happens elsewhere. */
sealed interface DetectionEvent {
    data class ForegroundAppChanged(val packageName: String) : DetectionEvent
    // Later: data class FeedEntered(val packageName: String, val feedId: String) : DetectionEvent
}

interface DetectionSource {
    val events: Flow<DetectionEvent>
}
