# Auto-SprintPlus 1.3 — Grouped Multi-Version Build

This project shares one `src/` folder and one root `build/` folder, and produces two JARs:

- **Legacy JAR:** Minecraft 1.21–1.21.8 (legacy mouse-event API)
- **Modern JAR:** Minecraft 1.21.9–1.21.11 (modern mouse-event API)

## Requirements
- Java 21
- Internet connection for the first Gradle build

## Build both JARs
From this directory run:

```bash
chmod +x gradlew
./gradlew clean build
```

Output JARs should be in `build/legacy/libs/` and `build/modern/libs/`.

## Notes
These JARs are grouped by the source-level mouse API boundary. Each is compiled against the oldest Minecraft version in its group, so the code only uses APIs available across the whole range. Minecraft/Fabric compatibility across every version in each range still needs testing in-game. The build has not been validated against downloaded dependencies in this environment.

## Fixes in this version (HUD and editor)

- **Text escaping the HUD box:** the box was a fixed 190×26 scaled by HUD scale, while the text used its own text scale. Large text overflowed small boxes. The box is now sized from the text (never smaller than the base size), so the text always fits.
- **Text not scaling with the HUD:** text scale is now HUD scale × text scale, so scaling the HUD scales its text too. The text scale slider still works as an extra multiplier.
- **Corners and border:** rounded corners and border width scale with the HUD.
- **Text drawn at the screen corner:** if the pose-stack transform methods could not be found, the text was drawn with no transform at the screen origin. Now it is drawn unscaled at its correct position in that case.
- **Editor hit-testing:** dragging used the base size, not the drawn size, and clicks on the editor panel could start a HUD drag. Both now use the drawn size, and clicks on the panel go to its buttons.
- **Editor toggles:** their on/off state was read from button text. It now comes from the config directly.
- **Reset All Settings** button: it was drawn at half width. It is full width again.
- **Legacy jar range:** the jar compiles against Minecraft 1.21 (the oldest version in its range) and requires Fabric API 0.102.0+1.21 or newer, so it loads on 1.21 through 1.21.8. Compiling against the oldest version guarantees the code only uses APIs that exist across the whole range.
- **HUD registration fallback:** if the newer HUD API is present but unusable, the mod falls back to the older HUD callback instead of failing.
