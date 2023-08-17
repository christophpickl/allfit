package allfit.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import java.util.Properties

object MetaPropsLoader {

    val instance: MetaProps by lazy {
        loadMetaProps()
    }

    private val log = logger {}
    private const val CLASSPATH = "/allfit.properties"

    private fun loadMetaProps(): MetaProps {
        log.debug { "Loading application meta properties from classpath at: $CLASSPATH" }
        val stream = MetaPropsLoader::class.java.getResourceAsStream(CLASSPATH)
            ?: error("Cannot find versions file at classpath: $CLASSPATH")
        val props = Properties().apply { load(stream) }
        return MetaProps(
            version = props.getProperty("version")?.toIntOrNull()
                ?: error("No/Invalid version specified in application meta properties.")
        ).also {
            log.info { "Meta properties: $it" }
        }
    }
}

data class MetaProps(
    val version: Int
) {
    companion object {
        val instance = MetaPropsLoader.instance
    }
}
