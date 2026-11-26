package com.nuvio.app.features.cs3

import android.content.Context
import dalvik.system.PathClassLoader
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

// Loads a CloudStream .cs3 file on Android.
// Behaviour follows CloudStream's PluginManager.loadPlugin (master, a28e646):
//  - the .cs3 is opened directly by PathClassLoader (it is a zip with classes.dex)
//  - manifest.json inside the archive names the entry class (pluginClassName)
//  - the entry class must extend BasePlugin, checked by name so this file compiles
//    before the compatibility layer (M4b) exists.

@Serializable
internal data class Cs3Manifest(
    val name: String? = null,
    val version: Int? = null,
    val pluginClassName: String? = null,
    val requiresResources: Boolean = false,
)

internal class Cs3LoadedPlugin(
    val manifest: Cs3Manifest,
    val instance: Any,
)

internal object Cs3AndroidLoader {
    private const val BASE_PLUGIN_CLASS = "com.lagradost.cloudstream3.plugins.BasePlugin"
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun load(context: Context, file: File): Result<Cs3LoadedPlugin> = runCatching {
        check(file.isFile) { "Extension file not found: ${file.name}" }
        // CloudStream marks the file read-only before loading (Android 14+ requirement).
        // Its result is not fatal there, so it is not fatal here either.
        file.setReadOnly()

        val loader = PathClassLoader(file.absolutePath, context.classLoader)
        val manifestText = loader.getResourceAsStream("manifest.json")
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: error("Extension ${file.name} has no manifest.json.")
        val manifest = json.decodeFromString(Cs3Manifest.serializer(), manifestText)

        val className = manifest.pluginClassName
            ?: error("Extension ${file.name} manifest has no pluginClassName.")
        val pluginClass = loader.loadClass(className)
        check(extendsClass(pluginClass, BASE_PLUGIN_CLASS)) {
            "Extension ${file.name} entry class does not extend BasePlugin. " +
                "The CloudStream compatibility layer is not installed in this build."
        }

        Cs3LoadedPlugin(
            manifest = manifest,
            instance = pluginClass.getDeclaredConstructor().newInstance(),
        )
    }

    private fun extendsClass(type: Class<*>, superName: String): Boolean {
        var current: Class<*>? = type
        while (current != null) {
            if (current.name == superName) return true
            current = current.superclass
        }
        return false
    }
}
