package com.nuvio.app.features.cs3

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Models for CloudStream repository files: repo.json (repository manifest)
// and plugins.json (list of plugin entries). Field names follow the
// CloudStream repository format. Unknown fields are ignored on parse.

@Serializable
data class Cs3RepositoryManifest(
    val name: String,
    val description: String? = null,
    val manifestVersion: Int = 1,
    val pluginLists: List<String> = emptyList(),
)

@Serializable
data class Cs3PluginEntry(
    @SerialName("internalName") val internalName: String,
    val url: String,
    val name: String = internalName,
    val version: Int = 1,
    val status: Int = 1,
    val language: String? = null,
    val tvTypes: List<String> = emptyList(),
    val authors: List<String> = emptyList(),
    val description: String? = null,
    val iconUrl: String? = null,
    val apiVersion: Int? = null,
    val fileSize: Long? = null,
    val fileHash: String? = null,
)
