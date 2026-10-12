package com.focusgate.domain.rules

import com.focusgate.domain.detect.DetectionEvent
import com.focusgate.domain.model.InterruptConfig

sealed interface Trigger {
    /** Fires when the given app (or any blocklisted app, if [packageName] is null) comes to the foreground. */
    data class AppOpened(val packageName: String? = null) : Trigger
    // Later: FeedEntered, Schedule, UsageThreshold
}

sealed interface Condition {
    data class Cooldown(val ms: Long) : Condition
    // Later: TimeOfDay, PerAppSetting
}

sealed interface Action {
    data class PlayClip(val config: InterruptConfig) : Action
    // Later: ShowReasonPrompt, HardBlock, DoNothing
}

data class Rule(
    val id: String,
    val trigger: Trigger,
    val conditions: List<Condition>,
    val action: Action,
)

/** Everything the engine needs to decide, supplied by the caller so evaluation stays pure. */
data class EvalContext(
    val nowMs: Long,
    val blockedPackages: Set<String>,
    /** Last time an interrupt was dismissed with "continue", per package. */
    val lastContinueMs: Map<String, Long> = emptyMap(),
)

interface RuleEngine {
    fun evaluate(event: DetectionEvent, ctx: EvalContext): List<Action>
}
