package com.focusgate.app

import com.focusgate.detection.AccessibilityBridge
import com.focusgate.domain.detect.*
import com.focusgate.domain.model.Clip
import com.focusgate.domain.repo.*
import com.focusgate.domain.rules.*
import com.focusgate.domain.usecase.InterruptCoordinator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Composition root for the interrupt flow. v1 ships exactly one rule: blocked app opened -> play clip. */
@Module
@InstallIn(SingletonComponent::class)
object CoordinatorModule {
    private const val COOLDOWN_AFTER_CONTINUE_MS = 60_000L

    @Provides @Singleton
    fun ruleEngine(): RuleEngine = DefaultRuleEngine(
        listOf(
            Rule(
                id = "blocked-app-opened",
                trigger = Trigger.AppOpened(),
                conditions = listOf(Condition.Cooldown(COOLDOWN_AFTER_CONTINUE_MS)),
                action = Action.PlayClip(),
            ),
        ),
    )

    @Provides @Singleton
    fun coordinator(
        bridge: AccessibilityBridge,
        engine: RuleEngine,
        blocklist: BlocklistRepository,
        clips: ClipRepository,
        settings: SettingsRepository,
        presenter: InterruptPresenter,
    ) = InterruptCoordinator(
        source = bridge,
        engine = engine,
        blocklist = blocklist,
        clips = clips,
        settings = settings,
        presenter = presenter,
        home = bridge,
        clock = { System.currentTimeMillis() },
        fallbackClip = Clip(id = 0, uri = "asset:///sample.mp4", title = "Sample"),
    )
}
