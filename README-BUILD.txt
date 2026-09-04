Auto Sprint+ 1.0.2 — Minecraft 26.1.x

Changes:
- Scrollable main settings GUI with visible scrollbar.
- HUD position is resolution-independent and survives Minecraft GUI scale changes.
- HUD editor and main configuration screen recalculate layout when GUI scale/window size changes.
- Removed Only While Moving Forward setting; sprint is always forward-only.
- Removed Disable While Sneaking setting.
- Kept Disable While Swimming and Disable While Flying.
- Simplified sprint selection to "Sprint: Toggled / Vanilla / Vanilla Toggled".
- HUD now displays only "Sprint: <mode>".
- Added Background toggle.
- Added Rounded Corners toggle.
- Added Text Color setting.
- Added HUD opacity.
- HUD editor/grid/snap features retained.

Build on Windows with JDK 25:
1. Open PowerShell in this project folder.
2. Use your installed Gradle if desired:
   & "C:\Users\febre\Documents\gradle-9.7.1\bin\gradle.bat" clean build
3. JAR output: build\\libs\\Auto-SprintPlus-1.0.2.jar
