# Fix 'versionCode' Unresolved Reference in build.gradle.kts

The error `Unresolved reference 'versionCode'` occurs because the `:app` module is currently configured as an Android Library (`com.android.library`), which does not support `versionCode` or `versionName` in its DSL (these were removed in recent AGP versions as they are not used for libraries).

Based on the module name `:app` and the presence of a `LAUNCHER` activity in the `AndroidManifest.xml`, this module should be configured as an Android Application (`com.android.application`).

## Proposed Changes

### Build Configuration

#### [MODIFY] [app/build.gradle.kts](file:///C:/Users/J.Hernandez/AndroidStudioProjects/Livemap/app/build.gradle.kts)
- Change the applied plugin from `com.android.library` to `com.android.application` using the version catalog alias.
- Add `applicationId` to the `defaultConfig` block.

## Verification Plan

### Automated Tests
- Run Gradle Sync to verify that the 'versionCode' reference is now resolved and the project syncs successfully.
- Run `./gradlew :app:assembleDebug` to ensure the application builds correctly.

### Manual Verification
- Verify that the IDE no longer shows an error in `app/build.gradle.kts`.
