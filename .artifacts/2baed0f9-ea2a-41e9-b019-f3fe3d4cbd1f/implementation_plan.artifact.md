# JitPack Compatibility Implementation Plan

Para maging compatible ang iyong project sa JitPack (para ma-publish ito bilang library o dependency), kailangang i-configure ang `maven-publish` plugin at magdagdag ng `jitpack.yml` file.

## Proposed Changes

### [app] Module

#### [MODIFY] [build.gradle.kts](file:///C:/Users/J.Hernandez/AndroidStudioProjects/Livemap/app/build.gradle.kts)
- I-a-update ang `publishing` block para mai-publish ang release version ng app/library.
- Bagama't ang `:app` ay kasalukuyang `com.android.application`, lalagyan natin ito ng configuration para ma-detect ng JitPack ang components na ipu-publish.

### Root Project

#### [NEW] [jitpack.yml](file:///C:/Users/J.Hernandez/AndroidStudioProjects/Livemap/jitpack.yml)
- Magdadagdag ng configuration file para sa JitPack upang gamitin ang tamang Java version (Java 17) na kinakailangan ng modernong Android Gradle Plugin (AGP 9.3.2).

## Verification Plan

### Manual Verification
- Sasabihan ang user na i-push ang mga changes sa GitHub.
- Maaaring subukan ang build sa [jitpack.io](https://jitpack.io) gamit ang URL ng kanilang repository.
- I-che-check kung matagumpay ang "Log" sa JitPack.
