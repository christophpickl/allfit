package allfit

data class AppConfig(
    val mockClient: Boolean,
    val mockSyncer: Boolean,
    val mockDb: Boolean,
    val mockDataStore: Boolean,
    val useLogFileAppender: Boolean,
) {
    companion object {
        val develop = AppConfig(
            mockClient = false,
            mockSyncer = false,
            mockDb = false,
            mockDataStore = true,
            useLogFileAppender = true,
        )
        val prod = AppConfig(
            mockClient = false,
            mockSyncer = false,
            mockDb = false,
            mockDataStore = false,
            useLogFileAppender = true,
        )
    }
}
