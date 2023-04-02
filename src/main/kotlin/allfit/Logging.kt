package allfit

import allfit.service.DirectoryEntry
import allfit.service.FileResolver
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import ch.qos.logback.core.spi.FilterReply
import org.slf4j.LoggerFactory
import java.io.File

fun reconfigureLog() {
    val context = LoggerFactory.getILoggerFactory() as LoggerContext
    val rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME)
    rootLogger.detachAndStopAllAppenders()

//    when (Environment.current) {
//        Environment.Production -> rootLogger.reconfigureProdLog(context)
//        Environment.Development -> rootLogger.reconfigureDevLog(context)
//    }
    rootLogger.reconfigureProdLog(context)
    rootLogger.level = Level.WARN

    mapOf(
        "allfit" to Level.ALL,
        "liquibase" to Level.INFO,
        "Exposed" to Level.INFO,
    ).forEach { (packageName, logLevel) ->
        context.getLogger(packageName).level = logLevel
    }
}

private fun Logger.reconfigureProdLog(context: LoggerContext) {
    val targetLogFile = File(FileResolver.resolve(DirectoryEntry.ApplicationLogs), "allfit.log")
    println("[AllFit] Writing logs to: ${targetLogFile.absolutePath}")
    addAppender(RollingFileAppender<ILoggingEvent>().also { appender ->
        appender.context = context
        appender.name = "AllFitFileAppender"
        appender.encoder = buildPattern(context)
        appender.file = targetLogFile.absolutePath
        appender.isAppend = true
        appender.isImmediateFlush = true
        appender.rollingPolicy = TimeBasedRollingPolicy<ILoggingEvent>().also { policy ->
            policy.context = context
            policy.setParent(appender)
            policy.fileNamePattern = "allfit-%d{yyyy-MM-dd}.log"
            policy.maxHistory = 10
            policy.start()
        }
        appender.start()
        appender.addFilter(MyThresholdFilter(Level.ALL))
    })
}

private fun Logger.reconfigureDevLog(context: LoggerContext) {
    addAppender(ConsoleAppender<ILoggingEvent>().also { appender ->
        appender.context = context
        appender.name = "AllFitConsoleAppender"
        appender.encoder = buildPattern(context)
        appender.start()
        appender.addFilter(MyThresholdFilter(Level.ALL))
    })
}

private fun buildPattern(context: LoggerContext) = PatternLayoutEncoder().also { encoder ->
    encoder.context = context
    encoder.pattern = "%d{HH:mm:ss.SSS} %logger{5}.%line@[%-4.30thread] %-5level-%msg %xException{full} %n"
    encoder.start()
}

private class MyThresholdFilter(private val level: Level) : Filter<ILoggingEvent>() {
    init {
        start()
    }

    /** Strangely has to be implemented yourself... */
    override fun decide(event: ILoggingEvent): FilterReply {
        if (event.level.isGreaterOrEqual(level)) {
            return FilterReply.ACCEPT
        }
        return FilterReply.DENY
    }

}
