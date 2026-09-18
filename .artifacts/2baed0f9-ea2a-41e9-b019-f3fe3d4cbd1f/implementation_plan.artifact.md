# Plan to Fix Unresolved Dependencies (Convert App to Library for JitPack)

Ang error na `No matching variant... Variant 'releaseAabPublication' was found` ay nangyayari dahil ang iyong proyekto ay kasalukuyang naka-configure bilang isang **Android Application** (`com.android.application`). Kapag binuo ito ng JitPack, naglalabas ito ng Android App Bundle (.aab) sa halip na isang Android Library (.aar), kaya hindi ito magamit ng ibang apps bilang dependency.

Para maayos ito, babaguhin natin ang proyekto mula sa pagiging Application patungo sa pagiging isang **Android Library** (`com.android.library`).

## Proposed Changes

### 1. Dependency Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/J.Hernandez/AndroidStudioProjects/Livemap/gradle/libs.versions.toml)
- Magdaragdag ng definition para sa `android-library` plugin gamit ang parehong bersyon ng AGP (9.3.2).

### 2. Root Project Configuration

#### [MODIFY] [build.gradle.kts](file:///C:/Users/J.Hernandez/AndroidStudioProjects/Livemap/build.gradle.kts)
- Papalitan ang `android-application` plugin ng `android-library` plugin (naka-set bilang `apply false`).

### 3. App/Library Module Configuration

#### [MODIFY] [build.gradle.kts](file:///C:/Users/J.Hernandez/AndroidStudioProjects/Livemap/app/build.gradle.kts)
- Papalitan ang plugin mula `android.application` patungong `android.library`.
- Tatanggalin ang `applicationId` dahil ang mga Android Library ay walang `applicationId`.

### 4. Manifest Cleanup

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/J.Hernandez/AndroidStudioProjects/Livemap/app/src/main/AndroidManifest.xml)
- Tatanggalin o lilinisin ang mga attributes sa `<application>` tag (gaya ng `android:icon`, `android:label`, `android:theme`) at ang LAUNCHER intent-filter upang hindi ito magkaroon ng conflict sa app ng gagamit ng iyong library.

## Verification Plan

### Automated Tests
- Patatakbuhin ang Gradle sync at `./gradlew assembleRelease` para masigurong maayos ang pag-build bilang library.
