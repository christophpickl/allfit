package allfit

@Suppress("SimplifyBooleanWithConstants")
data class AppConfig(
    val mockClient: Boolean,
    val mockSyncer: Boolean,
    val mockDb: Boolean,
    val mockDataStore: Boolean,
    val useFileAppender: Boolean,
) {
    companion object {
        val develop = AppConfig(
            mockClient = false,
            mockSyncer = false,
            mockDb = false,
            mockDataStore = false,
            useFileAppender = true,
        )
        val prod = AppConfig(
            mockClient = false,
            mockSyncer = false,
            mockDb = false,
            mockDataStore = false,
            useFileAppender = true,
        )
    }
}
