package com.streamhub.tv.data.model

/**
 * The fixed set of top-level Live TV categories used to group channels coming from the
 * remote JSON. The category strings must match the `category` field in channels.json
 * (case-insensitive comparison is used when grouping, see [ChannelCategory.from]).
 *
 * Channels themselves (Sports 1..10, Al Jazeera, etc.) are NOT hardcoded here - only the
 * category names and their display order/icon are. The actual channel list always comes
 * from the network/cache.
 */
enum class ChannelCategory(val displayName: String) {
    SPORTS("Sports"),
    NEWS("News"),
    MOVIES("Movies"),
    SERIES("Series"),
    KIDS("Kids"),
    DOCUMENTARY("Documentary"),
    RELIGIOUS("Religious"),
    ENTERTAINMENT("Entertainment"),
    OTHER("Other");

    companion object {
        fun from(raw: String): ChannelCategory =
            entries.firstOrNull { it.displayName.equals(raw.trim(), ignoreCase = true) } ?: OTHER

        /** Preferred display order on Home/Live TV screens. */
        val orderedCategories: List<ChannelCategory> = listOf(
            SPORTS, NEWS, MOVIES, SERIES, KIDS, DOCUMENTARY, RELIGIOUS, ENTERTAINMENT
        )
    }
}
