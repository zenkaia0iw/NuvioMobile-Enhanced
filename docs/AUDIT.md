# Repo audit: zenkaia0iw/NuvioMobile-Enhanced

Scope: `enhanced` at `0dc9e1242` (chore(release): bump to 1.1.0-beta.3). Read-only review. No code, workflow, or config was changed. Secret values were never read; only names are listed.

Path references are relative to the repo root. Line numbers were taken from the audited commit.

## Summary

| Severity | Count | Themes |
|---|---|---|
| High | 4 | A workflow that auto-closes every new issue and PR; split app identity; in-app updater pointed at another owner; a Trakt client secret compiled into the client |
| Medium | 10 | Submodule hygiene; hardcoded upstream owners in CI; shared `nuvio://` scheme; stale store files; `Docs/` vs `docs/` collision; committed binaries; iOS release validation; direct push from CI; beta pins; unpinned actions |
| Low / Info | 4 | Test bundle IDs; official backend hosts; upstream-owned links; minor workflow notes |

Items marked **Unverified** need a check outside this sandbox (no Maven, Actions UI, or branch-protection access).

---

## High

### H1. `discontinue-legacy-main.yml` auto-closes all new issues and PRs
- File: `.github/workflows/discontinue-legacy-main.yml`
- Lines 21-28: `issues` (`opened`, `reopened`) and `pull_request_target` (`opened`, `reopened`) triggers.
- Lines 129-163: `auto-close-issue` comments on and closes every new issue.
- Lines 164-199: `auto-close-pull-request` comments on and closes every new PR.
- Lines 36-127: manual bulk close. Inputs `close_existing_issues` and `close_existing_pull_requests` default to `true` (lines 9-15). Dispatching with defaults closes every open issue and PR.
- No `if: false` guard exists anywhere in the file.
- The message says the "current Nuvio Mobile app is being discontinued in favor of a full Compose Multiplatform rewrite" and refers to a "legacy React Native codebase". This tree is already Compose Multiplatform (`composeApp/`), and there is no `package.json` at the root. The text does not describe this repo.
- Impact: community intake is closed automatically. The PR for this audit will be auto-closed by this workflow if it is enabled in Actions settings (**Unverified**: whether it is enabled).

### H2. Split application identity across Android, iOS, store feeds, and tooling
Android is `com.mew.mobile`. iOS, the store feeds, and the baseline profile still use `com.nuvio.*`:

| Location | Value |
|---|---|
| `androidApp/build.gradle.kts:71` | `applicationId = "com.mew.mobile"` |
| `androidApp/build.gradle.kts:222` | debug `com.mew.mobile.debug` |
| `iosApp/Configuration/Config.xcconfig:6` | `PRODUCT_BUNDLE_IDENTIFIER=com.mew.mobile$(TEAM_ID)` |
| `iosApp/iosApp.xcodeproj/project.pbxproj:562` | app target `com.nuvio.media` |
| `iosApp/iosApp.xcodeproj/project.pbxproj:610` | another config `com.nuvio.app` |
| `iosApp/iosApp.xcodeproj/project.pbxproj:461`, `:485` | widget `com.nuvio.app.DownloadsWidgetExtension` vs `com.nuvio.media.DownloadsWidgetExtension` |
| `iosApp/iosApp.xcodeproj/project.pbxproj:510`, `:698` | test target `com.nuvio.SubtitleTests` |
| `composeApp/build.gradle.kts:261` | `iosFrameworkBundleId = "com.nuvio.media"` |
| `baselineprofile/.../BaselineProfileGenerator.kt:73` | `DEFAULT_PACKAGE_NAME = "com.nuvio.media"` (should match `com.mew.mobile`) |
| `scripts/run-mobile.sh:16` | `IOS_BUNDLE_ID="com.nuvio.app.Nuvio"` |
| `store.json:9`, `:14`; `store-beta.json:9`, `:14` | `com.nuvio.media` |
| `NuvioFull.json:12`; `NuvioEnhanced.json:12` | `com.nuvio.media` |

