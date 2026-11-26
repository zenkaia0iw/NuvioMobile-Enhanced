// CloudStream compatibility: MainAPI, APIHolder (M4b, batch 1).
// Mirrors library/src/commonMain/kotlin/com/lagradost/cloudstream3/MainAPI.kt
// (CloudStream master, commit a28e646). GPL-3.0.
package com.lagradost.cloudstream3

object APIHolder {
    val allProviders: MutableList<MainAPI> = mutableListOf()
}

abstract class MainAPI {
    /** Name shown in UI. Plugins set it in their class body. */
    open var name: String = "NONE"

    /** Main URL. Plugins set it in their class body. */
    open var mainUrl: String = "NONE"

    open var lang: String = "en"

    open val hasMainPage: Boolean = false
    open val hasQuickSearch: Boolean = false

    open val supportedTypes: Set<TvType> = setOf(
        TvType.Movie,
        TvType.TvSeries,
        TvType.Cartoon,
        TvType.Anime,
        TvType.OVA,
    )

    open val mainPage: List<MainPageData> = listOf(MainPageData("", "", false))

    /** Name of the plugin file that registered this provider. */
    var sourcePlugin: String? = null

    open suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse? {
        throw NotImplementedError()
    }

    /** Paginated search. Starts with page 1. */
    open suspend fun search(query: String, page: Int): SearchResponseList? {
        val results = search(query) ?: return null
        return SearchResponseList(results, false)
    }

    open suspend fun search(query: String): List<SearchResponse>? {
        throw NotImplementedError()
    }

    open suspend fun quickSearch(query: String): List<SearchResponse>? {
        throw NotImplementedError()
    }

    /** Builds a LoadResponse from a page url. */
    open suspend fun load(url: String): LoadResponse? {
        throw NotImplementedError()
    }

    /** Calls [callback] for each playable link found for [data]. */
    open suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit,
    ): Boolean {
        throw NotImplementedError()
    }

    /** Makes a relative url absolute using [mainUrl], the same way CloudStream does. */
    fun fixUrl(url: String): String {
        if (url.startsWith("http") || url.startsWith("{\"") || url.startsWith("[")) {
            return url
        }
        if (url.isEmpty()) return ""
        if (url.startsWith("//")) return "https:$url"
        if (url.startsWith("/")) return mainUrl + url
        return "$mainUrl/$url"
    }

    fun fixUrlNull(url: String?): String? {
        if (url.isNullOrEmpty()) return null
        return fixUrl(url)
    }
}
