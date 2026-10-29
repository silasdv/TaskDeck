# Contributing

TaskDeck is vibecoded, with substantial AI assistance guided by SilasDV. Human review and device feedback are welcome.

For a bug, include TaskDeck version, device/Android firmware, Shizuku version/status, controller label profile and exact steps. Describe touch versus controller input and the Last action result where relevant. Redact personal app names/content.

Build with JDK 17, SDK 35 and Gradle 8.11.1. Run `testDebugUnitTest lintRelease assembleRelease` before proposing a change. Keep controller/touch navigation, current-user/protection checks, decimal MB and per-app unknown-result reporting intact. Never add arbitrary shell commands or silently retry destructive requests.

Do not commit APKs, local paths, signing keys, passwords or build caches. Native firmware/Shizuku and notification-banner compatibility need physical device testing; fake-IPC UI tests do not prove it.
