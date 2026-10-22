# Auto Sprint+ 1.4 versioning

## Build 26.3

    ./gradlew clean build -Ptarget_mc=26.3

The output JAR is placed in `build/libs/` and includes the Minecraft version in its filename.

## Adding a future Minecraft version

Create `versions/<minecraft-version>.properties`, for example:

    minecraft_version=26.4
    loader_version=<compatible-loader>
    fabric_version=<compatible-fabric-api>
    java_version=25
    fabric_api_min_version=<compatible-fabric-api>

Then build with `./gradlew clean build -Ptarget_mc=26.4`.

If a future release removes or renames an API used by the GUI, HUD or keybind code,
change only the affected compatibility code.

## Included profiles

- `26.3` — Fabric Loader `0.19.5`, Fabric API `0.160.6+26.3`
