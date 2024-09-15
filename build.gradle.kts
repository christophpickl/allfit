import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.9.0"
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    kotlin("plugin.serialization") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.github.ben-manes.versions") version "0.47.0"
}

dependencies {
    implementation("no.tornado:tornadofx:1.7.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    implementation("io.insert-koin:koin-core:3.4.3")
    implementation("io.insert-koin:koin-logger-slf4j:3.4.3")

    implementation("org.jetbrains.exposed:exposed-core:0.42.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.42.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.42.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.42.0")
    implementation("com.h2database:h2:2.2.222")
    implementation("org.liquibase:liquibase-core:4.23.0")
    implementation("com.mattbertolini:liquibase-slf4j:5.0.0")

    implementation("io.github.oshai:kotlin-logging:5.1.0")
    implementation("ch.qos.logback:logback-classic:1.4.11")

    implementation("org.jsoup:jsoup:1.16.1")

    fun ktor(artifact: String) = "io.ktor:ktor-$artifact:2.3.3"
    listOf(
        "client-core",
        "client-cio",
        "client-logging",
        "client-content-negotiation",
        "serialization-kotlinx-json"
    ).forEach {
        implementation(ktor(it))
    }

    testImplementation("io.ktor:ktor-client-mock:2.3.3")
    testImplementation("io.kotest:kotest-runner-junit5:5.6.2")
    testImplementation("io.kotest:kotest-assertions-core:5.6.2")
    testImplementation("io.kotest:kotest-property:5.6.2")
    testImplementation("io.mockk:mockk:1.13.5")
}

application {
    mainClass.set("allfit.AllFit")
}

kotlin {
    jvmToolchain(11)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

javafx {
    version = "19.0.2.1" // 20.0.2, 21.0.3, 22.0.1 ... no, would require higher JDK, but tornadofx doesn't support that!
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.web")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    val rejectPatterns = listOf(".*-ea.*", ".*RC", ".*[Bb]eta.*", ".*[Aa]lpha.*").map { Regex(it) }
    rejectVersionIf {
        rejectPatterns.any {
            it.matches(candidate.version)
        }
    }
}


configure<ProcessResources>("processResources") {
    from("src/main/resources") {
        include("allfit.properties")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        filter<ReplaceTokens>(
            "tokens" to mapOf(
                "version" to (project.properties["allfit.version"] ?: "0"),
            ),
        )
    }
}

inline fun <reified C> Project.configure(name: String, configuration: C.() -> Unit) {
    (this.tasks.getByName(name) as C).configuration()
}
