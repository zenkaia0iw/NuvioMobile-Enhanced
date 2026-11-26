// CloudStream compatibility: BasePlugin and Plugin (M4b, batch 1).
// Mirrors library/src/commonMain/kotlin/com/lagradost/cloudstream3/plugins/BasePlugin.kt
// and app/src/main/java/com/lagradost/cloudstream3/plugins/Plugin.kt
// (CloudStream master, commit a28e646). GPL-3.0.
package com.lagradost.cloudstream3.plugins

import android.content.Context
import com.lagradost.cloudstream3.APIHolder
import com.lagradost.cloudstream3.MainAPI

abstract class BasePlugin {

    /** Full file path of the plugin. Set by the loader. */
    var filename: String? = null

    /** Registers a provider. Called by plugin code from its load(). */
    fun registerMainAPI(element: MainAPI) {
        element.sourcePlugin = this.filename
        APIHolder.allProviders.add(element)
    }

    @Throws(Throwable::class)
    open fun beforeUnload() {
    }

    @Throws(Throwable::class)
    open fun load() {
    }

    class Manifest {
        var name: String? = null
        var pluginClassName: String? = null
        var requiresResources: Boolean = false
        var version: Int? = null
    }
}

abstract class Plugin : BasePlugin() {
    /** Called by the host with the Android context. Default forwards to load(). */
    @Throws(Throwable::class)
    open fun load(context: Context) {
        load()
    }
}
