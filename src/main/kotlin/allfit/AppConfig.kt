package allfit

@Suppress("SimplifyBooleanWithConstants")
object AppConfig {
    private val enabled = Environment.current != Environment.Production
    val mockSyncer = false && enabled
    val mockDb = false && enabled
}
