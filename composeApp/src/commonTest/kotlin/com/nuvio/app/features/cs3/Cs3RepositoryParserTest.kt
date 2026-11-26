package com.nuvio.app.features.cs3

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class Cs3RepositoryParserTest {

    @Test
    fun parsesRepositoryManifestAndIgnoresUnknownFields() {
        val text = """
            {
              "name": "Sample repo",
              "description": "test",
              "manifestVersion": 1,
              "pluginLists": ["https://example.invalid/plugins.json"],
              "somethingNew": true
            }
        """.trimIndent()

        val manifest = Cs3RepositoryParser.parseManifest(text).getOrNull()

        assertEquals("Sample repo", manifest?.name)
        assertEquals(listOf("https://example.invalid/plugins.json"), manifest?.pluginLists)
    }

    @Test
    fun parsesPluginListWithDefaults() {
        val text = """
            [
              {
                "internalName": "SamplePlugin",
                "url": "https://example.invalid/SamplePlugin.cs3",
                "version": 3,
                "tvTypes": ["Movie"]
              }
            ]
        """.trimIndent()

        val plugins = Cs3RepositoryParser.parsePlugins(text).getOrNull()

        assertEquals(1, plugins?.size)
        val plugin = plugins!!.first()
        assertEquals("SamplePlugin", plugin.internalName)
        assertEquals("SamplePlugin", plugin.name)
        assertEquals(3, plugin.version)
        assertEquals(listOf("Movie"), plugin.tvTypes)
    }

    @Test
    fun malformedJsonReturnsFailureNotException() {
        val result = Cs3RepositoryParser.parsePlugins("{not json")
        assertTrue(result.isFailure)
    }

    @Test
    fun validPluginPasses() {
        val plugin = Cs3PluginEntry(
            internalName = "SamplePlugin",
            url = "https://example.invalid/SamplePlugin.cs3",
        )
        assertEquals(Cs3PluginValidation.Valid, plugin.validate())
    }

    @Test
    fun blankInternalNameIsRejected() {
        val plugin = Cs3PluginEntry(internalName = " ", url = "https://example.invalid/a.cs3")
        assertIs<Cs3PluginValidation.Invalid>(plugin.validate())
    }

    @Test
    fun nonHttpsUrlIsRejected() {
        val plugin = Cs3PluginEntry(internalName = "A", url = "http://example.invalid/a.cs3")
        assertIs<Cs3PluginValidation.Invalid>(plugin.validate())
    }
}
