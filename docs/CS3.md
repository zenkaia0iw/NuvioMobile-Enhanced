# CloudStream CS3 plan and progress

Owner: zenkaia0iw. Engineering lead: Claude. Branch: `feature/cs3` (single branch). PR: #28 (draft until acceptance criteria are met).
Last updated: 2026-11-26.

## Scope decisions (owner)
- CS3 execution is mandatory, Android only. iOS shows an unsupported state.
- Repository and extension code is platform neutral. No third-party repository or streaming source is hardcoded, bundled, or recommended by this project. Users add their own repository URLs.
- Settings entry: Settings → Content → Discovery → CloudStream, directly below Plugins. Separate CloudStream screen with a legal and content disclaimer.
- Credit CloudStream (GPL-3.0) and Nuvio (GPL-3.0).
- Test extension: a demo `.cs3` written by this project. It returns static test data.

## License
- Nuvio: GPL-3.0 (verified in `LICENSE`).
- CloudStream (`recloudstream/cloudstream`): GPL-3.0 (verified via GitHub repo metadata, 2026-11-25).
- Consequence: code and API signatures can be used if the project stays GPL-3.0, source stays public, and copyright and license notices are kept. Each ported file names the original file and the change in its header.

## CS3 format (facts)
- A `.cs3` file is a zip archive with `classes.dex` and `manifest.json`. It is loaded at runtime with a class loader.
- A repository is two JSON files: `repo.json` (repository metadata, plugin list URLs) and `plugins.json` (plugin entries with `url`, `version`, `fileHash`, and so on).
- Plugins subclass `MainAPI` and register through a plugin entry point annotated `@CloudstreamPlugin`.
- Plugin code imports `com.lagradost.cloudstream3.*`.
- DEX loading depends on Android APIs. It cannot run on iOS.

## Platform split
- `commonMain`: models, repository parser and client, partition with skip reasons, runtime contract, settings page `expect`.
- `androidMain`: SHA-256, installer (download, hash verify, atomic write, index, uninstall), settings page actual, runtime (stub until M4).
- `iosMain`: unsupported actuals (settings page, runtime, SHA-256).

## Upstream files touched (record every change)
- `composeApp/src/commonMain/kotlin/com/nuvio/app/features/settings/SettingsModels.kt`: `CloudStream` page entry, icon mapping.
- `composeApp/src/commonMain/kotlin/com/nuvio/app/features/settings/SettingsScreen.kt`: routing for mobile and tablet, click handler for discovery.
- `composeApp/src/commonMain/kotlin/com/nuvio/app/features/settings/ContentDiscoverySettingsPage.kt`: parameter, imports, CloudStream row below Plugins.
- Applied by `scripts/cs3-wire-settings.py`.

## Milestones and status
| Milestone | Scope | Status |
|---|---|---|
| M0 | Research record (`docs/CS3.md`, `docs/CS3.md` research) | Done, merged (#27) |
| M1 | Models, parser, validation, runtime contract, platform stubs, parser unit tests | Done in branch. Android build green. |
| M2 | Settings entry, separate CloudStream page, disclaimer, iOS unsupported state | Done in branch. Android build green. |
| M3 | Installer (download, SHA-256 verify, https only, 50 MB cap, atomic write, index, uninstall), repo client with skip reasons, installer tests | Code done in branch. Unit tests: pending CI verification (see progress log). |
| M3b | Repository add, remove, and list UI. Install and uninstall buttons. Skipped plugin count shown | Not started |
| M4a | Android DEX loader spike: load `classes.dex` from a `.cs3`, find the `@CloudstreamPlugin` entry, instantiate | Not started |
| M4b | Phase 1 compatibility layer (`MainAPI`, `SearchResponse`, `MainPageRequest`, `HomePageResponse`, `SubtitleFile`, utils) with license headers | Not started |
| M4c | Demo `.cs3` project (static data), runtime calls `mainPage` and `search`, results in the app | Not started |
| M5 | Update detection, safe update (keep previous file until the new one loads), rollback | Not started |
| M6 | Search UI (name, language, type), bulk actions, error states | Not started |
| M7 | Device test on the owner's phone | Owner only, last stage |

## Remaining work (batches)
- M3b: 1 batch (UI and repo storage).
- M4a, M4b, M4c: 3 batches (the runtime is the largest risk).
- M5: 1 batch.
- M6: 1 batch.
- Total: 6 Claude batches, plus 1 owner device test.
These estimates depend on the M4a spike result. If DEX loading needs a different approach, the count changes and the owner is told before continuing.

## Progress log
- 2026-11-25: M0 merged (#27). M1 batch pushed. Build green for M1 and M2 on PR #28.
- 2026-11-26: M3 batch (installer, repo client, SHA-256, settings storage init). Build `fullDebug` green (run 45759206490, head `5ca0033`).
- 2026-11-26: Installer corrupt-index fix (`8a10416`), skip reasons (`09ea6a3`, `78263cc`), size cap (`bd6f0cd`), installer seam and https enforcement (`2777b75`), installer tests (`24e7e10`), PR tests workflow (`c57f6ef`, `faaa8fc`).
- 2026-11-26: CI runs for `faaa8fc` (build `45760995290`, tests `45760995383`) were started. Results are not recorded here until verified.
- 2026-11-26: CLAUDE.md updated with working rules (`f2b24f0`). CI runs on this head are in progress.

## Acceptance criteria
1. Add a repository by URL, browse it, and see its extensions.
2. Search by name, language and type.
3. Install a `.cs3`, verify hash, see it installed.
4. Demo `.cs3` runs `mainPage` and `search` on Android.
5. Uninstall, update detection, safe update, and rollback work.
6. Runtime errors show a clear message and do not crash the app.
7. iOS shows the unsupported state. Existing Nuvio plugins and features are unchanged.
8. Disclaimer is visible on the CloudStream screen.
9. CI is green (Android build and unit tests). Unit tests pass.
10. Device test on the owner's phone passes.

## Blockers
1. Device testing is the owner's job, last stage.
2. The compatibility layer must match CloudStream's real signatures. Copy from the CloudStream source at a pinned version, not from memory.
3. DEX loading is Android-only, and cannot be verified without a device or emulator run in CI.
