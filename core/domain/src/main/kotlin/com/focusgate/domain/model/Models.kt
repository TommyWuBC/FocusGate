package com.focusgate.domain.model

/** A user-curated clip. [uri] is a content:// or https:// URI, or an asset:// URI for bundled samples. */
data class Clip(val id: Long, val uri: String, val title: String)

data class BlockedApp(val packageName: String, val label: String)

/** Tunables for the interrupt flow. Durations in milliseconds. */
data class InterruptConfig(
    val clipLengthMs: Long = 5_000,
    val frictionEnabled: Boolean = true,
    val continueDelayMs: Long = 30_000,
)
