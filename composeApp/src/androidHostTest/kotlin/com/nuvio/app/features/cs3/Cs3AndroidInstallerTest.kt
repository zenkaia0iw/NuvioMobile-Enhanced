package com.nuvio.app.features.cs3

import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Cs3AndroidInstallerTest {
    private lateinit var storage: File
    private val payload = "fake-cs3-bytes".encodeToByteArray()
    private val goodHash = sha256Hex(payload)
    private val httpsUrl = "https://example.invalid/Sample.cs3"
    private val repo = "https://example.invalid/repo.json"

    @BeforeTest
    fun setUp() {
        storage = createTempDirectory("cs3-installer-test").toFile()
        Cs3AndroidInstaller.initializeForTest(storage)
        Cs3AndroidInstaller.downloadFn = { payload }
    }

    @AfterTest
    fun tearDown() {
        storage.deleteRecursively()
    }

    private fun entry(
        hash: String? = goodHash,
        url: String = httpsUrl,
    ) = Cs3PluginEntry(
        internalName = "Sample",
        url = url,
        version = 2,
        fileHash = hash,
    )

    @Test
    fun installsWhenHashMatches() {
        val result = Cs3AndroidInstaller.install(entry(), repo)

        assertTrue(result.isSuccess)
        val listed = Cs3AndroidInstaller.listInstalled().getOrThrow()
        assertEquals(listOf("Sample"), listed.map { it.internalName })
        assertEquals(2, listed.single().version)
        assertTrue(File(storage, "Sample.cs3").exists())
    }

    @Test
    fun refusesHashMismatchAndWritesNothing() {
        val result = Cs3AndroidInstaller.install(entry(hash = "deadbeef"), repo)

        assertTrue(result.isFailure)
        assertFalse(File(storage, "Sample.cs3").exists())
        assertTrue(Cs3AndroidInstaller.listInstalled().getOrThrow().isEmpty())
    }

    @Test
    fun refusesMissingHash() {
        val result = Cs3AndroidInstaller.install(entry(hash = null), repo)

        assertTrue(result.isFailure)
        assertFalse(File(storage, "Sample.cs3").exists())
    }

    @Test
    fun refusesHttpUrlBeforeDownloading() {
        var downloaded = false
        Cs3AndroidInstaller.downloadFn = {
            downloaded = true
            payload
        }

        val result = Cs3AndroidInstaller.install(entry(url = "http://example.invalid/a.cs3"), repo)

        assertTrue(result.isFailure)
        assertFalse(downloaded)
    }

    @Test
    fun corruptIndexIsNeverOverwritten() {
        File(storage, "installed.json").writeText("{not json")

        val result = Cs3AndroidInstaller.install(entry(), repo)

        assertTrue(result.isFailure)
        assertEquals("{not json", File(storage, "installed.json").readText())
        assertTrue(Cs3AndroidInstaller.listInstalled().isFailure)
    }

    @Test
    fun uninstallRemovesFileAndRecord() {
        Cs3AndroidInstaller.install(entry(), repo).getOrThrow()

        val result = Cs3AndroidInstaller.uninstall("Sample")

        assertTrue(result.isSuccess)
        assertFalse(File(storage, "Sample.cs3").exists())
        assertTrue(Cs3AndroidInstaller.listInstalled().getOrThrow().isEmpty())
    }
}
