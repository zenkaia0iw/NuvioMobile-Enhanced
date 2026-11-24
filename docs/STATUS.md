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
- #10 Sideload stores (`store.json`, `store-beta.json`) identity and no upstream versions
- #11 Sync workflow keeps fork store metadata on conflict
- #12 Upstream sync (luqmanfadlli `enhanced`)
- #13 Remove unused submodule gitlinks
- #18 Audit cleanup: legacy auto-close workflow, stale root feeds, run-mobile and baseline profile IDs

## In progress
- Nothing in code. Trakt and SIMKL login on-device test is pending (owner).

## On hold
- #15 Bundle ID migration and Keychain: needs a plan. Saved login and PIN reset on bundle change.
- #16 URL scheme: `nuvio://` is shared with the upstream app.
- iOS signing and release: no Apple Developer account or signing secrets yet. iOS build is skipped in release.

## Known issues
- Release `test-build` for beta has not been run on the latest `enhanced`.
- `validate_pr_body` (PR template check) fails on every fork PR. See #17.
- `xcode-27` runner label is not a standard GitHub runner. iOS job may not start.
- CI passes `CONTRIBUTIONS_URL` only on iOS. Owners are hardcoded to `luqmanfadlli` in several workflows (audit M2).
- Release jobs do not check iOS required properties (audit M7).

## Pending decisions (owner)
- #17 PR template check: keep, make fork-aware, or disable.
- #16 URL scheme: keep `nuvio://` or rename and re-register OAuth apps.
- #15 Whether to plan the bundle ID migration now or after iOS ships.
- iOS: get an Apple Developer account, or stay Android-only for now.

## Open cleanup
- #14 Rename `Docs/` (case clash with `docs/`).
- Audit medium items M2, M3, M5, M7, M8, M9, M10 (see `docs/AUDIT.md`).