- Impact: the baseline profile targets a package that is not installed. The sideload source advertises a bundle ID that does not match the Android app ID or `Config.xcconfig`. Changing an iOS bundle ID orphans Keychain items. The keychain services `com.nuvio.media.*` are hardcoded in `composeApp/src/iosMain/...` (for example `ServerStorage.ios.kt:72`, `ProfileBiometricAuth.ios.kt:69`), so stored credentials become unreadable after a change.
- The Kotlin `namespace = "com.nuvio.app"` (`composeApp/build.gradle.kts:353`) and component names in `androidApp/src/main/AndroidManifest.xml` (lines 13, 31, 37, 95-170) are not a bug by themselves, since `applicationId` may differ from the namespace. They do need to be listed in any migration.

### H3. In-app updater reads releases from another owner
- `composeApp/src/commonMain/kotlin/com/nuvio/app/features/updater/AppUpdaterRepository.kt:36-37`: `GITHUB_OWNER = "luqmanfadlli"`, `GITHUB_REPO = "NuvioMobile-Enhanced"`.
- Builds from this fork check for updates from a repository this fork does not control. A user of this fork's build can be prompted to install a binary from `luqmanfadlli`, which is not the artifact this pipeline signs or publishes.
- The same owner is used by `WhatsNewRepository.kt` (imports `GITHUB_OWNER`/`GITHUB_REPO`) and by the `releaseUrl` in `WhatsNewSnapshotBuilderTest.kt:561`.

### H4. Trakt client secret is compiled into the client
- `composeApp/build.gradle.kts:102-104`: `TRAKT_CLIENT_ID`, `TRAKT_CLIENT_SECRET` (line 103), `TRAKT_REDIRECT_URI`, exposed as generated constants.
- Any value embedded in an APK or IPA can be extracted. The secret should not ship in a client.
- Also check `OMDB_API_KEY` (line 160), `MDBLIST_CLIENT_ID` (line 132), `PREMIUMIZE_CLIENT_ID` (line 173). Keys are less sensitive than secrets, but they are still embedded and billable or rate-limited.
- **Unverified**: whether the Trakt secret was ever shipped in a published build. If so, treat it as exposed and rotate.

---

## Medium

### M1. Submodule gitlinks with no `.gitmodules` entry
- `.gitmodules` lists only `MPVKit` (line 3 `url = https://github.com/luqmanfadlli/MPVKit.git`, branch `nuvio-enhanced`).
- The index also contains three gitlinks with no `.gitmodules` entry, so there is no URL to fetch them from:
  - `libass-android` at `c10b71a` (the "No url found" warning)
  - `vendor/TorrServer` at `b79dbaf`
  - `vendor/quickjs-kt` at `57ce096`
- All three directories are empty in the clone. Nothing in the Gradle files, scripts, or workflows references `libass-android`, `vendor/TorrServer`, or `vendor/quickjs-kt`. The Android libraries actually used are the AARs in `composeApp/libs/`.
- No `actions/checkout` step sets `submodules:`. Only `scripts/prepare-ios-dependencies.sh:12` initializes `MPVKit`, and only on iOS.
- Fix: remove the three unused gitlinks, or add real `.gitmodules` entries if they are needed.

### M2. Upstream owner and repos hardcoded in CI
- `.github/workflows/mobile-release.yml`:
  - `:75` and `:227` `CONTRIBUTIONS_URL` hardcodes `luqmanfadlli/NuvioMobile-Enhanced`.
  - `:297` the iOS job reads `secrets.CONTRIBUTIONS_URL` instead. The two platforms can therefore produce different supporter data.
  - `:158-160` and `:185-186` upstream is `NuvioMedia/NuvioMobile` on `cmp-rewrite`, while the fork's documented parent is `luqmanfadlli`.
- `.github/workflows/ios-test-build.yml:46`: the same `CONTRIBUTIONS_URL` literal.
- `.github/workflows/upstream-sync.yml:33-34`, `:40-41`, `:62`: upstream is `luqmanfadlli/NuvioMobile-Enhanced`, `enhanced`.
- `docs/FORK_WORKFLOW.md:16-17` tells contributors to add `luqmanfadlli` as `upstream`.
- Fix: move these to repository variables (`vars.*`) and make the iOS and Android sources match.

