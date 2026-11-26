package com.nuvio.app.features.settings

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.nuvio.app.features.cs3.Cs3AndroidInstaller
import com.nuvio.app.features.cs3.createCs3Runtime
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.cloudstream_credit
import nuvio.composeapp.generated.resources.cloudstream_disclaimer
import nuvio.composeapp.generated.resources.cloudstream_repositories_empty
import nuvio.composeapp.generated.resources.cloudstream_section_disclaimer
import nuvio.composeapp.generated.resources.cloudstream_section_repositories
import nuvio.composeapp.generated.resources.cloudstream_settings_title
import org.jetbrains.compose.resources.stringResource

internal actual fun LazyListScope.cloudStreamSettingsContent(isTablet: Boolean) {
    item {
        val context = LocalContext.current
        remember(context) { Cs3AndroidInstaller.initialize(context.applicationContext) }
        val runtime = remember { createCs3Runtime() }
        val runtimeNote = runtime.unsupportedReason

        SettingsSection(
            title = stringResource(Res.string.cloudstream_section_disclaimer),
            isTablet = isTablet,
        ) {
            SettingsGroup(isTablet = isTablet) {
                SettingsNavigationRow(
                    title = stringResource(Res.string.cloudstream_section_disclaimer),
                    description = stringResource(Res.string.cloudstream_disclaimer),
                    isTablet = isTablet,
                    onClick = {},
                )
                SettingsNavigationRow(
                    title = stringResource(Res.string.cloudstream_settings_title),
                    description = stringResource(Res.string.cloudstream_credit),
                    isTablet = isTablet,
                    onClick = {},
                )
            }
        }

        SettingsSection(
            title = stringResource(Res.string.cloudstream_section_repositories),
            isTablet = isTablet,
        ) {
            SettingsGroup(isTablet = isTablet) {
                SettingsNavigationRow(
                    title = stringResource(Res.string.cloudstream_section_repositories),
                    description = runtimeNote ?: stringResource(Res.string.cloudstream_repositories_empty),
                    isTablet = isTablet,
                    onClick = {},
                )
            }
        }
    }
}
