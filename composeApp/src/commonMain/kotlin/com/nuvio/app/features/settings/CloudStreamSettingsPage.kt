package com.nuvio.app.features.settings

import androidx.compose.foundation.lazy.LazyListScope

// CloudStream settings page. Platform-specific content is provided by
// the Android and iOS actuals. Common code holds no Android types.
internal expect fun LazyListScope.cloudStreamSettingsContent(isTablet: Boolean)