### M3. Custom URL scheme `nuvio://` is shared with the upstream app
- OAuth redirects and deep links use `nuvio://`:
  - `composeApp/build.gradle.kts:104`, `:118` (Trakt and Simkl redirect defaults)
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/features/anilist/AniListConfig.kt:5`
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/core/deeplink/AppUrlBridge.kt:62`, `:68`
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/features/player/InfusePlayback.kt:107`, `:109`
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/features/party/PartyRepository.kt:715`
  - `composeApp/src/iosMain/kotlin/com/nuvio/app/features/player/ExternalPlayerPlatform.ios.kt:111`
  - `iosApp/DownloadsWidgetExtension/DownloadsLiveActivityWidget.swift:19`
  - `iosApp/iosApp/Info.plist:45` (`com.nuvio.app.auth` URL type)
- `androidApp/src/main/AndroidManifest.xml:89` declares `android:host="nuvio.tv"`.
- If the upstream app is installed alongside this one, both claim `nuvio://`. Changing the scheme requires re-registering the Trakt, Simkl, and AniList OAuth apps with new redirect URIs.
- Fix: decide on a fork-specific scheme and re-register, or document the conflict and rely on host verification.

### M4. Stale store and install artifacts at the root
- `NuvioFull.json` and `NuvioEnhanced.json` (root): describe version 0.4.6 with `bundleIdentifier` `com.nuvio.media`. Their `website`, `iconURL`, and `downloadURL` values point to `luqmanfadlli/NuvioMobile-iOS`. Neither file is read by any workflow. `update-store-source.yml` only touches `store.json` and `store-beta.json`.
- `store.json:6` `iconURL` points to `luqmanfadlli/NuvioMobile-Enhanced/.../enhanced/...`. `store.json:18` `iconURL` points to `NuvioMedia/NuvioMobile/0.4.11`. `store.json:417` `downloadURL` points to `NuvioMedia/NuvioMobile`.
- `README.md:35`, `:44` and the `store-beta.json` entries show install sources under `luqmanfadlli`.
- Fix: delete the two root JSON files, or regenerate them from the current store feed. Update icon and download URLs for the fork.

### M5. `Docs/` and `docs/` differ only by case
- Both directories exist at the root. `Docs/` contains "Stremio addons refer...". `docs/` contains `FORK_WORKFLOW.md` and `subtitle-autosync.md`.
- On macOS and Windows, which use case-insensitive filesystems by default, checkout will merge or fail. Most iOS contributors use macOS.
- Fix: rename or move `Docs/`.

### M6. Binary AARs are committed, and provenance and versions are unclear
- `composeApp/libs/` contains about 38 MB of AARs. The largest are `lib-nuvio-engine-android-0.1.6.aar` (16.4 MB) and `nuvio-libass-release.aar` (5.2 MB). They are tracked in git (`git ls-files '*.aar'`).
- `composeApp/build.gradle.kts:484` says libass is "from NuvioMedia/nuvio-libass". The AAR is copied in at line 485. No build step reproduces it.
- `composeApp/build.gradle.kts:506` loads `libs/quickjs-kt-android-1.0.5-nuvio.aar`, but `gradle/libs.versions.toml:38` pins `quickjsKt = "1.0.15"`. These disagree. Only one of them is actually used.
- `scripts/prepare-ios-dependencies.sh:6` pins `engine_version=0.1.6`, matching the Android AAR.
- Fix: document the source and build of each AAR, or move them to a Maven artifact, and settle one quickjs version.

### M7. iOS release job does not validate required properties
- `.github/workflows/mobile-release.yml`: the Android job checks `NUVIO_RELEASE_STORE_PASSWORD`, `NUVIO_RELEASE_KEY_ALIAS`, `NUVIO_RELEASE_KEY_PASSWORD`, `TRAKT_CLIENT_ID`, and `SIMKL_CLIENT_ID`. The iOS job (`:287`, `:296`) only decodes the file.
- `.github/workflows/ios-test-build.yml:74-81` skips writing `local.properties` entirely when the secret is empty (`if: env.RELEASE_PROPERTIES_BASE64 != ''`). The build then runs with missing keys.
- `runs-on: xcode-27` (`mobile-release.yml:287`, `ios-test-build.yml:35`) is a custom label. If no runner carries it, the job queues indefinitely (**Unverified**).
- Fix: apply the same required-key check to iOS and fail fast.

