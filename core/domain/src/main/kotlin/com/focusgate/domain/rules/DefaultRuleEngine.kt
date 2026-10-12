package com.focusgate.domain.rules

import com.focusgate.domain.detect.DetectionEvent

class DefaultRuleEngine(private val rules: List<Rule>) : RuleEngine {

    override fun evaluate(event: DetectionEvent, ctx: EvalContext): List<Action> =
        rules.filter { matches(it.trigger, event, ctx) && it.conditions.all { c -> holds(c, event, ctx) } }
            .map { it.action }

    private fun matches(trigger: Trigger, event: DetectionEvent, ctx: EvalContext): Boolean =
        when (trigger) {
            is Trigger.AppOpened -> event is DetectionEvent.ForegroundAppChanged &&
                event.packageName in ctx.blockedPackages &&
                (trigger.packageName == null || trigger.packageName == event.packageName)
        }

    private fun holds(condition: Condition, event: DetectionEvent, ctx: EvalContext): Boolean =
        when (condition) {
            is Condition.Cooldown -> {
                val pkg = (event as? DetectionEvent.ForegroundAppChanged)?.packageName
                val last = pkg?.let { ctx.lastContinueMs[it] }
                last == null || ctx.nowMs - last >= condition.ms
            }
        }
}
