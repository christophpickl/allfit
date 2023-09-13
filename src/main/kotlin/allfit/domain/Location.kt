package allfit.domain

enum class Location(val shortCode: String, val label: String) {
    Amsterdam("AMS", "Amsterdam"),
    DenHaag("HAG", "Den Haag"),
    Utrecht("UTR", "Utrecht"),
    Rotterdam("ROT", "Rotterdam"),
    Haarlem("HAA", "Haarlem"),
    Leiden("LDN", "Leiden"),
    Eindhoven("EIN", "Eindhoven"),
    Breda("BRE", "Breda"),
    Arnhem("ARN", "Arnhem"),
    Nijmegen("NIJ", "Nijmegen"), ;

    companion object {
        val DEFAULT = Amsterdam

        private val locationsByShortCode by lazy {
            entries.associateBy { it.shortCode }
        }

        fun byShortCode(search: String): Location =
            locationsByShortCode[search] ?: error("Invalid short code: '$search'")
    }
}

data class LocationAnd<OTHER>(
    val location: Location?,
    val other: OTHER,
)
