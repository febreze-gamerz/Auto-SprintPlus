Auto Sprint+ 1.4 — Minecraft 26.2

Changes in 1.4:
- HUD box is always sized from its text. The box never clips the text, and the
  text scales with the HUD scale (HUD scale x text scale).
- Rounded corners and border width scale with the HUD.
- Rounded outline no longer has gaps on its curves or at the straight edges.
- HUD editor: the box you drag is the box that is drawn, at every HUD scale.
- HUD editor: dragging moves the HUD by the mouse movement, so the grab point stays under the cursor.
- HUD editor: clicks on the editor panel go to its buttons, even when the HUD is under the panel.
- HUD editor toggles read their state from the settings, not from the button text.
- Config screen: "Reset All Settings" spans the full width.
- Config screen: "Reset HUD" refreshes the slider positions straight away.
- Sprint: respects vanilla's canSprint() check (no forced sprint when food is too low).
- Sprint: holding W and S together does not sprint (no running particles), as in vanilla.
- Sprint: reflection lookups are cached, so the sprint check does no class searching every tick.
- Config: a damaged HUD position in the config file is replaced with the default, not stored as NaN.
- Version is 1.4 (mod_version in gradle.properties).

BUILD
=====

Build on Windows with JDK 25:
    .\gradlew.bat clean build

Or with the build script (Linux/macOS):
    ./build-version.sh 26.2

JAR output: build/libs/Auto-SprintPlus-1.4-26.2.jar
