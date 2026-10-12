package com.focusgate.domain.usecase

import com.focusgate.domain.detect.*
import com.focusgate.domain.model.Clip
import com.focusgate.domain.repo.*
import com.focusgate.domain.rules.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

/**
 * Wires detection events -> rule engine -> actions. No Android types, so it is testable on the JVM.
 * Only one interrupt is shown at a time; events arriving meanwhile are dropped.
 */
class InterruptCoordinator(
    private val source: DetectionSource,
    private val engine: RuleEngine,
    private val blocklist: BlocklistRepository,
    private val clips: ClipRepository,
    private val settings: SettingsRepository,
    private val presenter: InterruptPresenter,
    private val home: HomeNavigator,
    private val clock: Clock,
    private val pickClip: PickClip = PickClip(),
    /** Played when the user's library is empty. */
    private val fallbackClip: Clip? = null,
) {
    private val lastContinueMs = mutableMapOf<String, Long>()
    @Volatile private var busy = false

    fun start(scope: CoroutineScope): Job = scope.launch {
        source.events.collect { event ->
            if (busy) return@collect
            busy = true
            launch {
                try { handle(event) } finally { busy = false }
            }
        }
    }

    internal suspend fun handle(event: DetectionEvent) {
        val ctx = EvalContext(
            nowMs = clock.nowMs(),
            blockedPackages = blocklist.blockedApps.first().map { it.packageName }.toSet(),
            lastContinueMs = lastContinueMs.toMap(),
        )
        val pkg = (event as? DetectionEvent.ForegroundAppChanged)?.packageName ?: return
        for (action in engine.evaluate(event, ctx)) when (action) {
            is Action.PlayClip -> {
                val library = clips.clips.first()
                val clip = pickClip(library, clips.lastPlayedId()) ?: fallbackClip
                if (clip != null && library.any { it.id == clip.id }) clips.setLastPlayed(clip.id)
                when (presenter.present(pkg, clip, action.config ?: settings.interruptConfig.first())) {
                    InterruptResult.GoBack -> home.goHome()
                    is InterruptResult.Continue -> lastContinueMs[pkg] = clock.nowMs()
                }
            }
        }
    }
}
