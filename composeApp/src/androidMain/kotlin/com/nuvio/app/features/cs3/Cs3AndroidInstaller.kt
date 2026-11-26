package com.nuvio.app.features.cs3

import android.content.Context
import com.nuvio.app.core.network.SharedOkHttpClient
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.Request
import java.io.File

// Android CS3 file lifecycle: download, verify SHA-256, atomic write,
// persistent install index, uninstall. Files live in app-private storage.
internal object Cs3AndroidInstaller {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    private val listSerializer = ListSerializer(InstalledCs3Plugin.serializer())
    private var root: File? = null

    fun initialize(context: Context) {
        if (root == null) {
            root = File(context.filesDir, "cs3").apply { mkdirs() }
        }
    }

    private fun dir(): File = checkNotNull(root) { "CloudStream storage is not initialized." }

    private fun indexFile(): File = File(dir(), "installed.json")

    fun listInstalled(): List<InstalledCs3Plugin> {
        val file = indexFile()
        if (!file.exists()) return emptyList()
        return runCatching { json.decodeFromString(listSerializer, file.readText()) }
            .getOrDefault(emptyList())
    }

    private fun saveInstalled(records: List<InstalledCs3Plugin>) {
        indexFile().writeText(json.encodeToString(listSerializer, records))
    }

    // Refuses plugins without a hash: integrity cannot be verified without one.
    fun install(entry: Cs3PluginEntry, repoUrl: String): Result<InstalledCs3Plugin> = runCatching {
        val expectedHash = entry.fileHash?.trim()?.takeIf { it.isNotEmpty() }
            ?: error("Repository gave no hash for ${entry.internalName}. Install refused.")
        val bytes = download(entry.url)
        val actualHash = sha256Hex(bytes)
        check(actualHash.equals(expectedHash, ignoreCase = true)) {
            "Hash mismatch for ${entry.internalName}. Nothing was installed."
        }

        val fileName = "${entry.internalName}.cs3"
        val target = File(dir(), fileName)
        val partial = File(dir(), "$fileName.part")
        partial.writeBytes(bytes)
        if (target.exists()) target.delete()
        check(partial.renameTo(target)) { "Could not save ${entry.internalName}." }

        val record = InstalledCs3Plugin(
            internalName = entry.internalName,
            version = entry.version,
            fileName = fileName,
            sha256 = actualHash,
            sourceUrl = entry.url,
            repoUrl = repoUrl,
        )
        saveInstalled(listInstalled().filterNot { it.internalName == entry.internalName } + record)
        record
    }

    fun uninstall(internalName: String): Result<Unit> = runCatching {
        val current = listInstalled()
        current.firstOrNull { it.internalName == internalName }?.let { record ->
            File(dir(), record.fileName).delete()
        }
        saveInstalled(current.filterNot { it.internalName == internalName })
    }

    private fun download(url: String): ByteArray {
        val request = Request.Builder().url(url).build()
        SharedOkHttpClient.base.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "Download failed with status ${response.code}." }
            return response.body?.bytes() ?: error("Empty download.")
        }
    }
}
