package allfit

//private val enabled = Environment.current != Environment.Production

@Suppress("SimplifyBooleanWithConstants")
data class AppConfig(
    val mockClient: Boolean,
    val mockSyncer: Boolean,
    val mockDb: Boolean
) {
    companion object {
        val develop = AppConfig(
            mockClient = true,
            mockSyncer = true,
            mockDb = true,
        )
        val prod = AppConfig(
            mockClient = false,
            mockSyncer = false,
            mockDb = false,
        )
    }
}
