// Compatibility layer for CloudStream .cs3 extensions (M4b, batch 1).
// Signatures mirror CloudStream master (commit a28e646):
//   library/src/commonMain/kotlin/com/lagradost/cloudstream3/MainAPI.kt
// Licensed GPL-3.0, same as CloudStream. Only the base contracts are here.
// Helper functions (newMovieLoadResponse, newEpisode, ...) come in later batches.
package com.lagradost.cloudstream3

enum class TvType {
    Movie,
    AnimeMovie,
    TvSeries,
    Cartoon,
    Anime,
    OVA,
    Torrent,
    Documentary,
    AsianDrama,
    Live,
    NSFW,
    Others,
    Music,
    AudioBook,
    CustomMedia,
    Audio,
    Podcast,
    Video,
}

data class MainPageData(
    val name: String,
    val data: String,
    val horizontalImages: Boolean = false,
)

data class MainPageRequest(
    val name: String,
    val data: String,
    val horizontalImages: Boolean,
)

data class HomePageList(
    val name: String,
    var list: List<SearchResponse>,
    val isHorizontalImages: Boolean = false,
)

data class HomePageResponse(
    val items: List<HomePageList>,
    val hasNext: Boolean = false,
)

data class SearchResponseList(
    val items: List<SearchResponse>,
    val hasNext: Boolean = false,
)

interface SearchResponse {
    val name: String
    val url: String
    val apiName: String
    var type: TvType?
    var posterUrl: String?
}

interface LoadResponse {
    var name: String
    var url: String
    var apiName: String
    var type: TvType
    var posterUrl: String?
    var plot: String?
}

data class SubtitleFile(
    val lang: String,
    val url: String,
)

data class ExtractorLink(
    val source: String,
    val name: String,
    val url: String,
    val referer: String,
    val quality: Int,
    val isM3u8: Boolean = false,
)
