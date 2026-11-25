# CloudStream CS3 research (M0)

Status: research record only. No runtime code exists yet.
Date: 2026-11-25. Owner: zenkaia0iw. Engineering lead: Claude (single session, no sub-agents available).

## Scope decisions (owner)
- CS3 execution is mandatory, Android only. iOS shows an unsupported state.
- Repository and extension code is platform neutral. No third-party repository or streaming source is hardcoded, bundled, or recommended by this project. Users add their own repository URLs.
- Settings entry: Settings → Content → Discovery → CloudStream, directly below Plugins. Separate CloudStream screen. Nuvio plugins are not mixed with CS3 extensions.
- Credit CloudStream (GPL-3.0) in the README and in the About/licenses screen.
- Test extension: a demo `.cs3` written by this project. It returns static test data. It exists to verify the runtime end to end.

## License
- Nuvio: GPL-3.0 (verified in `LICENSE`).
- CloudStream (`recloudstream/cloudstream`): GPL-3.0 (verified via GitHub repo metadata, 2026-11-25).
- Consequence: code and API signatures can be used if the project stays GPL-3.0, source stays public, and copyright and license notices are kept. Each ported file must carry a header that names the original file and the change.

## CS3 format (facts)
- A `.cs3` file is a zip archive that contains `classes.dex` and `manifest.json`. It is loaded at runtime with a custom class loader.
- A CloudStream repository is two JSON files. `repo.json` describes the repository. `plugins.json` lists plugins with their `.cs3` URL, version, and file hash. The hash is used to verify downloads.
- Plugins subclass `MainAPI` and register themselves with a plugin entry point annotated `@CloudstreamPlugin`.
- Plugin code imports `com.lagradost.cloudstream3.*` (for example `MainAPI`, `MainPageRequest`, `SearchResponse`, `SearchResponseList`, `SubtitleFile`, `utils.loadExtractor`, `utils.newExtractorLink`).
- The loader depends on Android APIs (DEX class loading). It cannot run on iOS or on a plain JVM without stubs.

## Platform note
- Repository browsing, metadata, search, and file lifecycle are common code.
- DEX loading and CloudStream runtime are `androidMain` only, behind an interface such as `Cs3Runtime`.
- `iosMain` returns an unsupported result.

## Reuse in Nuvio (audit)
- `features/plugins/`: repository model (`PluginRepositoryItem`), scraper model, enable/disable, refresh interval, cache. Reuse the patterns, not the classes.
- `features/updater/`: release update checks. Not reused for extensions.
- `quickjs-kt` (full flavor): JS runtime for Nuvio plugins. Not used for CS3.

## Target CloudStream API surface (phase 1 subset)
Only names observed in public CloudStream plugin code are listed. Signatures must be copied from the CloudStream source at the matching version, not from memory.
- Entry: `@CloudstreamPlugin`, plugin registration
- Provider: `MainAPI` with `mainPage`, `search`, `load`, `loadLinks`
- Models: `MainPageRequest`, `HomePageResponse`, `SearchResponse`, `SearchResponseList`, `SubtitleFile`
- Utilities: `utils.loadExtractor`, `utils.newExtractorLink`
- Anything beyond this subset is out of phase 1 and must be listed before it is added.

## Blockers (current)
1. No build or device loop in chat. Android CI runs take about 20-30 minutes. Device testing happens at the end, on the owner's phone.
2. The API compatibility layer is large. It must match CloudStream's real signatures for the subset above.
3. Sub-agents are not available in this environment. Work is sequential.
4. iOS cannot load DEX. This is accepted and shown as unsupported.

## Upstream conflict map (initial)
Files that existing upstream code owns and this work may touch. Each change must be minimal and recorded.
- Settings screen file that lists Plugins (add one row for CloudStream).
- Navigation file that registers settings routes (add one route).
- `composeApp/build.gradle.kts` (only if a dependency is needed).
- `README.md` (credit line only).
New code lives only in a new package, for example `features/cs3/` in `commonMain` (interfaces and UI state) and `androidMain` (runtime). Existing plugin files are not modified.

## Milestones (proposed)
- M0: this document (done on branch `docs/cs3-research`).
- M1: `features/cs3/` common interfaces and repository parser (`repo.json`, `plugins.json`), plus unit tests in `commonTest`. CI build.
- M2: Settings → Content → Discovery → CloudStream entry and screen, with iOS unsupported state.
- M3: Android install, verify hash, store, uninstall, and version tracking for `.cs3` files.
- M4: Android DEX loader and phase 1 compatibility layer. Demo `.cs3` executes `mainPage` and `search`.
- M5: update detection, safe update (keep previous file until the new one loads), and rollback on failure.
- M6: device test on the owner's phone (last stage).

## Acceptance criteria
- Add a repository by URL, browse it, search, install a `.cs3`, run its `mainPage` and `search`, and see results on Android.
- Uninstall works. Update detection and safe update work.
- iOS shows the unsupported state. Existing Nuvio plugins and features are unchanged.
- Runtime errors show a clear message and do not crash the app.
