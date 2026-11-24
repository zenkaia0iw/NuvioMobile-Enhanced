# Project status

Last updated: 2026-11-24. Update this file in the same PR as any change to shipped work, open work, or decisions.

## Shipped (merged into `enhanced`)
- #2 Daily upstream sync workflow
- #3 Release: drop Trakt client secret requirement
- #4 Package ID `com.mew.mobile` (Android and iOS)
- #5 Version `1.1.0-beta.3`, build 160
- #7 Trakt PKCE login (no client secret)
- #8 iOS framework bundle ID `com.mew.mobile`
- #9 Updater checks this fork's releases
- #10 Sideload stores: identity and no upstream versions
- #11 Sync workflow keeps fork store metadata on conflict
- #12 Upstream sync (luqmanfadlli `enhanced`)
- #13 Remove unused submodule gitlinks
- #18 Audit cleanup: legacy auto-close workflow, stale root feeds, run-mobile and baseline profile IDs
- #19 CLAUDE.md and project status
- #20 iOS job on `macos-latest` (later superseded by #24)
- #21 Decisions record (`docs/DECISIONS.md`)
- #22 Rename `Docs/` to `reference-stremio-addons/`
- #23 iOS job on Xcode 27.1 (later superseded by #24)
- #24 iOS job on the `xcode-27` runner image (Xcode 27 preinstalled)

## Verified
- Trakt login (PKCE): working on device
- SIMKL login: working on device
- iOS test build (`test-build`, beta, ios-only): passed, run `45406265542`. Produces an unsigned IPA (`nuvio-1.1.0-beta.3-ios-full-release`). No release or tag created.

## In progress
- Nothing in code.

## On hold
- #15 Bundle ID migration and Keychain: needs a plan. Saved login and PIN reset on bundle change.
- #16 URL scheme: `nuvio://` is shared with the upstream app. Trakt and SIMKL use it now.
- iOS public release and signing: no Apple Developer account or signing secrets. Sideload only (AltStore, SideStore, Sideloadly), re-sign every 7 days.

## Known issues
- `validate_pr_body` (PR template check) fails on every fork PR. See #17.
- `ios-test-build.yml` is not verified after the runner change.
- Owners are hardcoded to `luqmanfadlli` in several workflows (audit M2), including `CONTRIBUTIONS_URL` in `mobile-release.yml`.
- Release jobs do not check iOS required properties (audit M7).
- Action versions are tags, not SHAs (audit M10).

## Pending decisions (owner)
- #17 PR template check: keep, make fork-aware, or disable.
- #16 URL scheme: keep `nuvio://` or rename and re-register OAuth apps.
- #15 Plan the bundle ID migration now or after iOS ships.
- iOS: get an Apple Developer account, or stay sideload only.
- Android `draft` release `1.1.0-beta.3`: when the owner is ready.
- Audit H4: check whether an old build with a Trakt secret was published. If yes, rotate it.

## Open cleanup
- Audit medium items M2, M3, M5, M7, M8, M9, M10 (see `docs/AUDIT.md`).
