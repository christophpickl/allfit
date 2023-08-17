package allfit.persistence

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import java.sql.DriverManager
import liquibase.command.CommandScope
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.helpers.DbUrlConnectionCommandStep
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection

object LiquibaseMigrator {

    private const val CHANGELOG_CLASSPATH = "/liquibase.xml"
    private val log = logger {}

    fun migrate(config: LiquibaseConfig) {
        log.info { "Migrating database..." }
        val connection = DriverManager.getConnection(config.jdbcUrl, config.username, config.password)
        val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(connection))
        val updateCommand = CommandScope(*UpdateCommandStep.COMMAND_NAME)
        updateCommand.addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, database);
        updateCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, CHANGELOG_CLASSPATH);
        updateCommand.execute()

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
