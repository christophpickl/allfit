package allfit

import java.io.File

enum class Environment {
    Production,
    Development;

    companion object {
        val current by lazy {
            if (File("").canonicalPath == "/Applications") {
                Production
            } else {
                Development
            }
        }
    }
}
