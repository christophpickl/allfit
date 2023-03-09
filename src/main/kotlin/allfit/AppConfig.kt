package allfit

@Suppress("SimplifyBooleanWithConstants")
object AppConfig {
    private val enabled = Environment.current != Environment.Production
    val mockClient = true && enabled
    val mockSyncer = false && enabled
    val mockDb = true && enabled
}
