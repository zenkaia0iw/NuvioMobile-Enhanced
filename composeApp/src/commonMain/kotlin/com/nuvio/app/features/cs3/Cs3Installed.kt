package com.nuvio.app.features.cs3

import kotlinx.serialization.Serializable

// Installed CS3 extension record, persisted by the Android installer.
@Serializable
internal data class InstalledCs3Plugin(
    val internalName: String,
    val version: Int,
    val fileName: String,
    val sha256: String,
    val sourceUrl: String,
    val repoUrl: String,
)
