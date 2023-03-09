package allfit.persistence

import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import mu.KotlinLogging.logger
import java.sql.DriverManager

object LiquibaseMigrator {

    private const val changelogClasspath = "/liquibase.xml"
    private val log = logger {}

    fun migrate(config: LiquibaseConfig) {
        log.info { "Migrating database..." }
        val connection = DriverManager.getConnection(config.jdbcUrl, config.username, config.password)
        val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(connection))
        val liquibase = Liquibase(changelogClasspath, ClassLoaderResourceAccessor(), database)
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
