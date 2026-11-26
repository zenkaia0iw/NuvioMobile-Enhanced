package com.nuvio.app.features.settings

import androidx.compose.foundation.lazy.LazyListScope
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.cloudstream_unsupported_description
import nuvio.composeapp.generated.resources.cloudstream_unsupported_title
import org.jetbrains.compose.resources.stringResource

// iOS: CloudStream extensions run on Android only. Show the unsupported state.
internal actual fun LazyListScope.cloudStreamSettingsContent(isTablet: Boolean) {
    item {
        SettingsSection(
            title = stringResource(Res.string.cloudstream_unsupported_title),
            isTablet = isTablet,
        ) {
            SettingsGroup(isTablet = isTablet) {
                SettingsNavigationRow(
                    title = stringResource(Res.string.cloudstream_unsupported_title),
                    description = stringResource(Res.string.cloudstream_unsupported_description),
                    isTablet = isTablet,
                    onClick = {},
                )
            }
        }
    }
}
