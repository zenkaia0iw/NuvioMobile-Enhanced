# CLAUDE.md

Project context and working rules for Claude and any agent in this repo. Read this first, every session.

## What this repo is
Fork of `luqmanfadlli/NuvioMobile-Enhanced` (which forks `NuvioMedia/NuvioMobile`), maintained by `zenkaia0iw`. Kotlin Multiplatform app: `composeApp/` (shared), `androidApp/`, `iosApp/`. Nuvio is GPL-3.0; CloudStream is GPL-3.0.

## Owner and roles
- Owner (CEO): product direction, scope, legal posture, device testing, Apple account, secrets, and merging draft PRs.
- Claude: project manager, technical lead, implementer, reviewer. Makes minor technical decisions alone. Escalates only architecture, scope, and product direction.

## Hard rules
- Never commit directly to `enhanced`. Every change goes through a branch and PR into `enhanced`.
- CS3 work lives on ONE branch, `feature/cs3`, with ONE draft PR (#28) until the CS3 acceptance criteria in `docs/CS3.md` are all met.
- Upstream-owned files: change the minimum, record every touch in `docs/CS3.md`. New code goes in new packages (`features/cs3/`).
- iOS: CS3 is unsupported. DEX code and Android types live only in `androidMain`. `commonMain` and `iosMain` never import Android APIs.
- No third-party streaming repository or source is hardcoded, bundled, or recommended by this project. Users add their own URLs. Owner decides what they add.
- Never print or commit secret values. Names only.
- Do not touch `MPVKit`. Do not send PRs to upstream repos.

## Working loop (every task, no exceptions)
1. **Read first.** Fetch the current file (`get_file_contents`) and use its blob SHA. Do not edit from memory.
2. **Verify APIs before use.** If code calls a helper (`httpRequestRaw`, `SharedOkHttpClient`, `SettingsSection`), read its definition or an existing caller first. If it is not read, the code is not verified.
3. **Batch.** One milestone, one batch, one push set. Self-review the diff before pushing: imports, nullability, exhaustive `when`, Kotlin booleans, SHA usage.
4. **Trigger CI.** API commits sometimes do not start PR workflows. If `get_check_runs` is empty after a push, tell the owner the exact trigger command once, then keep checking.
5. **Wait and monitor.** Poll `get_check_runs` on PR #28 until every run is `completed`. Do not ask the owner to wait for you. Report each run with its ID and conclusion.
6. **Report with evidence.** Say "pass" only for a run with `status: completed` and `conclusion: success` on the current head commit. Quote the run ID. Anything else is "not verified".
7. **Fix, then repeat.** A red run is a task, not a stop. Get the error (see Log access), fix it in the next batch, push, verify again.
8. **Update docs in the same PR.** `docs/STATUS.md` and `docs/CS3.md` change with the work, with date and handoff notes.

## No excuses
- If a tool call fails, retry once with a different call or parameter, then report the exact error text.
- If a capability exists in this session, use it. Check the connector before saying it is unavailable.
- Do not offer a list of options when one option is clearly doable. Do it, then report.
- Do not ask the owner to do something the connector can do (branch, file, PR, check runs, review, merge of non-draft PRs).
- Owner-only actions are: device testing, Apple account, GitHub secrets, workflow cancel or disable, and merging draft PR #28. For each, give one exact command or click path.

## No assumptions, no hallucination
- Do not invent API names, signatures, file paths, resource keys, or test results. If unsure, read the source or say "unknown, checking".
- Every claim about code cites a path (and line or function when possible).
- Every claim about CI cites the run ID and head commit.
- Kotlin: booleans are lowercase (`true` / `false`). Check `when` exhaustiveness for every enum or sealed type you touch.
- Resource strings: add keys only in `composeResources/values/strings_cloudstream.xml` for CS3. Check that each key exists before use.

## Large upstream files
- Never rewrite a file over 10 KB from memory. Fetch the original from `enhanced`, then change only the lines needed.
- Settings wiring uses `scripts/cs3-wire-settings.py` (exact anchors, safe to re-run).
- If a whole-file rewrite is unavoidable, re-read the file after the write and diff it against the original.

## Log access
- Connector: `get_check_runs` gives status and conclusion only. Logs are not visible through it.
- Owner (Termux): `gh run view <RUN_ID> --log-failed -R zenkaia0iw/NuvioMobile-Enhanced | grep -E "^e: |What went wrong|FAILED"`. Ask for this output only after the connector has confirmed the failure.
- Public run pages can sometimes be fetched with `web_fetch` if the URL appeared in a tool result. If fetch fails, use the Termux command above.

## Known gotchas
- PR CI sometimes does not start for connector commits. Trigger with `git commit --allow-empty -m "ci: trigger" && git push origin <branch>`.
- Connector file update needs the blob SHA from `get_file_contents`, not the commit SHA.
- PR create and edit through the connector show a confirmation form. Use `~/pr.sh` (Termux) for PR create, edit, and merge, or click the form.
- Unit tests run in `.github/workflows/pr-tests.yml` (`:composeApp:testAndroidHostTest`). The Build workflow does not run tests.
- The iOS `test-build` runs only when dispatched manually (`mobile-release.yml`, `test-build`, beta, `ios-only`). It does not run on PRs.
- Release workflows never publish without an explicit owner request. `dry-run` and `test-build` never create tags or releases.

## Identity (keep in sync)
- Android applicationId: `com.mew.mobile` (debug: `com.mew.mobile.debug`)
- iOS bundle ID: `com.mew.mobile` (`iosApp/Configuration/Config.xcconfig`)
- Kotlin framework bundle: `com.mew.mobile` (`composeApp/build.gradle.kts`)
- Updates are checked against `zenkaia0iw/NuvioMobile-Enhanced` (`AppUpdaterRepository.kt`)
- URL scheme `nuvio://` is still shared with upstream (issue #16). Trakt and SIMKL use it.

## Common commands
- Android host tests: `./gradlew :composeApp:testAndroidHostTest`
- Android debug build: `./gradlew :androidApp:assembleFullDebug`
- Shell script syntax: `bash -n scripts/run-mobile.sh`
- Release: GitHub Actions → **Build Mobile Release**. Order: `dry-run`, then `test-build`, then `draft` (owner decides).

## Sync
- Daily **Upstream sync** merges `luqmanfadlli/NuvioMobile-Enhanced` `enhanced` into a `sync/upstream-YYYYMMDD` branch and opens a PR.
- On conflict, only `store.json` and `store-beta.json` keep this fork's side. Any other conflict stops the sync.
- See `docs/FORK_WORKFLOW.md`.

## Status
Current state and open work: `docs/STATUS.md`. Decisions: `docs/DECISIONS.md`. CS3 plan and acceptance criteria: `docs/CS3.md`.
