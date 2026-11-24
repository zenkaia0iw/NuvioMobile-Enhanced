# Decisions

Short record of decisions that shape this fork. Add an entry when a decision changes how code, CI, or releases work. Newest first.

## 2026-11-24: Keep `nuvio://` URL scheme
- Context: Trakt and SIMKL redirect to `nuvio://auth/trakt` and `nuvio://auth/simkl`, and both work on device. The scheme is shared with the upstream app.
- Decision: keep the scheme. Renaming means re-registering both OAuth apps for no user benefit right now.
- Consequence: installing this fork and the upstream app on one phone can cause intent conflicts. Revisit before any public release (issue #16).

## 2026-11-24: PR template check disabled on this fork
- Context: `validate_pr_body` checks the upstream contribution template. It failed on every fork PR, and the fork has one maintainer.
- Decision: disable `pr-template-check.yml` on this fork. The file stays in the repo, so it can be re-enabled.
- Consequence: PRs are not checked against the template. Review is manual (issue #17 closed as decided).

## 2026-11-24: Trakt secret was never issued
- Context: Trakt issued no client secret for this PKCE app. No real secret was ever stored in the repo or in Actions secrets.
- Decision: nothing to rotate. Audit H4 closed as not applicable.
- Consequence: the release workflow requires only `TRAKT_CLIENT_ID` and `SIMKL_CLIENT_ID`.

## 2026-11-24: iOS is sideload-only, unsigned
- Context: no paid Apple Developer account and no signing certificate or provisioning profile.
- Decision: iOS output is an unsigned IPA. Users sideload with AltStore, SideStore, or Sideloadly using their own Apple ID (re-sign every 7 days).
- Consequence: no public iOS release. Keychain migration (issue #15) waits until iOS is published.

## 2026-11-24: Keychain and bundle ID migration deferred
- Context: iOS Keychain items are tied to the bundle ID and service names. Changing them logs users out and resets the profile PIN.
- Decision: defer until iOS is published. Plan it then (issue #15).

## 2026-11-24: iOS job runs on the `xcode-27` runner image
- Context: `xcode-27` is not a standard label. `macos-latest` is an Xcode 26 image, and Xcode 27.1 is not installed there.
- Decision: use the `xcode-27` runner image with Xcode 27 preinstalled. No `setup-xcode` step.
- Consequence: iOS test builds pass (run 45406265542).

## 2026-11-24: Trakt and SIMKL use PKCE, no client secret in the app
- Context: Trakt issued no client secret for this app. The client must not ship secrets.
- Decision: Trakt login uses PKCE (S256). Only `TRAKT_CLIENT_ID` and `SIMKL_CLIENT_ID` are required.
- Consequence: `TRAKT_CLIENT_SECRET` is no longer required by the release workflow.

## 2026-11-24: Updater and store feeds point at this fork
- Context: the in-app updater and sideload feeds referenced the upstream owner, so users could install a build not signed by this fork.
- Decision: updater checks `zenkaia0iw/NuvioMobile-Enhanced`. `store.json` and `store-beta.json` carry bundle ID `com.mew.mobile` and no upstream versions.
- Consequence: the sync workflow keeps this fork's store metadata on conflict.

## 2026-11-24: Upstream sync source is the direct parent
- Context: this fork is based on `luqmanfadlli/NuvioMobile-Enhanced`, not `NuvioMedia/NuvioMobile`.
- Decision: the daily sync merges `luqmanfadlli/NuvioMobile-Enhanced` `enhanced` into `enhanced` via a dated branch and PR.
- Consequence: the upstream auto-close workflow was removed (it closed every new issue and PR).

## 2026-11-24: Package IDs
- Context: the upstream app uses `com.nuvio.media`. Installing both on one device would conflict, and changing an ID later breaks updates.
- Decision: Android `com.mew.mobile` (debug `com.mew.mobile.debug`), iOS `com.mew.mobile`, Kotlin framework `com.mew.mobile`.
- Consequence: iOS Keychain service names still use the old ID until issue #15 is done.

## 2026-11-24: Keep unused submodules out
- Context: `libass-android`, `vendor/TorrServer`, `vendor/quickjs-kt` had no `.gitmodules` entry and caused CI warnings. The build uses `libs/*.aar` instead.
- Decision: removed the three gitlinks. `MPVKit` stays (player dependency).
