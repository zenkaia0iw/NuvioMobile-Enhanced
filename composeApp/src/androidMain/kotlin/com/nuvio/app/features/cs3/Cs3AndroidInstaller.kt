package com.nuvio.app.features.cs3

import android.content.Context
import com.nuvio.app.core.network.SharedOkHttpClient
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

// Android CS3 file lifecycle: download, verify SHA-256, atomic write,
// persistent install index, uninstall. Files live in app-private storage.
//
// Integrity note: the hash comes from the same repository as the file.
// It proves the download matches what the repository listed, not that the
// repository is trustworthy. Users decide which repositories to trust.
internal object Cs3AndroidInstaller {
    // Upper bound for one extension file. Protects memory and storage from oversized downloads.
    const val MAX_CS3_BYTES: Long = 50L * 1024 * 1024

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

    // Missing index means nothing is installed. A corrupt index is an error:
    // it must never be treated as empty, or the next write would erase records.
    private fun readIndex(): List<InstalledCs3Plugin> {
        val file = indexFile()
        if (!file.exists()) return emptyList()
        return try {
            json.decodeFromString(listSerializer, file.readText())
        } catch (error: Exception) {
            throw IllegalStateException(
                "The CloudStream install index is corrupt. Nothing was changed.",
                error,
            )
        }
    }

    private fun writeIndex(records: List<InstalledCs3Plugin>) {
        val target = indexFile()
        val partial = File(dir(), "installed.json.part")
        partial.writeText(json.encodeToString(listSerializer, records))
        if (target.exists()) target.delete()
        check(partial.renameTo(target)) { "Could not save the CloudStream install index." }
    }

    fun listInstalled(): Result<List<InstalledCs3Plugin>> = runCatching { readIndex() }

    // Refuses plugins without a hash: integrity cannot be verified without one.
    fun install(entry: Cs3PluginEntry, repoUrl: String): Result<InstalledCs3Plugin> = runCatching {
        val expectedHash = entry.fileHash?.trim()?.takeIf { it.isNotEmpty() }
            ?: error("Repository gave no hash for ${entry.internalName}. Install refused.")
        val current = readIndex()
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
        writeIndex(current.filterNot { it.internalName == entry.internalName } + record)
        record
    }

    fun uninstall(internalName: String): Result<Unit> = runCatching {
        val current = readIndex()
        current.firstOrNull { it.internalName == internalName }?.let { record ->
            File(dir(), record.fileName).delete()
        }
        writeIndex(current.filterNot { it.internalName == internalName })
    }

    private fun download(url: String): ByteArray {
        val request = Request.Builder().url(url).build()
        SharedOkHttpClient.base.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "Download failed with status ${response.code}." }
            val body = response.body ?: error("Empty download.")
            val declared = body.contentLength()
            check(declared < 0 || declared <= MAX_CS3_BYTES) {
                "Extension is larger than the ${MAX_CS3_BYTES / (1024 * 1024)} MB limit."
            }
            return body.byteStream().use { readCapped(it) }
        }
    }

    // Reads at most MAX_CS3_BYTES. Covers servers that send no Content-Length.
    private fun readCapped(input: InputStream): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER)
        var total = 0L
        while (true) {
            val read = input.read(buffer)
            if (read == -1) break
            total += read
            check(total <= MAX_CS3_BYTES) {
                "Extension is larger than the ${MAX_CS3_BYTES / (1024 * 1024)} MB limit."
            }
            output.write(buffer, 0, read)
        }
        return output.toByteArray()
    }

    private const val DEFAULT_BUFFER = 8 * 1024
}
