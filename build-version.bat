@echo off
setlocal

if "%~1"=="" (
    echo Usage: build-version.bat ^<minecraft-version^>
    echo Example: build-version.bat 26.3
    exit /b 1
)

if not exist "versions\%~1.properties" (
    echo Unknown Minecraft profile: %~1
    echo Create versions\%~1.properties first.
    exit /b 1
)

echo Building Auto Sprint+ for Minecraft %~1...

if exist "gradle\wrapper\gradle-wrapper.jar" (
    call gradlew.bat clean build -Ptarget_mc=%~1
) else (
    gradle clean build -Ptarget_mc=%~1
)

endlocal
