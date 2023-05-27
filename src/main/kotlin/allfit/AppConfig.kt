package allfit

import java.time.LocalDate

data class AppConfig(
    val mockClient: Boolean,
    val preSyncEnabled: Boolean,
    val syncEnabled: Boolean,
    val mockDb: Boolean,
    val mockDataStore: Boolean,
    val useLogFileAppender: Boolean,
    val dummyDate: LocalDate? = null,
) {

    companion object {
        val develop = AppConfig(
            mockClient = true,
            preSyncEnabled = false,
            syncEnabled = false,
            mockDb = false,
            mockDataStore = false,
            dummyDate = LocalDate.parse("2023-05-26"),
            useLogFileAppender = false,
        )
        val prod = AppConfig(
            mockClient = false,
            preSyncEnabled = false, // TODO enable once feature is implemented
            syncEnabled = true,
            mockDb = false,
            mockDataStore = false,
            useLogFileAppender = true,
        )
    }
}
