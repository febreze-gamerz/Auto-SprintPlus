Auto Sprint+ 1.3 — Minecraft 26.2 / 26.3

VERSION MANAGEMENT
==================

Minecraft-version-specific values are stored in:

    versions/26.2.properties
    versions/26.3.properties

The shared Java source stays in src/main/java.

Build 26.3:
    ./build-version.sh 26.3

Build 26.2:
    ./build-version.sh 26.2

Or directly:
    ./gradlew clean build -Ptarget_mc=26.3
    ./gradlew clean build -Ptarget_mc=26.2

If your Gradle wrapper is unavailable, use your installed Gradle 9.x:
    gradle clean build -Ptarget_mc=26.3

OUTPUT
======

The JAR is written to:

    build/libs/

and uses the Minecraft version in the filename, for example:

    Auto-SprintPlus-1.3-26.3.jar
    Auto-SprintPlus-1.3-26.2.jar

ADDING A FUTURE VERSION
=======================

1. Copy an existing profile:

    cp versions/26.3.properties versions/26.4.properties

2. Change only the Minecraft/Loader/Fabric API values in the new file.

3. Build it:

    ./build-version.sh 26.4

If the new Minecraft version changes a Minecraft/Fabric API method used by the
mod, only the affected compatibility code should need adjustment. Keep those
changes isolated instead of scattering version checks throughout the mod.
