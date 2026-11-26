# Project status

Last updated: 2026-11-26. Update this file in the same PR as any change to shipped work, open work, or decisions.

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
- #20 iOS job on `macos-latest` (superseded by #24)
- #21 Decisions record (`docs/DECISIONS.md`)
- #22 Rename `Docs/` to `reference-stremio-addons/`
- #23 iOS job on Xcode 27.1 (superseded by #24)
- #24 iOS job on the `xcode-27` runner image
- #25 Status update after iOS test build
- #26 Round-2 decisions

## Verified
- Trakt login (PKCE): working on device.
- SIMKL login: working on device.
- iOS test build (`test-build`, beta, ios-only): passed, run `45406265542`. Unsigned IPA only. No release created.

## In progress: CloudStream CS3 (PR #28, draft, branch `feature/cs3`)
- M1 (models, parser, runtime contract): Android build green.
- M2 (Settings → Content → Discovery → CloudStream page, disclaimer, iOS unsupported): Android build green.
- M3 (installer, repo client, SHA-256, size cap, https, tests): code pushed. Build green on `5ca0033`. Installer and partition tests: result pending on the latest head.
- Remaining Claude batches: M3b (repo and install UI), M4a (DEX spike), M4b (compat layer), M4c (demo `.cs3`), M5 (updates and rollback), M6 (search UI). Six batches in total.
- Owner: device test (M7) at the end.
- Details: `docs/CS3.md`.

## On hold
- #15 Bundle ID migration and Keychain: needs a plan. Saved login and PIN reset on bundle change.
- #16 URL scheme: `nuvio://` is shared with the upstream app. Trakt and SIMKL use it now.
- iOS public release and signing: no Apple Developer account or signing secrets. Sideload only (AltStore, SideStore, Sideloadly), re-sign every 7 days.

## Known issues
- `ios-test-build.yml` is not verified after the runner change.
- Owners are hardcoded to `luqmanfadlli` in several workflows (audit M2), including `CONTRIBUTIONS_URL` in `mobile-release.yml`.
- Release jobs do not check iOS required properties (audit M7).
- Action versions are tags, not SHAs (audit M10).
- PR CI sometimes does not start for connector commits. Trigger with an empty commit push.

## Decisions (owner)
- #17 PR template check: disabled on this fork.
- #16 URL scheme: keep `nuvio://`.
- #15 Keychain migration: after iOS ships.
- iOS: sideload only.
- Android draft release `1.1.0-beta.3`: when the owner is ready.
- Audit H4 (Trakt secret): not applicable. No secret was ever issued.
- CS3 repository trust: per-repository explicit trust prompt (see `docs/CS3.md`, pending in M3b).

## Open cleanup
- Audit medium items M2, M3, M5, M7, M8, M9, M10 (see `docs/AUDIT.md`).
- Android `draft` release `1.1.0-beta.3`: when the owner is ready.
