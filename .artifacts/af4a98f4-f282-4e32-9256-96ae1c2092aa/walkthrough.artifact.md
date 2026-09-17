# Walkthrough - Fixing 'versionCode' Unresolved Reference

The `versionCode` error was resolved by correctly identifying that the `:app` module should be an Android Application rather than an Android Library.

## Changes

### app module

#### [app/build.gradle.kts](file:///C:/Users/J.Hernandez/AndroidStudioProjects/Livemap/app/build.gradle.kts)

- **Switched Plugin**: Changed from `id("com.android.library")` to `alias(libs.plugins.android.application)`. This enables the `applicationId`, `versionCode`, and `versionName` DSL properties.
- **Added applicationId**: Configured `applicationId = "com.tknetwork.livemap"` in `defaultConfig` to comply with application requirements.

## Verification Results

### Automated Tests
- **Gradle Sync**: Successfully completed without errors.
- **Build**: `:app:assembleDebug` finished successfully.

```
BUILD SUCCESSFUL in 5s
```
