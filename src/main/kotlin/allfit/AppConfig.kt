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
            mockClient = true,
            mockSyncer = true,
            mockDb = true,
            mockDataStore = true,
            useLogFileAppender = false,
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