### M8. `update-store-source` pushes directly to the default branch
- `.github/workflows/update-store-source.yml:105-120`: the job commits with `github-actions[bot]` and runs `git push origin "HEAD:${RELEASE_BRANCH}"` with `contents: write`.
- If branch protection is on, this push fails. If it is off, any published release changes the default branch with no review.
- Fix: open a PR for store-file updates, or record that the branch is intentionally unprotected.

### M9. Beta and pre-release pins in the version catalog
- `gradle/libs.versions.toml:22` `composeMultiplatform = "1.13.0-beta01"`
- `gradle/libs.versions.toml:34` `material3 = "1.13.0-beta01"`
- `gradle/libs.versions.toml:32` `kmpalette = "4.0.0-beta02"`
- These are used in release builds. **Unverified**: whether stable releases exist for each. Versions could not be checked against Maven Central from this sandbox.
- Other pins (`kotlin = "2.4.10"`, `ktor = "3.4.1"`, `supabase = "3.4.1"`, `agp = "9.4.0"`) should be checked the same way. Do not treat them as outdated without that check.

### M10. Third-party actions pinned by tag, and checkout versions disagree
- All `uses:` references are tags, not commit SHAs: `actions/checkout@v6` (6 uses), `actions/github-script@v7` (9), `actions/setup-java@v5` (4), `actions/upload-artifact@v7` (5), `actions/download-artifact@v8` (4), `actions/cache@v5` (2), `gradle/actions/setup-gradle@v6` (4).
- `.github/workflows/upstream-sync.yml:25` uses `actions/checkout@v4`. The others use `@v6`.
- **Unverified**: whether these tags exist. They could not be checked from this sandbox.
- Fix: pin each action to a full SHA with a version comment, and align on one checkout version.

---

## Low

- `iosApp/iosApp.xcodeproj/project.pbxproj:510` and `:698`: test target bundle ID `com.nuvio.SubtitleTests`. Align with the rest of the bundle-ID scheme (H2).
- `.github/workflows/discontinue-legacy-main.yml` uses `pull_request_target`. It does not check out PR code, so it is not a code-execution risk. It does run with `pull-requests: write` and `issues: write` on every PR, which is why H1 matters.
- `OMDB_API_KEY` and the MDBList and Premiumize client IDs are compiled into the client (see H4).

## Info (intentional, verify)

- Official backend and links: `composeApp/src/commonMain/kotlin/com/nuvio/app/core/network/ServerDiscovery.kt:51` (`https://api.nuvio.tv`), `core/auth/DeviceLinkAuthRepository.kt:55` (`https://nuvio.tv/link`), `features/settings/AboutSettingsPage.kt:37` (privacy policy), `features/auth/AuthScreen.kt:791` (terms). These are expected for a client on the official service. Confirm that the fork should authenticate against the upstream backend.
- `composeApp/src/commonMain/kotlin/com/nuvio/app/features/settings/LicensesAttributionsPage.kt:50` links to the upstream repo. This is correct attribution.
- `pr-full-debug-build.yml` runs on `pull_request` for `enhanced`. Fork PRs get no secrets, which is the intended behavior.
- `pr-full-debug-build-comment.yml` runs on `workflow_run`, does not check out code, and only posts a comment. Safe as written.

---

## Secrets and `local.properties` keys (names only)

### Repository secrets used by workflows
- `NUVIO_LOCAL_PROPERTIES_BASE64`: `mobile-release.yml:73`, `:225`, `:296`; `ios-test-build.yml:45`; `pr-full-debug-build.yml:36`. Base64 of `local.properties`. Required for release.
- `NUVIO_RELEASE_KEYSTORE_BASE64`: `mobile-release.yml:74`, `:226`. Base64 of the release keystore. Required for release.
- `CONTRIBUTIONS_URL` (optional): `mobile-release.yml:297`. iOS only (see M2).
- `GITHUB_TOKEN`: all jobs. `contents: write` in release and store jobs; `issues`/`pull-requests: write` in the close and sync workflows.

### `local.properties` keys
Required by the Android release job (checked, `mobile-release.yml` "Configure release properties"):
- `NUVIO_RELEASE_STORE_PASSWORD`, `NUVIO_RELEASE_KEY_ALIAS`, `NUVIO_RELEASE_KEY_PASSWORD`
- `TRAKT_CLIENT_ID`, `SIMKL_CLIENT_ID`

