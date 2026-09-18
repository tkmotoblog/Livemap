# JitPack Compatibility Walkthrough

Matagumpay na na-configure ang iyong project para maging handa at compatible sa JitPack.

## Changes Made

### Root Project
- [NEW] [jitpack.yml](file:///C:/Users/J.Hernandez/AndroidStudioProjects/Livemap/jitpack.yml) - Nagdagdag ng configuration para pilitin ang JitPack na gamitin ang `openjdk17` sa pag-build, na kinakailangan ng Android Gradle Plugin v9.3.2.

### [app] Module
- [MODIFY] [build.gradle.kts](file:///C:/Users/J.Hernandez/AndroidStudioProjects/Livemap/app/build.gradle.kts) - Idinagdag ang `publishing` configuration sa loob ng `android` block para i-export ang release variant kasama ang sources nito, at isinet-up ang `MavenPublication` para sa artifact na `livemap`.

## Verification Results
- Matagumpay na natapos ang Gradle Sync nang walang error.

> [!TIP]
> Kapag i-pu-push mo na ito sa GitHub upang gamitin sa JitPack, siguraduhing palitan ang `groupId` (`com.github.TKNetwork`) kung iba ang iyong GitHub username/organization name sa `TKNetwork`.
