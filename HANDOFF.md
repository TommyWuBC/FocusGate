# HANDOFF
## Done
Milestones 0-7 implemented: scaffold, accessibility foreground detection, blocklist, full-screen overlay, Media3 playback (bundled `sample.mp4`), user clip library (device files via document picker or https links, random pick avoiding immediate repeat), friction step (reason chips, delayed Continue) with configurable clip length / delay in Settings. Domain unit tests (rule engine, coordinator, clip picking) pass.
Verified on emulator (Medium_Phone_API_37.0): service connects, blocked app triggers overlay, friction countdown works, "Go back" returns home.

## Not verified / known issues
- Video playback is unverified: the emulator's software H.264 decoder (`c2.goldfish.h264.decoder`) throws on every clip. Test on the real phone.
- Overlay takes several seconds to appear on a cold emulator (first Compose + ExoPlayer init); an instant black backdrop hides the app meanwhile. Consider pre-warming if it is also slow on device.
- Overlay does not cover the navigation bar area.
- Clip phase has no skip; friction step has no persisted "reason" yet (reason is passed to the coordinator and dropped).

## Next
Install on the phone and use v1 for a while, then decide on milestones 8+ (do not start until asked).

## Dev notes
- Build: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"; ./gradlew :app:assembleDebug` (system Java 27 is too new for AGP).
- Emulator: `emulator -avd Medium_Phone_API_37.0`; enable service via `adb shell settings put secure enabled_accessibility_services com.focusgate.app/com.focusgate.detection.FocusAccessibilityService`, overlay via `adb shell appops set com.focusgate.app SYSTEM_ALERT_WINDOW allow`.
- After reinstalling, the service reconnects a few seconds later; wait before testing.
