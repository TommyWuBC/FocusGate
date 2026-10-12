package com.focusgate.domain

import com.focusgate.domain.detect.DetectionEvent
import com.focusgate.domain.model.InterruptConfig
import com.focusgate.domain.rules.*
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.Assert.assertTrue

class RuleEngineTest {
    private val play = Action.PlayClip(InterruptConfig())
    private val engine = DefaultRuleEngine(
        listOf(Rule("r", Trigger.AppOpened(), listOf(Condition.Cooldown(60_000)), play)),
    )
    private fun open(pkg: String) = DetectionEvent.ForegroundAppChanged(pkg)

    @Test fun firesForBlockedApp() {
        val ctx = EvalContext(nowMs = 0, blockedPackages = setOf("a"))
        assertEquals(listOf<Action>(play), engine.evaluate(open("a"), ctx))
    }

    @Test fun ignoresUnblockedApp() {
        val ctx = EvalContext(nowMs = 0, blockedPackages = setOf("a"))
        assertTrue(engine.evaluate(open("b"), ctx).isEmpty())
    }

    @Test fun cooldownSuppressesThenExpires() {
        val base = EvalContext(nowMs = 30_000, blockedPackages = setOf("a"), lastContinueMs = mapOf("a" to 0L))
        assertTrue(engine.evaluate(open("a"), base).isEmpty())
        assertEquals(1, engine.evaluate(open("a"), base.copy(nowMs = 60_000)).size)
    }
}
