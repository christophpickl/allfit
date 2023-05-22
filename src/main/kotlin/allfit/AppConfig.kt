package allfit

import java.time.LocalDate

data class AppConfig(
    val mockClient: Boolean,
    val mockSyncer: Boolean,
    val mockDb: Boolean,
    val mockDataStore: Boolean,
    val useLogFileAppender: Boolean,
    val dummyDate: LocalDate? = null,
) {
    companion object {
        val develop = AppConfig(
            mockClient = true,
            mockSyncer = true,
            mockDb = false,
            mockDataStore = false,
            dummyDate = LocalDate.parse("2023-05-26"),
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
