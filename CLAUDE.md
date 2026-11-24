# CLAUDE.md

Project context for Claude Code and any agent working in this repo. Read this first.

## What this repo is
Fork of `luqmanfadlli/NuvioMobile-Enhanced` (which forks `NuvioMedia/NuvioMobile`), maintained by `zenkaia0iw`. Kotlin Multiplatform app: `composeApp/` (shared), `androidApp/`, `iosApp/`.

## Rules
- Never commit directly to `enhanced`. Every change goes through a branch and PR into `enhanced`.
- One problem per PR. Use the PR template sections.
- Do not touch `MPVKit` (submodule, needed for player).
- Do not send PRs to upstream repos. This is fork-internal work.
- Never print or commit secret values. Only names.

## Identity (keep in sync)
- Android applicationId: `com.mew.mobile` (debug: `com.mew.mobile.debug`)
- iOS bundle ID: `com.mew.mobile` (`iosApp/Configuration/Config.xcconfig`)
- Kotlin framework bundle: `com.mew.mobile` (`composeApp/build.gradle.kts`)
- Updates are checked against `zenkaia0iw/NuvioMobile-Enhanced` (`AppUpdaterRepository.kt`)
- URL scheme `nuvio://` is still shared with upstream (see issue #16)

## Common commands
- Android debug: `./gradlew :androidApp:assembleFullDebug`
- Shell script syntax: `bash -n scripts/run-mobile.sh`
- Release: GitHub Actions → **Build Mobile Release**. Use `dry-run` first, then `test-build`, then `draft`.

## Sync
- Daily **Upstream sync** workflow merges `luqmanfadlli/NuvioMobile-Enhanced` `enhanced` into a `sync/upstream-YYYYMMDD` branch and opens a PR.
- Store metadata files (`store.json`, `store-beta.json`) keep this fork's side on conflict.
- See `docs/FORK_WORKFLOW.md`.

## Status
Current state and open work: `docs/STATUS.md`. Decisions: `docs/DECISIONS.md`.
