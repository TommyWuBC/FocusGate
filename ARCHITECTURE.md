# FocusGate architecture (PROPOSAL, awaiting approval)

Status: Milestone 0 draft. No feature code yet.

## Principles
Layered (UI -> domain <- data), dependencies point inward. Domain is pure Kotlin. The AccessibilityService only emits events; decisions live in the domain rule engine.

## Module layout (recommendation: multi-module from day one)
| Module | Contents | Depends on |
|---|---|---|
| `:app` | Application, Hilt root, MainActivity, nav, onboarding | all |
| `:core:domain` | Entities, rule engine, use cases, repository interfaces (pure Kotlin/JVM) | none |
| `:core:data` | Room DB + migrations, DataStore, repository impls | domain |
| `:core:branding` | Display name / applicationId constants, strings (single rename point) | none |
| `:feature:blocker` | Blocklist UI + ViewModels | domain |
| `:feature:overlay` | Overlay window, Media3 player, friction step | domain |
| `:service:detection` | AccessibilityService, `DetectionSource` impl, feed-rule loader | domain |

Tradeoff: multi-module costs ~1 extra day of Gradle setup and slower first sync, but enforces the boundaries mechanically (domain physically cannot import Android) and speeds incremental builds as the app grows. Single-module with strict packages is cheaper now but boundaries erode. Recommended: multi-module, with convention plugins in `build-logic/` to avoid repeated Gradle config.

## Rule engine (domain)
```kotlin
sealed interface Trigger   { AppOpened(pkg), FeedEntered(pkg, feedId), Schedule(...), UsageThreshold(...) }
sealed interface Condition { TimeOfDay(range), Cooldown(duration), PerAppSetting(...) }
sealed interface Action    { PlayClip(config), ShowReasonPrompt, HardBlock, DoNothing }
data class Rule(id, trigger, conditions: List<Condition>, action: Action)
interface RuleEngine { fun evaluate(event: DetectionEvent, ctx: Context): List<Action> }
```
v1 implements `AppOpened` + `PlayClip` (+ friction step as an Action sequence). Other variants exist as types but are unhandled until needed.

## Detection boundary
`DetectionSource` interface emits `DetectionEvent` (`ForegroundAppChanged`, later `ScreenChanged`/feed) as a Flow. The service implements it; nothing else. Feed rules are versioned JSON (`assets/feed_rules.json`, schema version field), loaded by a `FeedRuleRepository`.

## Other decisions
- DI: **Hilt** (standard, first-party-adjacent, good Compose/ViewModel support).
- UI: Compose, ViewModels expose a single `StateFlow<UiState>`, events flow up (UDF).
- Storage: Room (blocklist, clips, stats; exported schemas, explicit migrations, no destructive fallback) + DataStore (settings).
- Tests: JUnit5/Kotlin tests for domain + rule engine; fake `DetectionSource` for overlay logic.
- Dependencies beyond Jetpack/Media3: **none requested**. (Hilt, Room, KSP are Jetpack/standard; flagging Hilt/KSP for your OK.)

## Decision log
- 2026-10-11: Proposed multi-module + Hilt + Room. Reason: spec expects large refactors; mechanical boundaries pay off.
