# Project memory (loaded automatically every session)

This is an Android app (working name "FocusGate", a placeholder) that interrupts distracting app use with a motivational video. Full details are in FOCUSGATE_SPEC.md.

## Session start routine
1. Read FOCUSGATE_SPEC.md, ARCHITECTURE.md (if it exists), and HANDOFF.md (if it exists).
2. Find the first unfinished milestone in the spec's milestone list. HANDOFF.md says where the last session stopped.
3. If the user says only "continue" (or similar), start that milestone without asking what to do. Use plan mode first for milestone 0 and any change that touches architecture.

## Rules
- One milestone at a time. Build it, test it (on device or emulator where possible), commit.
- Milestones 1 to 7 are v1. Do not start milestones 8 and later until the user says so.
- The app will grow a lot after v1. Follow the architecture requirements in the spec, and ask before making structural shortcuts.
- Keep the app name and package name centralized so renaming is easy.
- Ask before adding dependencies beyond standard Android/Jetpack and Media3.

## Session end routine
Before finishing, update HANDOFF.md with: what was done, what is in progress, what is next, and any decisions or gotchas discovered. Update ARCHITECTURE.md if structure changed.
