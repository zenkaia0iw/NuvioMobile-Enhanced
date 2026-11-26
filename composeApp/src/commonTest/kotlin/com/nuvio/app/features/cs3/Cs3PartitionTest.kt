package com.nuvio.app.features.cs3

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Cs3PartitionTest {

    @Test
    fun validEntriesAreKeptAndInvalidOnesReportAReason() {
        val plugins = listOf(
            Cs3PluginEntry(internalName = "Good", url = "https://example.invalid/Good.cs3"),
            Cs3PluginEntry(internalName = "", url = "https://example.invalid/Blank.cs3"),
            Cs3PluginEntry(internalName = "Insecure", url = "http://example.invalid/Insecure.cs3"),
        )

        val partition = Cs3RepositoryParser.partitionPlugins(plugins)

        assertEquals(listOf("Good"), partition.valid.map { it.internalName })
        assertEquals(2, partition.skipped.size)
        assertTrue(partition.skipped.any { it.reason.contains("https") })
        assertTrue(partition.skipped.any { it.reason.contains("internalName") })
    }

    @Test
    fun emptyListProducesEmptyPartition() {
        val partition = Cs3RepositoryParser.partitionPlugins(emptyList())
        assertTrue(partition.valid.isEmpty())
        assertTrue(partition.skipped.isEmpty())
    }

    @Test
    fun unnamedSkippedPluginGetsPlaceholderName() {
        val partition = Cs3RepositoryParser.partitionPlugins(
            listOf(Cs3PluginEntry(internalName = " ", url = "https://example.invalid/x.cs3")),
        )
        assertEquals("(unnamed)", partition.skipped.single().name)
    }
}
