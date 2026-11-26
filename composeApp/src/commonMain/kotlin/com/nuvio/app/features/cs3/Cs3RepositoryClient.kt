package com.nuvio.app.features.cs3

import com.nuvio.app.features.addons.httpRequestRaw

// Fetches a CloudStream repository: repo.json, then every plugin list it names.
// Uses the app's shared HTTP helper. No binary downloads here (see the Android installer).

internal data class Cs3RepositoryContent(
    val repoUrl: String,
    val manifest: Cs3RepositoryManifest,
    val plugins: List<Cs3PluginEntry>,
    val skipped: List<Cs3SkippedPlugin>,
)

internal object Cs3RepositoryClient {

    suspend fun fetch(repoUrl: String): Result<Cs3RepositoryContent> = runCatching {
        require(repoUrl.lowercase().startsWith("https://")) { "Repository URL must use https." }
        val manifest = Cs3RepositoryParser.parseManifest(getText(repoUrl)).getOrThrow()
        val pluginListUrl = manifest.pluginLists.firstOrNull()
            ?: error("Repository has no plugin list.")
        val all = Cs3RepositoryParser.parsePlugins(getText(pluginListUrl)).getOrThrow()
        val partition = Cs3RepositoryParser.partitionPlugins(all)
        Cs3RepositoryContent(
            repoUrl = repoUrl,
            manifest = manifest,
            plugins = partition.valid,
            skipped = partition.skipped,
        )
    }

    private suspend fun getText(url: String): String {
        val response = httpRequestRaw(
            method = "GET",
            url = url,
            headers = mapOf("Accept" to "application/json"),
            body = "",
        )
        if (response.status !in 200..299) {
            error("Request to $url failed with status ${response.status}.")
        }
        return response.body
    }
}
