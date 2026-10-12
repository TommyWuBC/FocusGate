# FocusGate: Android distraction interrupter

NOTE: "FocusGate" is a placeholder name. Do not hardcode it into anything hard to change later. Keep the app name, package name, and branding strings centralized (one place for the display name, one for the applicationId) so a rename is a small change.

## Goal
Personal Android app that interrupts distracting app use with a motivational video. Like an app blocker, but the block screen plays a short clip from a curated library.

## Scope: v1 vs. later
- **v1 (ship this first):** a simple app blocker. When the user opens a blocklisted app, a full-screen overlay plays a short motivational clip, then lets them go back home. That is milestones 1 to 7 below.
- **Later (likely, if v1 works well):** feed-level detection (Reels/Shorts/TikTok vs. DMs), per-app rules, schedules, stats, and potentially many more features and possibly public release. Treat v1 as the foundation of a much larger app, not a throwaway prototype. The user expects large refactors and many feature additions, so architecture matters from day one (see next section).

## Decisions already made
- Platform: Android only for v1. User's phone is Android. iOS is out of scope (Screen Time API can't play video on the shield, and can't detect in-app screens).
- Distribution: sideloaded APK for personal use, no Play Store. Revisit publishing only if it works well. Publishing would trigger Google's accessibility-service review, so keep that in mind before any design choice that depends on it.
- Language/tooling: Kotlin, Android Studio or Gradle CLI, install via `adb install`.
- Video source: a library the user curates (local files or links they pick). No automatic scraping of reels.
- Detection: AccessibilityService reading the foreground app and UI tree.
- Interrupt UI: full-screen overlay using the "display over other apps" permission.

## Architecture requirements (expansion-ready)
Build v1 small in features but not in structure. Before writing milestone 1, propose the architecture and package layout, and get the user's approval.
- Use a clean, layered architecture: UI layer (Jetpack Compose, ViewModels), domain layer (use cases, pure Kotlin, no Android imports), data layer (repositories over Room/DataStore). Dependencies point inward.
- Consider a multi-module Gradle setup from the start (for example app, core-domain, core-data, feature-blocker, feature-overlay, service-detection), or at minimum strict package boundaries that can be split into modules later. Explain the tradeoff and recommend one.
- Isolate the AccessibilityService behind an interface. The service should only emit events (foreground app changed, screen/feed changed) into the domain layer. Blocking logic must not live inside the service class, so detection and decision-making can change independently.
- Make "what triggers an interrupt" and "what the interrupt does" pluggable. Model them as a rule engine: Trigger (app opened, feed entered, schedule, usage threshold) plus Condition (time of day, cooldown, per-app settings) plus Action (play clip, show reason prompt, hard block, do nothing). v1 implements one trigger and one action, but the types should allow adding more without rewriting existing code.
- Keep feed-detection rules as data (config/JSON or a table), versioned, so they can be updated when Instagram/TikTok/YouTube change their UI without code changes everywhere.
- Dependency injection (Hilt or Koin), a single source of truth per piece of state, and unidirectional data flow in the UI.
- Version the local database schema and write migrations from the start. Do not use destructive migrations.
- Tests: unit tests for the domain layer and rule engine from the beginning. Detection and overlay logic should be testable without a device where possible.
- Add a lightweight architecture doc (ARCHITECTURE.md) and keep it updated as decisions are made. Record major decisions with a short reason so future refactors have context.
- Prefer boring, widely used libraries. Avoid clever abstractions that only serve v1.

## Known gotchas
- Android 13+ blocks sideloaded apps from enabling accessibility services by default. One-time fix: Settings > Apps > this app > menu > Allow restricted settings, then enable the service.
- Instagram/TikTok/YouTube change their UI often, so feed-detection rules (view IDs, content descriptions) will break occasionally. Keep detection rules in a config file or table, not scattered in code, so they are easy to re-tune.
- Aggressive battery optimization on some OEMs kills accessibility services. Document a whitelist step.

## Design note to evaluate
A motivational short-form clip may feed the same habit it is meant to break. Build the interrupt as a short clip (about 5 seconds) followed by a friction step, for example "What are you opening this for?" with options like "Go back" and "Continue anyway (30s delay)". Make clip length and the friction step configurable so the user can test what actually works.

## Milestones (small, independently testable tickets)
Milestones 1 to 7 are v1. Milestones 8 to 11 are post-v1 and should only start after the user has used v1 for a while. Milestone 0 comes first.
0. Architecture proposal: module/package layout, rule-engine types, DI choice, and ARCHITECTURE.md. No feature code yet. Wait for user approval.
1. Project scaffold: Kotlin app, single activity, permissions onboarding screen (overlay + accessibility), builds and installs via adb.
2. AccessibilityService that logs the foreground package name when it changes. Verify on device.
3. Blocklist: user picks which apps are targeted (stored locally). Interrupt triggers on app open for those apps.
4. Overlay: full-screen overlay with a placeholder message and a "go home" button. Verify it appears over a target app.
5. Video playback in the overlay (ExoPlayer/Media3) from a bundled sample clip.
6. Video library: user adds clips from device storage or links, app picks one at random, avoids immediate repeats.
7. Friction step after the clip (reason prompt, delayed "continue" button), with configurable durations.
8. Feed-level detection for Instagram Reels: inspect the UI tree to detect the Reels viewer vs. DMs/home. Log candidate view IDs first, then write the rule.
9. Same for YouTube Shorts and TikTok. Detection rules live in a config.
10. Settings: cooldown after "continue anyway", per-app rules, simple stats (interrupts per day, how often the user went back vs. continued).
11. Hardening: battery optimization guidance, service restart handling, crash logging.

## Working agreement for the coding agent
- Do one milestone at a time. After each, build, run on device or emulator where possible, commit, and update HANDOFF.md with status and next step.
- Ask before adding any dependency beyond the standard Android/Jetpack stack and Media3.
- Flag anything that would not survive Play Store review so the user can decide whether it matters.

## Open questions for the user
- Which apps first (Instagram, TikTok, YouTube, others)?
- Clip source for v1: downloaded files or links?
- Should "continue anyway" ever be allowed, or is the interrupt a hard block during certain hours?
