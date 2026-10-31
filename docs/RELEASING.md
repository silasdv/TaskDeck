# Release signing

TaskDeck 1.0 uses versionCode 100 and a dedicated TaskDeck release key. Keep the key and its passwords private and backed up; future upgrades must use the same key.

Provide TASKDECK_KEYSTORE_PATH, TASKDECK_STORE_PASSWORD, TASKDECK_KEY_ALIAS and TASKDECK_KEY_PASSWORD environment variables. Alternatively, create ignored `.local/release-signing.properties` with storeFile, storePassword, keyAlias and keyPassword. Relative storeFile paths resolve from the project root. The original local key is `.local/taskdeck-release.keystore`; none of this private material belongs in source archives or GitHub.

```sh
./gradlew testDebugUnitTest lintRelease assembleRelease bundleRelease
```

Without signing material, release builds are unsigned and must not be distributed as installable APKs. Release sets debuggable=false. Verify the signed APK using `apksigner verify --verbose --print-certs`, verify the manifest/version, generate SHA-256 checksums and attach APK, App Bundle, source archive and checksums to tag v1.0.

The 0.x test builds used a different debug key. Uninstall one before installing 1.0, first noting settings/mappings; uninstalling clears app data. Releases signed with the 1.0 key can update one another normally.

Run the hardware acceptance checklist and describe unresolved firmware limitations honestly. There is no implied Play Store publication. The workflow in ci-workflow.yml is an optional unsigned CI template; activate it only with an account/token permitted to write GitHub workflows. Never put signing passwords in a workflow file.

Version 1.1 uses versionCode 110 and the same dedicated release key as 1.0. Keep v1.0 assets immutable; publish updates under a new tag. Quick Access/native drag and firmware behavior remain device acceptance cases.

Version 1.1.1 uses versionCode 111 and the same release key. Signed APK/AAB, source ZIP and checksums are prepared locally; publishing is a separate step.

Version 1.1.2 uses versionCode 112 and the same release key. APK, AAB, source ZIP and checksums are prepared locally; existing published assets remain unchanged.

Version 1.1.3 uses versionCode 113 and the same release key. Signed assets and source archive are prepared locally.

Version 1.1.4 uses versionCode 114 and the same release key. Signed APK/AAB, clean source ZIP and checksums are prepared locally; existing published assets remain unchanged.

Version 1.1.5 uses versionCode 115 and the same release key. Signed assets and clean source archive are prepared locally.

Version 1.1.6 uses versionCode 116 and the same release key. Signed local assets do not imply GitHub publication. Include the usability review and device acceptance limits.

Version 1.1.7 uses versionCode 117 and the existing release key. Prepare signed assets locally; no implied publication.

Version 1.1.8 uses versionCode 118, UserService 9 and the existing release key. VIBRATE supports optional close feedback. INTERNET remains absent. Signed assets are prepared locally, without implied publication.

Version 1.1.9 uses versionCode 119, UserService 9 and the existing release key. Assets are prepared locally.

Version 1.1.10 uses versionCode 120, UserService 9 and the existing key. Include official brand assets and their notices/license in clean source archives. Local packaging is separate from publication.
