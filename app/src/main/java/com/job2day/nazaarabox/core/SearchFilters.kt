package com.job2day.nazaarabox.core

data class SearchFilters(
    val genre: String = "All",
    val country: String = "All",
    val year: String = "All",
    val language: String = "All",
    val sortBy: String = "Hottest",
) {
    val isDefault: Boolean
        get() = genre == "All" && country == "All" && year == "All" &&
            language == "All" && sortBy == "Hottest"

    val activeCount: Int
        get() {
            var count = 0
            if (genre != "All") count++
            if (country != "All") count++
            if (year != "All") count++
            if (language != "All") count++
            if (sortBy != "Hottest") count++
            return count
        }

    companion object {
        val genres = listOf(
            "All", "Action", "Adventure", "Animation", "Biography", "Comedy",
            "Crime", "Documentary", "Drama", "Family", "Fantasy", "Film-Noir",
            "Game-Show", "History", "Horror", "Music", "Musical", "Mystery",
            "News", "Reality-TV", "Romance", "Sci-Fi", "Short", "Sport",
            "Talk-Show", "Thriller", "War", "Western", "Other",
        )

        val countries = listOf(
            "All", "United States", "United Kingdom", "Korea", "Japan",
            "Bangladesh", "China", "Egypt", "France", "Germany", "India",
            "Indonesia", "Iraq", "Italy", "Ivory Coast", "Kenya", "Lebanon",
            "Mexico", "Morocco", "Nigeria", "Pakistan", "Philippines", "Russia",
            "Saudi Arabia", "South Africa", "Spain", "Syria", "Thailand",
            "Malaysia", "Turkey", "Other",
        )

        val years = listOf(
            "All", "2026", "2025", "2024", "2023", "2022", "2021", "2020",
            "2010s", "2000s", "1990s", "1980s", "Other",
        )

        val languages = listOf(
            "All", "English dub", "French dub", "Hindi dub", "Bengali dub",
            "Urdu dub", "Punjabi dub", "Tamil dub", "Telugu dub", "Malayalam dub",
            "Kannada dub", "Arabic dub", "Arabic sub", "Tagalog dub",
            "Indonesian dub", "Russian dub", "Kurdish sub", "Spanish dub",
            "Spanish sub", "Spanish Latam dub",
        )

        val sortOptions = listOf("Hottest", "Latest", "Rating")

        val genreIds = mapOf(
            "Action" to 28, "Adventure" to 12, "Animation" to 16, "Comedy" to 35,
            "Crime" to 80, "Documentary" to 99, "Drama" to 18, "Family" to 10751,
            "Fantasy" to 14, "History" to 36, "Horror" to 27, "Music" to 10402,
            "Mystery" to 9648, "Romance" to 10749, "Sci-Fi" to 878, "Thriller" to 53,
            "War" to 10752, "Western" to 37,
        )

        val countryCodes = mapOf(
            "United States" to "US", "United Kingdom" to "GB", "Korea" to "KR",
            "Japan" to "JP", "Bangladesh" to "BD", "China" to "CN", "Egypt" to "EG",
            "France" to "FR", "Germany" to "DE", "India" to "IN", "Indonesia" to "ID",
            "Iraq" to "IQ", "Italy" to "IT", "Ivory Coast" to "CI", "Kenya" to "KE",
            "Lebanon" to "LB", "Mexico" to "MX", "Morocco" to "MA", "Nigeria" to "NG",
            "Pakistan" to "PK", "Philippines" to "PH", "Russia" to "RU",
            "Saudi Arabia" to "SA", "South Africa" to "ZA", "Spain" to "ES",
            "Syria" to "SY", "Thailand" to "TH", "Malaysia" to "MY", "Turkey" to "TR",
        )

        val languageCodes = mapOf(
            "English dub" to "en", "French dub" to "fr", "Hindi dub" to "hi",
            "Bengali dub" to "bn", "Urdu dub" to "ur", "Punjabi dub" to "pa",
            "Tamil dub" to "ta", "Telugu dub" to "te", "Malayalam dub" to "ml",
            "Kannada dub" to "kn", "Arabic dub" to "ar", "Arabic sub" to "ar",
            "Tagalog dub" to "tl", "Indonesian dub" to "id", "Russian dub" to "ru",
            "Kurdish sub" to "ku", "Spanish dub" to "es", "Spanish sub" to "es",
            "Spanish Latam dub" to "es",
        )
    }
}

data class AnimeFilters(
    val country: String = "All",
    val year: String = "All",
    val sortBy: String = "ForYou",
) {
    val isDefault: Boolean get() = country == "All" && year == "All" && sortBy == "ForYou"

    val activeCount: Int
        get() {
            var count = 0
            if (country != "All") count++
            if (year != "All") count++
            if (sortBy != "ForYou") count++
            return count
        }

    companion object {
        val countries = listOf(
            "All", "United States", "United Kingdom", "France", "Japan", "China", "Korea", "Other",
        )
        val years = SearchFilters.years
        val sortOptions = listOf("ForYou", "Hottest", "Latest", "Rating")
        val countryCodes = mapOf(
            "United States" to "US", "United Kingdom" to "GB", "France" to "FR",
            "Japan" to "JP", "China" to "CN", "Korea" to "KR",
        )
    }
}
