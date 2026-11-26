#!/usr/bin/env python3
"""Wire the CloudStream settings entry into upstream-owned settings files.

Exact-match anchors where the text is known, regex where whitespace may vary.
Each anchor must occur the expected number of times, otherwise the script
stops and changes nothing. Re-running is safe: if already wired, it exits.
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SETTINGS = ROOT / "composeApp/src/commonMain/kotlin/com/nuvio/app/features/settings"
MODELS = SETTINGS / "SettingsModels.kt"
SCREEN = SETTINGS / "SettingsScreen.kt"
DISCOVERY = SETTINGS / "ContentDiscoverySettingsPage.kt"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def write(path: Path, text: str) -> None:
    path.write_text(text, encoding="utf-8")


def replace_exact(text: str, old: str, new: str, expected: int, label: str) -> str:
    found = text.count(old)
    if found != expected:
        sys.exit(f"[{label}] anchor found {found} times, expected {expected}. No files changed.")
    return text.replace(old, new)


def replace_regex(text: str, pattern: str, template: str, expected: int, label: str) -> str:
    regex = re.compile(pattern, re.MULTILINE)
    matches = list(regex.finditer(text))
    if len(matches) != expected:
        sys.exit(f"[{label}] pattern matched {len(matches)} times, expected {expected}. No files changed.")
    return regex.sub(template, text)


def already_wired() -> bool:
    return "SettingsPage.CloudStream" in read(SCREEN) and "CloudStream(" in read(MODELS)


def main() -> None:
    if already_wired():
        print("CloudStream settings already wired. Nothing to do.")
        return

    # 1. Models: enum entry after Plugins, and icon mapping.
    models = read(MODELS)
    models = replace_exact(
        models,
        "    Plugins(\n"
        "        titleRes = Res.string.compose_settings_page_plugins,\n"
        "        category = SettingsCategory.Watch,\n"
        "        parentPage = ContentDiscovery,\n"
        "    ),\n",
        "    Plugins(\n"
        "        titleRes = Res.string.compose_settings_page_plugins,\n"
        "        category = SettingsCategory.Watch,\n"
        "        parentPage = ContentDiscovery,\n"
        "    ),\n"
        "    CloudStream(\n"
        "        titleRes = Res.string.cloudstream_settings_title,\n"
        "        category = SettingsCategory.Watch,\n"
        "        parentPage = ContentDiscovery,\n"
        "    ),\n",
        expected=1,
        label="models-enum",
    )
    models = replace_regex(
        models,
        r"^( *)SettingsPage\.Plugins -> ([^\n]+)\n",
        r"\g<0>\g<1>SettingsPage.CloudStream -> Icons.Default.Extension,\n",
        expected=1,
        label="models-icon",
    )
    write(MODELS, models)

    # 2. Screen: page routing in mobile (first) and tablet (second) layouts.
    screen = read(SCREEN)
    pattern = re.compile(
        r"^( *)SettingsPage\.Plugins -> if \(AppFeaturePolicy\.pluginsEnabled\) "
        r"pluginsSettingsContent\(\) else addonsSettingsContent\(\)\n",
        re.MULTILINE,
    )
    matches = list(pattern.finditer(screen))
    if len(matches) != 2:
        sys.exit(f"[screen-routing] found {len(matches)} Plugins routes, expected 2. No files changed.")
    pieces, last = [], 0
    for index, match in enumerate(matches):
        is_tablet = "True" if index == 1 else "False"
        pieces.append(screen[last:match.end()])
        pieces.append(
            f"{match.group(1)}SettingsPage.CloudStream -> "
            f"cloudStreamSettingsContent(isTablet = {is_tablet})\n"
        )
        last = match.end()
    pieces.append(screen[last:])
    screen = "".join(pieces)

    screen = replace_regex(
        screen,
        r"^( *)onMediaServersClick = \{ onPageChange\(SettingsPage\.MediaServers\) \},\n",
        r"\g<0>\g<1>onCloudStreamClick = { onPageChange(SettingsPage.CloudStream) },\n",
        expected=1,
        label="screen-mobile-discovery",
    )
    screen = replace_regex(
        screen,
        r"^( *)onMediaServersClick = \{ openInlinePage\(SettingsPage\.MediaServers\) \},\n",
        r"\g<0>\g<1>onCloudStreamClick = { openInlinePage(SettingsPage.CloudStream) },\n",
        expected=1,
        label="screen-tablet-discovery",
    )
    write(SCREEN, screen)

    # 3. Discovery page: parameter, row below Plugins, and imports.
    discovery = read(DISCOVERY)
    discovery = replace_exact(
        discovery,
        "import nuvio.composeapp.generated.resources.compose_settings_page_plugins\n",
        "import nuvio.composeapp.generated.resources.compose_settings_page_plugins\n"
        "import nuvio.composeapp.generated.resources.cloudstream_settings_description\n"
        "import nuvio.composeapp.generated.resources.cloudstream_settings_title\n",
        expected=1,
        label="discovery-imports",
    )
    discovery = replace_exact(
        discovery,
        "    onMediaServersClick: () -> Unit,\n) {\n",
        "    onMediaServersClick: () -> Unit,\n"
        "    onCloudStreamClick: () -> Unit = {},\n"
        ") {\n",
        expected=1,
        label="discovery-param",
    )
    discovery = replace_exact(
        discovery,
        "                        onClick = onPluginsClick,\n"
        "                    )\n"
        "                }\n",
        "                        onClick = onPluginsClick,\n"
        "                    )\n"
        "                }\n"
        "                SettingsNavigationRow(\n"
        "                    title = stringResource(Res.string.cloudstream_settings_title),\n"
        "                    description = stringResource(Res.string.cloudstream_settings_description),\n"
        "                    isTablet = isTablet,\n"
        "                    onClick = onCloudStreamClick,\n"
        "                )\n",
        expected=1,
        label="discovery-row",
    )
    write(DISCOVERY, discovery)

    print("CloudStream settings wired into 3 upstream files.")
    print("Next: git diff --stat, then commit and push on feature/cs3.")


if __name__ == "__main__":
    main()
