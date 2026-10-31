# TaskDeck 3.0 release procedure

The user explicitly authorized publishing version 3.0 to silasdv/TaskDeck. It consolidates every local 2.1–2.7 change since public v2.0 (`888ad06bbe0baf69aafdbbbdfb5cf2d188c0cc7d`). VersionCode 300; UserService 9; same package, signing identity and permissions. Preserve old releases and do not rewrite remote history.

1. Run tests, lint and release APK/AAB gates; record exact results and physical-device limits.
2. Consolidate final behavior in README, CHANGELOG and RELEASE-3.0.md. Keep historical milestones explicitly historical. Preserve SilasDV, MIT, vibecoded, support and third-party disclosures.
3. Commit source/docs. Refresh release VCS metadata, build final artifacts and check final-commit provenance.
4. Verify APK signer/version/permissions/debuggable state. Archive tracked source, excluding private/generated files; create SHA-256 sums for APK/AAB/source ZIP.
5. Push main and the annotated v3.0 tag with the existing authorized account. Create a draft release, upload four assets, verify draft/body/tag and remote asset digests, then publish as latest. Verify readback afterward.
6. Prepare the Reddit draft after GitHub release, centered on Android Recent Apps controller usability. The user will add a real screenshot. Reddit publication is a separate step; none is sent by this release procedure.

Signing configuration still uses ignored .local/release-signing.properties or TASKDECK_KEYSTORE_PATH/TASKDECK_STORE_PASSWORD/TASKDECK_KEY_ALIAS/TASKDECK_KEY_PASSWORD. Never print or publish keys/credentials. Public builders use their own signing material. See the historical release instructions below for earlier local versions.

## Historical procedure

# Release signing and publication

TaskDeck 2.0 uses versionCode 200, UserService 9 and the same dedicated release key as 1.0. Keep keys/passwords private and backed up.

Provide TASKDECK_KEYSTORE_PATH, TASKDECK_STORE_PASSWORD, TASKDECK_KEY_ALIAS and TASKDECK_KEY_PASSWORD, or ignored `.local/release-signing.properties` (storeFile, storePassword, keyAlias, keyPassword). Relative storeFile paths resolve from the project root. Private signing files never belong in Git or source archives.

```sh
./gradlew testDebugUnitTest lintRelease assembleRelease bundleRelease
```

Without signing material release artifacts are unsigned; do not distribute them as installable APKs. Verify APK signing identity and debuggable=false, version/permissions, and final-commit provenance in the AAB. Archive tracked source at that commit and check that no private/generated files are included. Generate SHA-256 checksums for APK, AAB and source ZIP.

The user explicitly authorized GitHub v2.0 publication. The previous published release is v1.0; include every intervening local 1.1–1.1.12 change in docs/RELEASE-2.0.md and CHANGELOG.md. Push main and annotated tag without rewriting remote history. Upload assets to a draft, check body/tag/asset metadata and checksums, then publish as latest. Preserve v1.0 assets.

Updates over 1.0/signed 1.1 builds retain settings. Earlier 0.x debug builds require uninstalling first, which clears app data. Real-device acceptance limits remain documented; tests do not certify physical RP6 compatibility. No Play Store publication is implied.

2.1 is a local update, versionCode 210, UserService 9, existing key. It does not modify the published v2.0 release. Package APK/AAB, clean source and hashes after gates; no new GitHub publication is implied.

2.2 is local, versionCode 220, UserService 9; same key. Include Ko-fi artwork notice. GitHub 2.0 is unchanged.

## Local 2.3

Version 2.3 / code 230, UserService 9. Same package, permissions and signing key. 182 tests pass; lint 0 errors/33 warnings; release APK/AAB pass. Package a source archive and SHA-256 sums. No GitHub publication is authorized by this local update request.

## Local 2.4

Version 2.4 / code 240, UserService 9, same package, permissions and signing key. Local Dock UX update; public GitHub 2.0 assets remain untouched. Source audit follow-ups are documented separately and are not implemented by this package.

## Local 2.5

Version 2.5 / code 250, UserService 9. Same package/key/permissions. 189 tests pass; lint 0 errors / 33 existing warnings; release APK/AAB pass. Two sibling Dock customization menus replace the combined screen. No GitHub publication is included.

## Local 2.6

Version 2.6 / code 260, UserService 9. Same key/package/permissions. 193 tests pass; lint 0 errors / 33 existing warnings; release APK/AAB pass. Nine audit findings remain proposals. No GitHub publication.

## Local 2.7

Version 2.7 / code 270, UserService 9. Same package/key/permissions. 216 tests passed; lint 0 errors / 33 existing warnings; release APK/AAB passed. Implements the nine 2.6 review follow-ups and user-directed six/eight-app Dock limits. No GitHub publication.
