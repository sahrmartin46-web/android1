# Fix CMake NDK Error and Gradle DSL Issues

The project is failing to build because it's looking for a CMake toolchain file in an incomplete NDK installation (28.2.13676358). Additionally, the `app/build.gradle.kts` file contains several non-standard or deprecated DSL usages that likely contribute to "invalid signature" or sync errors.

## Proposed Changes

### [app]

#### [MODIFY] [build.gradle.kts](file:///C:/flutter/src/flutter/sociamediabackend/app/build.gradle.kts)
- Replace the `compileSdk` block with the standard `compileSdk = 37`.
- Add `ndkVersion = "27.0.12077973"` to use a known working NDK installation.
- Clean up the `buildTypes` block to use standard AGP 8+ properties.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to verify the build completes.
- Trigger a Gradle Sync in Android Studio.

### Manual Verification
- Check if the CMake error persists in the Build output.
