package com.focusgate.domain

import com.focusgate.domain.detect.*
import com.focusgate.domain.model.*
import com.focusgate.domain.repo.*
import com.focusgate.domain.rules.*
import com.focusgate.domain.usecase.InterruptCoordinator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CoordinatorTest {
    private var now = 0L
    private var homes = 0
    private val shown = mutableListOf<Clip?>()
    private var answer: InterruptResult = InterruptResult.GoBack

    private val blocklist = object : BlocklistRepository {
        override val blockedApps = flowOf(listOf(BlockedApp("a", "A")))
        override suspend fun setBlocked(app: BlockedApp, blocked: Boolean) {}
    }
    private val clipRepo = object : ClipRepository {
        override val clips = flowOf(listOf(Clip(1, "u", "t")))
        override suspend fun add(uri: String, title: String) {}
        override suspend fun remove(id: Long) {}
        override suspend fun lastPlayedId(): Long? = null
        override suspend fun setLastPlayed(id: Long) {}
    }
    private val settings = object : SettingsRepository {
        override val interruptConfig = flowOf(InterruptConfig())
        override suspend fun update(config: InterruptConfig) {}
    }
    private val coordinator = InterruptCoordinator(
        source = object : DetectionSource { override val events = emptyFlow<DetectionEvent>() },
        engine = DefaultRuleEngine(listOf(Rule("r", Trigger.AppOpened(), listOf(Condition.Cooldown(60_000)), Action.PlayClip()))),
        blocklist = blocklist, clips = clipRepo, settings = settings,
        presenter = object : InterruptPresenter {
            override suspend fun present(packageName: String, clip: Clip?, config: InterruptConfig): InterruptResult {
                shown += clip; return answer
            }
        },
        home = { homes++ },
        clock = { now },
    )

    @Test fun goBackSendsHome() = runTest {
        coordinator.handle(DetectionEvent.ForegroundAppChanged("a"))
        assertEquals(1, homes); assertEquals(1, shown.size)
    }

    @Test fun continueStartsCooldown() = runTest {
        answer = InterruptResult.Continue("work")
        coordinator.handle(DetectionEvent.ForegroundAppChanged("a"))
        now = 10_000
        coordinator.handle(DetectionEvent.ForegroundAppChanged("a"))
        assertEquals(1, shown.size); assertEquals(0, homes)
        now = 61_000
        coordinator.handle(DetectionEvent.ForegroundAppChanged("a"))
        assertEquals(2, shown.size)
    }

    @Test fun otherAppsIgnored() = runTest {
        coordinator.handle(DetectionEvent.ForegroundAppChanged("b"))
        assertEquals(0, shown.size)
    }
}
