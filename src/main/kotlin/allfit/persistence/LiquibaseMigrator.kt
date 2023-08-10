package allfit.persistence

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import java.sql.DriverManager
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor

object LiquibaseMigrator {

    private const val CHANGELOG_CLASSPATH = "/liquibase.xml"
    private val log = logger {}

    fun migrate(config: LiquibaseConfig) {
        log.info { "Migrating database..." }
        val connection = DriverManager.getConnection(config.jdbcUrl, config.username, config.password)
        val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(connection))
        val liquibase = Liquibase(CHANGELOG_CLASSPATH, ClassLoaderResourceAccessor(), database)
        liquibase.update()
        log.info { "Migrating database done ✅" }
    }
}

data class LiquibaseConfig(
    val username: String,
    val password: String,
    val jdbcUrl: String,
) {
    override fun toString() = "LiquibaseConfig[jdbcUrl=$jdbcUrl; username=$username]"
}