Set by CI, not by the secret: `NUVIO_RELEASE_STORE_FILE` (the decoded keystore path), `CONTRIBUTIONS_URL`.

Read by Gradle with defaults or as optional (`composeApp/build.gradle.kts` and `androidApp/build.gradle.kts`):
- Signing: `NUVIO_RELEASE_STORE_FILE` (`androidApp:29`)
- Trakt: `TRAKT_CLIENT_SECRET` (`:103`), `TRAKT_REDIRECT_URI` (`:104`)
- Simkl: `SIMKL_REDIRECT_URI` (`:118`), `SIMKL_APP_NAME` (`:119`)
- Other: `MDBLIST_CLIENT_ID` (`:132`), `INTRODB_API_URL` (`:145`), `IMDB_RATINGS_API_BASE_URL` (`:158`), `IMDB_TAPFRAME_API_BASE_URL` (`:159`), `OMDB_API_KEY` (`:160`), `PREMIUMIZE_CLIENT_ID` (`:173`), `SUPPORTERS_WALL_URL` (`:201`), `DONATIONS_BASE_URL` (`:202`), `DONATIONS_DONATE_URL` (`:203`)
- Distribution: `NUVIO_IOS_DISTRIBUTION` (`:250`), `NUVIO_ANDROID_DISTRIBUTION` (`:279`)

Missing keys in the iOS job are not checked (M7).

---

## Fix list (in priority order)

1. **Disable or delete `discontinue-legacy-main.yml`** (H1). It is the only item that closes intake automatically. Delete the file, or add `if: false` to every job until intake is decided.
2. **Choose one application identifier** and align it everywhere in the H2 table. Fix `BaselineProfileGenerator.kt:73` and `scripts/run-mobile.sh:16` first, since they are one-line changes. Plan the iOS migration and Keychain re-login before changing the bundle ID.
3. **Point the updater at this fork** (or make owner and repo configurable) in `AppUpdaterRepository.kt:36-37`. Check `WhatsNewRepository.kt` for the same owner.
4. **Remove `TRAKT_CLIENT_SECRET` from the client** (`composeApp/build.gradle.kts:103`). Move the token exchange to a backend. Rotate the secret if it has shipped (**Unverified**).
5. **Fix the submodules** (M1): remove the three unused gitlinks (`libass-android`, `vendor/TorrServer`, `vendor/quickjs-kt`) or add `.gitmodules` entries. Keep `MPVKit` and add `submodules:` only where a job needs it.
6. **Rename `Docs/`** (M5).
7. **Move owner and upstream references to repo variables** (M2). Use one source for `CONTRIBUTIONS_URL` on both platforms.
8. **Decide on the URL scheme** (M3). Either rename to a fork-specific scheme and re-register the OAuth apps, or document the shared-scheme risk.
9. **Require the same release properties on iOS** and fail fast (M7). Confirm the `xcode-27` runner label exists (**Unverified**).
10. **Replace direct pushes in `update-store-source.yml`** with a PR, or record that the default branch is intentionally unprotected (M8).
11. **Clean up store and install files**: delete or regenerate `NuvioFull.json` and `NuvioEnhanced.json`, and update the `store.json` icon and download URLs (M4).
12. **Document the vendored AARs** (source, version, build) or move them to Maven. Settle the quickjs version (M6).
13. **Verify and move off beta pins** (`composeMultiplatform`, `material3`, `kmpalette`) once stable versions are confirmed (M9).
14. **Pin actions to SHAs** and align `actions/checkout` on one version (M10).
15. **Align the test-target bundle IDs** with H2 (Low).

## Not verified in this audit

- Latest stable versions of Gradle, Kotlin, Compose, and AndroidX dependencies (no Maven access from the sandbox).
- That the `actions/*@vN` tags exist, and what the latest tags are.
- Whether any workflow is enabled or disabled in the Actions UI.
- Branch protection rules on `enhanced` and the default branch.
- Whether the required repository secrets exist, and their values. Values were not read.
- Whether the `xcode-27` runner label is provisioned.
- Whether `TRAKT_CLIENT_SECRET` has shipped in any published build.
