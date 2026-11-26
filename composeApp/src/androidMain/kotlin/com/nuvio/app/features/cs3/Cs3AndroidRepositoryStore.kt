package com.nuvio.app.features.cs3

import android.content.Context
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.io.File

// Persists the list of repository URLs the user added. Stored in app-private
// storage next to the installed extensions. Only https URLs are accepted.
internal object Cs3AndroidRepositoryStore {
    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(String.serializer())
    private var root: File? = null

    fun initialize(context: Context) {
        if (root == null) {
            root = File(context.filesDir, "cs3").apply { mkdirs() }
        }
    }

    private fun file(): File = File(checkNotNull(root) { "Repository store not initialized." }, "repositories.json")

    fun list(): Result<List<String>> = runCatching {
        val target = file()
        if (!target.exists()) return@runCatching emptyList()
        try {
            json.decodeFromString(serializer, target.readText())
        } catch (error: Exception) {
            throw IllegalStateException("Repository list is corrupt. Nothing was changed.", error)
        }
    }

    fun add(url: String): Result<List<String>> = runCatching {
        val normalized = url.trim()
        require(normalized.lowercase().startsWith("https://")) { "Repository URL must use https." }
        val current = list().getOrThrow()
        if (normalized in current) return@runCatching current
        val updated = current + normalized
        write(updated)
        updated
    }

    fun remove(url: String): Result<List<String>> = runCatching {
        val updated = list().getOrThrow().filterNot { it == url }
        write(updated)
        updated
    }

    private fun write(urls: List<String>) {
        val target = file()
        val partial = File(target.parentFile, "repositories.json.part")
        partial.writeText(json.encodeToString(serializer, urls))
        if (target.exists()) target.delete()
        check(partial.renameTo(target)) { "Could not save the repository list." }
    }
}
