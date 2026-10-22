Auto Sprint+ 1.4 — Minecraft 26.3

Changes in 1.4:
- HUD box is always sized from its text. The box never clips the text, and the
  text scales with the HUD scale (HUD scale x text scale).
- Rounded corners and border width scale with the HUD.
- Rounded outline no longer has gaps on its curves or at the straight edges.
- HUD editor: the box you drag is the box that is drawn, at every HUD scale.
- HUD editor: clicks on the editor panel go to its buttons, even when the HUD is under the panel.
- HUD editor toggles read their state from the settings, not from the button text.
- Config screen: "Reset All Settings" spans the full width.
- Config screen: "Reset HUD" refreshes the slider positions straight away.
- Sprint: respects vanilla's canSprint() check again (no forced sprint when food is too low).
- Version is 1.4 (mod_version in gradle.properties).

VERSION MANAGEMENT
==================

Minecraft-version-specific values are stored in:

    versions/26.3.properties

The shared Java source stays in src/main/java.

Build 26.3:
    ./build-version.sh 26.3

Or directly:
    ./gradlew clean build -Ptarget_mc=26.3

OUTPUT
======

The JAR is written to build/libs/ and uses the Minecraft version in the filename:

    Auto-SprintPlus-1.4-26.3.jar

ADDING A FUTURE VERSION
=======================

1. cp versions/26.3.properties versions/26.4.properties
2. Change only the Minecraft/Loader/Fabric API values in the new file.
3. ./build-version.sh 26.4
