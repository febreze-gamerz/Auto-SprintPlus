# Auto Sprint+ 1.3 versioning

## Build 26.3

```bash
./gradlew clean build -Ptarget_mc=26.3
```

## Build 26.2

```bash
./gradlew clean build -Ptarget_mc=26.2
```

The output JAR is placed in `build/libs/` and includes the Minecraft version in
its filename.

## Adding a future Minecraft version

Create:

```text
versions/<minecraft-version>.properties
```

For example:

```properties
minecraft_version=26.4
loader_version=<compatible-loader>
fabric_version=<compatible-fabric-api>
java_version=25
```

Then build:

```bash
./gradlew clean build -Ptarget_mc=26.4
```

### What still may require code changes?

Changing the version profile is enough when Minecraft/Fabric keeps the APIs used
by Auto Sprint+ compatible.

If a future release removes or renames an API used by the GUI/HUD/keybind code,
only the affected compatibility code should be changed. Keep version-specific
Minecraft calls in small compatibility classes rather than spreading version
checks through the whole project.

## Current included profiles

- `26.2` — Fabric Loader `0.19.3`, Fabric API `0.153.0+26.2`
- `26.3` — Fabric Loader `0.19.5`, Fabric API `0.160.6+26.3`
