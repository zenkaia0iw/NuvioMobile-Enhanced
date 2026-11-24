# Decisions

Short record of decisions that shape this fork. Add an entry when a decision changes how code, CI, or releases work. Newest first.

## 2026-11-24: iOS job runs on `macos-latest`
- Context: `xcode-27` is not a standard GitHub runner label, so the iOS job never started.
- Decision: use `macos-latest` and `maxim-lobanov/setup-xcode` with `latest-stable`.
- Consequence: iOS builds are test-only until an Apple Developer account and signing are set up.

## 2026-11-24: iOS ships unsigned; no public iOS release yet
- Context: no paid Apple Developer account, no signing certificate or provisioning profile.
- Decision: iOS output is an unsigned IPA. Users can sideload with AltStore, SideStore, or Sideloadly using their own Apple ID (re-sign every 7 days).
- Consequence: no public iOS release until the owner decides on an Apple Developer account.

## 2026-11-24: Trakt and SIMKL use PKCE, no client secret in the app
- Context: Trakt issued no client secret for this app. The client also must not ship secrets.
- Decision: Trakt login uses PKCE (S256). Only `TRAKT_CLIENT_ID` and `SIMKL_CLIENT_ID` are required. Redirects: `nuvio://auth/trakt`, `nuvio://auth/simkl`.
- Consequence: `TRAKT_CLIENT_SECRET` is no longer required by the release workflow.

## 2026-11-24: Updater and store feeds point at this fork
- Context: the in-app updater and sideload feeds still referenced the upstream owner, so users could install a build not signed by this fork.
- Decision: updater checks `zenkaia0iw/NuvioMobile-Enhanced`. `store.json` and `store-beta.json` carry bundle ID `com.mew.mobile` and no upstream versions.
- Consequence: the sync workflow keeps this fork's store metadata on conflict.

## 2026-11-24: Upstream sync source is the direct parent
- Context: this fork is based on `luqmanfadlli/NuvioMobile-Enhanced`, not `NuvioMedia/NuvioMobile`.
- Decision: the daily sync merges `luqmanfadlli/NuvioMobile-Enhanced` `enhanced` into `enhanced` via a dated branch and PR.
- Consequence: upstream auto-close workflow was removed (it closed every new issue and PR).

## 2026-11-24: Package IDs
- Context: the upstream app uses `com.nuvio.media`. Installing both on one device would conflict, and changing an ID later breaks updates.
- Decision: Android `com.mew.mobile` (debug `com.mew.mobile.debug`), iOS `com.mew.mobile`, Kotlin framework `com.mew.mobile`.
- Consequence: iOS Keychain service names still use the old ID. Migration is open (issue #15).

## 2026-11-24: Keep unused submodules out
- Context: `libass-android`, `vendor/TorrServer`, `vendor/quickjs-kt` had no `.gitmodules` entry and caused CI warnings. The build uses `libs/*.aar` instead.
- Decision: removed the three gitlinks. `MPVKit` stays (player dependency).
