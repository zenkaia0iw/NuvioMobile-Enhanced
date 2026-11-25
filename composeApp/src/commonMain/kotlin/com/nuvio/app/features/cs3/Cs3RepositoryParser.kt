package com.nuvio.app.features.cs3

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

// Pure parsing and validation for CloudStream repository files.
// No network, no Android APIs. Safe in commonMain.

internal object Cs3RepositoryParser {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun parseManifest(text: String): Result<Cs3RepositoryManifest> = runCatching {
        json.decodeFromString(Cs3RepositoryManifest.serializer(), text)
    }

    fun parsePlugins(text: String): Result<List<Cs3PluginEntry>> = runCatching {
        json.decodeFromString(ListSerializer(Cs3PluginEntry.serializer()), text)
    }
}

sealed interface Cs3PluginValidation {
    data object Valid : Cs3PluginValidation
    data class Invalid(val reason: String) : Cs3PluginValidation
}

// Structural checks only. Compatibility (apiVersion, runtime support) is
// checked by the runtime in M3 and M4.
internal fun Cs3PluginEntry.validate(): Cs3PluginValidation {
    if (internalName.isBlank()) {
        return Cs3PluginValidation.Invalid("Plugin has no internalName.")
    }
    if (!url.lowercase().startsWith("https://")) {
        return Cs3PluginValidation.Invalid("Plugin URL must use https.")
    }
    return Cs3PluginValidation.Valid
}
