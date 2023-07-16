package allfit

enum class Environment {
    Production,
    Development;

    companion object {
        val current by lazy {
            println("allfit.env = [${System.getProperty("allfit.env")}]")
            val heapSize = Runtime.getRuntime().totalMemory().toDouble()
            println("heapSize = ${(heapSize / 1024.0 / 1024.0).toInt()}MB")
            if (System.getProperty("allfit.env") == "PROD") Production else Development
        }
    }
}
